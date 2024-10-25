package com.example.if570_lab_uts_christopher_66460

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation) // Update ke BottomNavigationView
        bottomNavigationView.setupWithNavController(navController) // Set up with NavController

        if (auth.currentUser != null) {
            // User is already logged in, navigate to HomeFragment
            navController.navigate(R.id.homeFragment)
        } else {
            // User is not logged in, navigate to LoginFragment
            if (savedInstanceState == null) {
                navController.navigate(R.id.loginFragment)
            }
        }
    }

    fun showBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.visibility = View.VISIBLE
    }

    fun hideBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.visibility = View.GONE
    }
}
