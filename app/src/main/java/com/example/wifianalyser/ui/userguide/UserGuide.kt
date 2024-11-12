package com.example.wifianalyser.ui.userguide

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.wifianalyser.R
import com.example.wifianalyser.databinding.ActivityUserGuideBinding
import com.example.wifianalyser.ui.setting.UserSetting

class UserGuide : AppCompatActivity() {
    private var dialog: Dialog? = null
    lateinit var binding:ActivityUserGuideBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgBack.setOnClickListener {
            finish()
            startActivity(Intent(this, UserSetting::class.java))
        }

        binding.webview.webViewClient = WebViewClient()
        dialog = makeWaitDialog()
        dialog?.show()

        binding.webview.loadUrl(getString(R.string.privacy_policy_link))


        binding.webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                dialog?.dismiss()
            }
        }

    }

    private fun makeWaitDialog(): Dialog {
        val waitDialog = Dialog(this@UserGuide)
        waitDialog.setCancelable(false)
        waitDialog.setContentView(R.layout.waitcustomdiolog)
        waitDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        waitDialog.window?.setLayout(getScreenWidth(), ViewGroup.LayoutParams.WRAP_CONTENT)
        return waitDialog
    }

    private fun getScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

}