package com.example.personalfinance.adapters

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinance.R
import com.example.personalfinance.databinding.ItemHorizontalWalletBinding
import com.example.personalfinance.models.Account
import com.example.personalfinance.utils.CurrencyFormatter
import java.util.Locale

class HorizontalAccountAdapter(
    private val context: Context,
    private val accounts: List<Account>
) : RecyclerView.Adapter<HorizontalAccountAdapter.ViewHolder>() {

    fun interface OnAccountClickListener {
        fun onAccountClick(account: Account)
    }

    private var clickListener: OnAccountClickListener? = null

    fun setOnAccountClickListener(listener: OnAccountClickListener) {
        this.clickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHorizontalWalletBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(accounts[position])
    }

    override fun getItemCount(): Int = accounts.size

    inner class ViewHolder(private val binding: ItemHorizontalWalletBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(account: Account) {
            var name = account.accountName
            if ("Ví chính".equals(name, ignoreCase = true)) name = "Wallet"
            binding.tvAccountName.text = name
            binding.tvBalance.text = CurrencyFormatter.formatVND(account.balance)

            val balanceColor = if (account.balance < 0) {
                ContextCompat.getColor(context, R.color.expense_red)
            } else {
                ContextCompat.getColor(context, R.color.text_primary)
            }
            binding.tvBalance.setTextColor(balanceColor)

            // Only show star for first account
            binding.txtStarBadge.visibility = if (bindingAdapterPosition == 0) View.VISIBLE else View.GONE

            // Setup colors and icons based on Account Type
            val type = account.accountType?.uppercase(Locale.ROOT) ?: "CASH"
            val background = binding.viewTypeColor.background as? GradientDrawable

            when (type) {
                "BANK" -> {
                    binding.imgAccountIcon.setImageResource(R.drawable.ic_transaction)
                    background?.setColor(ContextCompat.getColor(context, R.color.primary))
                }
                "EWALLET" -> {
                    binding.imgAccountIcon.setImageResource(R.drawable.ic_transaction)
                    background?.setColor(ContextCompat.getColor(context, R.color.status_active))
                }
                "CREDIT" -> {
                    binding.imgAccountIcon.setImageResource(R.drawable.ic_credit_card)
                    background?.setColor(ContextCompat.getColor(context, R.color.expense_red))
                }
                "CASH" -> {
                    binding.imgAccountIcon.setImageResource(R.drawable.ic_home)
                    background?.setColor(ContextCompat.getColor(context, R.color.income_green))
                }
                else -> {
                    binding.imgAccountIcon.setImageResource(R.drawable.ic_home)
                    background?.setColor(ContextCompat.getColor(context, R.color.income_green))
                }
            }

            itemView.setOnClickListener {
                clickListener?.onAccountClick(account)
            }
        }
    }
}
