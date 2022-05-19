package com.example.criminalintent.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.criminalintent.data.model.Crime

@Dao
interface CrimeDao {

    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>>

    @Query("SELECT * FROM crime WHERE id =(:id)")
    fun getCrime(id: Int): LiveData<Crime?>

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateCrime(crime: Crime)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCrime(crime: Crime): Long

    @Transaction
    fun addOrUpdate(crime: Crime) {
        val id: Long = addCrime(crime)
        if (id == -1L) {
            updateCrime(crime)
        }
    }
}
