package com.example.appzetaadmin.adminmenu

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetaadmin.R

class MenuViewHolder(
    view: View
) : RecyclerView.ViewHolder(view) {

    val tvNombrePlato: TextView =
        view.findViewById(R.id.tvMenuPlato)

    val tvPrecio: TextView =
        view.findViewById(R.id.tvPrecio)

    val btnEditar: ImageButton =
        view.findViewById(R.id.btnEditar)

    val btnEliminar: ImageButton =
        view.findViewById(R.id.btnEliminar)

    @SuppressLint("SetTextI18n")
    fun render(
        taskMenu: TaskMenu,
        onEditClick: (Int) -> Unit,
        onDeleteClick: (Int) -> Unit
    ) {

        tvNombrePlato.text =
            taskMenu.name

        tvPrecio.text =
            "S/ %.2f".format(taskMenu.precio)

        // EDITAR
        btnEditar.setOnClickListener {

            val posicion =
                bindingAdapterPosition

            if (posicion != RecyclerView.NO_POSITION) {
                onEditClick(posicion)
            }
        }

        // ELIMINAR
        btnEliminar.setOnClickListener {

            val posicion =
                bindingAdapterPosition

            if (posicion != RecyclerView.NO_POSITION) {
                onDeleteClick(posicion)
            }
        }
    }
}