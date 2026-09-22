package com.example.appzetaadmin.adminmenu.entradasadmin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetaadmin.R

class EntradasAdapter(
    var entradas: MutableList<TaskEntradas>, // Cambiado a MutableList
    private val onItemClick: (Int) -> Unit  // Nuevo callback
) : RecyclerView.Adapter<EntradasViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EntradasViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task_entradas, parent, false)
        return EntradasViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: EntradasViewHolder,
        position: Int
    ) {
        // Le pasamos el onItemClick al ViewHolder
        holder.render(entradas[position], onItemClick)
    }

    override fun getItemCount() = entradas.size
}