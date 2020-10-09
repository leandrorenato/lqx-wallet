package de.schildbach.wallet.ui.dashpay

import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import de.schildbach.wallet.WalletApplication
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener
import org.bitcoinj.wallet.Wallet
import org.bitcoinj.wallet.listeners.WalletChangeEventListener
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener

abstract class WalletBalanceBasedLiveData<T>(val walletApplication: WalletApplication = WalletApplication.getInstance())
    : LiveData<T>(), WalletCoinsReceivedEventListener, WalletCoinsSentEventListener, WalletChangeEventListener, TransactionConfidenceEventListener {

    private var listening = false

    private val wallet: Wallet
        get() = walletApplication.wallet

    override fun onActive() {
        maybeAddEventListener()
        onUpdate(wallet)
    }

    override fun onInactive() {
        maybeRemoveEventListener()
    }

    private fun maybeAddEventListener() {
        if (!listening && hasActiveObservers()) {
            val mainThreadExecutor = ContextCompat.getMainExecutor(walletApplication)
            wallet.addCoinsReceivedEventListener(mainThreadExecutor, this)
            wallet.addCoinsSentEventListener(mainThreadExecutor, this)
            wallet.addChangeEventListener(mainThreadExecutor, this)
            wallet.addTransactionConfidenceEventListener(mainThreadExecutor, this)
            listening = true
        }
    }

    private fun maybeRemoveEventListener() {
        if (listening) {
            wallet.removeCoinsReceivedEventListener(this)
            wallet.removeCoinsSentEventListener(this)
            wallet.removeChangeEventListener(this)
            wallet.removeTransactionConfidenceEventListener(this)
            listening = false
        }
    }

    override fun onCoinsReceived(wallet: Wallet, tx: Transaction, prevBalance: Coin, newBalance: Coin) {
        onUpdate(wallet)
    }

    override fun onCoinsSent(wallet: Wallet, tx: Transaction, prevBalance: Coin, newBalance: Coin) {
        onUpdate(wallet)
    }

    override fun onWalletChanged(wallet: Wallet) {
        onUpdate(wallet)
    }

    override fun onTransactionConfidenceChanged(wallet: Wallet, tx: Transaction?) {
        onUpdate(wallet)
    }

    abstract fun onUpdate(wallet: Wallet)

}