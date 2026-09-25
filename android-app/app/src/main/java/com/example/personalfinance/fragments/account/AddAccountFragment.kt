package com.example.personalfinance.fragments.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.personalfinance.R
import com.example.personalfinance.databinding.FragmentAddAccountBinding
import com.example.personalfinance.models.domain.Account
import com.example.personalfinance.models.domain.User
import com.example.personalfinance.utils.SharedPrefManager
import com.example.personalfinance.viewmodels.AccountViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddAccountFragment : BottomSheetDialogFragment() {

    fun interface OnAccountSavedListener {
        fun onAccountSaved()
    }

    private var _binding: FragmentAddAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AccountViewModel
    private var currentUser: User? = null
    private var savedListener: OnAccountSavedListener? = null

    private val typeDisplayNames = arrayOf("Tiền mặt", "Ngân hàng", "Ví điện tử", "Thẻ tín dụng")
    private val typeServerKeys = arrayOf("CASH", "BANK", "EWALLET", "CREDIT")

    fun setOnAccountSavedListener(listener: OnAccountSavedListener) {
        this.savedListener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = SharedPrefManager.getInstance(requireContext()).user ?: return
        viewModel = ViewModelProvider(this)[AccountViewModel::class.java]

        // Set up Spinner
        val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, typeDisplayNames).apply {
            setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        }
        binding.spAccountType.adapter = adapter

        // Cancel
        binding.btnCancel.setOnClickListener { dismiss() }

        // Save
        binding.btnSave.setOnClickListener { saveAccount() }

        // Register Observers
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.accountCreated.observe(viewLifecycleOwner) { account ->
            if (account != null) {
                binding.btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Tạo ví thành công!", Toast.LENGTH_SHORT).show()
                savedListener?.onAccountSaved()
                dismiss()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                binding.btnSave.isEnabled = true
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAccount() {
        val name = binding.edtAccountName.text.toString().trim()
        val balanceText = binding.edtBalance.text.toString().trim()

        if (name.isEmpty()) {
            binding.edtAccountName.error = "Vui lòng nhập tên ví"
            binding.edtAccountName.requestFocus()
            return
        }

        if (balanceText.isEmpty()) {
            binding.edtBalance.error = "Vui lòng nhập số dư ban đầu"
            binding.edtBalance.requestFocus()
            return
        }

        val balance = try {
            balanceText.toDouble()
        } catch (e: NumberFormatException) {
            binding.edtBalance.error = "Số dư không hợp lệ"
            binding.edtBalance.requestFocus()
            return
        }

        val typeIdx = binding.spAccountType.selectedItemPosition
        if (typeIdx < 0) return

        val serverType = typeServerKeys[typeIdx]

        binding.btnSave.isEnabled = false

        val account = Account(
            userId = currentUser?.userId,
            accountName = name,
            accountType = serverType,
            balance = balance,
            currency = "VND"
        )

        viewModel.createAccount(account)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
