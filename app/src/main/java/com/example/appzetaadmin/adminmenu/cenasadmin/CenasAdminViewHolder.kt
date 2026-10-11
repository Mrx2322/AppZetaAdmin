package com.example.appzetaadmin.adminmenu.cenasadmin

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetaadmin.R

class CenasAdminViewHolder(
    view: View
) : RecyclerView.ViewHolder(view) {

    private val tvNombreCena: TextView =
        view.findViewById(R.id.tvMenuPlato)

    private val tvPrecio: TextView =
        view.findViewById(R.id.tvPrecio)

    private val btnEditar: ImageButton =
        view.findViewById(R.id.btnEditar)

    private val btnEliminar: ImageButton =
        view.findViewById(R.id.btnEliminar)

    @SuppressLint("SetTextI18n")
    fun render(
        cena: TaskCenas,
        onEditClick: (Int) -> Unit,
        onDeleteClick: (Int) -> Unit
    ) {
        tvNombreCena.text = cena.name
        tvPrecio.text = "S/ %.2f · Stock: %d".format(
            java.util.Locale.forLanguageTag("es-PE"),
            cena.precio,
            cena.stock
        )

        btnEditar.setOnClickListener {
            val posicion = bindingAdapterPosition

            if (posicion != RecyclerView.NO_POSITION) {
                onEditClick(posicion)
            }
        }

        btnEliminar.setOnClickListener {
            val posicion = bindingAdapterPosition

            if (posicion != RecyclerView.NO_POSITION) {
                onDeleteClick(posicion)
            }
        }
    }
}