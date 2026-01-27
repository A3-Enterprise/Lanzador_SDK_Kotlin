# IdFactory Android SDK - Guía de Integración

Guía técnica para desarrolladores que desean integrar el SDK de verificación de identidad de IdFactory en sus aplicaciones Android.

## 🏗️ Configuración Inicial

### Requisitos del Sistema
- **Android API**: 26+ (Android 8.0)
- **Kotlin/Java**: Compatible con ambos
- **Permisos**: CAMERA, INTERNET, ACCESS_NETWORK_STATE
- **Hardware**: Cámara frontal y trasera

### Dependencias
```kotlin
// build.gradle.kts (app)
dependencies {
    implementation(files("libs/idfactory_sdk.aar"))
    implementation("androidx.webkit:webkit:1.8.0")
    implementation("org.json:json:20230227")
}
```

### Permisos en AndroidManifest.xml
```xml
<!-- Permisos requeridos -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CAMERA" />

<!-- Hardware requerido -->
<uses-feature android:name="android.hardware.camera" android:required="true" />
```

### Permisos del SDK (v1.0.50)

El SDK utiliza únicamente 3 permisos esenciales siguiendo el principio de mínimo privilegio:

#### Permisos Obligatorios:
- **INTERNET**: Cargar URL de enrollment y comunicación con servicios
- **ACCESS_NETWORK_STATE**: Verificar conectividad de red antes de iniciar
- **CAMERA**: Liveness detection y captura de documento de identidad

#### Geolocalización (Dinámica):
- **No requiere declaración en Manifest**
- **Solicitud dinámica**: El permiso se solicita automáticamente solo cuando la invitación lo requiera
- **Configuración por invitación**: El comportamiento se define al crear la invitación en el portal de IdFactory:
  - **Apagada**: No se solicita geolocalización
  - **Voluntaria**: Se solicita al usuario, puede denegar y continuar el flujo
  - **Obligatoria**: Se solicita al usuario, debe conceder el permiso para continuar
- **Control automático**: El SDK maneja la solicitud, validación y retroalimentación según la configuración de la invitación
- **Sin código adicional**: No requiere implementación por parte del desarrollador

#### Permisos Removidos (v1.0.50):
- ~~CAMERA2~~ - Permiso inválido de Android (removido)
- ~~SYSTEM_ALERT_WINDOW~~ - No utilizado por el SDK (removido)
- ~~ACCESS_COARSE_LOCATION~~ - Ya removido desde v1.0.48

## 🔧 Implementación del SDK

### 1. Inicialización Básica

#### Método `start()` - Integración Simple
```kotlin
class MainActivity : AppCompatActivity() {
    private val idFactorySDK = IdFactorySDK1.instance
    
    private fun startVerification(invitationUrl: String) {
        idFactorySDK.start(
            activity = this,
            url_invitation = invitationUrl,
            handler = createSDKHandler()
        )
    }
}
```

#### Método `startSDKProcess()` - Integración Avanzada con Loader
```kotlin
private fun startSDKProcess(invitationUrl: String) {
    // 1. Mostrar tu loader personalizado
    showCustomLoader()
    
    // 2. Configurar listener para ocultar loader cuando esté listo
    idFactorySDK.setOnWebReadyListener(object : OnWebReadyListener {
        override fun onWebReady() {
            hideCustomLoader() // Ocultar loader cuando contenido esté listo
        }
    })
    
    // 3. Iniciar SDK
    idFactorySDK.start(
        activity = this,
        url_invitation = invitationUrl,
        handler = createSDKHandler()
    )
}
```

### 2. Implementar Callbacks Obligatorios

```kotlin
private fun createSDKHandler(): IDFactoryHandler {
    return object : IDFactoryHandler {
        override fun onSuccess(response: String?) {
            // ✅ Verificación completada exitosamente
            val csid = parseCSID(response)
            showSuccessMessage("Verificación exitosa", csid)
        }
        
        override fun onPending(response: String?) {
            // ⏳ Requiere revisión manual
            val transactionId = parseTransactionId(response)
            val csid = parseCSID(response)
            showPendingMessage("Pendiente de revisión", transactionId, csid)
        }
        
        override fun onFailure(response: String?) {
            // ❌ Error en el proceso
            val errorMessage = parseMessage(response)
            showErrorMessage("Error en verificación", errorMessage)
        }
    }
}
```

### 3. Parsear Respuestas del SDK

```kotlin
private fun parseCSID(response: String?): String {
    return try {
        val jsonObject = JSONObject(response ?: "{}")
        jsonObject.optString("CSID", "N/A")
    } catch (e: Exception) {
        "N/A"
    }
}

private fun parseTransactionId(response: String?): String {
    return try {
        val jsonObject = JSONObject(response ?: "{}")
        jsonObject.optString("idTransaction", "N/A")
    } catch (e: Exception) {
        "N/A"
    }
}

private fun parseMessage(response: String?): String {
    return try {
        val jsonObject = JSONObject(response ?: "{}")
        jsonObject.optString("message", "Sin mensaje")
    } catch (e: Exception) {
        response ?: "Sin respuesta"
    }
}
```

## 🎯 Diferencias entre Métodos

### `start()` vs `startSDKProcess()`

| Aspecto | `start()` | `startSDKProcess()` |
|---------|-----------|---------------------|
| **Uso** | Integración básica | Integración con loader personalizado |
| **Loader** | No incluye | Incluye manejo de loader |
| **Complejidad** | Simple | Avanzado |
| **Control UX** | Limitado | Completo |
| **Recomendado para** | Pruebas rápidas | Producción |

### Cuándo usar cada método:

#### Usar `start()` cuando:
- Necesitas una integración rápida
- No requieres loader personalizado
- Estás en fase de pruebas

#### Usar `startSDKProcess()` cuando:
- Quieres controlar la experiencia de usuario
- Necesitas mostrar un loader mientras carga el contenido
- Implementación para producción

## 🔄 Flujo Completo de Integración

### 1. Verificar Permisos
```kotlin
private fun hasRequiredPermissions(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == 
           PackageManager.PERMISSION_GRANTED
}

private fun requestCameraPermissions() {
    ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.CAMERA),
        PERMISSION_REQUEST_CODE
    )
}
```

### 2. Manejo de Permisos
```kotlin
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    
    if (requestCode == PERMISSION_REQUEST_CODE) {
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startSDKProcess(invitationUrl)
        } else {
            showPermissionError()
        }
    }
}
```

### 3. Implementación Completa
```kotlin
class MainActivity : AppCompatActivity() {
    private val idFactorySDK = IdFactorySDK1.instance
    private var loaderDialog: Dialog? = null
    
    fun initiateVerification(invitationUrl: String) {
        if (!hasRequiredPermissions()) {
            requestCameraPermissions()
            return
        }
        startSDKProcess(invitationUrl)
    }
    
    private fun showCustomLoader() {
        loaderDialog = Dialog(this).apply {
            setContentView(R.layout.custom_loader)
            setCancelable(false)
            show()
        }
    }
    
    private fun hideCustomLoader() {
        loaderDialog?.dismiss()
        loaderDialog = null
    }
}
```

## 📋 Estructura de Respuestas

### Formato de Eventos
Todos los callbacks reciben un JSON con esta estructura:
```json
{
  "status": "Success|Pending|Failure",
  "message": "Descripción del resultado",
  "CSID": "ID único del proceso",
  "idTransaction": "ID de transacción (solo en Pending)"
}
```

### Estados de Respuesta

#### ✅ Success
- **Significado**: Verificación completada y aprobada
- **Acción**: Mostrar mensaje de éxito al usuario
- **Datos**: Incluye CSID para referencia

#### ⏳ Pending
- **Significado**: Requiere revisión manual
- **Acción**: Informar al usuario sobre el tiempo de espera
- **Datos**: Incluye CSID e idTransaction

#### ❌ Failure
- **Significado**: Error en el proceso
- **Acción**: Mostrar error específico y permitir reintento
- **Datos**: Incluye mensaje de error detallado

## ⚠️ Manejo de Errores

### Errores Comunes y Soluciones

| Error | Causa | Solución |
|-------|-------|----------|
| `"Unauthorized"` | Token inválido/expirado | Renovar token de invitación |
| `"Invitation key isn't valid"` | URL inválida/usada/expirada | Generar nueva URL |
| `"Deny consent"` | Usuario rechazó consentimiento | Usuario debe aceptar términos |
| `"No internet connection"` | Pérdida de conectividad | Verificar conexión a internet |
| `"Internal Server Error Liveness"` | Error en detección de vida | Reintentar proceso |

### Manejo de Errores de Permisos
```kotlin
// Los errores de permisos NO emiten eventos del SDK
// Deben manejarse a nivel de aplicación
if (!hasRequiredPermissions()) {
    showPermissionDialog()
    return
}
```

## 🔍 Debugging y Testing

### Logs del SDK
En modo debug, el SDK emite logs detallados:
```kotlin
// Habilitar logs en BuildConfig.DEBUG
if (BuildConfig.DEBUG) {
    Log.d("IdFactorySDK", "Evento recibido: $eventData")
}
```

### URLs de Testing
- **Sandbox**: `https://sandbox.idfactory.com/invitation/...`
- **Producción**: `https://app.idfactory.com/invitation/...`

## 📞 Soporte Técnico

### Información para Soporte
Cuando contactes soporte, incluye:
- **CSID**: ID único del proceso
- **idTransaction**: ID de transacción (si aplica)
- **Logs**: Logs del SDK en modo debug
- **URL**: URL de invitación utilizada

### Contacto
- **Email Técnico**: dev-support@idfactory.com
- **Documentación**: [docs.idfactory.com](https://docs.idfactory.com)

---

**SDK Versión**: 1.0.50  
**Guía Versión**: 3.1  
**Última actualización**: Enero 2026  
**Compatibilidad**: Android 8.0+ (API 26+)

## 📝 Changelog

### v1.0.50 (Enero 2026)
- ✅ Optimización de permisos (de 5 a 3 permisos)
- ✅ Removido permiso inválido CAMERA2
- ✅ Removido permiso no utilizado SYSTEM_ALERT_WINDOW
- ✅ Geolocalización manejada dinámicamente por WebView
- ✅ Mejoras de seguridad y privacidad
- ✅ Cumplimiento de principio de mínimo privilegio

### v1.0.49 (Noviembre 2024)
- Versión estable anterior

### v1.0.48
- Removido ACCESS_COARSE_LOCATION