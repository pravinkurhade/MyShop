package com.bsktech.myshop.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bsktech.myshop.R
import com.bsktech.myshop.models.Store
import com.bsktech.myshop.utils.AppUtils
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.bsktech.myshop.adaptor.StoreListAdaptor.StoreListViewHolder

internal class StoreListAdaptor(private val barcodeFieldList: List<Store>, private val clickListener: (Store) -> Unit) :
    RecyclerView.Adapter<StoreListViewHolder>() {

    internal class StoreListViewHolder private constructor(view: View) :
        RecyclerView.ViewHolder(view) {

        private val name: TextView = view.findViewById(R.id.textView_name)
        private val price: TextView = view.findViewById(R.id.textView_price)
        private val size: TextView = view.findViewById(R.id.textView_size)
        private val code: TextView = view.findViewById(R.id.textView_code)
        private val imageView: ImageView = view.findViewById(R.id.imageView_image)

        fun bindBarcodeField(
            barcodeField: Store,
            clickListener: (Store) -> Unit
        ) {
            name.text = AppUtils.toCamalCase(barcodeField.name)
            code.text = barcodeField.address
            size.text = "GSTIN " + barcodeField.GSTIN
            price.text = barcodeField.phone
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference

            itemView.setOnClickListener { clickListener(barcodeField)}

            val pathReference = storageRef.child(barcodeField.image.toString())

            Glide.with(imageView.context)
                .load(pathReference)
                .into(imageView)
        }

        companion object {
            fun create(parent: ViewGroup): StoreListViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_store_list_view, parent, false)
                return StoreListViewHolder(view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreListViewHolder = StoreListViewHolder.create(parent)

    override fun onBindViewHolder(holder: StoreListViewHolder, position: Int) =
        holder.bindBarcodeField(barcodeFieldList[position],clickListener)

    override fun getItemCount(): Int =
        barcodeFieldList.size
}