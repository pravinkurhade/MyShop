package com.bsktech.myshop

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bsktech.myshop.adaptor.StoreListAdaptor
import com.bsktech.myshop.mlkit.LiveBarcodeScanningActivity
import com.bsktech.myshop.models.Store
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity(), (Store) -> Unit {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        getShopsList()
    }

    private fun getShopsList() {
        db.collection("Stores")
            .get()
            .addOnSuccessListener { result ->
                val storeList = ArrayList<Store>()
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    val store = document.toObject(Store::class.java)
                    store._id = document.id
                    storeList.add(store)
                }

                recyclerView_shops.apply {
                    layoutManager = LinearLayoutManager(this@MainActivity)
                    adapter = StoreListAdaptor(storeList, this@MainActivity)
                }

            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                signOut()
            }
            R.id.action_profile -> {
                startActivity(Intent(this, MyProfileActivity::class.java))
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun signOut() {
        // Firebase sign out
        auth.signOut()
        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun invoke(store: Store) {
        val intent = Intent(this, StoreCartActivity::class.java);
        intent.putExtra("store", store)
        startActivity(intent)
    }
}
