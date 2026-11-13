package com.example.personalfinance.fragments.transaction

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.personalfinance.R
import com.example.personalfinance.activities.ScanBillActivity
import com.example.personalfinance.activities.ScanProductActivity

class AddOptionsBottomSheet : DialogFragment() {

    private var onOptionSelected: ((OptionType) -> Unit)? = null

    enum class OptionType {
        MANUAL, OCR, YOLO
    }

    companion object {
        fun newInstance(prefilledDay: Int = -1): AddOptionsBottomSheet {
            val fragment = AddOptionsBottomSheet()
            val args = Bundle()
            args.putInt("prefilled_day", prefilledDay)
            fragment.arguments = args
            return fragment
        }
    }

    val prefilledDay: Int
        get() = arguments?.getInt("prefilled_day", -1) ?: -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_options, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.BOTTOM)
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnManual = view.findViewById<View>(R.id.btnOptionManual)
        val btnOcr = view.findViewById<View>(R.id.btnOptionOcr)
        val btnYolo = view.findViewById<View>(R.id.btnOptionYolo)

        val manualListener = View.OnClickListener {
            dismiss()
            onOptionSelected?.invoke(OptionType.MANUAL)
        }
        btnManual?.setRecursiveClickListener(manualListener)

        val ocrListener = View.OnClickListener {
            dismiss()
            if (onOptionSelected != null) {
                onOptionSelected?.invoke(OptionType.OCR)
            } else {
                startActivity(Intent(requireContext(), ScanBillActivity::class.java))
            }
        }
        btnOcr?.setRecursiveClickListener(ocrListener)

        val yoloListener = View.OnClickListener {
            dismiss()
            if (onOptionSelected != null) {
                onOptionSelected?.invoke(OptionType.YOLO)
            } else {
                startActivity(Intent(requireContext(), ScanProductActivity::class.java))
            }
        }
        btnYolo?.setRecursiveClickListener(yoloListener)
    }

    fun setOnOptionSelectedListener(listener: (OptionType) -> Unit) {
        this.onOptionSelected = listener
    }

    private fun View.setRecursiveClickListener(listener: View.OnClickListener) {
        setOnClickListener(listener)
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                getChildAt(i).setRecursiveClickListener(listener)
            }
        }
    }
}
