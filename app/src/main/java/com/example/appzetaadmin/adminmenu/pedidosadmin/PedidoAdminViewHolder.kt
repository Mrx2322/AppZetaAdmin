package com.example.appzetaadmin.adminmenu.pedidosadmin

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetaadmin.R
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale

class PedidoAdminViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    private val context =
        itemView.context

    // =========================================================
    // INFORMACIÓN DEL PEDIDO
    // =========================================================

    private val tvNumeroPedido: TextView =
        itemView.findViewById(R.id.tvNumeroPedido)

    private val tvFechaPedido: TextView =
        itemView.findViewById(R.id.tvFechaPedido)

    private val tvNombreUsuario: TextView =
        itemView.findViewById(R.id.tvNombreUsuario)

    private val tvTelefono: TextView =
        itemView.findViewById(R.id.tvTelefono)

    private val tvProductos: TextView =
        itemView.findViewById(R.id.tvProductos)

    private val tvTipoEntrega: TextView =
        itemView.findViewById(R.id.tvTipoEntrega)

    private val tvDireccion: TextView =
        itemView.findViewById(R.id.tvDireccion)

    private val tvReferencia: TextView =
        itemView.findViewById(R.id.tvReferencia)

    private val tvMetodoPago: TextView =
        itemView.findViewById(R.id.tvMetodoPago)

    private val tvTotal: TextView =
        itemView.findViewById(R.id.tvTotal)

    private val tvEstadoPedido: TextView =
        itemView.findViewById(R.id.tvEstadoPedido)

    // =========================================================
    // CONTROLES
    // =========================================================

    private val layoutCambiarEstado: View =
        itemView.findViewById(R.id.layoutCambiarEstado)

    private val btnConfirmado: MaterialButton =
        itemView.findViewById(R.id.btnConfirmado)

    private val btnPreparando: MaterialButton =
        itemView.findViewById(R.id.btnPreparando)

    private val btnEnCamino: MaterialButton =
        itemView.findViewById(R.id.btnEnCamino)

    private val btnEntregado: MaterialButton =
        itemView.findViewById(R.id.btnEntregado)

    // =========================================================
    // COLORES
    // =========================================================

    private val colorNaranja =
        ContextCompat.getColor(
            context,
            R.color.pedido_estado_naranja
        )

    private val colorNaranjaClaro =
        ContextCompat.getColor(
            context,
            R.color.pedido_estado_naranja_claro
        )

    private val colorVerde =
        ContextCompat.getColor(
            context,
            R.color.pedido_estado_verde
        )

    private val colorVerdeClaro =
        ContextCompat.getColor(
            context,
            R.color.pedido_estado_verde_claro
        )

    private val colorRojo =
        ContextCompat.getColor(
            context,
            R.color.pedido_estado_rojo
        )

    private val colorRojoClaro =
        ContextCompat.getColor(
            context,
            R.color.pedido_estado_rojo_claro
        )

    // =========================================================
    // MOSTRAR PEDIDO
    // =========================================================

    fun render(
        pedido: PedidoAdmin,
        onCambiarEstado: (PedidoAdmin, String) -> Unit,
        modoHistorial: Boolean = false
    ) {

        mostrarNumeroPedido(pedido)
        mostrarFecha(pedido)
        mostrarCliente(pedido)
        mostrarEntrega(pedido)
        mostrarPago(pedido)
        mostrarProductos(pedido.productos)
        mostrarEstado(pedido.estadoPedido)

        if (modoHistorial) {

            layoutCambiarEstado.visibility =
                View.GONE

        } else {

            layoutCambiarEstado.visibility =
                View.VISIBLE

            configurarBotones(
                pedido = pedido,
                onCambiarEstado = onCambiarEstado
            )
        }
    }

    // =========================================================
    // NÚMERO
    // =========================================================

    private fun mostrarNumeroPedido(
        pedido: PedidoAdmin
    ) {

        tvNumeroPedido.text =
            if (pedido.numeroPedido > 0L) {

                context.getString(
                    R.string.pedido_admin_numero,
                    pedido.numeroPedido
                )

            } else {

                context.getString(
                    R.string.pedido_admin_sin_numero
                )
            }
    }

    // =========================================================
    // FECHA
    // =========================================================

    private fun mostrarFecha(
        pedido: PedidoAdmin
    ) {

        val fecha =
            pedido.fecha

        tvFechaPedido.text =
            if (fecha != null) {

                val formato =
                    SimpleDateFormat(
                        "dd/MM/yyyy • hh:mm a",
                        Locale.forLanguageTag("es-PE")
                    )

                context.getString(
                    R.string.pedido_admin_fecha,
                    formato.format(fecha.toDate())
                )

            } else {

                context.getString(
                    R.string.pedido_admin_fecha_no_disponible
                )
            }
    }

    // =========================================================
    // CLIENTE
    // =========================================================

    private fun mostrarCliente(
        pedido: PedidoAdmin
    ) {

        tvNombreUsuario.text =
            context.getString(
                R.string.pedido_admin_cliente,
                pedido.nombreUsuario
            )

        tvTelefono.text =
            if (pedido.telefono.isNotBlank()) {
                context.getString(
                    R.string.pedido_admin_telefono,
                    pedido.telefono
                )
            } else {
                context.getString(
                    R.string.pedido_admin_sin_telefono
                )
            }
    }

    // =========================================================
    // ENTREGA
    // =========================================================

    private fun mostrarEntrega(
        pedido: PedidoAdmin
    ) {

        tvTipoEntrega.text =
            if (pedido.esDelivery()) {
                context.getString(
                    R.string.pedido_admin_delivery
                )
            } else {
                context.getString(
                    R.string.pedido_admin_recojo_tienda
                )
            }

        if (pedido.esDelivery()) {

            tvDireccion.text =
                if (pedido.direccion.isNotBlank()) {
                    context.getString(
                        R.string.pedido_admin_direccion,
                        pedido.direccion
                    )
                } else {
                    context.getString(
                        R.string.pedido_admin_sin_direccion
                    )
                }

            tvReferencia.text =
                if (pedido.referencia.isNotBlank()) {
                    context.getString(
                        R.string.pedido_admin_referencia,
                        pedido.referencia
                    )
                } else {
                    context.getString(
                        R.string.pedido_admin_sin_referencia
                    )
                }

        } else {

            tvDireccion.text =
                context.getString(
                    R.string.pedido_admin_ubicacion_recojo
                )

            tvReferencia.text =
                context.getString(
                    R.string.pedido_admin_no_aplica
                )
        }
    }

    // =========================================================
    // PAGO
    // =========================================================

    private fun mostrarPago(
        pedido: PedidoAdmin
    ) {

        tvMetodoPago.text =
            context.getString(
                R.string.pedido_admin_pago,
                pedido.metodoPago,
                pedido.estadoPago
            )

        tvTotal.text =
            context.getString(
                R.string.pedido_admin_total,
                pedido.total
            )
    }

    // =========================================================
    // ESTADO
    // =========================================================

    private fun mostrarEstado(
        estadoOriginal: String
    ) {

        val estado =
            normalizarEstado(
                estadoOriginal
            )

        tvEstadoPedido.text =
            estado.uppercase(
                Locale.ROOT
            )

        when (estado) {

            "Entregado" -> {

                tvEstadoPedido.setBackgroundColor(
                    colorVerdeClaro
                )

                tvEstadoPedido.setTextColor(
                    colorVerde
                )
            }

            "Cancelado" -> {

                tvEstadoPedido.setBackgroundColor(
                    colorRojoClaro
                )

                tvEstadoPedido.setTextColor(
                    colorRojo
                )
            }

            else -> {

                tvEstadoPedido.setBackgroundColor(
                    colorNaranjaClaro
                )

                tvEstadoPedido.setTextColor(
                    colorNaranja
                )
            }
        }
    }

    // =========================================================
    // CONFIGURAR BOTONES
    // =========================================================

    private fun configurarBotones(
        pedido: PedidoAdmin,
        onCambiarEstado: (PedidoAdmin, String) -> Unit
    ) {

        val estadoActual =
            normalizarEstado(
                pedido.estadoPedido
            )

        val estadoDespacho =
            if (pedido.esDelivery()) {
                "En camino"
            } else {
                "Listo para recoger"
            }

        btnConfirmado.text =
            context.getString(
                R.string.pedido_admin_confirmado
            )

        btnPreparando.text =
            context.getString(
                R.string.pedido_admin_en_preparacion
            )

        btnEnCamino.text =
            estadoDespacho

        btnEntregado.text =
            context.getString(
                R.string.pedido_admin_entregado
            )

        /*
         * Primero se desactivan todos.
         */
        desactivarBoton(
            btnConfirmado
        )

        desactivarBoton(
            btnPreparando
        )

        desactivarBoton(
            btnEnCamino
        )

        desactivarBoton(
            btnEntregado
        )

        /*
         * Después se habilita únicamente
         * el siguiente estado.
         */
        when (estadoActual) {

            "Pendiente" -> {

                activarBoton(
                    btnConfirmado
                )
            }

            "Confirmado" -> {

                marcarCompletado(
                    btnConfirmado
                )

                activarBoton(
                    btnPreparando
                )
            }

            "En preparación" -> {

                marcarCompletado(
                    btnConfirmado
                )

                marcarCompletado(
                    btnPreparando
                )

                activarBoton(
                    btnEnCamino
                )
            }

            "En camino",
            "Listo para recoger" -> {

                marcarCompletado(
                    btnConfirmado
                )

                marcarCompletado(
                    btnPreparando
                )

                marcarCompletado(
                    btnEnCamino
                )

                activarBoton(
                    btnEntregado
                )
            }

            "Entregado" -> {

                marcarCompletado(
                    btnConfirmado
                )

                marcarCompletado(
                    btnPreparando
                )

                marcarCompletado(
                    btnEnCamino
                )

                marcarCompletado(
                    btnEntregado
                )
            }
        }

        // =====================================================
        // ACCIONES
        // =====================================================

        btnConfirmado.setOnClickListener {

            if (btnConfirmado.isEnabled) {

                onCambiarEstado(
                    pedido,
                    "Confirmado"
                )
            }
        }

        btnPreparando.setOnClickListener {

            if (btnPreparando.isEnabled) {

                onCambiarEstado(
                    pedido,
                    "En preparación"
                )
            }
        }

        btnEnCamino.setOnClickListener {

            if (btnEnCamino.isEnabled) {

                onCambiarEstado(
                    pedido,
                    estadoDespacho
                )
            }
        }

        btnEntregado.setOnClickListener {

            if (btnEntregado.isEnabled) {

                onCambiarEstado(
                    pedido,
                    "Entregado"
                )
            }
        }
    }

    // =========================================================
    // APARIENCIA DE BOTONES
    // =========================================================

    private fun activarBoton(
        button: MaterialButton
    ) {

        button.isEnabled =
            true

        button.alpha =
            1f

        button.setTypeface(
            null,
            Typeface.BOLD
        )
    }

    private fun desactivarBoton(
        button: MaterialButton
    ) {

        button.isEnabled =
            false

        button.alpha =
            0.45f

        button.setTypeface(
            null,
            Typeface.NORMAL
        )
    }

    private fun marcarCompletado(
        button: MaterialButton
    ) {

        button.isEnabled =
            false

        button.alpha =
            0.75f

        button.text =
            context.getString(
                R.string.pedido_admin_completado,
                button.text
            )

        button.setTypeface(
            null,
            Typeface.NORMAL
        )
    }

    // =========================================================
    // PRODUCTOS
    // =========================================================

    private fun mostrarProductos(
        productos: List<Map<String, Any>>
    ) {

        if (productos.isEmpty()) {

            tvProductos.text =
                context.getString(
                    R.string.pedido_admin_sin_productos
                )

            return
        }

        val texto =
            StringBuilder(
                context.getString(
                    R.string.pedido_admin_productos_titulo
                )
            )

        productos.forEach { producto ->

            val nombre =
                producto["nombre"]
                    ?.toString()
                    ?: context.getString(
                        R.string.pedido_admin_producto
                    )

            val cantidad =
                (producto["cantidad"] as? Number)
                    ?.toInt()
                    ?: 1

            val precio =
                (producto["precio"] as? Number)
                    ?.toDouble()
                    ?: 0.0

            texto.append(
                context.getString(
                    R.string.pedido_admin_producto_detalle,
                    cantidad,
                    nombre,
                    precio * cantidad
                )
            )
        }

        tvProductos.text =
            texto.toString()
                .trim()
    }

    // =========================================================
    // NORMALIZAR ESTADO
    // =========================================================

    private fun normalizarEstado(
        estado: String
    ): String {

        return when (
            estado.trim().lowercase(
                Locale.ROOT
            )
        ) {

            "pendiente",
            "pedido recibido" ->
                "Pendiente"

            "confirmado" ->
                "Confirmado"

            "preparando",
            "en preparación",
            "en preparacion" ->
                "En preparación"

            "en camino" ->
                "En camino"

            "listo para recoger",
            "listo para recojo" ->
                "Listo para recoger"

            "entregado" ->
                "Entregado"

            "cancelado" ->
                "Cancelado"

            else ->
                "Pendiente"
        }
    }
}