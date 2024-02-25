package com.wmsoftware.unirutas.presentation.ui.forgot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.wmsoftware.unirutas.R
import com.wmsoftware.unirutas.databinding.ActivityForgotPasswordBinding
import com.wmsoftware.unirutas.presentation.ui.login.LoginActivity
import com.wmsoftware.unirutas.presentation.viewmodel.AuthenticationViewModel
import com.wmsoftware.unirutas.util.utilities.Validator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: AuthenticationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
    }

    private fun initListeners(){
        viewModel.loading.observe(this){ isLoading ->
            binding.loading.isVisible = isLoading
            binding.btnContinue.isEnabled = !isLoading
        }

        viewModel.emailSent.observe(this){ isEmailSent ->
            if(isEmailSent){
                MaterialAlertDialogBuilder(this)
                    .setTitle("Enlace enviado")
                    .setMessage("Se ha enviado el correo de recuperación exitosamente.")
                    .setPositiveButton("Entendido") { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            } else {
                Snackbar.make(binding.root, "Ha ocurrido un error al enviar enlace, inténtelo nuevamente.", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnContinue.setOnClickListener {
            if(validateForm()){
                viewModel.sendPasswordResetEmail(binding.txtEmail.text.toString())
            }
        }
    }

    private fun validateForm(): Boolean {
        if (binding.txtEmail.text.isNullOrEmpty()) {
            binding.txtEmail.error = "Debe ingresar su correo institucional"
            binding.txtEmail.requestFocus()
            return false
        } else if (!Validator.isValidEmail(binding.txtEmail.text.toString())) {
            binding.txtEmail.error = "Sólo se admiten correos @uniguajira.edu.co"
            binding.txtEmail.requestFocus()
            return false
        }

        return true
    }
}