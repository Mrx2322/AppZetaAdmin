package com.example.appzetar.Usuario.Carrito

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetar.R

class PedidoViewHolder(
    view: View
) : RecyclerView.ViewHolder(view) {

    private val tvNombre =
        view.findViewById<TextView>(
            R.id.tvNombreProducto
        )

    private val tvPrecio =
        view.findViewById<TextView>(
            R.id.tvPrecioProducto
        )

    private val tvDetalleProducto =
        view.findViewById<TextView>(
            R.id.tvDetalleProducto
        )

    private val tvCantidad =
        view.findViewById<TextView>(
            R.id.tvCantidad
        )

    private val tvSubtotal =
        view.findViewById<TextView>(
            R.id.tvSubtotalProducto
        )

    private val btnMenos =
        view.findViewById<ImageButton>(
            R.id.btnMenos
        )

    private val btnMas =
        view.findViewById<ImageButton>(
            R.id.btnMas
        )

    private val btnEliminar =
        view.findViewById<ImageButton>(
            R.id.btnEliminar
        )


    // =========================================================
    // RENDER
    // =========================================================

    fun render(
        item: PedidoItem,
        onAumentar: (PedidoItem) -> Unit,
        onDisminuir: (PedidoItem) -> Unit,
        onEliminar: (PedidoItem) -> Unit
    ) {

        // =====================================================
        // DATOS
        // =====================================================

        tvNombre.text =
            item.nombre

        if (item.tipo == TipoPedido.MENU) {

            tvDetalleProducto.visibility =
                View.VISIBLE

            tvDetalleProducto.text =
                if (item.entradas.isEmpty()) {

                    "Sin entrada"

                } else {

                    val etiqueta =
                        if (item.cantidadEntradas() == 1) {
                            "Entrada"
                        } else {
                            "Entradas"
                        }

                    "$etiqueta: ${item.descripcionEntradas()}"
                }

        } else {

            tvDetalleProducto.visibility =
                View.GONE

            tvDetalleProducto.text =
                ""
        }

        tvPrecio.text =
            "S/ %.2f c/u".format(
                item.precio
            )

        tvCantidad.text =
            item.cantidad.toString()


        // =====================================================
        // SUBTOTAL
        // =====================================================

        val subtotal =
            item.subtotal()

        tvSubtotal.text =
            "S/ %.2f".format(
                subtotal
            )


        // =====================================================
        // BOTÓN +
        // =====================================================

        btnMas.setOnClickListener {

            onAumentar(item)
        }


        // =====================================================
        // BOTÓN -
        // =====================================================

        btnMenos.setOnClickListener {

            onDisminuir(item)
        }


        // =====================================================
        // ELIMINAR
        // =====================================================

        btnEliminar.setOnClickListener {

            onEliminar(item)
        }
    }


    // =========================================================
    // HABILITAR / DESHABILITAR BOTONES
    // =========================================================

    fun setBotonesHabilitados(
        habilitado: Boolean
    ) {

        btnMas.isEnabled =
            habilitado

        btnMenos.isEnabled =
            habilitado

        btnEliminar.isEnabled =
            habilitado

        val alpha =
            if (habilitado) {
                1f
            } else {
                0.5f
            }

        btnMas.alpha =
            alpha

        btnMenos.alpha =
            alpha

        btnEliminar.alpha =
            alpha
    }
}