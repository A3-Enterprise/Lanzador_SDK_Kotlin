package com.example.lanzador_kotlin

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.SDK_kotlin.mywebview.IDFactoryHandler
import com.SDK_kotlin.mywebview.IdFactorySDK1
import com.SDK_kotlin.mywebview.OnWebReadyListener
import com.SDK_kotlin.mywebview.MyApp


class SplashScreen : AppCompatActivity() {
    private lateinit var uri: String
    private val TAG = "SplashScreen"
    val idFactorySDK1 = IdFactorySDK1.instance




    companion object {
        val instance = SplashScreen()
        val myApp = MyApp.instance
        private const val PERMISSION_REQUEST_CODE = 1234
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        myApp.SetContext(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        
        val uriInvitation: EditText = findViewById(R.id.uri_invitation)
        
        // Configurar doble click para seleccionar todo el texto
        uriInvitation.setOnClickListener { view ->
            if (view is EditText) {
                view.selectAll()
            }
        }
        
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
    


    fun capture(uri: String) {
        if (!hasRequiredPermissions()) {
            pendingUri = uri
            requestCameraPermissions()
            return
        }
        startSDKProcess(uri)
    }
    
    private fun startSDKProcess(uri: String) {
        showParentLoader()
        
        // Configurar callback para ocultar loader cuando el contenido esté listo
        idFactorySDK1.setOnWebReadyListener(object : OnWebReadyListener {
            override fun onWebReady() {
                hideParentLoader()
            }
        })
        
        idFactorySDK1.start(
            this,
            uri,
            createSDKHandler()
        )
    }
    
    private fun createSDKHandler(): IDFactoryHandler {
        return object : IDFactoryHandler {
                override fun onSuccess(response: String?) {
                    Log.e("LAUNCHER_DEBUG", "=== SDK onSuccess llamado ===")
                    Log.e("LAUNCHER_DEBUG", "🤖 Android Lanzador: onSuccess() - Respuesta recibida")
                    Log.e("LAUNCHER_DEBUG", "Response: $response")
                    
                    val csid = parseCSID(response)
                    Log.e("LAUNCHER_DEBUG", "✅ Proceso completado exitosamente - CSID: $csid")
                    
                    // Limpiar campo URL
                    findViewById<EditText>(R.id.uri_invitation).setText("")
                    
                    showSuccessDialog(
                        "✅ Proceso Completado", 
                        "El proceso de verificación se completó exitosamente.\n\nCSID: $csid", 
                        response
                    )
                }
                
                override fun onPending(response: String?) {
                    Log.e("LAUNCHER_DEBUG", "=== SDK onPending llamado ===")
                    Log.e("LAUNCHER_DEBUG", "🤖 Android Lanzador: onPending() - Respuesta recibida")
                    Log.e("LAUNCHER_DEBUG", "Response: $response")
                    
                    val idTransaction = parseTransactionId(response)
                    val csid = parseCSID(response)
                    Log.e("LAUNCHER_DEBUG", "⏳ Proceso pendiente - Transaction: $idTransaction")
                    
                    // Limpiar campo URL
                    findViewById<EditText>(R.id.uri_invitation).setText("")
                    
                    showSuccessDialog(
                        "⏳ Proceso Pendiente", 
                        "El proceso está pendiente de aprobación. Se requiere revisión manual.\n\nTransaction ID: $idTransaction\nCSID: $csid", 
                        response
                    )
                }
                
                override fun onFailure(response: String?) {
                    Log.e("LAUNCHER_DEBUG", "=== SDK onFailure llamado ===")
                    Log.e("LAUNCHER_DEBUG", "🤖 Android Lanzador: onFailure() - Error recibido")
                    Log.e("LAUNCHER_DEBUG", "Response: $response")
                    
                    val message = parseMessage(response)
                    Log.e("LAUNCHER_DEBUG", "❌ Error en el proceso - Message: $message")
                    
                    // Limpiar campo URL
                    findViewById<EditText>(R.id.uri_invitation).setText("")
                    
                    showErrorDialog("❌ Error en el Proceso", "Error: $message", response)
                }
            }
    }

    // MARK: - Response Parsers
    
    private fun parseCSID(response: String?): String {
        return try {
            val jsonObject = org.json.JSONObject(response ?: "{}")
            jsonObject.optString("CSID", "N/A")
        } catch (e: Exception) {
            "N/A"
        }
    }
    
    private fun parseTransactionId(response: String?): String {
        return try {
            val jsonObject = org.json.JSONObject(response ?: "{}")
            jsonObject.optString("idTransaction", "N/A")
        } catch (e: Exception) {
            "N/A"
        }
    }
    
    private fun parseMessage(response: String?): String {
        return try {
            val jsonObject = org.json.JSONObject(response ?: "{}")
            jsonObject.optString("message", "Sin mensaje")
        } catch (e: Exception) {
            response ?: "Sin respuesta"
        }
    }
    
    // MARK: - Dialog Handlers
    
    private fun showSuccessDialog(title: String, message: String, fullResponse: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_custom_dailog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val btnYes: Button = dialog.findViewById(R.id.btnYes)
        val btnNo: Button = dialog.findViewById(R.id.btnNo)

        tvMessage.text = "$title\n\n$message"
        btnYes.text = "Ver Respuesta Completa"
        btnNo.text = "Nueva Invitación"

        btnYes.setOnClickListener {
            showFullResponseDialog(fullResponse)
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            Log.i("Launcher", "Nueva invitación solicitada")
            dialog.dismiss()
        }
        dialog.show()
    }
    
    private fun showErrorDialog(title: String, message: String, fullResponse: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_custom_dailog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val btnYes: Button = dialog.findViewById(R.id.btnYes)
        val btnNo: Button = dialog.findViewById(R.id.btnNo)

        tvMessage.text = "$title\n\n$message"
        btnYes.text = "Ver Respuesta Completa"
        btnNo.text = "Reintentar"

        btnYes.setOnClickListener {
            showFullResponseDialog(fullResponse)
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            Log.i("Launcher", "Reintento solicitado")
            dialog.dismiss()
        }
        dialog.show()
    }
    
    private fun showFullResponseDialog(response: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_custom_dailog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val btnYes: Button = dialog.findViewById(R.id.btnYes)
        val btnNo: Button = dialog.findViewById(R.id.btnNo)

        tvMessage.text = "Respuesta Completa del SDK:\n\n${response ?: "Sin respuesta"}"
        btnYes.text = "Cerrar"
        btnNo.visibility = View.GONE

        btnYes.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
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
            Log.i("Launcher", "Se aceptó el cierre del splashScreenLanzador")
            finish()
        }

        btnNo.setOnClickListener {
            Log.i("Launcher", "No se aceptó el cierre del splashScreenLanzador")
            dialog.dismiss()
        }
        dialog.show()
    }
    
    // MARK: - WebReady Callback
    
    private fun onWebReady() {
        hideParentLoader()
    }
    
    // MARK: - Permission Management
    
    private var pendingUri: String? = null
    private var loaderDialog: android.app.Dialog? = null
    
    private val requiredPermissions = arrayOf(
        android.Manifest.permission.CAMERA
    )
    
    private fun hasRequiredPermissions(): Boolean {
        return requiredPermissions.all {
            androidx.core.content.ContextCompat.checkSelfPermission(this, it) == 
            android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestCameraPermissions() {
        androidx.core.app.ActivityCompat.requestPermissions(
            this,
            requiredPermissions,
            PERMISSION_REQUEST_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }
            
            if (allGranted && pendingUri != null) {
                startSDKProcess(pendingUri!!)
            } else {
                Toast.makeText(this, "Se requieren permisos de cámara para continuar", Toast.LENGTH_LONG).show()
            }
            
            pendingUri = null
        }
    }
    
    private fun showParentLoader() {
        try {
            loaderDialog = Dialog(this)
            loaderDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            loaderDialog?.setCancelable(false)
            loaderDialog?.setContentView(R.layout.loader_dialog)
            loaderDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            loaderDialog?.window?.setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            loaderDialog?.show()
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    private fun hideParentLoader() {
        try {
            loaderDialog?.dismiss()
            loaderDialog = null
        } catch (e: Exception) {
            // Handle error
        }
    }
    


}