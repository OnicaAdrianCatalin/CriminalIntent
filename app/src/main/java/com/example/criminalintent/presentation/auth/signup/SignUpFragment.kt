package com.example.criminalintent.presentation.auth.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.criminalintent.R
import com.example.criminalintent.utils.SignUpErrors
import com.example.criminalintent.utils.observeEvent
import com.example.criminalintent.utils.onFailure
import com.example.criminalintent.utils.onSuccess
import com.google.android.material.snackbar.Snackbar

class SignUpFragment : Fragment() {

    private lateinit var emailAddressEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginTextView: TextView

    private val viewModel by lazy {
        ViewModelProvider(this)[SignUpViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.fragment_signup, container, false)
        bindViews(view)
        setOnClickListeners()
        observeViewModel()
        return view
    }

    private fun observeViewModel() {
        viewModel.authResult.observeEvent(viewLifecycleOwner) {
            it.onSuccess {
                Snackbar.make(
                    signUpButton, "SignedUp Successfully!",
                    Snackbar.LENGTH_LONG
                )
                    .show()
                findNavController().navigate(R.id.action_signUpFragment_to_crimeListFragment)
            }.onFailure { signUpErrors: SignUpErrors ->
                when (signUpErrors) {
                    SignUpErrors.PASSWORD_MISMATCH -> {
                        passwordEditText.error = "passwords are not matching"
                        confirmPasswordEditText.error = "passwords are not matching"
                    }
                    SignUpErrors.EMPTY_FIELDS -> {
                        checkEmptyEditText(emailAddressEditText)
                        checkEmptyEditText(passwordEditText)
                        checkEmptyEditText(confirmPasswordEditText)
                    }

                    SignUpErrors.EMAIL_ALREADY_IN_USE -> {
                        emailAddressEditText.error = "The Email already exists"
                    }
                }
            }
        }
    }

    private fun setOnClickListeners() {
        signUpButton.setOnClickListener {
            onSignUpButtonPressed()
        }
        loginTextView.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
    }

    private fun onSignUpButtonPressed() {
        viewModel.signUpUserWithEmailAndPassword(
            emailAddressEditText.text.toString(),
            passwordEditText.text.toString(),
            confirmPasswordEditText.text.toString(),
        )
    }

    private fun checkEmptyEditText(editText: EditText) {
        if (editText.text.toString().isEmpty()) {
            editText.error = "Cannot be empty"
        }
    }

    private fun bindViews(view: View) {
        signUpButton = view.findViewById(R.id.signup_button)
        emailAddressEditText = view.findViewById(R.id.email_address_edit_text)
        passwordEditText = view.findViewById(R.id.password_edit_text)
        confirmPasswordEditText = view.findViewById(R.id.confirm_password_edit_text)
        loginTextView = view.findViewById(R.id.login_text_view)
    }
}
