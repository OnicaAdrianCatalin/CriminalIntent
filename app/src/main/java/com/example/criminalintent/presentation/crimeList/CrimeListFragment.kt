package com.example.criminalintent.presentation.crimeList

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.R
import com.example.criminalintent.data.model.Crime
import org.koin.androidx.viewmodel.ext.android.viewModel

class CrimeListFragment : Fragment() {

    private val viewModel by viewModel<CrimeListViewModel>()
    private var adapter: CrimeAdapter = CrimeAdapter(emptyList())
    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var warningTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        bindViews(view)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addMenuItems()
        observeData()
    }

    private fun addMenuItems() {
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_crime_list, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.new_crime -> {
                            val crime = Crime()
                            val action =
                                CrimeListFragmentDirections.actionCrimeListFragmentToCrimeFragment(
                                    crime.id
                                )
                            findNavController().navigate(action)
                            true
                        }
                        R.id.sign_out -> {
                            viewModel.auth.signOut()
                            findNavController().navigate(R.id.action_crimeListFragment_to_loginFragment)
                            true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner, Lifecycle.State.RESUMED
        )
    }

    private fun observeData() {
        viewModel.crimeListLiveData.observe(viewLifecycleOwner) { crimes ->
            warningTextView.isVisible = crimes.isEmpty()
            Log.i(TAG, "Got crimes ${crimes.size}")
            updateUI(crimes)
        }
    }

    private fun updateUI(crimes: List<Crime>) {
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

    private fun bindViews(view: View) {
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view)
        warningTextView = view.findViewById(R.id.warning_text_view)
    }

    private inner class CrimeHolder(view: View) :
        RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var crime: Crime
        val titleTextView = itemView.findViewById<View>(R.id.crime_title) as TextView
        val dateTextView = itemView.findViewById<View>(R.id.crime_date) as TextView
        val solvedImageView = itemView.findViewById<View>(R.id.crime_solved) as ImageView

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = crime.title
            dateTextView.text = crime.date.toString()
            solvedImageView.isVisible = crime.isSolved
        }

        override fun onClick(view: View?) {
            findNavController().navigate(
                CrimeListFragmentDirections.actionCrimeListFragmentToCrimeFragment(
                    crime.id
                )
            )
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>) :
        RecyclerView.Adapter<CrimeHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

        override fun getItemCount(): Int = crimes.size
    }

    companion object {
        private const val TAG = "CrimeListFragment"
    }
}
