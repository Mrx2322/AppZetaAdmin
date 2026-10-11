package com.example.appzetaadmin.adminmenu.cenasadmin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetaadmin.R

class CenasAdminAdapter(
    private val cenasList: MutableList<TaskCenas>,
    private val onEditClick: (position: Int) -> Unit,
    private val onDeleteClick: (position: Int) -> Unit
) : RecyclerView.Adapter<CenasAdminViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CenasAdminViewHolder {

        val layoutInflater =
            LayoutInflater.from(parent.context)

        return CenasAdminViewHolder(
            layoutInflater.inflate(
                R.layout.item_task_menu,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: CenasAdminViewHolder,
        position: Int
    ) {
        val item = cenasList[position]

        holder.render(
            item,
            onEditClick,
            onDeleteClick
        )
    }

    override fun getItemCount(): Int =
        cenasList.size
}