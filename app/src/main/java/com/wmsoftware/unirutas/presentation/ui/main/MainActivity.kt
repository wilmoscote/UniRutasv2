package com.wmsoftware.unirutas.presentation.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.wmsoftware.unirutas.R
import com.wmsoftware.unirutas.databinding.ActivityMainBinding
import com.wmsoftware.unirutas.network.service.LocationService
import com.wmsoftware.unirutas.presentation.ui.login.LoginActivity
import com.wmsoftware.unirutas.presentation.viewmodel.MainViewModel
import com.wmsoftware.unirutas.util.utilities.Const.TAG
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var analytics: FirebaseAnalytics
    private val viewModel: MainViewModel by viewModels()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            val postNotificationsGranted = permissions[if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.POST_NOTIFICATIONS
            } else {
                Manifest.permission.INTERNET
            }] ?: false

            if (locationGranted && coarseLocationGranted && postNotificationsGranted) {
                // Todos los permisos concedidos
            } else {
                // Permiso(s) denegado(s), manejar según corresponda
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissionsIfNeeded()
        initView()
        initObservers()
        viewModel.checkCurrentUser()
        viewModel.getUserChanges()
        viewModel.getUserLasLocation()
        checkFcmToken()
    }

    private fun initView(){
        analytics = Firebase.analytics
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun requestPermissionsIfNeeded() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                // Los permisos ya están concedidos, proceder con la funcionalidad
            }
            else -> {
                // Solicitar los permisos
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    )
                } else {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                    )
                }
            }
        }
    }

    private fun initObservers(){
        viewModel.userDesactivated.observe(this){ disabled ->
            if(disabled){
                Blurry.with(this)
                    .radius(10)
                    .sampling(8)
                    .async()
                    .onto(binding.root)
                binding.navHostFragment.isVisible = false
                MaterialAlertDialogBuilder(this)
                    .setTitle("Cuenta desactivada")
                    .setMessage("Por motivos de seguridad su cuenta ha sido inhabilitada")
                    .setPositiveButton("Entendido") { _, _ ->
                        viewModel.logout()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finishAffinity()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            }
        }
    }

    private fun checkFcmToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            viewModel.updateFcmToken(token)
            Log.i(TAG,"FCM TOKEN: $token")
        })
    }
}