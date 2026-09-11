package com.example.appzetar.Usuario

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetar.R

class CategoriaAdapter(
    private val categorias: List<CategoriaItem>,
    private val onCategoriaClick: (CategoriaItem) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    // ID de la categoría seleccionada
    private var categoriaSeleccionada = 0

    inner class CategoriaViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val imgCategoria =
            itemView.findViewById<ImageView>(
                R.id.imgCategoria
            )

        private val tvCategoria =
            itemView.findViewById<TextView>(
                R.id.tvCategoria
            )

        fun bind(categoria: CategoriaItem) {

            // -------------------------------------------------
            // IMAGEN
            // -------------------------------------------------

            imgCategoria.setImageResource(
                categoria.icono
            )


            // -------------------------------------------------
            // NOMBRE
            // -------------------------------------------------

            tvCategoria.text =
                categoria.nombre


            // -------------------------------------------------
            // ESTADO SELECCIONADO
            // -------------------------------------------------

            if (categoria.id == categoriaSeleccionada) {

                tvCategoria.setTextColor(
                    Color.parseColor("#FF9500")
                )

                tvCategoria.alpha =
                    1f

            } else {

                tvCategoria.setTextColor(
                    Color.parseColor("#333333")
                )

                tvCategoria.alpha =
                    0.75f
            }


            // -------------------------------------------------
            // CLICK
            // -------------------------------------------------

            itemView.setOnClickListener {

                categoriaSeleccionada =
                    categoria.id

                notifyDataSetChanged()

                onCategoriaClick(
                    categoria
                )
            }
        }
    }


    // =========================================================
    // CREAR VIEW HOLDER
    // =========================================================

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoriaViewHolder {

        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_categoria,
                parent,
                false
            )

        return CategoriaViewHolder(
            view
        )
    }


    // =========================================================
    // ASIGNAR DATOS
    // =========================================================

    override fun onBindViewHolder(
        holder: CategoriaViewHolder,
        position: Int
    ) {

        holder.bind(
            categorias[position]
        )
    }


    // =========================================================
    // CANTIDAD
    // =========================================================

    override fun getItemCount(): Int =
        categorias.size
}