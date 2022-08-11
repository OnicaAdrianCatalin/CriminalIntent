package com.example.criminalintent

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.criminalintent.data.local.CrimeDao
import com.example.criminalintent.data.local.CrimeDatabase
import com.example.criminalintent.data.model.Crime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class CrimeDaoTest {

    private lateinit var database: CrimeDatabase
    private lateinit var crimeDao: CrimeDao

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CrimeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        crimeDao = database.crimeDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun addCrime() {
        val crime = Crime(1, "", Date(), false, "")
        crimeDao.addCrime(crime)
        val crimes = crimeDao.getCrimes().getOrAwaitValue().size
        assertEquals(crimes, 1)
    }

    @Test
    fun updateCrime() {
        val crime = Crime(1, "", Date(), false, "")
        crimeDao.addCrime(crime)
        crime.title = "title"
        crimeDao.updateCrime(crime)
        val crimes = crimeDao.getCrimes().getOrAwaitValue()
        assertEquals(crimes[0].title, "title")
    }
}