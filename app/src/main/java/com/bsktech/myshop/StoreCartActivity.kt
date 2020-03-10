package com.bsktech.myshop

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bsktech.myshop.adaptor.StoreCartItemsAdapter
import com.bsktech.myshop.mlkit.LiveBarcodeScanningActivity
import com.bsktech.myshop.models.Store
import com.bsktech.myshop.models.StoreItem
import com.bsktech.myshop.utils.AppUtils
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_store_cart.*
import kotlinx.android.synthetic.main.content_store_cart.*
import java.util.ArrayList

class StoreCartActivity : AppCompatActivity(), (StoreItem) -> Unit {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    var store: Store? = null
    private lateinit var listAdapter: StoreCartItemsAdapter
    private val barcodeFieldList = ArrayList<StoreItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_cart)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        store = intent.getParcelableExtra<Store>("store")

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        toolbar_layout.title = AppUtils.toCamalCase(store?.name)

        val pathReference = storageRef.child(store?.image.toString())

        Glide.with(this)
            .load(pathReference)
            .into(imageView_img)

        button_checkout.setOnClickListener {
            val intent = Intent(this, OrderSummaryActivity::class.java);
            intent.putExtra("store", store)
            startActivity(intent)
        }

        fab.setOnClickListener {
            val intent = Intent(this, LiveBarcodeScanningActivity::class.java);
            intent.putExtra("store", store)
            startActivity(intent)
        }

        recyclerView_shop_cart_items.apply {
            layoutManager = LinearLayoutManager(this@StoreCartActivity)
            listAdapter = StoreCartItemsAdapter(barcodeFieldList, this@StoreCartActivity)
            adapter = listAdapter
        }

        getAllCartItems()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                supportFinishAfterTransition()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getAllCartItems() {
        db.collection("orderItems").whereEqualTo("storeId", store?._id)
            .whereEqualTo("uid", auth.currentUser?.uid).addSnapshotListener { value, e ->

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                barcodeFieldList.clear()

                for (document in value!!) {
                    val storeItemCart = document.toObject(StoreItem::class.java)
                    storeItemCart.cartItemId = document.id
                    barcodeFieldList.add(storeItemCart)
                }

                listAdapter.notifyDataSetChanged()
                setTotal();
            }
    }

    private fun setTotal() {
        var total: Double? = 0.0
        for (storeCartItem in barcodeFieldList) {
            total = storeCartItem.price?.times(storeCartItem.quantity!!)?.let { total?.plus(it) }
        }
        textView_total.text = "Total ₹ $total"
    }



    companion object {
        private const val TAG = "StoreCartActivity"
    }

    override fun invoke(cartStoreItem: StoreItem) {
        db.collection("orderItems").document(cartStoreItem.cartItemId.toString()).delete()
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }
}
