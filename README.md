# SDK Android - Guía de Integración

Guía completa para integrar el SDK Android de ID Factory en tu aplicación para procesos de verificación de identidad y enrollment.

## 📋 Requisitos

- **Android**: API 21+ (Android 5.0)
- **Kotlin**: 1.8+
- **Gradle**: 8.0+

## 🚀 Instalación

1. Copiar `idfactory_1_0_48.aar` a la carpeta `app/libs/`
2. Agregar en `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(files("libs/idfactory_1_0_48.aar"))
}
```

3. Importar en tu código:

```kotlin
import com.SDK_kotlin.mywebview.IDFactoryHandler
import com.SDK_kotlin.mywebview.IdFactorySDK1
```

## 📦 Implementación

### 1. Implementar el Handler

Implementa la interface `IDFactoryHandler` con los 3 métodos:

```kotlin
class MainActivity : AppCompatActivity() {
    
    private val idFactorySDK = IdFactorySDK1.instance
    
    fun startVerification(url: String) {
        idFactorySDK.start(
            this,
            url,
            object : IDFactoryHandler {
                
                // Handler para Success
                override fun onSuccess(response: String?) {
                    Log.d("SDK", "✅ Success: Proceso completado")
                    handleSuccess(response)
                }
                
                // Handler para Pending
                override fun onPending(response: String?) {
                    Log.d("SDK", "⏳ Pending: Requiere aprobación manual")
                    handlePending(response)
                }
                
                // Handler para Failure
                override fun onFailure(response: String?) {
                    Log.d("SDK", "❌ Failure: Error en el proceso")
                    handleFailure(response)
                }
            }
        )
    }
}
```

### 2. Parsear Respuestas

```kotlin
private fun parseResponse(response: String?): JSONObject {
    return try {
        JSONObject(response ?: "{}")
    } catch (e: Exception) {
        JSONObject()
    }
}

private fun handleSuccess(response: String?) {
    val data = parseResponse(response)
    val csid = data.optString("CSID", "N/A")
    
    Log.d("SDK", "CSID: $csid")
    // Guardar CSID en tu base de datos
    // Redirigir a pantalla de éxito
}

private fun handlePending(response: String?) {
    val data = parseResponse(response)
    val idTransaction = data.optString("idTransaction", "N/A")
    val csid = data.optString("CSID", "N/A")
    
    Log.d("SDK", "Transaction ID: $idTransaction")
    // Implementar polling para verificar estado
    // Mostrar mensaje al usuario
}

private fun handleFailure(response: String?) {
    val data = parseResponse(response)
    val message = data.optString("message", "Error desconocido")
    
    Log.d("SDK", "Error: $message")
    
    // Manejar errores específicos
    when (message) {
        "Unauthorized" -> {
            // Token expirado - renovar y reintentar
        }
        "Invitation key isn't valid" -> {
            // Key inválida - generar nueva
        }
        "Deny consent" -> {
            // Usuario rechazó consentimiento
        }
        "No internet connection" -> {
            // Pérdida de conectividad
        }
    }
}
```

## 📡 Estructura de Respuesta

### Success
```json
{
  "status": "Success",
  "message": "Process completed successfully",
  "CSID": "abc123-def456-ghi789",
  "callback": "https://your-callback-url.com"
}
```

### Pending
```json
{
  "status": "Pending",
  "message": "Manual review required",
  "CSID": "abc123-def456-ghi789",
  "idTransaction": "txn-123456",
  "callback": "https://your-callback-url.com"
}
```

### Failure
```json
{
  "status": "Failure",
  "message": "Unauthorized",
  "CSID": ""
}
```

## 🧪 Respuesta en Simulador

Cuando el SDK se ejecuta en un **emulador/simulador**, retorna automáticamente una respuesta mock para facilitar el testing:

```json
{
  "Id": 6376,
  "Approved": true,
  "NotApproved": false,
  "Pending": false,
  "message": ""
}
```

**Características de la respuesta simulada:**
- **Siempre Success**: `Approved: true, Pending: false`
- **ID fijo**: `6376` para identificar respuestas de testing
- **Sin errores**: `message` vacío
- **Detección automática**: No requiere configuración adicional

**Uso en testing:**
```kotlin
override fun onSuccess(response: String?) {
    val data = parseResponse(response)
    val id = data.optInt("Id", 0)
    
    if (id == 6376) {
        Log.d("SDK", "🧪 Respuesta de simulador detectada")
        // Lógica específica para testing
    } else {
        Log.d("SDK", "📱 Respuesta de dispositivo real")
        // Lógica de producción
    }
}
```

## 🚨 Mensajes de Error Comunes

| Mensaje | Causa | Solución |
|---------|-------|----------|
| `"Unauthorized"` | Token expirado | Renovar token y reintentar |
| `"Invitation key isn't valid"` | Key inválida/usada | Generar nueva key |
| `"Deny consent"` | Usuario rechazó | Usuario debe aceptar |
| `"No internet connection"` | Pérdida prolongada de conectividad | Verificar conexión a internet |

## 💡 Ejemplo Completo

```kotlin
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.SDK_kotlin.mywebview.IDFactoryHandler
import com.SDK_kotlin.mywebview.IdFactorySDK1
import org.json.JSONObject

class VerificationActivity : AppCompatActivity() {
    
    private val idFactorySDK = IdFactorySDK1.instance
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)
        
        val url = "https://enrolldev.idfactory.me/enroll?SubCustomer=TestCustomer&key=abc123"
        startVerification(url)
    }
    
    private fun startVerification(url: String) {
        idFactorySDK.start(
            this,
            url,
            object : IDFactoryHandler {
                
                override fun onSuccess(response: String?) {
                    val data = parseResponse(response)
                    val csid = data.optString("CSID", "")
                    
                    // Guardar en base de datos
                    saveVerification(csid)
                    
                    // Mostrar éxito
                    showSuccessDialog(csid)
                }
                
                override fun onPending(response: String?) {
                    val data = parseResponse(response)
                    val idTransaction = data.optString("idTransaction", "")
                    
                    // Iniciar polling
                    startPolling(idTransaction)
                    
                    // Mostrar mensaje
                    showPendingDialog(idTransaction)
                }
                
                override fun onFailure(response: String?) {
                    val data = parseResponse(response)
                    val message = data.optString("message", "Error")
                    
                    // Mostrar error
                    showErrorDialog(message)
                }
            }
        )
    }
    
    private fun parseResponse(response: String?): JSONObject {
        return try {
            JSONObject(response ?: "{}")
        } catch (e: Exception) {
            JSONObject()
        }
    }
}
```

## 🔄 Polling para Pending

```kotlin
private fun startPolling(transactionId: String) {
    val handler = Handler(Looper.getMainLooper())
    val runnable = object : Runnable {
        override fun run() {
            checkTransactionStatus(transactionId) { status ->
                when (status) {
                    "Success" -> {
                        showSuccessDialog("...")
                    }
                    "Failure" -> {
                        showErrorDialog("Proceso rechazado")
                    }
                    else -> {
                        handler.postDelayed(this, 30000) // Reintentar en 30s
                    }
                }
            }
        }
    }
    handler.post(runnable)
}
```

## 🔧 Permisos Requeridos

Agregar en `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

## 📝 Notas Importantes

1. **Todos los handlers son obligatorios** - Debes implementar los 3 métodos
2. **Thread safety** - Los handlers se ejecutan en el hilo principal
3. **Parsing** - Siempre valida el JSON antes de usar los datos
4. **Permisos** - El SDK maneja automáticamente los permisos de cámara
5. **Simulador** - Respuesta mock automática para testing (ID: 6376)

## 🔧 Troubleshooting

### SDK no se importa
1. Verificar que el `.aar` esté en `app/libs/`
2. Limpiar proyecto: Build → Clean Project
3. Verificar dependencia en `build.gradle.kts`

### No se reciben eventos
1. Verificar que implementas los 3 handlers
2. Revisar logs en Logcat (buscar "IDFactory_SDK:")
3. Verificar URL de invitación válida

### Error de permisos
1. Verificar permisos en `AndroidManifest.xml`
2. Para Android 6+, el SDK maneja permisos automáticamente
3. Verificar que el dispositivo tenga cámara

## 📞 Soporte

- **Email**: support@idfactory.me
- **Documentación**: https://docs.idfactory.me

---

**Versión SDK**: 1.0.48  
**Última actualización**: Enero 2025