package com.example.appzetaadmin.adminmenu.pedidosadmin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetaadmin.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PedidoAdminAdapter(
    private val listaPedidos: MutableList<PedidoAdmin>,
    private val onCambiarEstado: (PedidoAdmin, String) -> Unit,
    private val modoHistorial: Boolean = false,
    private val onEliminarPedido: (PedidoAdmin) -> Unit = {}
) : RecyclerView.Adapter<PedidoAdminViewHolder>() {

    private val zonaPeru = TimeZone.getTimeZone("America/Lima")
    private val localePeru = Locale.forLanguageTag("es-PE")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoAdminViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido_admin, parent, false)

        return PedidoAdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoAdminViewHolder, position: Int) {
        configurarEncabezadoFecha(holder.itemView, position)

        val pedido = listaPedidos[position]
        val btnEliminarPedido = holder.itemView.findViewById<MaterialButton>(
            R.id.btnEliminarPedido
        )

        btnEliminarPedido.visibility = if (modoHistorial) View.VISIBLE else View.GONE
        btnEliminarPedido.setOnClickListener {
            onEliminarPedido(pedido)
        }

        holder.render(
            pedido = pedido,
            onCambiarEstado = onCambiarEstado,
            modoHistorial = modoHistorial
        )
    }

    override fun getItemCount(): Int = listaPedidos.size

    fun actualizarPedidos(nuevosPedidos: List<PedidoAdmin>) {
        val resultado = DiffUtil.calculateDiff(
            PedidoDiffCallback(
                anteriores = listaPedidos.toList(),
                nuevos = nuevosPedidos
            )
        )

        listaPedidos.clear()
        listaPedidos.addAll(nuevosPedidos)
        resultado.dispatchUpdatesTo(this)
    }

    private fun configurarEncabezadoFecha(itemView: View, position: Int) {
        val encabezado = itemView.findViewById<TextView>(R.id.tvEncabezadoFecha)

        if (!modoHistorial) {
            encabezado.visibility = View.GONE
            return
        }

        val fechaActual = listaPedidos[position].fecha
        val fechaAnterior = listaPedidos.getOrNull(position - 1)?.fecha
        val iniciaNuevoDia = fechaAnterior == null || !esMismoDia(fechaActual, fechaAnterior)

        if (iniciaNuevoDia) {
            encabezado.text = formatearEncabezado(itemView, fechaActual)
            encabezado.visibility = View.VISIBLE
        } else {
            encabezado.visibility = View.GONE
        }
    }

    private fun formatearEncabezado(view: View, fecha: Timestamp?): String {
        fecha ?: return view.context.getString(R.string.admin_history_without_date)

        return when {
            esDiaRespectoHoy(fecha, 0) ->
                view.context.getString(R.string.admin_history_today)

            esDiaRespectoHoy(fecha, -1) ->
                view.context.getString(R.string.admin_history_yesterday)

            else -> SimpleDateFormat("EEEE, d 'DE' MMMM", localePeru).apply {
                timeZone = zonaPeru
            }.format(fecha.toDate()).uppercase(localePeru)
        }
    }

    private fun esDiaRespectoHoy(fecha: Timestamp, dias: Int): Boolean {
        val referencia = Calendar.getInstance(zonaPeru).apply {
            add(Calendar.DAY_OF_YEAR, dias)
        }
        val calendarioFecha = Calendar.getInstance(zonaPeru).apply {
            time = fecha.toDate()
        }

        return referencia.get(Calendar.YEAR) == calendarioFecha.get(Calendar.YEAR) &&
                referencia.get(Calendar.DAY_OF_YEAR) ==
                calendarioFecha.get(Calendar.DAY_OF_YEAR)
    }

    private fun esMismoDia(primera: Timestamp?, segunda: Timestamp?): Boolean {
        if (primera == null || segunda == null) return false

        val calendarioPrimero = Calendar.getInstance(zonaPeru).apply {
            time = primera.toDate()
        }
        val calendarioSegundo = Calendar.getInstance(zonaPeru).apply {
            time = segunda.toDate()
        }

        return calendarioPrimero.get(Calendar.YEAR) == calendarioSegundo.get(Calendar.YEAR) &&
                calendarioPrimero.get(Calendar.DAY_OF_YEAR) ==
                calendarioSegundo.get(Calendar.DAY_OF_YEAR)
    }

    private class PedidoDiffCallback(
        private val anteriores: List<PedidoAdmin>,
        private val nuevos: List<PedidoAdmin>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = anteriores.size

        override fun getNewListSize(): Int = nuevos.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            anteriores[oldItemPosition].id == nuevos[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            anteriores[oldItemPosition] == nuevos[newItemPosition]
    }
}
