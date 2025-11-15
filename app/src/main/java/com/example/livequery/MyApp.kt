package com.example.livequery

import android.app.Application
import com.parse.Parse

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 🔹 Inicializa Parse al arrancar la app
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("Aj7A5ON6a3mHS5IPZboi9JXrYVueDAfm0snSDYhC") // <-- tu Application ID
                .clientKey("gIcsQ3YwaVGE8odrJOAjF9cnUhyYeLaR2tsMlfsm")   // <-- tu Client Key
                .server("https://parseapi.back4app.com/")                // <-- tu URL del servidor
                .build()
        )
        println("✅ Parse inicializado correctamente")
    }
}
