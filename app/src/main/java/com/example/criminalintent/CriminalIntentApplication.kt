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
        val migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE new_table ( id INTEGER NOT NULL," +
                            "title TEXT NOT NULL," +
                            "date INTEGER NOT NULL, " +
                            "isSolved INTEGER NOT NULL," +
                            "PRIMARY KEY(id))"
                )
                database.execSQL(
                    "INSERT INTO new_table(" +
                            "title," +
                            "date," +
                            "isSolved)" +
                            " SELECT title, date, isSolved FROM Crime"
                )
                database.execSQL("DROP TABLE Crime")
                database.execSQL("ALTER TABLE new_table RENAME TO Crime")
            }
        }
        return migration
    }
}
