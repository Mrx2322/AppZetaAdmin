package com.example.appzetar.Usuario.Carrito

object PedidoManager {

    val pedido = mutableListOf<PedidoItem>()

    // =========================================================
    // AGREGAR PRODUCTO
    // =========================================================

    fun agregarProducto(item: PedidoItem) {

        val existente =
            buscarProducto(item)

        if (existente != null) {
            existente.cantidad += item.cantidad
        } else {
            pedido.add(item)
        }
    }

    // =========================================================
    // AUMENTAR CANTIDAD
    // =========================================================

    fun aumentarCantidad(
        item: PedidoItem
    ) {

        buscarProducto(item)
            ?.let { producto ->
                producto.cantidad++
            }
    }

    /**
     * Compatibilidad temporal con pantallas que todavía envían id y tipo.
     * Después actualizaremos esas pantallas para que envíen PedidoItem.
     */
    fun aumentarCantidad(
        id: Int,
        tipo: TipoPedido
    ) {

        pedido.find { producto ->
            producto.id == id &&
                    producto.tipo == tipo
        }?.let { producto ->
            aumentarCantidad(producto)
        }
    }

    // =========================================================
    // DISMINUIR CANTIDAD
    // =========================================================

    fun disminuirCantidad(
        item: PedidoItem
    ) {

        val producto =
            buscarProducto(item)
                ?: return

        if (producto.cantidad > 1) {
            producto.cantidad--
        } else {
            pedido.remove(producto)
        }
    }

    fun disminuirCantidad(
        id: Int,
        tipo: TipoPedido
    ) {

        pedido.find { producto ->
            producto.id == id &&
                    producto.tipo == tipo
        }?.let { producto ->
            disminuirCantidad(producto)
        }
    }

    // =========================================================
    // ELIMINAR PRODUCTO
    // =========================================================

    fun eliminarProducto(
        item: PedidoItem
    ) {

        buscarProducto(item)
            ?.let { producto ->
                pedido.remove(producto)
            }
    }

    fun eliminarProducto(
        id: Int,
        tipo: TipoPedido
    ) {

        pedido.removeAll { producto ->
            producto.id == id &&
                    producto.tipo == tipo
        }
    }

    // =========================================================
    // CANTIDADES COMPROMETIDAS PARA STOCK
    // =========================================================

    fun cantidadMenuEnPedido(
        menuId: Int
    ): Int {

        return pedido
            .filter { item ->
                item.tipo == TipoPedido.MENU &&
                        item.id == menuId
            }
            .sumOf { item ->
                item.cantidad
            }
    }

    fun cantidadEntradaEnPedido(
        entradaId: Int
    ): Int {

        val entradasAsociadas =
            pedido
                .filter { item ->
                    item.tipo == TipoPedido.MENU
                }
                .sumOf { item ->

                    val cantidadPorMenu =
                        item.entradas
                            .filter { entrada ->
                                entrada.id == entradaId
                            }
                            .sumOf { entrada ->
                                entrada.cantidad
                            }

                    cantidadPorMenu * item.cantidad
                }

        val entradasAntiguas =
            pedido
                .filter { item ->
                    item.tipo == TipoPedido.ENTRADA &&
                            item.id == entradaId
                }
                .sumOf { item ->
                    item.cantidad
                }

        return entradasAsociadas +
                entradasAntiguas
    }

    // =========================================================
    // TOTAL
    // =========================================================

    fun totalPedido(): Double {
        return pedido.sumOf { item ->
            item.subtotal()
        }
    }

    // =========================================================
    // CANTIDAD TOTAL
    // =========================================================

    fun cantidadTotal(): Int {
        return pedido.sumOf {
            it.cantidad
        }
    }

    // =========================================================
    // LIMPIAR CARRITO
    // =========================================================

    fun limpiar() {
        pedido.clear()
    }

    // =========================================================
    // BUSCAR CONFIGURACIÓN EXACTA
    // =========================================================

    private fun buscarProducto(
        item: PedidoItem
    ): PedidoItem? {

        return pedido.find { producto ->
            producto.tieneMismaConfiguracionQue(item)
        }
    }
}