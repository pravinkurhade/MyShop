package com.bsktech.myshop

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bsktech.myshop.adaptor.OrderSummaryItemsAdapter
import com.bsktech.myshop.models.Order
import com.bsktech.myshop.models.Store
import com.bsktech.myshop.models.StoreItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_order_summary.*
import kotlinx.android.synthetic.main.content_order_summary.*
import kotlinx.android.synthetic.main.content_store_cart.textView_total
import java.util.*

class OrderSummaryActivity : AppCompatActivity(), (StoreItem) -> Unit {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    var store: Store? = null

    private lateinit var listAdapter: OrderSummaryItemsAdapter
    private val orderItems = ArrayList<StoreItem>()
    var total: Double? = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        store = intent.getParcelableExtra<Store>("store")

        recyclerView_order_summary_items.apply {
            layoutManager = LinearLayoutManager(this@OrderSummaryActivity)
            listAdapter = OrderSummaryItemsAdapter(orderItems, this@OrderSummaryActivity)
            adapter = listAdapter
        }

        button_pay.setOnClickListener {
            startPayment();
        }

        getAllCartItems()
    }

    private fun placeOrder(approvalRefNo: String, transactionObject: String) {
        val order: Order = Order(
            "",
            auth.currentUser?.uid,
            store?._id,
            auth.currentUser?.uid + "_" + store?._id,
            orderItems,
            total,
            "UPI",
            approvalRefNo,
            transactionObject,
            System.currentTimeMillis()
        )
        db.collection("orders").add(order).addOnSuccessListener {
            for (orderMenu in orderItems) {
                db.collection("orderItems").document(orderMenu.cartItemId!!).delete()
                finish()
            }
        }
    }

    private fun startPayment() {
        val uri: Uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", store?.upi)
            .appendQueryParameter("pn", store?.name)
            .appendQueryParameter("tn", "Grocery Purchase")
            .appendQueryParameter("am", total.toString())
            .appendQueryParameter("cu", "INR")
            .build()

        val upiPayIntent = Intent(Intent.ACTION_VIEW)

        upiPayIntent.data = uri

        // will always show a dialog to user to choose an app
        val chooser = Intent.createChooser(upiPayIntent, "Pay with")

        // check if intent resolves
        if (null != chooser.resolveActivity(packageManager)) {
            startActivityForResult(chooser, 5645)
        } else {
            Toast.makeText(
                this,
                "No UPI app found, please install one to continue",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            5645 -> {
                if ((Activity.RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        val trxt = data.getStringExtra("response")
                        Log.d("UPI", "onActivityResult: $trxt")
                        val dataList: ArrayList<String> = ArrayList()
                        dataList.add(trxt)
                        upiPaymentDataOperation(dataList)
                    } else {
                        Log.d("UPI", "onActivityResult: " + "Return data is null")
                        val dataList: ArrayList<String> = ArrayList()
                        dataList.add("nothing")
                        upiPaymentDataOperation(dataList)
                    }
                } else {
                    Log.d(
                        "UPI",
                        "onActivityResult: " + "Return data is null"
                    ); //when user simply back without payment
                    val dataList: ArrayList<String> = ArrayList()
                    dataList.add("nothing")
                    upiPaymentDataOperation(dataList);
                }
            }
        }
    }

    private fun upiPaymentDataOperation(data: ArrayList<String>) {
        var str: String = data[0]
        Log.d("UPIPAY", "upiPaymentDataOperation: $str")
        var paymentCancel = ""
        if (str == null) str = "discard"
        var status = ""
        var approvalRefNo = ""
        val response = str.split("&").toTypedArray()
        for (i in response.indices) {
            val equalStr =
                response[i].split("=").toTypedArray()
            if (equalStr.size >= 2) {
                if (equalStr[0].toLowerCase() == "Status".toLowerCase()) {
                    status = equalStr[1].toLowerCase()
                } else if (equalStr[0]
                        .toLowerCase() == "ApprovalRefNo".toLowerCase() || equalStr[0]
                        .toLowerCase() == "txnRef".toLowerCase()
                ) {
                    approvalRefNo = equalStr[1]
                }
            } else {
                paymentCancel = "Payment cancelled by user."
            }
        }
        when {
            status == "success" -> {
                //Code to handle successful transaction here.
                placeOrder(approvalRefNo, str)
                Toast.makeText(
                        this@OrderSummaryActivity,
                        "Transaction successful.",
                        Toast.LENGTH_SHORT
                    )
                    .show()
                Log.d("UPI", "responseStr: $approvalRefNo")
            }
            "Payment cancelled by user." == paymentCancel -> {
                Toast.makeText(
                        this@OrderSummaryActivity,
                        "Payment cancelled by user.",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
            else -> {
                Toast.makeText(
                    this@OrderSummaryActivity,
                    "Transaction failed.Please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getAllCartItems() {
        db.collection("orderItems").whereEqualTo("storeId", store?._id)
            .whereEqualTo("uid", auth.currentUser?.uid).addSnapshotListener { value, e ->

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                orderItems.clear()

                for (document in value!!) {
                    val storeItemCart = document.toObject(StoreItem::class.java)
                    storeItemCart.cartItemId = document.id
                    orderItems.add(storeItemCart)
                }

                listAdapter.notifyDataSetChanged()
                setTotal();
            }
    }

    private fun setTotal() {
        for (storeCartItem in orderItems) {
            total = storeCartItem.price?.times(storeCartItem.quantity!!)?.let { total?.plus(it) }
        }
        textView_total.text = "Total ₹ $total"
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

    companion object {
        private const val TAG = "OrderSummaryActivity"
    }

    override fun invoke(p1: StoreItem) {
        TODO("Not yet implemented")
    }
}
