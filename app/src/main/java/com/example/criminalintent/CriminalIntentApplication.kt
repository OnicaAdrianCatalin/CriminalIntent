package com.example.criminalintent

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.criminalintent.data.local.CrimeDatabase
import com.example.criminalintent.data.repository.CrimeRepository

class CriminalIntentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val database =
            Room.databaseBuilder(this, CrimeDatabase::class.java, CrimeRepository.DATABASE_NAME)
                .addMigrations(migration()).build()
        CrimeRepository.initialize(database.crimeDao())
    }

    private fun migration(): Migration {
        return object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
