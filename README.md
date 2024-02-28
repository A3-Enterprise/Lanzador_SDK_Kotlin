# Lanzador de ejemplo para integración de la libreria idfactory

El lanzador es un ejemplo de implementación de las librerias necesarias para iniciar el proceso de validación.

## Instalación

Primero, añadir las librería "idfactory_1_0_3_"
en las dependencias del proyecto. 

`dependencies{
  implementation(files("libs/idfactory_1_0_2.aar"))
  }`
  
Asi mismo es necesario importar las siguientes librerías.

//Android.x
    implementation ("androidx.appcompat:appcompat:1.0.0-beta01")
    implementation ("androidx.core:core-ktx:1.1.0-alpha05")
    implementation ("androidx.constraintlayout:constraintlayout:1.1.3")

La librería responde el resultado de la transacción en un objeto llamado CloseResponse  

### Version minima del SDK Android

Cambiar la versión minima del SDK Android a 26 y targetSdk a 24 (o mas alta) en el archivo `android/app/build.gradle`

### Ejemplo

Este es un pequeño ejemplo de como invocar el metodo que lanzara la librería. 
    
fun capture (uri: String){
        idFactorySDK.start(
            this,
            uri,
            object : IDFactoryHandler {
                override fun onSuccess(response: CloseResponse?) {
                    val code1 = response!!
                }
                override fun onFailure(response: CloseResponse?) {
                    val code = response!!
                }
            })
    }
