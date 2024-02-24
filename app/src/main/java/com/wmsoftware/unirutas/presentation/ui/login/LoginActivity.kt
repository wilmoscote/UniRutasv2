package com.wmsoftware.unirutas.presentation.ui.login

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.wmsoftware.unirutas.R
import com.wmsoftware.unirutas.databinding.ActivityLoginBinding
import com.wmsoftware.unirutas.presentation.ui.main.MainActivity
import com.wmsoftware.unirutas.presentation.ui.register.RegisterActivity
import com.wmsoftware.unirutas.presentation.ui.splash.StartActivity
import com.wmsoftware.unirutas.presentation.viewmodel.AuthenticationViewModel
import com.wmsoftware.unirutas.util.utilities.Validator.isValidEmail
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.random.Random

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthenticationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initListeners()
        observeLoginStatus()
    }

    private fun initView() {
        binding.versionInfo.text = "UniRutas v${getVersionName()}"
        setRandomLoginPhrase()
    }

    private fun initListeners() {
        binding.btnLogin.setOnClickListener {
            if (validateForm()) {
                viewModel.login(binding.txtEmail.text.toString(), binding.txtPass.text.toString())
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateForm(): Boolean {
        if (binding.txtEmail.text.isNullOrEmpty()) {
            binding.txtEmail.error = "Debe ingresar su correo institucional"
            binding.txtEmail.requestFocus()
            return false
        } else if (!isValidEmail(binding.txtEmail.text.toString())) {
            binding.txtEmail.error = "Dirección de correo no permitida"
            binding.txtEmail.requestFocus()
            return false
        } else if (binding.txtPass.text.isNullOrEmpty()) {
            binding.txtPass.error = "Debe ingresar su contraseña"
            binding.txtPass.requestFocus()
            return false
        }

        return true
    }

    private fun observeLoginStatus() {
        viewModel.loading.observe(this){ isLoading ->
            binding.loading.isVisible = isLoading
            binding.btnLogin.isEnabled = !isLoading
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginStatus.collect { result ->
                    result?.let {
                        when (result.isSuccess) {
                            true -> {
                                val user = result.getOrThrow()
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }

                            false -> {
                                if(result.exceptionOrNull()?.message.toString().contains("There is no user record corresponding to this identifier")){
                                    Snackbar.make(binding.root, "No se ha encontrado un usuario registrado con este correo institucional.", Snackbar.LENGTH_LONG).show()
                                } else if(result.exceptionOrNull()?.message.toString().contains("The password is invalid or the user does not have a password")){
                                    Snackbar.make(binding.root, "Correo electrónico o contraseña incorrecta", Snackbar.LENGTH_LONG).show()
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

    private fun setRandomLoginPhrase() {
        val loginPhrases = arrayOf(
            "🚌 Sube a bordo del conocimiento. 📚 Localiza tu ruta universitaria y nunca llegues tarde. ⏰",
            "🗓️ Planifica tu día, maximiza tu tiempo. ⏳ Ve las rutas en tiempo real 🚏 y captura cada oportunidad. 🌟",
            "📍 Desde donde estés, hacia donde vayas. 🚀 Conecta con tu campus, sigue tu ruta en vivo. 📲"
        )

        binding.quote.text = loginPhrases[Random.nextInt(loginPhrases.size)]
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