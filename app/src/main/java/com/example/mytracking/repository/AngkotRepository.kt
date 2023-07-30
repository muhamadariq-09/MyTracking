package com.example.mytracking.repository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.mytracking.models.Angkot
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AngkotRepository {
    private val dbRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("Angkot")

    @Volatile private var INSTANCE : AngkotRepository?= null

    fun getInstance() : AngkotRepository {
        return INSTANCE ?: synchronized(this){

            val instance = AngkotRepository()
            INSTANCE = instance
            instance
        }


    }





    fun loadUsers(userList : MutableLiveData<List<Angkot>>){

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                try {

                    val _userList : List<Angkot> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(Angkot::class.java)!!



                    }

                    userList.postValue(_userList)

                }catch (e : Exception){


                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })


    }



}