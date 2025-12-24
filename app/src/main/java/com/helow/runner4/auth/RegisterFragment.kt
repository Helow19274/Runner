package com.helow.runner4.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.auth
import com.helow.runner4.MainActivity
import com.helow.runner4.R
import com.helow.runner4.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<RegisterFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        binding.email.editText?.setText(args.email)
        binding.password.editText?.setText(args.password)

        binding.goToLoginButton.setOnClickListener {
            findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment(binding.email.editText?.text.toString(), binding.password.editText?.text.toString()))
        }

        binding.registerButton.setOnClickListener {
            val email = binding.email.editText?.text
            val password = binding.password.editText?.text
            if (email.isNullOrEmpty())
                binding.email.error = getString(R.string.field_not_filled)
            if (password.isNullOrEmpty())
                binding.password.error = getString(R.string.field_not_filled)

            if (!email.isNullOrEmpty() && !password.isNullOrEmpty())
                lifecycleScope.launch {
                    try {
                        Firebase.auth.createUserWithEmailAndPassword(email.toString(), password.toString()).await()
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    } catch (_: FirebaseNetworkException) {
                        Toast.makeText(requireContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show()
                    } catch (e: FirebaseAuthException) {
                        when(e.errorCode) {
                            "ERROR_INVALID_EMAIL" -> {
                                binding.email.error = getString(R.string.invalid_email)
                                binding.password.error = null
                            }
                            "ERROR_EMAIL_ALREADY_IN_USE" -> {
                                binding.email.error = getString(R.string.email_already_in_use)
                                binding.password.error = null
                            }
                            "ERROR_WEAK_PASSWORD" -> {
                                binding.email.error = null
                                binding.password.error = getString(R.string.weak_password)
                            }
                            else -> Toast.makeText(requireContext(), e.errorCode, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "${e.javaClass.name}: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        return binding.root
    }
}