# SDK Android - Aplicación de Ejemplo

## Descripción
Aplicación de ejemplo que demuestra la integración del SDK Android de IDFactory para procesos de verificación de identidad y enrollment.

## Características del SDK

### ✅ Comunicación Dual
- **Eventos JavaScript**: Comunicación en tiempo real via `genieEventGeneral`
- **URL Redirect**: Fallback automático para compatibilidad

### ✅ Métodos de Respuesta
- **`onSuccess(response: String?)`**: Para Success y Pending
- **`onFailure(response: String?)`**: Para Failure y Failure-liveness

### ✅ Status Soportados
- **`Success`**: Proceso completado exitosamente
- **`Pending`**: Proceso pendiente de aprobación (requiere polling)
- **`Failure`**: Error general en el proceso
- **`Failure-liveness`**: Error específico de liveness

## Requisitos

- **Android**: API 21+ (Android 5.0)
- **Kotlin**: 1.9+
- **Gradle**: 8.0+

## Instalación

1. Añadir el archivo AAR del SDK en `app/libs/`
2. Configurar dependencias en `build.gradle`:

```kotlin
dependencies {
    implementation files('libs/idfactory_1_0_47.aar')
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'
    // Otras dependencias...
}
```

## Implementación

### Configuración del Handler

```kotlin
import com.SDK_kotlin.mywebview.IDFactoryHandler
import com.SDK_kotlin.mywebview.IdFactorySDK1

class MainActivity : AppCompatActivity() {
    
    private val idFactorySDK = IdFactorySDK1.instance
    
    private fun startSDK(url: String) {
        idFactorySDK.start(
            this,
            url,
            object : IDFactoryHandler {
                override fun onSuccess(response: String?) {
                    // Success y Pending
                    handleSuccessResponse(response)
                }
                
                override fun onFailure(response: String?) {
                    // Failure y Failure-liveness
                    handleFailureResponse(response)
                }
            }
        )
    }
}
```

### Parseo de Respuesta

```kotlin
private fun parseStatus(response: String?): String {
    return try {
        val jsonObject = org.json.JSONObject(response ?: "{}")
        jsonObject.optString("status", "Unknown")
    } catch (e: Exception) {
        "Unknown"
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
```

### Manejo por Status

```kotlin
private fun handleSuccessResponse(response: String?) {
    val status = parseStatus(response)
    
    when (status) {
        "Success" -> {
            // Proceso completado exitosamente
            showSuccessDialog("✅ Proceso Completado", "Verificación exitosa")
        }
        "Pending" -> {
            // Proceso pendiente - implementar polling
            showSuccessDialog("⏳ Proceso Pendiente", "Requiere revisión manual")
        }
        else -> {
            // Status desconocido, tratar como success
            showSuccessDialog("✅ Éxito", "Proceso completado")
        }
    }
}

private fun handleFailureResponse(response: String?) {
    val status = parseStatus(response)
    val message = parseMessage(response)
    
    when (status) {
        "Failure-liveness" -> {
            showErrorDialog("❌ Error de Liveness", "Error en detección de vida: $message")
        }
        "Failure" -> {
            showErrorDialog("❌ Error en el Proceso", "Error: $message")
        }
        else -> {
            showErrorDialog("❌ Error", message)
        }
    }
}
```

## Uso

1. **Configurar URL**: Ingresa la URL de invitación en el campo de texto
2. **Iniciar Proceso**: Presiona "Iniciar SDK" para comenzar la verificación
3. **Manejar Respuesta**: El SDK llamará automáticamente al método apropiado

### Ejemplos de URL

**Enrollment:**
```
https://enrolldev.idfactory.me/enroll?SubCustomer=TestCustomer&key=abc123
```

**Verificación:**
```
https://enrolldev.idfactory.me/verify?SubCustomer=TestCustomer&key=xyz789
```

## Estructura de Respuesta

```json
{
  "status": "Success|Pending|Failure|Failure-liveness",
  "message": "Mensaje descriptivo",
  "CSID": "ID de la sesión",
  "token": "Token actualizado",
  "callback": "URL de callback (opcional)",
  "idTransaction": "ID de transacción (para Pending)"
}
```

## Logs de Debug

El SDK incluye logs detallados para debugging:

```
🤖 Android: Respuesta obtenida via JavaScript Event
🤖 Android: LLAMANDO onSuccess() VIA JAVASCRIPT EMIT - Status: Success
🤖 Android Lanzador: onSuccess() - Respuesta recibida
```

## Funcionalidades del Lanzador

### Diálogos Mejorados
- **Ver Respuesta Completa**: Muestra el JSON completo del SDK
- **Nueva Invitación**: Limpia los campos para otra prueba
- **Reintentar**: Para casos de error

### Manejo de Status Específico
- **Success**: Diálogo verde con mensaje de éxito
- **Pending**: Diálogo amarillo indicando espera
- **Failure**: Diálogo rojo con detalles del error
- **Failure-liveness**: Diálogo específico para errores de liveness

## Ejemplo Completo

```kotlin
class SplashScreen : AppCompatActivity() {
    
    private val idFactorySDK = IdFactorySDK1.instance
    
    private fun capture(uri: String) {
        Log.d("LAUNCHER", "Iniciando SDK con URI: $uri")
        
        idFactorySDK.start(
            this,
            uri,
            object : IDFactoryHandler {
                override fun onSuccess(response: String?) {
                    Log.d("LAUNCHER", "🤖 Android Lanzador: onSuccess() llamado")
                    
                    val status = parseStatus(response)
                    when (status) {
                        "Success" -> showSuccessDialog("✅ Completado", "Proceso exitoso", response)
                        "Pending" -> showSuccessDialog("⏳ Pendiente", "Requiere aprobación", response)
                        else -> showSuccessDialog("✅ Éxito", "Proceso completado", response)
                    }
                }
                
                override fun onFailure(response: String?) {
                    Log.d("LAUNCHER", "🤖 Android Lanzador: onFailure() llamado")
                    
                    val status = parseStatus(response)
                    val message = parseMessage(response)
                    
                    when (status) {
                        "Failure-liveness" -> showErrorDialog("❌ Error Liveness", "Error de vida: $message", response)
                        "Failure" -> showErrorDialog("❌ Error Proceso", "Error: $message", response)
                        else -> showErrorDialog("❌ Error", message, response)
                    }
                }
            }
        )
    }
}
```

## Configuración del Proyecto

### build.gradle (Module: app)

```kotlin
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.example.lanzador_kotlin"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation files('libs/idfactory_1_0_47.aar')
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
```

### AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:theme="@style/Theme.LanzadorSDKKotlin">
    
    <activity
        android:name=".SplashScreen"
        android:exported="true"
        android:screenOrientation="portrait">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```

## Compatibilidad

- ✅ **Dual Communication**: JavaScript events + URL redirect fallback
- ✅ **Status Mapping**: Success/Pending → onSuccess, Failure/Failure-liveness → onFailure
- ✅ **Logs Detallados**: Identificación del origen de cada respuesta
- ✅ **Retrocompatible**: Funciona con versiones anteriores del WebComponent

## Troubleshooting

### Problemas Comunes

1. **SDK no responde**: Verificar permisos de cámara y ubicación
2. **Error de red**: Verificar conectividad a internet
3. **URL inválida**: Verificar formato de URL de invitación

### Logs Útiles

```kotlin
// Habilitar logs detallados
Log.d("LAUNCHER_DEBUG", "Iniciando captura con URI: $uri")
Log.d("LAUNCHER_DEBUG", "SDK Instance: ${idFactorySDK.javaClass.name}")
```

## Versiones del SDK

- **Actual**: v1.0.47 (Enero 2025)
- **Características**: JavaScript Events + URL Redirect Fallback
- **Compatibilidad**: Android API 21+

## Soporte

Para más información, consultar:
- README del SDK en `/SDK_Kotlin/README.md`
- Documentación del WebComponent en `/A3.Frontend.WebComponent/readme.md`