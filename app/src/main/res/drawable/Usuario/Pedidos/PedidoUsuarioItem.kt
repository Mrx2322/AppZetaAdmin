package com.example.appzetar.Usuario

data class PedidoUsuarioItem(

    val id: String = "",

    val numeroPedido: Long = 0L,

    val estado: String = "Pendiente",

    val tipoEntrega: String = "Delivery",

    val total: Double = 0.0,

    val fecha: Long = 0L,

    val fechaEntrega: Long = 0L,

    val fechaActualizacion: Long = 0L

) {

    companion object {

        // Dos horas
        private const val TIEMPO_VISIBLE_ENTREGADO =
            2L * 60L * 60L * 1000L

        // Una hora
        private const val TIEMPO_VISIBLE_CANCELADO =
            1L * 60L * 60L * 1000L
    }

    // =========================================================
    // NÚMERO FORMATEADO
    // =========================================================

    fun numeroPedidoFormateado(): String {

        return if (numeroPedido > 0L) {

            String.format(
                "%04d",
                numeroPedido
            )

        } else {

            "----"
        }
    }

    // =========================================================
    // TIPO DE ENTREGA
    // =========================================================

    fun esDelivery(): Boolean {

        return tipoEntrega.equals(
            "Delivery",
            ignoreCase = true
        )
    }

    // =========================================================
    // ESTADO NORMALIZADO
    // =========================================================

    fun estadoNormalizado(): String {

        return when (
            estado.trim().lowercase()
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
                estado.trim().ifBlank {
                    "Pendiente"
                }
        }
    }

    // =========================================================
    // POSICIÓN DEL ESTADO
    // =========================================================

    fun posicionEstado(): Int {

        return when (estadoNormalizado()) {

            "Pendiente" ->
                0

            "Confirmado" ->
                1

            "En preparación" ->
                2

            "En camino",
            "Listo para recoger" ->
                3

            "Entregado" ->
                4

            "Cancelado" ->
                -1

            else ->
                0
        }
    }

    // =========================================================
    // VISIBILIDAD PARA EL CLIENTE
    // =========================================================

    fun debeMostrarse(
        ahora: Long = System.currentTimeMillis()
    ): Boolean {

        return when (estadoNormalizado()) {

            "Entregado" -> {

                /*
                 * Los pedidos antiguos que no tengan
                 * fechaEntrega no se mostrarán.
                 */
                if (fechaEntrega <= 0L) {

                    false

                } else {

                    val tiempoTranscurrido =
                        ahora - fechaEntrega

                    tiempoTranscurrido <
                            TIEMPO_VISIBLE_ENTREGADO
                }
            }

            "Cancelado" -> {

                if (fechaActualizacion <= 0L) {

                    false

                } else {

                    val tiempoTranscurrido =
                        ahora - fechaActualizacion

                    tiempoTranscurrido <
                            TIEMPO_VISIBLE_CANCELADO
                }
            }

            else -> {

                /*
                 * Los pedidos activos siempre aparecen.
                 */
                true
            }
        }
    }
}