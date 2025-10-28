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
import com.SDK_kotlin.mywebview.MyApp


class SplashScreen : AppCompatActivity() {
    private lateinit var uri: String
    private val TAG = "SplashScreen"
    val idFactorySDK1 = IdFactorySDK1.instance




    companion object {
        val instance = SplashScreen()
        val myApp = MyApp.instance
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        myApp.SetContext(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Log.e("LAUNCHER_DEBUG", "=== NUEVA VERSION LANZADOR INICIADA ===")
        Log.e("LAUNCHER_DEBUG", "SplashScreen onCreate ejecutado")
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
    


    fun capture(uri: String) {
        Log.e("LAUNCHER_DEBUG", "=== LAUNCHER DEBUG START ===")
        Log.e("LAUNCHER_DEBUG", "Iniciando captura con URI: $uri")
        Log.e("LAUNCHER_DEBUG", "SDK Instance: ${idFactorySDK1.javaClass.name}")
        Log.e("LAUNCHER_DEBUG", "SDK Package: ${idFactorySDK1.javaClass.`package`?.name}")
        
        // Test log para confirmar que funciona
        println("=== LANZADOR: BOTON PRESIONADO ===")
        Log.e("TEST_HARDWARE", "=== TELEFONO HARDWARE CONECTADO ===")
        Log.w("TEST_HARDWARE", "=== WARNING LOG TEST ===")
        Log.i("TEST_HARDWARE", "=== INFO LOG TEST ===")
        Log.d("TEST_HARDWARE", "=== DEBUG LOG TEST ===")
        Log.v("TEST_HARDWARE", "=== VERBOSE LOG TEST ===")
        Log.e("TIMEOUT_COUNTER", "=== TEST LOG DESDE LANZADOR ===")
        
        idFactorySDK1.start(
            this,
            uri,
            object : IDFactoryHandler {
                override fun onSuccess(response: String?) {
                    Log.e("LAUNCHER_DEBUG", "=== SDK onSuccess llamado ===")
                    Log.e("LAUNCHER_DEBUG", "🤖 Android Lanzador: onSuccess() - Respuesta recibida")
                    Log.e("LAUNCHER_DEBUG", "Response: $response")
                    
                    val csid = parseCSID(response)
                    Log.e("LAUNCHER_DEBUG", "✅ Proceso completado exitosamente - CSID: $csid")
                    
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
                    
                    showErrorDialog("❌ Error en el Proceso", "Error: $message", response)
                }
            }
        )
        Log.e("LAUNCHER_DEBUG", "=== LAUNCHER DEBUG END ===")
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
            // Limpiar campo URL para nueva invitación
            findViewById<EditText>(R.id.uri_invitation).setText("")
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


}