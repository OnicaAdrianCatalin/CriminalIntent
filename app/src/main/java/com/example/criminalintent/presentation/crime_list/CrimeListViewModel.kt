package com.example.criminalintent.presentation.crime_list

import androidx.lifecycle.ViewModel
import com.example.criminalintent.data.model.Crime
import com.example.criminalintent.data.repository.CrimeRepository

class CrimeListViewModel : ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime){
        crimeRepository.addCrime(crime)
    }
}
