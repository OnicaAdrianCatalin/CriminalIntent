package com.example.criminalintent.presentation.auth.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.criminalintent.utils.Event
import com.example.criminalintent.utils.Result
import com.example.criminalintent.utils.SignUpErrors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    val currentUser: FirebaseUser? = auth.currentUser
    private val _authResult = MutableLiveData<Event<Result<SignUpErrors, Unit>>>()
    val authResult: LiveData<Event<Result<SignUpErrors, Unit>>> = _authResult

    init {
        Log.d(TAG, "$currentUser: ")
    }

    fun loginWithEmailAndPassword(
        emailAddress: String,
        password: String,
    ) {
        if (emailAddress.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signedIn successfully ")
                        _authResult.value = Event(Result.Success(Unit))
                    } else {
                        if (task.exception is IllegalArgumentException) {
                            _authResult.value =
                                Event(Result.Failure(SignUpErrors.EMPTY_FIELDS))
                        }
                        if (task.exception is FirebaseAuthInvalidUserException) {
                            _authResult.value =
                                Event(Result.Failure(SignUpErrors.EMAIL_OR_PASSWORD_INCORRECT))
                        }
                        Log.e(TAG, "signIn failed", task.exception)
                    }
                }
        } else {
            Log.d(TAG, "views are empty")
            _authResult.value = Event(Result.Failure(SignUpErrors.EMPTY_FIELDS))
        }
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}
