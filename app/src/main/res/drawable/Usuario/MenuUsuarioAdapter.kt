package com.example.appzetar.Usuario

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetar.AdminMenu.TaskMenu
import com.example.appzetar.R

class MenuUsuarioAdapter(
    private val listaMenu: MutableList<TaskMenu>,
    private val onAgregarClick: (TaskMenu) -> Unit
) : RecyclerView.Adapter<MenuUsuarioViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MenuUsuarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_task_menu_usuario,
            parent,
            false
        )

        return MenuUsuarioViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MenuUsuarioViewHolder,
        position: Int
    ) {
        holder.render(
            listaMenu[position],
            onAgregarClick
        )
    }

    override fun getItemCount(): Int {
        return listaMenu.size
    }
}