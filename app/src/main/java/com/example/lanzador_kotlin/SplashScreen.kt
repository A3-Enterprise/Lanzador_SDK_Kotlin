package com.example.lanzador_kotlin

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.SDK_kotlin.mywebview.IDFactoryHandler
import com.SDK_kotlin.mywebview.IdFactorySDK1
import com.SDK_kotlin.mywebview.MyApp


class SplashScreen : AppCompatActivity() {
    private lateinit var uri: String
    private val TAG = "SplashScreen"
    val idFactorySDK = IdFactorySDK1.instance



    companion object {
        val instance = SplashScreen()
        val myApp = MyApp.instance
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        myApp.SetContext(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Log.i("Launcher", "Se inicio el splashScreenLanzador")
        val uriInvitation: EditText = findViewById(R.id.uri_invitation)
        val btn_start: Button = findViewById(R.id.btn_start)
        btn_start.setOnClickListener {
            if (uriInvitation.text.toString() != ""){
                uri = uriInvitation.text.toString()
                capture(uri)
            }
        }

        val btn_close: Button = findViewById(R.id.btn_close)
        btn_close.setOnClickListener {
            showCustomDialogBox("Estás seguro que deseas cerrar el app?")
        }
    }

    fun capture (uri: String){
        idFactorySDK.start(
            this,
            uri,
            object : IDFactoryHandler {
                override fun onSuccess(response: String?) {
                    //val textView: TextView = findViewById(R.id.textView)
                    //textView.visibility
                    //textView.setText(response)
                    showCustomDialogBox(response)

                }
                override fun onFailure(response: String?) {
                    //val textView: TextView = findViewById(R.id.textView)
                    //textView.setText(response)
                    showCustomDialogBox(response)
                }
            })
    }

    private fun showCustomDialogBox(message: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_custom_dailog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val btnYes: Button = dialog.findViewById(R.id.btnYes)
        val btnNo: Button = dialog.findViewById(R.id.btnNo)

        tvMessage.text = message

        btnYes.setOnClickListener {
            Toast.makeText(this, "click on Yes", Toast.LENGTH_LONG).show()
            Log.i("Launcher", "Se aceptó el cierre  del splashScreenLanzador")
            finish()
        }

        btnNo.setOnClickListener {
            Log.i("Launcher", "No se aceptó el cierre  del splashScreenLanzador")
            dialog.dismiss()
        }
        dialog.show()
    }
}