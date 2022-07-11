package com.example.criminalintent.presentation.crimeList

import androidx.lifecycle.ViewModel
import com.example.criminalintent.data.repository.CrimeRepository
import com.google.firebase.auth.FirebaseAuth

class CrimeListViewModel(crimeRepository: CrimeRepository) : ViewModel() {
    val crimeListLiveData = crimeRepository.getCrimes()
    val auth = FirebaseAuth.getInstance()
}
