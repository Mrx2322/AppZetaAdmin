package com.example.appzetar.Usuario

import com.example.appzetar.Usuario.Carrito.PedidoItem
import com.example.appzetar.Usuario.Carrito.TipoPedido
import com.google.firebase.firestore.FirebaseFirestore

object StockManager {

    private val db =
        FirebaseFirestore.getInstance()


    // =========================================================
    // DESCONTAR STOCK
    // =========================================================
    //
    // Se utiliza cuando:
    // MENU     -> +1 en carrito
    // ENTRADA  -> +1 en carrito
    //
    // EXTRA no utiliza este método porque no tiene stock.
    // =========================================================

    fun descontarStock(
        item: PedidoItem,
        cantidad: Int = 1,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        if (item.tipo == TipoPedido.EXTRA) {

            onSuccess()
            return
        }

        if (cantidad <= 0) {

            onError(
                IllegalArgumentException(
                    "La cantidad debe ser mayor que cero"
                )
            )

            return
        }

        val coleccion =
            obtenerColeccion(item.tipo)

        if (coleccion == null) {

            onError(
                IllegalArgumentException(
                    "Tipo de producto no válido"
                )
            )

            return
        }


        buscarDocumentoPorId(
            coleccion = coleccion,
            id = item.id,

            onEncontrado = { documentRef ->

                db.runTransaction { transaction ->

                    val snapshot =
                        transaction.get(documentRef)

                    val stockActual =
                        snapshot.getLong("stock")
                            ?.toInt()
                            ?: 0

                    if (stockActual < cantidad) {

                        throw IllegalStateException(
                            "SIN_STOCK"
                        )
                    }

                    val nuevoStock =
                        stockActual - cantidad

                    transaction.update(
                        documentRef,
                        "stock",
                        nuevoStock
                    )

                }.addOnSuccessListener {

                    onSuccess()

                }.addOnFailureListener { exception ->

                    onError(exception)
                }
            },

            onError = onError
        )
    }


    // =========================================================
    // DEVOLVER STOCK
    // =========================================================
    //
    // Se utiliza cuando:
    //
    // MENU     -> disminuir o eliminar
    // ENTRADA  -> disminuir o eliminar
    //
    // EXTRA no modifica stock.
    // =========================================================

    fun devolverStock(
        item: PedidoItem,
        cantidad: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        if (item.tipo == TipoPedido.EXTRA) {

            onSuccess()
            return
        }

        if (cantidad <= 0) {

            onError(
                IllegalArgumentException(
                    "La cantidad debe ser mayor que cero"
                )
            )

            return
        }

        val coleccion =
            obtenerColeccion(item.tipo)

        if (coleccion == null) {

            onError(
                IllegalArgumentException(
                    "Tipo de producto no válido"
                )
            )

            return
        }


        buscarDocumentoPorId(
            coleccion = coleccion,
            id = item.id,

            onEncontrado = { documentRef ->

                db.runTransaction { transaction ->

                    val snapshot =
                        transaction.get(documentRef)

                    val stockActual =
                        snapshot.getLong("stock")
                            ?.toInt()
                            ?: 0

                    val nuevoStock =
                        stockActual + cantidad

                    transaction.update(
                        documentRef,
                        "stock",
                        nuevoStock
                    )

                }.addOnSuccessListener {

                    onSuccess()

                }.addOnFailureListener { exception ->

                    onError(exception)
                }
            },

            onError = onError
        )
    }


    // =========================================================
    // OBTENER COLECCIÓN
    // =========================================================

    private fun obtenerColeccion(
        tipo: TipoPedido
    ): String? {

        return when (tipo) {

            TipoPedido.MENU ->
                "menu"

            TipoPedido.ENTRADA ->
                "entradas"

            TipoPedido.EXTRA ->
                null
        }
    }


    // =========================================================
    // BUSCAR DOCUMENTO POR ID
    // =========================================================
    //
    // Tus documentos pueden tener un ID automático de Firestore,
    // mientras que el producto tiene su propio campo "id".
    //
    // Por eso buscamos:
    //
    // whereEqualTo("id", id)
    // =========================================================

    private fun buscarDocumentoPorId(
        coleccion: String,
        id: Int,
        onEncontrado: (com.google.firebase.firestore.DocumentReference) -> Unit,
        onError: (Exception) -> Unit
    ) {

        db.collection(coleccion)
            .whereEqualTo(
                "id",
                id
            )
            .limit(1)
            .get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) {

                    onError(
                        IllegalStateException(
                            "No se encontró el producto con id $id"
                        )
                    )

                    return@addOnSuccessListener
                }

                val document =
                    result.documents.first()

                onEncontrado(
                    document.reference
                )
            }
            .addOnFailureListener { exception ->

                onError(exception)
            }
    }
}