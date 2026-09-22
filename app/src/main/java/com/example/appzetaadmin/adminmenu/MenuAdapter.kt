package com.example.appzetaadmin.adminmenu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetaadmin.R

class MenuAdapter(
    private val menuList: MutableList<TaskMenu>,
    private val onEditClick: (position: Int) -> Unit,
    private val onDeleteClick: (position: Int) -> Unit
) : RecyclerView.Adapter<MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MenuViewHolder(layoutInflater.inflate(R.layout.item_task_menu, parent, false))
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuList[position]
        holder.render(item, onEditClick, onDeleteClick)
    }

    override fun getItemCount(): Int = menuList.size
}