package com.example.lanzador_kotlin

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mouredev.mywebview.IDFactoryHandler
import com.mouredev.mywebview.IdFactorySDK1
import com.mouredev.mywebview.MyApp
import com.mouredev.mywebview.model.CloseResponse

class TestSDK : AppCompatActivity()  {

    private lateinit var uri: String
    private val TAG = "TestSDK"
    val idFactorySDK = IdFactorySDK1.instance



    companion object {
        val instance = TestSDK()
        val myApp = MyApp.instance
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        myApp.SetContext(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_sdk_layout)
        val uriInvitation: EditText = findViewById(R.id.uri_invitation)
        val btn_start: Button = findViewById(R.id.btn_start)
        btn_start.setOnClickListener {
            if (uriInvitation.text.toString() != ""){
                uri = uriInvitation.text.toString()
                capture(uri)
            }
        }
    }

    fun capture (uri: String){
        idFactorySDK.start(
            this,
            uri,
            object : IDFactoryHandler {
                override fun onSuccess(response: String?) {
                    val textView: TextView = findViewById(R.id.textView)
                    textView.visibility
                    textView.setText(response)
                }
                override fun onFailure(response: String?) {
                    val textView: TextView = findViewById(R.id.textView)
                    textView.setText(response)
                }
            })
    }
}

