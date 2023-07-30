package com.example.mytracking.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mytracking.R
import com.example.mytracking.models.Angkot

class AngkotAdapter() : RecyclerView.Adapter<AngkotAdapter.MyViewHolder>() {
    private var userList = ArrayList<Angkot>()
    var onItemClick : ((Angkot) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.view_angkot,
            parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = userList[position]

        holder.namaAngkot.text = currentitem.namaAngkot
        holder.jurusan.text = currentitem.jurusan

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(currentitem)
        }



    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateAngkotList(userList : List<Angkot>){

        this.userList.clear()
        this.userList.addAll(userList)
        notifyDataSetChanged()

    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val namaAngkot : TextView = itemView.findViewById(R.id.tvAngkot)
        val jurusan : TextView = itemView.findViewById(R.id.tvJurusan)

    }

}


