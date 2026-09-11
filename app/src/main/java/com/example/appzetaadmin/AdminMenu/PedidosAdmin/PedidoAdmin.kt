package com.example.appzetaadmin.AdminMenu

import com.google.firebase.Timestamp

data class PedidoAdmin(

    val id: String,

    val numeroPedido: Long = 0L,

    val fecha: Timestamp? = null,

    val nombreUsuario: String,

    val correo: String,

    val total: Double,

    val tipoEntrega: String,

    val direccion: String,

    val referencia: String,

    val telefono: String,

    val metodoPago: String,

    val estadoPago: String,

    val estadoPedido: String,

    val productos: List<Map<String, Any>>

) {

    fun esDelivery(): Boolean {

        return tipoEntrega.equals(
            "Delivery",
            ignoreCase = true
        )
    }
}