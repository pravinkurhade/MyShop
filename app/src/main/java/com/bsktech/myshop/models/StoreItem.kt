package com.bsktech.myshop.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StoreItem(
    var _id: String? = null,
    val name: String? = null,
    val productCode: String? = null,
    val size: String? = null,
    val storeId: String? = null,
    val price: Double? = null,
    val image: ArrayList<String>? = null,
    var uid: String? = null,
    var cartItemId: String? = null,
    var quantity: Int? = null,
    var productCodeStoreId: String? = null,
    var timestamp: Long? = null
) : Parcelable
