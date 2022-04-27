package com.example.criminalintent

import android.app.Application

class CriminalIntentApplication : Application() {

    override fun onCreate() {
        CrimeRepository.initialize(this)
        super.onCreate()
    }
}
