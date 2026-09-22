package com.example.appzetaadmin.adminmenu.extrasadmin

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetaadmin.R
import java.util.Locale
import com.example.appzetaadmin.adminmenu.ExtraItem

class ExtraAdminViewHolder(
    view: View
) : RecyclerView.ViewHolder(view) {

    private val tvNombreExtraAdmin =
        view.findViewById<TextView>(
            R.id.tvNombreExtraAdmin
        )

    private val tvPrecioExtraAdmin =
        view.findViewById<TextView>(
            R.id.tvPrecioExtraAdmin
        )

    private val btnEditarExtra =
        view.findViewById<ImageButton>(
            R.id.btnEditarExtra
        )

    private val btnEliminarExtra =
        view.findViewById<ImageButton>(
            R.id.btnEliminarExtra
        )


    fun render(
        extra: ExtraItem,
        onEditClick: (Int) -> Unit,
        onDeleteClick: (Int) -> Unit
    ) {

        // =====================================================
        // NOMBRE
        // =====================================================

        tvNombreExtraAdmin.text =
            extra.nombre


        // =====================================================
        // PRECIO
        // =====================================================

        tvPrecioExtraAdmin.text =
            String.format(
                Locale.US,
                "S/ %.2f",
                extra.precio
            )


        // =====================================================
        // EDITAR
        // =====================================================

        btnEditarExtra.setOnClickListener {

            val posicion =
                bindingAdapterPosition

            if (
                posicion != RecyclerView.NO_POSITION
            ) {

                onEditClick(
                    posicion
                )
            }
        }


        // =====================================================
        // ELIMINAR
        // =====================================================

        btnEliminarExtra.setOnClickListener {

            val posicion =
                bindingAdapterPosition

            if (
                posicion != RecyclerView.NO_POSITION
            ) {

                onDeleteClick(
                    posicion
                )
            }
        }
    }
}