package com.wmsoftware.unirutas.presentation.ui.register

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.wmsoftware.unirutas.R
import com.wmsoftware.unirutas.databinding.ActivityRegisterBinding
import com.wmsoftware.unirutas.domain.model.User
import com.wmsoftware.unirutas.presentation.ui.main.MainActivity
import com.wmsoftware.unirutas.presentation.viewmodel.AuthenticationViewModel
import com.wmsoftware.unirutas.util.utilities.Validator
import com.wmsoftware.unirutas.util.utilities.Validator.getCurrentDateTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private val viewModel: AuthenticationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initListeners()
        observeRegistrationStatus()
    }

    private fun initView(){
        binding.versionInfo.text = "UniRutas v${getVersionName()}"
    }

    private fun initListeners(){
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnContinue.setOnClickListener {
            if(validateForm()){
                val userData = User(
                    name = binding.txtName.text.toString(),
                    lastName = binding.txtLastname.text.toString(),
                    email = binding.txtEmail.text.toString(),
                    createdAt = getCurrentDateTime()
                )
                viewModel.register(binding.txtEmail.text.toString(), binding.txtPass.text.toString(), userData)
            }
        }

        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnVerifyEmail.setOnClickListener {
            binding.verifyLoading.isVisible = true
            binding.btnVerifyEmail.isEnabled = false
            viewModel.login(binding.txtEmail.text.toString(), binding.txtPass.text.toString())
        }
    }

    private fun validateForm(): Boolean {
        if (binding.txtName.text.isNullOrEmpty()) {
            binding.txtName.error = "Debe ingresar su nombre"
            binding.txtName.requestFocus()
            return false
        } else if (binding.txtLastname.text.isNullOrEmpty()) {
            binding.txtLastname.error = "Debe ingresar su apellido"
            binding.txtLastname.requestFocus()
            return false
        } else if (binding.txtEmail.text.isNullOrEmpty()) {
            binding.txtEmail.error = "Debe ingresar su correo institucional"
            binding.txtEmail.requestFocus()
            return false
        } else if (!Validator.isValidEmail(binding.txtEmail.text.toString())) {
            binding.txtEmail.error = "Sólo se admiten correos @uniguajira.edu.co"
            binding.txtEmail.requestFocus()
            return false
        } else if (binding.txtPass.text.isNullOrEmpty()) {
            binding.txtPass.error = "Debe ingresar su contraseña"
            binding.txtPass.requestFocus()
            return false
        } else if ((binding.txtPass.text?.length ?: 0)<= 4) {
            binding.txtPass.error = "La contraseña debe tener mínimo 5 caracteres"
            binding.txtPass.requestFocus()
            return false
        }

        return true
    }

    private fun observeRegistrationStatus() {
        viewModel.loading.observe(this){ isLoading ->
            binding.loading.isVisible = isLoading
            binding.btnContinue.isEnabled = !isLoading
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registrationStatus.collect { result ->
                    result?.let {
                        when ( result.isSuccess) {
                            true -> {
                                binding.verifyLoading.isVisible = false
                                binding.verifyEmailLayout.isVisible = true
                            }
                            false -> {
                                Snackbar.make(binding.root, result.exceptionOrNull()?.message.toString(), Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginStatus.collect { result ->
                    result?.let {
                        when (result.isSuccess) {
                            true -> {
                                binding.verifyLoading.isVisible = false
                                binding.btnVerifyEmail.isEnabled = true
                                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                                finishAffinity()
                            }

                            false -> {
                                binding.verifyLoading.isVisible = false
                                binding.btnVerifyEmail.isEnabled = true
                                if(result.exceptionOrNull()?.message.toString().contains("There is no user record corresponding to this identifier")){
                                    Snackbar.make(binding.root, "No se ha encontrado un usuario registrado con este correo institucional.", Snackbar.LENGTH_LONG).show()
                                } else {
                                    Snackbar.make(binding.root, result.exceptionOrNull()?.message.toString(), Snackbar.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getVersionName(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.1"
        }
    }
}