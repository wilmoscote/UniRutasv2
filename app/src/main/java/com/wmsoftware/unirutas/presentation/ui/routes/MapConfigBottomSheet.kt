package com.wmsoftware.unirutas.presentation.ui.routes

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wmsoftware.unirutas.data.datasource.local.UserPreferences
import com.wmsoftware.unirutas.databinding.MapConfigBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MapConfigBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: MapConfigBottomSheetBinding

    @Inject
    lateinit var userPreferences: UserPreferences

    var onDismissListener: (() -> Unit)? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MapConfigBottomSheetBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setInitialMapType()
        initListeners()
    }

    private fun initListeners() {
        (binding.mapStyleSelect as? AutoCompleteTextView)?.setOnItemClickListener { adapterView, view, position, id ->
            lifecycleScope.launch {
                val selectedMapType = when (position) {
                    0 -> GoogleMap.MAP_TYPE_NORMAL
                    1 -> GoogleMap.MAP_TYPE_SATELLITE
                    2 -> GoogleMap.MAP_TYPE_TERRAIN
                    3 -> GoogleMap.MAP_TYPE_HYBRID
                    else -> GoogleMap.MAP_TYPE_NONE
                }

                userPreferences.saveMapTypeConfig(selectedMapType)
                dismiss()
            }
        }

        binding.traficViewOption.setOnCheckedChangeListener { _, isChecked ->
           lifecycleScope.launch {
               userPreferences.saveMapTraficConfig(isChecked)
           }
        }

        binding.myLocationOption.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                userPreferences.saveMapMyLocationConfig(isChecked)
            }
        }
    }

    private fun setInitialMapType() {
        lifecycleScope.launch {
            userPreferences.getMapTypeConfig().first { mapType ->
                if (mapType != null) {
                    val mapTypeName = when (mapType) {
                        GoogleMap.MAP_TYPE_NORMAL -> "Normal"
                        GoogleMap.MAP_TYPE_SATELLITE -> "Satélite"
                        GoogleMap.MAP_TYPE_TERRAIN -> "Terreno"
                        GoogleMap.MAP_TYPE_HYBRID -> "Híbrido"
                        else -> "Normal"
                    }
                    (binding.mapStyleSelect as? AutoCompleteTextView)?.setText(mapTypeName, false)
                } else {
                    (binding.mapStyleSelect as? AutoCompleteTextView)?.setText("Normal", false)
                }
                return@first true
            }

            userPreferences.getMapTraficConfig().first { isEnabled ->
                if (isEnabled != null) {
                    binding.traficViewOption.isChecked = isEnabled
                }
                return@first true
            }

            userPreferences.getMapMyLocationConfig().first { isEnabled ->
                if (isEnabled != null) {
                    binding.myLocationOption.isChecked = isEnabled
                } else binding.myLocationOption.isChecked = true
                return@first true
            }
        }
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }
}