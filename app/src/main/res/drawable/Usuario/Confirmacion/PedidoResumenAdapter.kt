package com.example.appzetar.Usuario.Confirmacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetar.R
import com.example.appzetar.Usuario.Carrito.PedidoItem

class PedidoResumenAdapter(
    private val listaPedido: MutableList<PedidoItem>
) : RecyclerView.Adapter<PedidoResumenAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_pedido_resumen,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        holder.render(
            listaPedido[position]
        )
    }

    override fun getItemCount(): Int {
        return listaPedido.size
    }

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        private val tvNombre =
            view.findViewById<TextView>(
                R.id.tvNombreProducto
            )

        private val tvCantidad =
            view.findViewById<TextView>(
                R.id.tvCantidadProducto
            )

        fun render(item: PedidoItem) {

            tvNombre.text =
                item.nombre

            tvCantidad.text =
                "x${item.cantidad}"
        }
    }
}