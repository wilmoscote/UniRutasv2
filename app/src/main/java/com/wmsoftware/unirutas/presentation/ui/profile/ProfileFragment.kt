package com.wmsoftware.unirutas.presentation.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wmsoftware.unirutas.R
import com.wmsoftware.unirutas.databinding.FragmentProfileBinding
import com.wmsoftware.unirutas.presentation.ui.login.LoginActivity
import com.wmsoftware.unirutas.presentation.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        initListeners()
    }

    private fun initView(){
        viewModel.user.asLiveData().observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.profileName.text = "${it.name} ${it.lastName}"
                binding.profileCarreer.text = it.carreer

                if(!it.profilePicture.isNullOrEmpty()){
                    Glide.with(this@ProfileFragment.requireContext())
                        .load(it.profilePicture)
                        .circleCrop()
                        .placeholder(R.drawable.user_default_picture)
                        .error(R.drawable.user_default_picture)
                        .into(binding.profilePicture)
                }
            }
        }
    }

    private fun initListeners() {
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(this@ProfileFragment.requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Esta seguro que desea salir?")
                .setPositiveButton("Cerrar sesión") { _, _ ->
                    viewModel.logout()
                    startActivity(Intent(this@ProfileFragment.requireContext(), LoginActivity::class.java))
                    activity?.finishAffinity()
                }
                .setNegativeButton("Cancelar") {dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .create()
                .show()
        }
    }
}