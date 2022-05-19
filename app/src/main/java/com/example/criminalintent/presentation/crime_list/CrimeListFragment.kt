package com.example.criminalintent.presentation.crime_list

import android.content.Context
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.R
import com.example.criminalintent.data.model.Crime

class CrimeListFragment : Fragment() {
    interface Callbacks {
        fun onCrimeSelected(crimeId: Int)
    }

    private val viewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }
    private var callbacks: Callbacks? = null
    private var adapter: CrimeAdapter = CrimeAdapter(emptyList())
    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var warningTextView: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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
        observeData()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.view_crimelist, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
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
            callbacks?.onCrimeSelected(crime.id)
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

        fun newInstance(): CrimeListFragment = CrimeListFragment()
    }
}
