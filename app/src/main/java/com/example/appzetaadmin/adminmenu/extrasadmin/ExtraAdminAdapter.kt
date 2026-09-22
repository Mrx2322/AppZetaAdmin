package com.example.appzetaadmin.adminmenu.extrasadmin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetaadmin.R
import com.example.appzetaadmin.adminmenu.ExtraItem

class ExtraAdminAdapter(
    private val listaExtras: MutableList<ExtraItem>,
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ExtraAdminViewHolder>() {


    // =========================================================
    // CREAR VIEWHOLDER
    // =========================================================

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExtraAdminViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_extra_admin,
                    parent,
                    false
                )

        return ExtraAdminViewHolder(
            view
        )
    }


    // =========================================================
    // MOSTRAR EXTRA
    // =========================================================

    override fun onBindViewHolder(
        holder: ExtraAdminViewHolder,
        position: Int
    ) {

        holder.render(
            listaExtras[position],
            onEditClick,
            onDeleteClick
        )
    }


    // =========================================================
    // CANTIDAD
    // =========================================================

    override fun getItemCount(): Int {

        return listaExtras.size
    }
}