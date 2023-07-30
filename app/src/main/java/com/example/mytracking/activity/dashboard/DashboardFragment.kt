package com.example.mytracking.activity.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mytracking.adapter.AngkotAdapter
import com.example.mytracking.databinding.FragmentDashboardBinding
import com.example.mytracking.models.Angkot
import com.example.mytracking.models.AngkotViewModel
import java.util.Locale

class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var viewModel: AngkotViewModel
    private lateinit var adapter: AngkotAdapter
    private lateinit var rvAngkot: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvAngkot = binding.rvAngkot
        rvAngkot.layoutManager = LinearLayoutManager(context)
        rvAngkot.setHasFixedSize(true)
        adapter = AngkotAdapter()
        rvAngkot.adapter = adapter

        viewModel = ViewModelProvider(this)[AngkotViewModel::class.java]

        viewModel.allUsers.observe(viewLifecycleOwner, Observer {

            adapter.updateAngkotList(it)

        })


        adapter.onItemClick = {
            val intent = Intent(context, DetailAngkotActivity::class.java)
            intent.putExtra("Angkot", it)
            startActivity(intent)

        }





    }




}









