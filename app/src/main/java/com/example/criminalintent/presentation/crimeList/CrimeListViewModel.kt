package com.example.criminalintent.presentation.crimeList

import androidx.lifecycle.ViewModel
import com.example.criminalintent.data.repository.CrimeRepository

class CrimeListViewModel : ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()
}
