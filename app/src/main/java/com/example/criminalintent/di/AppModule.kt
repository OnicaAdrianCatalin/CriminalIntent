package com.example.criminalintent.di

import androidx.room.Room
import com.example.criminalintent.data.local.CrimeDatabase
import com.example.criminalintent.data.repository.CrimeRepository
import com.example.criminalintent.data.repository.CrimeRepositoryImpl
import com.example.criminalintent.presentation.auth.login.LoginViewModel
import com.example.criminalintent.presentation.auth.signup.SignUpViewModel
import com.example.criminalintent.presentation.crimeDetail.CrimeDetailViewModel
import com.example.criminalintent.presentation.crimeList.CrimeListViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            CrimeDatabase::class.java,
            CrimeRepositoryImpl.DATABASE_NAME
        ).build().crimeDao()
    }
    single {
        androidApplication().filesDir
    }
    single<CrimeRepository> {
        CrimeRepositoryImpl(crimeDao = get(), filesDir = get())
    }
    viewModel {
        CrimeListViewModel(crimeRepository = get())
    }
    viewModel {
        CrimeDetailViewModel(crimeRepository = get())
    }
    viewModel {
        LoginViewModel()
    }
    viewModel {
        SignUpViewModel()
    }
}
