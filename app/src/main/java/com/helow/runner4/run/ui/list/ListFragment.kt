package com.helow.runner4.run.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.helow.runner4.R
import com.helow.runner4.databinding.FragmentListBinding
import com.helow.runner4.run.Run

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: CollectionReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)

        db = Firebase.firestore.collection("users").document(Firebase.auth.uid!!).collection("runs")
        val options = FirestoreRecyclerOptions.Builder<Run>().setQuery(db.orderBy("startTime", Query.Direction.DESCENDING)) {
            val run = it.toObject<Run>()!!
            run.id = it.id
            run
        }.setLifecycleOwner(viewLifecycleOwner).build()

        val adapter = RunAdapter(options)
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                requireContext(),
                binding.recyclerView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val run = adapter.getItem(position)
                        if (run.route.isEmpty())
                            Toast.makeText(
                                requireContext(),
                                R.string.no_points_available,
                                Toast.LENGTH_SHORT
                            ).show()
                        else
                            findNavController().navigate(
                                ListFragmentDirections.actionListFragmentToDetailFragment(
                                    run.id
                                )
                            )
                    }

                    override fun onLongItemClick(view: View, position: Int) {
                        val run = adapter.getItem(position)

                        val view = layoutInflater.inflate(R.layout.dialog_rename, null)
                        val input = view.findViewById<TextInputEditText>(R.id.input)
                        input.setText(run.name)

                        val dialog = MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.rename_title, run.name))
                            .setView(view)
                            .setPositiveButton(R.string.save, null)
                            .setNegativeButton(R.string.cancel, null)
                            .show()

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val text = input.text?.toString().orEmpty()
                            if (text.isBlank()) {
                                input.error = getString(R.string.name_not_set)
                            } else {
                                db.document(run.id).update("name", text)
                                dialog.dismiss()
                            }
                        }
                    }
                })
        )

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) =
                false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                db.document(adapter.getItem(viewHolder.bindingAdapterPosition).id).delete()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerView)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}