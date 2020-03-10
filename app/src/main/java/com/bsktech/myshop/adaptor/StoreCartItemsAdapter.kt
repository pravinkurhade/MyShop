/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bsktech.myshop.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bsktech.myshop.R
import com.bsktech.myshop.adaptor.StoreCartItemsAdapter.BarcodeFieldViewHolder
import com.bsktech.myshop.models.StoreItem
import com.bsktech.myshop.utils.AppUtils
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlin.math.min

/** Presents a list of field info in the detected barcode.  */
internal class StoreCartItemsAdapter(
    private val barcodeFieldList: List<StoreItem>,
    private val clickListener: (StoreItem) -> Unit
) :
    RecyclerView.Adapter<BarcodeFieldViewHolder>() {

    internal class BarcodeFieldViewHolder private constructor(view: View) :
        RecyclerView.ViewHolder(view) {

        private val name: TextView = view.findViewById(R.id.textView_name)
        private val price: TextView = view.findViewById(R.id.textView_price)
        private val size: TextView = view.findViewById(R.id.textView_size)
        private val code: TextView = view.findViewById(R.id.textView_code)
        private val quantity: TextView = view.findViewById(R.id.editText_quantity)
        private val addToCart: TextView = view.findViewById(R.id.button_add_to_cart)
        private val plus: TextView = view.findViewById(R.id.button_plus)
        private val minus: TextView = view.findViewById(R.id.button_minus)
        private val imageView: ImageView = view.findViewById(R.id.imageView_image)
        var auth: FirebaseAuth = FirebaseAuth.getInstance()
        var db: FirebaseFirestore = FirebaseFirestore.getInstance()

        fun bindBarcodeField(
            barcodeField: StoreItem,
            clickListener: (StoreItem) -> Unit
        ) {
            name.text = AppUtils.toCamalCase(barcodeField.name)
            size.text = barcodeField.size
            price.text = "₹ " + (barcodeField.price?.times(barcodeField.quantity!!))
            code.text = "EAN code : " + barcodeField.productCode
            quantity.text = barcodeField.quantity.toString()

            addToCart.setOnClickListener {
                barcodeField.quantity = quantity.text.toString().toInt()
                clickListener(barcodeField)
            }

            plus.setOnClickListener {
                if (quantity.text.toString().toInt() >= 1) {
                    val updates = hashMapOf<String, Any>(
                        "timestamp" to System.currentTimeMillis(),
                        "quantity" to (quantity.text.toString().toInt() + 1)
                    )
                    db.collection("orderItems").document(barcodeField.cartItemId.toString())
                        .update(updates);
                }
            }

            minus.setOnClickListener {
                if (quantity.text.toString().toInt() > 1) {
                    val updates = hashMapOf<String, Any>(
                        "timestamp" to System.currentTimeMillis(),
                        "quantity" to (quantity.text.toString().toInt() - 1)
                    )
                    db.collection("orderItems").document(barcodeField.cartItemId.toString())
                        .update(updates);
                }
            }

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference

            val pathReference = storageRef.child(barcodeField.image!![0])

            Glide.with(imageView.context)
                .load(pathReference)
                .into(imageView)
        }

        companion object {
            fun create(parent: ViewGroup): BarcodeFieldViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_cart_store_item, parent, false)
                return BarcodeFieldViewHolder(view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeFieldViewHolder =
        BarcodeFieldViewHolder.create(parent)

    override fun onBindViewHolder(holder: BarcodeFieldViewHolder, position: Int) =
        holder.bindBarcodeField(barcodeFieldList[position], clickListener)

    override fun getItemCount(): Int =
        barcodeFieldList.size
}
