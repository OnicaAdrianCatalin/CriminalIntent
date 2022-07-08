package com.example.criminalintent.presentation.auth.signup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.criminalintent.utils.Event
import com.example.criminalintent.utils.Result
import com.example.criminalintent.utils.SignUpErrors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class SignUpViewModel : ViewModel() {
    private val mAuth = FirebaseAuth.getInstance()
    private val _authResult = MutableLiveData<Event<Result<SignUpErrors, Unit>>>()
    val authResult: LiveData<Event<Result<SignUpErrors, Unit>>> = _authResult

    fun signUpUserWithEmailAndPassword(
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (!assertFieldsNotEmpty(email, password, confirmPassword)) {
            _authResult.value = Event(Result.Failure(SignUpErrors.EMPTY_FIELDS))
        } else {
            if (confirmPassword == password) {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "SignedUp Successfully")
                            _authResult.value = Event(Result.Success(Unit))
                            Log.d(TAG, "signUpUserWithEmailAndPassword: ${mAuth.currentUser}")
                        } else {
                            if (task.exception is FirebaseAuthUserCollisionException) {
                                _authResult.value =
                                    Event(Result.Failure(SignUpErrors.EMAIL_ALREADY_IN_USE))
                            }
                            Log.e(TAG, "signUpUserWithEmailAndPassword: ", task.exception)
                        }
                    }
            } else {
                Log.d(TAG, "passwords doesn't match")
                _authResult.value = Event(Result.Failure(SignUpErrors.PASSWORD_MISMATCH))
            }
        }
    }

    private fun assertFieldsNotEmpty(
        email: String,
        password: String,
        confirmPassword: String,
    ): Boolean {
        return if (email.isNotEmpty() or password.isNotEmpty() or confirmPassword.isNotEmpty()) {
            true
        } else {
            Log.d(TAG, "views are empty")
            false
        }
    }

    companion object {
        private const val TAG = "SignUpViewModel"
    }
}
