package com.wmsoftware.unirutas.presentation.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.wmsoftware.unirutas.R
import com.wmsoftware.unirutas.data.datasource.local.UserPreferences
import com.wmsoftware.unirutas.databinding.ActivityStartBinding
import com.wmsoftware.unirutas.presentation.ui.login.LoginActivity
import com.wmsoftware.unirutas.presentation.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        screenSplash.setKeepOnScreenCondition { true }

        checkUserLogged()
    }

    private fun checkUserLogged(){
        lifecycleScope.launch {
            userPreferences.getUser().firstOrNull {  user ->
                if(user == null){
                    startActivity(Intent(this@StartActivity, LoginActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this@StartActivity, MainActivity::class.java))
                    finish()
                }
                return@firstOrNull true
            }
        }
    }
}