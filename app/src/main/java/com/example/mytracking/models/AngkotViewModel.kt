package com.example.mytracking.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mytracking.repository.AngkotRepository

class AngkotViewModel : ViewModel() {

    private val repository : AngkotRepository
    private val _allUsers = MutableLiveData<List<Angkot>>()
    val allUsers : LiveData<List<Angkot>> = _allUsers




    init {

        repository = AngkotRepository().getInstance()
        repository.loadUsers(_allUsers)

    }
}