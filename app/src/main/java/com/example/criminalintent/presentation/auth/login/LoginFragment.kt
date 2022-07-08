package com.example.criminalintent.presentation.auth.login

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

class LoginFragment : Fragment() {

    private lateinit var notRegisteredTextView: TextView
    private lateinit var emailAddressEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.fragment_login, container, false)
        bindViews(view)
        setOnClickListeners()
        observeViewModel()
        return view
    }

    private fun observeViewModel() {
        viewModel.authResult.observeEvent(viewLifecycleOwner) {
            it.onSuccess {
                findNavController().navigate(R.id.action_loginFragment_to_crimeListFragment)
            }.onFailure { signUpErrors: SignUpErrors ->
                when (signUpErrors) {
                    SignUpErrors.EMPTY_FIELDS -> {
                        checkEmptyLabels(emailAddressEditText)
                        checkEmptyLabels(passwordEditText)
                    }
                    SignUpErrors.EMAIL_OR_PASSWORD_INCORRECT -> {
                        emailAddressEditText.error = "Email or password may not be correct"
                        passwordEditText.error = "Email or password may not be correct"
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkIfUserSignedIn()
    }

    private fun checkIfUserSignedIn() {
        if (viewModel.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_crimeListFragment)
        }
    }

    private fun bindViews(view: View) {
        notRegisteredTextView = view.findViewById(R.id.register_text_view)
        emailAddressEditText = view.findViewById(R.id.email_address_edit_text)
        passwordEditText = view.findViewById(R.id.password_edit_text)
        loginButton = view.findViewById(R.id.login_button)
    }

    private fun setOnClickListeners() {
        notRegisteredTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
        loginButton.setOnClickListener {
            onLoginButtonPressed()
        }
    }

    private fun onLoginButtonPressed() {
        viewModel.loginWithEmailAndPassword(
            emailAddressEditText.text.toString(),
            passwordEditText.text.toString()
        )
    }

    private fun checkEmptyLabels(editText: EditText) {
        if (editText.text.toString().isEmpty()) {
            editText.error = "Cannot be empty"
        }
    }
}
