package com.example.appzetar.Usuario.Entradas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetar.AdminMenu.EntradasAdmin.TaskEntradas
import com.example.appzetar.R
import com.example.appzetar.Usuario.Carrito.EntradaPedido
import com.example.appzetar.Usuario.Carrito.PedidoManager

class EntradasUsuarioAdapter(
    private val entradas: MutableList<TaskEntradas>
) : RecyclerView.Adapter<EntradasUsuarioViewHolder>() {

    private val cantidadesSeleccionadas =
        mutableMapOf<Int, Int>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EntradasUsuarioViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_task_entradas,
            parent,
            false
        )

        return EntradasUsuarioViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: EntradasUsuarioViewHolder,
        position: Int
    ) {
        val entrada = entradas[position]

        val cantidadEnCarrito =
            PedidoManager.cantidadEntradaEnPedido(entrada.id)

        val maximoSeleccionable =
            (entrada.stock - cantidadEnCarrito)
                .coerceAtLeast(0)

        val cantidadSeleccionada =
            cantidadesSeleccionadas[entrada.id] ?: 0

        holder.render(
            taskEntradas = entrada,
            cantidadSeleccionada = cantidadSeleccionada,
            maximoSeleccionable = maximoSeleccionable,

            onAlternarClick = {
                alternarEntrada(entrada)
            }
        )
    }

    override fun getItemCount(): Int = entradas.size

    fun obtenerEntradasSeleccionadas(): List<EntradaPedido> {
        return entradas.mapNotNull { entrada ->

            val cantidad =
                cantidadesSeleccionadas[entrada.id] ?: 0

            if (cantidad <= 0) {
                null
            } else {
                EntradaPedido(
                    id = entrada.id,
                    nombre = entrada.nombre,
                    cantidad = cantidad
                )
            }
        }
    }

    fun limpiarSeleccion() {
        if (cantidadesSeleccionadas.isEmpty()) {
            return
        }

        cantidadesSeleccionadas.clear()
        notifyDataSetChanged()
    }

    private fun alternarEntrada(
        entrada: TaskEntradas
    ) {
        val cantidadActual =
            cantidadesSeleccionadas[entrada.id] ?: 0

        // Si ya fue elegida, el segundo toque la desmarca.
        if (cantidadActual > 0) {
            cantidadesSeleccionadas.remove(entrada.id)

            notificarEntradaCambiada(entrada.id)
            return
        }

        val cantidadEnCarrito =
            PedidoManager.cantidadEntradaEnPedido(entrada.id)

        val maximoSeleccionable =
            (entrada.stock - cantidadEnCarrito)
                .coerceAtLeast(0)

        if (
            !entrada.disponible ||
            maximoSeleccionable <= 0
        ) {
            return
        }

        // Solo se selecciona una unidad por toque.
        cantidadesSeleccionadas[entrada.id] = 1

        notificarEntradaCambiada(entrada.id)
    }

    private fun notificarEntradaCambiada(
        entradaId: Int
    ) {
        val posicion = entradas.indexOfFirst { entrada ->
            entrada.id == entradaId
        }

        if (posicion >= 0) {
            notifyItemChanged(posicion)
        }
    }
}