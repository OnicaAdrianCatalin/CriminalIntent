package com.example.criminalintent

import android.app.Application
import androidx.room.Room
import com.example.criminalintent.database.CrimeDatabase

class CriminalIntentApplication : Application() {

    private val database =
        Room.databaseBuilder(this, CrimeDatabase::class.java, CrimeRepository.DATABASE_NAME).build()

    override fun onCreate() {
        CrimeRepository.initialize(database.crimeDao())
        super.onCreate()
    }
}
