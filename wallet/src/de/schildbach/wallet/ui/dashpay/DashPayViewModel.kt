/*
 * Copyright 2020 Dash Core Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.schildbach.wallet.ui.dashpay

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import de.schildbach.wallet.WalletApplication
import de.schildbach.wallet.data.UsernameSearch
import de.schildbach.wallet.livedata.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class DashPayViewModel(application: Application) : AndroidViewModel(application) {

    private val platformRepo = PlatformRepo(application as WalletApplication)
    private val walletApplication = application as WalletApplication

    private val usernameLiveData = MutableLiveData<String>()
    private val usernamesLiveData = MutableLiveData<UsernameSearch>()

    // Job instance (https://stackoverflow.com/questions/57723714/how-to-cancel-a-running-livedata-coroutine-block/57726583#57726583)
    private var getUsernameJob = Job()
    private var searchUsernamesJob = Job()

    val getUsernameLiveData = Transformations.switchMap(usernameLiveData) { username ->
        getUsernameJob.cancel()
        getUsernameJob = Job()
        liveData(context = getUsernameJob + Dispatchers.IO) {
            emit(Resource.loading(null))
            emit(platformRepo.getUsername(username))
        }
    }

    fun searchUsername(username: String) {
        usernameLiveData.value = username
    }

    override fun onCleared() {
        super.onCleared()
        getUsernameJob.cancel()
        searchUsernamesJob.cancel()
    }

    //
    // Search Usernames that start with "text".  Results are a list of documents for names
    // starting with text.  If no results are found then an empty list is returned.
    //
    val searchUsernamesLiveData = Transformations.switchMap(usernamesLiveData) { usernameSearch: UsernameSearch ->
        searchUsernamesJob.cancel()
        searchUsernamesJob = Job()
        liveData(context = searchUsernamesJob + Dispatchers.IO) {
            emit(Resource.loading(null))
            emit(platformRepo.searchUsernames(usernameSearch.text, usernameSearch.userId))
        }
    }

    fun searchUsernames(text: String, userId: String) {
        usernamesLiveData.value = UsernameSearch(text, userId)
    }

    val isPlatformAvailableLiveData = liveData(Dispatchers.IO) {
        emit(Resource.loading(null))
        emit(platformRepo.isPlatformAvailable())
    }
}