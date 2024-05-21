# Lanzador de ejemplo para integración de la libreria idfactory

El lanzador es un ejemplo de implementación de las librerias necesarias para iniciar el proceso de validación.

## Instalación

Primero, añadir las librería "idfactory_1_0_13_"
en las dependencias del proyecto. 

```gradle
dependencies{
  implementation(files("libs/idfactory_1_0_13.aar"))
}
```
  
Asi mismo es necesario importar las siguientes librerías.

```gradle
//Android.x
implementation ("androidx.appcompat:appcompat:1.0.0-beta01")
implementation ("androidx.core:core-ktx:1.1.0-alpha05")
implementation ("androidx.constraintlayout:constraintlayout:1.1.3")
```

### Importaciones necesarias

```kotlin
import com.SDK_kotlin.mywebview.IDFactoryHandler
import com.SDK_kotlin.mywebview.IdFactorySDK1
```

### Version minima del SDK Android

Cambiar la versión minima del SDK Android a 26 y targetSdk a 34 (o mas alta) en el archivo `android/app/build.gradle`

## Implementación

### Ejemplo de uso

```kotlin
fun capture(uri: String) {
    IdFactorySDK1.instance.start(
        this,
        uri,
        object : IDFactoryHandler {
            override fun onSuccess(response: String?) {
                // La respuesta viene como JSON string
            }
            
            override fun onFailure(response: String?) {
                // Error en formato string
            }
        }
    )
}
```
