package com.example.wifianalyser.ui.setting

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.example.wifianalyser.MainActivity
import com.example.wifianalyser.utils.UserPreferences
import com.example.wifianalyser.databinding.ActivitySettingBinding
import com.example.wifianalyser.ui.userguide.UserGuide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class UserSetting : AppCompatActivity() {
    lateinit var binding: ActivitySettingBinding
    @Inject
    lateinit var userPreferences: UserPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListener()
        initialize()
    }

    private fun initialize(){
       CoroutineScope(IO).launch {
           checkSwitchState()
       }
    }

    private fun setListener() {
        binding.switchDarkMode.setOnCheckedChangeListener{_,state ->

            if(state){
              AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                CoroutineScope(IO).launch {
                    userPreferences.setThemeStatus(state)
                }
            }else{
                CoroutineScope(IO).launch {
                    userPreferences.setThemeStatus(state)
                }
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            }
        }

        binding.tvUserGuide.setOnClickListener {
            startActivity(Intent(this,UserGuide::class.java))
        }

        binding.imgBack.setOnClickListener {
            finish()
            startActivity(Intent(this,MainActivity::class.java))
        }
        binding.tvPrivacyPolicy.setOnClickListener {
            finish()
            startActivity(Intent(this,UserGuide::class.java))
        }
    }


    private suspend fun checkSwitchState(){
        val switchState = userPreferences.getThemeStatus().first()
        withContext(Main){
            binding.switchDarkMode.isChecked = switchState
        }
    }
}