package com.example.admobinappbilling

import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.example.admobinappbilling.util.IabBroadcastReceiver
import com.example.admobinappbilling.util.IabHelper
import com.example.admobinappbilling.util.Purchase
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), IabBroadcastReceiver.IabBroadcastListener {

    companion object {
        private val REQUEST_CODE_BUY = 1
    }

    private lateinit var inAppBillingHelper: IabHelper
    private lateinit var mBroadcastReceiver: IabBroadcastReceiver

    private var purchase: Purchase? = null
    private val isPurchased: Boolean
        get() {
            return purchase != null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val base64EncodedPublicKey = null

        inAppBillingHelper = IabHelper(this, base64EncodedPublicKey)
        inAppBillingHelper.enableDebugLogging(true)

        inAppBillingHelper.startSetup(IabHelper.OnIabSetupFinishedListener { result ->
            if (!result.isSuccess) {
                complain("Problem setting up in-app billing", result.message)
                return@OnIabSetupFinishedListener
            }

            mBroadcastReceiver = IabBroadcastReceiver(this@MainActivity)

            registerReceiver(mBroadcastReceiver, IntentFilter(IabBroadcastReceiver.ACTION))

            try {
                inAppBillingHelper.queryInventoryAsync(mGotInventoryListener)
            } catch (e: IabHelper.IabAsyncInProgressException) {
                complain("Error querying inventory", "Another async operation in progress.")
            }
        })

        btnPurchase.setOnClickListener {
            if (isPurchased) {
                inAppBillingHelper.consumeAsync(purchase, consumeFinishedListener)
            } else {
                inAppBillingHelper.launchPurchaseFlow(this, InAppProduct.PURCHASED, REQUEST_CODE_BUY, mPurchaseFinishedListener)
            }
        }

        adView.loadAd(AdRequest.Builder().build())
    }

    override fun onResume() {
        super.onResume()

        updateUi()
    }

    override fun onPause() {
        super.onPause()

        adView.pause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!inAppBillingHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun updateUi() {
        if (purchase != null) {
            adView.visibility = View.GONE
            adView.pause()

            btnPurchase.text = getString(R.string.consume_purchase)
        } else {
            adView.visibility = View.VISIBLE
            adView.resume()

            btnPurchase.text = getString(R.string.disable_ads)
        }
    }

    private val mGotInventoryListener = IabHelper.QueryInventoryFinishedListener { result, inv ->
        if (result.isSuccess) {
            purchase = inv.getPurchase(InAppProduct.PURCHASED)

            updateUi()
        } else {
            complainError(result.message)
        }
    }

    private val mPurchaseFinishedListener: IabHelper.OnIabPurchaseFinishedListener = IabHelper.OnIabPurchaseFinishedListener { result, purchase ->
        if (result.isSuccess) {
            this.purchase = purchase

            updateUi()

            complain("Ads disabled", "Purchased successfully")
        } else {
            complain("Error purchasing", result.message)
        }
    }

    private var consumeFinishedListener: IabHelper.OnConsumeFinishedListener = IabHelper.OnConsumeFinishedListener { purchase, result ->
        if (result.isSuccess) {
            complain("Ads enabled", "Purchase consumed successfully")

            if (this.purchase == purchase) {
                this.purchase = null
            }
        } else {
            complain("Error while consuming", result.message)
        }

        updateUi()
    }

    override fun receivedBroadcast() {
        try {
            inAppBillingHelper.queryInventoryAsync(mGotInventoryListener)
        } catch (e: IabHelper.IabAsyncInProgressException) {
            complain("Error querying inventory", "Another async operation in progress.")
        }
    }

    private fun complainError(message: String) = complain("Error", message)

    private fun complain(title: String?, message: String) {
        AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton("OK", null)
                .create().show()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()

        unregisterReceiver(mBroadcastReceiver)

        inAppBillingHelper.disposeWhenFinished()
    }
}