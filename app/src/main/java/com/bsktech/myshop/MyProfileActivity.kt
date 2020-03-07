package com.bsktech.myshop

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.content_my_profile.*

class MyProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val docRef = db.collection("customers").document(auth.currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data!!["name"]}")
                    Log.d(TAG, "DocumentSnapshot data: ${document.data!!["email"]}")
                    Log.d(TAG, "DocumentSnapshot data: ${document.data!!["image"]}")
                    Log.d(TAG, "DocumentSnapshot data: ${document.data!!["phone"]}")

                    textView_name.text = document.data!!["name"].toString()
                    textView_email.text = document.data!!["email"].toString()
                    textView_phone.text = document.data!!["phone"].toString()

                    val pathReference = storageRef.child(document.data!!["image"].toString())

                    Glide.with(this)
                        .load(pathReference)
                        .into(imageView_image)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "MyProfileActivity"
    }
}
