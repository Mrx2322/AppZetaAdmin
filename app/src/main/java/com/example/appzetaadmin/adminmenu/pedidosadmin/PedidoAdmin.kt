package com.example.appzetaadmin.adminmenu.pedidosadmin

import com.google.firebase.Timestamp

data class PedidoAdmin(

    val id: String = "",

    val numeroPedido: Long = 0L,

    val fecha: Timestamp? = null,

    val nombreUsuario: String = "",

    val correo: String = "",

    val total: Double = 0.0,

    val tipoEntrega: String = "",

    val direccion: String = "",

    val referencia: String = "",

    val telefono: String = "",

    // =========================================================
    // UBICACIÓN GPS OPCIONAL
    // =========================================================

    val latitud: Double? = null,

    val longitud: Double? = null,

    val ubicacionMaps: String = "",

    val metodoPago: String = "",

    val estadoPago: String = "",

    val estadoPedido: String = "",

    val productos: List<Map<String, Any>> = emptyList(),

    val esSolicitud: Boolean = false,

    val observacion: String = ""

) {

    fun esDelivery(): Boolean {

        return tipoEntrega.equals(
            "Delivery",
            ignoreCase = true
        )
    }

    fun tieneUbicacion(): Boolean {

        return esDelivery() &&
                latitud != null &&
                longitud != null &&
                ubicacionMaps.isNotBlank()
    }
}
