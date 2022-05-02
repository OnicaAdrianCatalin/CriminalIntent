package com.example.criminalintent.presentation.crime_fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.criminalintent.data.model.Crime
import com.example.criminalintent.data.repository.CrimeRepository

class CrimeDetailViewModel : ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    private val crimeIdLiveData = MutableLiveData<Int>()

    var crimeLiveData: LiveData<Crime?> =
        Transformations.switchMap(crimeIdLiveData) { crimeId ->
            crimeRepository.getCrime(crimeId)
        }

    fun loadCrime(crimeId: Int) {
        crimeIdLiveData.value = crimeId
    }

    fun updateCrime(crime: Crime) {
        crimeRepository.updateCrime(crime)
    }
}