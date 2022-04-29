package com.example.criminalintent

import android.app.Application
import androidx.room.Room
import com.example.criminalintent.database.CrimeDatabase

class CriminalIntentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val database =
            Room.databaseBuilder(this, CrimeDatabase::class.java, CrimeRepository.DATABASE_NAME)
                .build()
        CrimeRepository.initialize(database.crimeDao())
    }
}
