package com.example.personalfinance.activities

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.personalfinance.R
import com.example.personalfinance.databinding.ActivityMainBinding
import com.example.personalfinance.fragments.category.CategoryLimitFragment
import com.example.personalfinance.fragments.home.HomeFragment
import com.example.personalfinance.fragments.transaction.AddTransactionFragment
import com.example.personalfinance.fragments.transaction.TransactionFragment
import com.example.personalfinance.fragments.transaction.TransactionListFragment
import com.example.personalfinance.models.domain.User
import com.example.personalfinance.utils.SharedPrefManager
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = SharedPrefManager.getInstance(this).user
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Set default fragment
        loadFragment(HomeFragment())

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_transactions -> {
                    loadFragment(TransactionListFragment())
                    true
                }
                R.id.nav_scan -> {
                    showAddOptionsBottomSheet()
                    false // keep previous highlight selected
                }
                R.id.nav_budget -> {
                    loadFragment(CategoryLimitFragment())
                    true
                }
                R.id.nav_statistics -> {
                    loadFragment(TransactionFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun setSelectedTab(itemId: Int) {
        binding.bottomNavigation.selectedItemId = itemId
    }

    private fun showAddOptionsBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme).apply {
            setContentView(R.layout.bottom_sheet_add_options)
        }

        val btnManual = bottomSheetDialog.findViewById<LinearLayout>(R.id.btnOptionManual)
        val btnOcr = bottomSheetDialog.findViewById<LinearLayout>(R.id.btnOptionOcr)
        val btnYolo = bottomSheetDialog.findViewById<LinearLayout>(R.id.btnOptionYolo)

        btnManual?.setOnClickListener {
            bottomSheetDialog.dismiss()
            val addFragment = AddTransactionFragment()
            addFragment.show(supportFragmentManager, "AddTransactionFragment")
        }

        btnOcr?.setOnClickListener {
            bottomSheetDialog.dismiss()
            startActivity(Intent(this, ScanBillActivity::class.java))
        }

        btnYolo?.setOnClickListener {
            bottomSheetDialog.dismiss()
            startActivity(Intent(this, ScanProductActivity::class.java))
        }

        bottomSheetDialog.show()
    }
}
