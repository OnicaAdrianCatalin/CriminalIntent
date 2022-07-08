package com.example.criminalintent.presentation.crimeList

import androidx.lifecycle.ViewModel
import com.example.criminalintent.data.repository.CrimeRepository
import com.google.firebase.auth.FirebaseAuth

class CrimeListViewModel : ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()
    val auth = FirebaseAuth.getInstance()
}
