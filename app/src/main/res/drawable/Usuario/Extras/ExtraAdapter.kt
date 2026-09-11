package com.example.appzetar.Usuario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetar.R
import java.util.Locale

class ExtraAdapter(
    extrasIniciales: List<ExtraItem>,
    private val onAgregarClick: (ExtraItem) -> Unit
) : RecyclerView.Adapter<ExtraAdapter.ExtraViewHolder>() {

    private var todosLosExtras = extrasIniciales.toList()
    private var extrasVisibles = todosLosExtras.toList()
    private var categoriaActual = 0

    inner class ExtraViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val imgExtra = itemView.findViewById<ImageView>(R.id.imgExtra)
        private val tvNombreExtra = itemView.findViewById<TextView>(R.id.tvNombreExtra)
        private val tvPrecioExtra = itemView.findViewById<TextView>(R.id.tvPrecioExtra)
        private val btnAgregarExtra = itemView.findViewById<View>(R.id.btnAgregarExtra)

        fun bind(extra: ExtraItem) {
            imgExtra.setImageResource(extra.icono)
            tvNombreExtra.text = extra.nombre
            tvPrecioExtra.text = String.format(
                Locale.US,
                "S/ %.2f",
                extra.precio
            )

            btnAgregarExtra.setOnClickListener {
                onAgregarClick(extra)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_extra,
            parent,
            false
        )
        return ExtraViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExtraViewHolder, position: Int) {
        holder.bind(extrasVisibles[position])
    }

    override fun getItemCount(): Int = extrasVisibles.size

    fun actualizarExtras(nuevosExtras: List<ExtraItem>, categoriaId: Int = categoriaActual) {
        todosLosExtras = nuevosExtras.toList()
        filtrarPorCategoria(categoriaId)
    }

    fun filtrarPorCategoria(categoriaId: Int) {
        categoriaActual = categoriaId
        extrasVisibles = if (categoriaId == 0) {
            todosLosExtras.toList()
        } else {
            todosLosExtras.filter { it.categoriaId == categoriaId }
        }
        notifyDataSetChanged()
    }
}
