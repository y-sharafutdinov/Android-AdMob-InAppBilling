package com.example.admobinappbilling

import org.json.JSONObject


class InAppProduct(jsonObject: JSONObject) {
    var sku: String? = null
    var storeName: String? = null
    var storeDescription: String? = null
    var price: String? = null
    var isSubscription: Boolean = false
    var priceAmountMicros: Int = 0
    var currencyIsoCode: String? = null

    val type: String get() = if (isSubscription) Type.IN_APP else Type.SUBS

    init {
        sku = jsonObject.getString("productId")
        storeName = jsonObject.getString("title")
        storeDescription = jsonObject.getString("description")
        price = jsonObject.getString("price")
        isSubscription = jsonObject.getString("type") == "subs"
        priceAmountMicros = Integer.parseInt(jsonObject.getString("price_amount_micros"))
        currencyIsoCode = jsonObject.getString("price_currency_code")
    }

    companion object {
        const val PURCHASED = "android.test.purchased"
        const val CANCELED = "android.test.canceled"
        const val REFUNDED= "android.test.refunded"
        const val ITEM_UNAVAILABLE = "android.test.item_unavailable"
    }

    interface Type {
        companion object {
            const val IN_APP = "inapp"
            const val SUBS = "subs"
        }
    }

}