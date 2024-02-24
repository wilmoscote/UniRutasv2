package com.wmsoftware.unirutas.presentation.ui.routes

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wmsoftware.unirutas.data.datasource.local.UserPreferences
import com.wmsoftware.unirutas.databinding.MapConfigBottomSheetBinding
import com.wmsoftware.unirutas.databinding.RouteSelectBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class RouteSelectBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: RouteSelectBottomSheetBinding

    @Inject
    lateinit var userPreferences: UserPreferences

    var onDismissListener: (() -> Unit)? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RouteSelectBottomSheetBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setInitialMapType()
        initListeners()
    }

    private fun initListeners() {

    }

    private fun setInitialMapType() {

    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }
}