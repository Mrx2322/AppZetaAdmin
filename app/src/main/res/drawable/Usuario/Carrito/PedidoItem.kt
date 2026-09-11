package com.example.appzetar.Usuario.Carrito

/**
 * Entrada que queda asociada a una unidad del menú.
 */
data class EntradaPedido(
    val id: Int,
    val nombre: String,
    val cantidad: Int = 1,
    val precioUnitario: Double =
        ReglasPrecioPedido.PRECIO_ENTRADA
) {

    fun subtotal(): Double {
        return precioUnitario * cantidad
    }
}

data class PedidoItem(
    val id: Int,
    val nombre: String,

    // Precio final por unidad que verá y pagará el cliente.
    val precio: Double,

    var cantidad: Int = 1,
    val tipo: TipoPedido = TipoPedido.MENU,

    // Solo se usa para los productos de tipo MENU.
    val precioBaseMenu: Double? = null,
    val entradas: List<EntradaPedido> = emptyList()
) {

    fun subtotal(): Double {
        return precio * cantidad
    }

    fun cantidadEntradas(): Int {
        return entradas.sumOf { entrada ->
            entrada.cantidad
        }
    }

    fun descripcionEntradas(): String {

        if (tipo != TipoPedido.MENU) {
            return ""
        }

        if (entradas.isEmpty()) {
            return "Sin entrada"
        }

        return entradas.joinToString(
            separator = ", "
        ) { entrada ->

            if (entrada.cantidad > 1) {
                "${entrada.cantidad} x ${entrada.nombre}"
            } else {
                entrada.nombre
            }
        }
    }

    /**
     * Permite agrupar cantidades únicamente cuando el producto
     * y su combinación de entradas son exactamente iguales.
     */
    fun tieneMismaConfiguracionQue(
        otro: PedidoItem
    ): Boolean {

        if (
            id != otro.id ||
            tipo != otro.tipo ||
            precio != otro.precio
        ) {
            return false
        }

        if (tipo != TipoPedido.MENU) {
            return true
        }

        return firmaEntradas() ==
                otro.firmaEntradas()
    }

    private fun firmaEntradas():
            List<Triple<Int, Int, Double>> {

        return entradas
            .groupBy { entrada ->
                entrada.id
            }
            .map { (entradaId, coincidencias) ->

                Triple(
                    entradaId,
                    coincidencias.sumOf { entrada ->
                        entrada.cantidad
                    },
                    coincidencias.first().precioUnitario
                )
            }
            .sortedBy { firma ->
                firma.first
            }
    }
}

enum class TipoPedido {
    MENU,
    ENTRADA,
    EXTRA
}

/**
 * El precio guardado para un menú representa el precio comercial
 * con una entrada. La interfaz solo muestra precios finales.
 */
object ReglasPrecioPedido {

    const val PRECIO_ENTRADA = 2.0
    const val DESCUENTO_SIN_ENTRADA = 1.0

    fun calcularPrecioMenu(
        precioMenuConEntrada: Double,
        cantidadEntradas: Int
    ): Double {

        return if (cantidadEntradas <= 0) {

            (precioMenuConEntrada -
                    DESCUENTO_SIN_ENTRADA).coerceAtLeast(0.0)

        } else {

            precioMenuConEntrada +
                    (PRECIO_ENTRADA *
                            (cantidadEntradas - 1))
        }
    }
}
