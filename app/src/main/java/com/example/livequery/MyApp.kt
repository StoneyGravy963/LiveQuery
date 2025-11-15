package com.example.livequery

import android.app.Application
import com.parse.Parse

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("TzNHtrZ6cGuXstwMGVNtIL3bpzJbAlGmVZvua8nF")
                .clientKey("aj5LYf4Xdyhz0Z3HrR3cZSHAok7xWxITyOSC2DCE")
                .server("https://parseapi.back4app.com/")
                .build()
        )
    }
}