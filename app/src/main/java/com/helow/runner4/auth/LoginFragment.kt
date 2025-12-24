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
import com.helow.runner4.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<LoginFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.email.editText?.setText(args.email)
        binding.password.editText?.setText(args.password)

        binding.goToRegisterButton.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment(binding.email.editText?.text.toString(), binding.password.editText?.text.toString()))
        }

        binding.loginButton.setOnClickListener {
            val email = binding.email.editText?.text
            val password = binding.password.editText?.text

            if (email.isNullOrEmpty()) {
                binding.email.error = getString(R.string.field_not_filled)
            }

            if (password.isNullOrEmpty()) {
                binding.password.error = getString(R.string.field_not_filled)
            }

            if (!email.isNullOrEmpty() && !password.isNullOrEmpty())
                lifecycleScope.launch {
                    try {
                        Firebase.auth.signInWithEmailAndPassword(email.toString(), password.toString()).await()
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
                            "ERROR_USER_NOT_FOUND" -> {
                                binding.email.error = getString(R.string.user_not_found)
                                binding.password.error = null
                            }
                            "ERROR_WRONG_PASSWORD" -> {
                                binding.email.error = null
                                binding.password.error = getString(R.string.wrong_password)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}