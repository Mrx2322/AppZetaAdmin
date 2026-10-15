package com.example.appzetaadmin.adminmenu.pedidosadmin

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import com.example.appzetaadmin.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ActivityPedidosAdmin : AppCompatActivity() {

    // =========================================================
    // FIREBASE
    // =========================================================

    private val db =
        FirebaseFirestore.getInstance()

    private var pedidosListener:
            ListenerRegistration? = null

    private var solicitudesListener: ListenerRegistration? = null

    // =========================================================
    // COMPONENTES
    // =========================================================

    private lateinit var rvPedidos: RecyclerView
    private lateinit var tvSinPedidos: TextView
    private lateinit var btnHistorial: MaterialButton

    // =========================================================
    // LISTA Y ADAPTER
    // =========================================================

    private val listaPedidos =
        mutableListOf<PedidoAdmin>()

    private val pedidosActivos = mutableListOf<PedidoAdmin>()

    private val solicitudesPendientes = mutableListOf<PedidoAdmin>()

    private lateinit var adapter:
            PedidoAdminAdapter

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContentView(
            R.layout.activity_pedidos_admin
        )

        aplicarInsets()
        initComponent()
        initUI()
    }

    // =========================================================
    // INSETS
    // =========================================================

    private fun aplicarInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }

    // =========================================================
    // COMPONENTES
    // =========================================================

    private fun initComponent() {

        rvPedidos =
            findViewById(R.id.rvPedidos)

        tvSinPedidos =
            findViewById(R.id.tvSinPedidos)

        btnHistorial =
            findViewById(R.id.btnHistorial)
    }

    // =========================================================
    // INTERFAZ
    // =========================================================

    private fun initUI() {

        adapter =
            PedidoAdminAdapter(

                listaPedidos =
                    listaPedidos,

                onCambiarEstado = { pedido, nuevoEstado ->

                    cambiarEstadoPedido(
                        pedido = pedido,
                        nuevoEstado = nuevoEstado
                    )
                },

                modoHistorial =
                    false,

                onAprobarSolicitud = ::aprobarSolicitud,

                onRechazarSolicitud = ::pedirMotivoRechazo
            )

        rvPedidos.apply {

            layoutManager =
                LinearLayoutManager(
                    this@ActivityPedidosAdmin
                )

            adapter =
                this@ActivityPedidosAdmin.adapter

            setHasFixedSize(
                false
            )
        }

        btnHistorial.setOnClickListener {

            val intent = Intent(
                this,
                ActivityHistorialPedidosAdmin::class.java
            )

            startActivity(intent)
        }
    }

    // =========================================================
    // ESCUCHAR PEDIDOS
    // =========================================================

    private fun escucharPedidos() {

        pedidosListener?.remove()

        pedidosListener =
            db.collection("pedidos")
                .addSnapshotListener { resultado, error ->

                    if (error != null) {

                        Log.e(
                            "PEDIDOS_ADMIN",
                            "Error cargando pedidos",
                            error
                        )

                        Toast.makeText(
                            this,
                            "Error al cargar pedidos",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@addSnapshotListener
                    }

                    if (resultado == null) {
                        return@addSnapshotListener
                    }

                    val nuevosPedidos =
                        mutableListOf<PedidoAdmin>()

                    for (documento in resultado.documents) {

                        val estadoPedido =
                            normalizarEstado(
                                documento.getString("estadoPedido")
                                    ?.trim()
                                    .orEmpty()
                                    .ifBlank {
                                        "Pendiente"
                                    }
                            )

                        /*
                         * Los pedidos entregados permanecen
                         * guardados en Firestore, pero aparecen
                         * únicamente en el historial.
                         */
                        if (
                            estadoPedido.equals(
                                "Entregado",
                                ignoreCase = true
                            )
                        ) {
                            continue
                        }

                        val pedido =
                            PedidoAdmin(

                                id =
                                    documento.id,

                                numeroPedido =
                                    documento.getLong(
                                        "numeroPedido"
                                    ) ?: 0L,

                                fecha =
                                    documento.getTimestamp(
                                        "fecha"
                                    ),

                                nombreUsuario =
                                    documento.getString(
                                        "nombreUsuario"
                                    ) ?: "Cliente",

                                correo =
                                    documento.getString(
                                        "correo"
                                    ).orEmpty(),

                                total =
                                    documento.getDouble("total")
                                        ?: documento.getLong("total")
                                            ?.toDouble()
                                        ?: 0.0,

                                tipoEntrega =
                                    documento.getString(
                                        "tipoEntrega"
                                    ) ?: "Delivery",

                                direccion =
                                    documento.getString(
                                        "direccion"
                                    ).orEmpty(),

                                referencia =
                                    documento.getString(
                                        "referencia"
                                    ).orEmpty(),

                                telefono =
                                    documento.getString(
                                        "telefono"
                                    ).orEmpty(),

                                latitud =
                                    documento.getDouble(
                                        "latitud"
                                    ),

                                longitud =
                                    documento.getDouble(
                                        "longitud"
                                    ),

                                ubicacionMaps =
                                    documento.getString(
                                        "ubicacionMaps"
                                    ).orEmpty(),

                                metodoPago =
                                    documento.getString(
                                        "metodoPago"
                                    ) ?: "Contra entrega",

                                estadoPago =
                                    documento.getString(
                                        "estadoPago"
                                    ) ?: "Pendiente",

                                estadoPedido =
                                    estadoPedido,

                                productos =
                                    convertirProductos(
                                        documento.get(
                                            "productos"
                                        )
                                    )
                            )

                        nuevosPedidos.add(
                            pedido
                        )
                    }

                    nuevosPedidos.sortByDescending { pedido ->

                        pedido.fecha
                            ?.toDate()
                            ?.time
                            ?: 0L
                    }

                    pedidosActivos.clear()
                    pedidosActivos.addAll(nuevosPedidos)
                    actualizarListaVisible()
                }
    }

    private fun escucharSolicitudes() {
        solicitudesListener?.remove()
        solicitudesListener = db.collection("solicitudesPedidos")
            .whereEqualTo("estado", ESTADO_SOLICITUD_PENDIENTE)
            .addSnapshotListener { resultado, error ->
                if (error != null) {
                    Log.e("PEDIDOS_ADMIN", "Error cargando solicitudes", error)
                    Toast.makeText(this, "Error al cargar solicitudes", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (resultado == null) return@addSnapshotListener

                solicitudesPendientes.clear()
                resultado.documents.forEach { documento ->
                    solicitudesPendientes += PedidoAdmin(
                        id = documento.id,
                        fecha = documento.getTimestamp("fecha"),
                        nombreUsuario = documento.getString("nombreUsuario") ?: "Cliente",
                        correo = documento.getString("correo").orEmpty(),
                        total = documento.getDouble("totalEstimado")
                            ?: documento.getLong("totalEstimado")?.toDouble() ?: 0.0,
                        tipoEntrega = documento.getString("tipoEntrega") ?: "Recojo en tienda",
                        direccion = documento.getString("direccion").orEmpty(),
                        referencia = documento.getString("referencia").orEmpty(),
                        telefono = documento.getString("telefono").orEmpty(),
                        latitud = documento.getDouble("latitud"),
                        longitud = documento.getDouble("longitud"),
                        ubicacionMaps = documento.getString("ubicacionMaps").orEmpty(),
                        metodoPago = documento.getString("metodoPago") ?: "Contra entrega",
                        estadoPago = "Pendiente",
                        estadoPedido = ESTADO_SOLICITUD_PENDIENTE,
                        productos = convertirProductos(documento.get("productos")),
                        esSolicitud = true,
                        observacion = documento.getString("observacion").orEmpty()
                    )
                }
                actualizarListaVisible()
            }
    }

    private fun actualizarListaVisible() {
        val pedidosVisibles = (pedidosActivos + solicitudesPendientes)
            .sortedByDescending { it.fecha?.toDate()?.time ?: 0L }
        adapter.actualizarPedidos(pedidosVisibles)
        actualizarEstadoVacio()
        if (pedidosVisibles.isEmpty()) {
            tvSinPedidos.text = getString(R.string.solicitud_admin_sin_pendientes)
        }
    }

    private fun pedirMotivoRechazo(solicitud: PedidoAdmin) {
        val entrada = EditText(this).apply {
            hint = getString(R.string.solicitud_admin_rechazo_motivo)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 2
            maxLines = 4
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.solicitud_admin_rechazo_titulo)
            .setView(entrada)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.solicitud_admin_rechazar) { _, _ ->
                rechazarSolicitud(solicitud, entrada.text.toString().trim().ifBlank { "No especificado" })
            }
            .show()
    }

    private fun rechazarSolicitud(solicitud: PedidoAdmin, motivo: String) {
        val solicitudRef = db.collection("solicitudesPedidos").document(solicitud.id)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(solicitudRef)
            check(snapshot.getString("estado") == ESTADO_SOLICITUD_PENDIENTE) {
                "La solicitud ya fue revisada"
            }
            transaction.update(
                solicitudRef,
                mapOf(
                    "estado" to "Rechazada",
                    "motivoRevision" to motivo.take(300),
                    "fechaRevision" to FieldValue.serverTimestamp()
                )
            )
            null
        }.addOnSuccessListener {
            Toast.makeText(this, R.string.solicitud_admin_rechazada, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { error -> mostrarErrorRevision(error) }
    }

    private fun aprobarSolicitud(solicitud: PedidoAdmin) {
        val solicitudRef = db.collection("solicitudesPedidos").document(solicitud.id)
        val contadorRef = db.collection("configuracion").document("contadorPedidos")
        val productosSolicitud = solicitud.productos
        val referencias = linkedMapOf<String, com.google.firebase.firestore.DocumentReference>()

        fun agregarReferencia(coleccion: String, id: String) {
            referencias["$coleccion/$id"] = db.collection(coleccion).document(id)
        }

        fun idCatalogoValido(id: String): Boolean = id.toIntOrNull()?.let { it > 0 } == true

        productosSolicitud.forEach { producto ->
            val tipo = producto["tipo"]?.toString()?.uppercase(Locale.ROOT).orEmpty()
            val coleccion = when (tipo) {
                "MENU" -> "menu"
                "CENA" -> "cenas"
                "ENTRADA" -> "entradas"
                "EXTRA" -> "extras"
                else -> null
            } ?: run {
                Toast.makeText(this, R.string.solicitud_admin_error_productos, Toast.LENGTH_LONG).show()
                return
            }
            val idProducto = producto["id"]?.toString().orEmpty()
            if (!idCatalogoValido(idProducto)) {
                Toast.makeText(this, R.string.solicitud_admin_error_productos, Toast.LENGTH_LONG).show()
                return
            }
            agregarReferencia(coleccion, idProducto)
            if (tipo == "MENU") {
                (producto["entradas"] as? List<*>)?.forEach { entrada ->
                    val mapa = entrada as? Map<*, *> ?: return@forEach
                    val idEntrada = mapa["id"]?.toString().orEmpty()
                    if (!idCatalogoValido(idEntrada)) {
                        Toast.makeText(this, R.string.solicitud_admin_error_productos, Toast.LENGTH_LONG).show()
                        return
                    }
                    agregarReferencia("entradas", idEntrada)
                }
            }
        }
        if (referencias.size > MAX_DOCUMENTOS_REVISION) {
            Toast.makeText(this, R.string.solicitud_admin_error_productos, Toast.LENGTH_LONG).show()
            return
        }

        db.runTransaction { transaction ->
            val solicitudSnapshot = transaction.get(solicitudRef)
            check(solicitudSnapshot.getString("estado") == ESTADO_SOLICITUD_PENDIENTE) {
                "La solicitud ya fue revisada"
            }
            val contadorSnapshot = transaction.get(contadorRef)
            val catalogo = referencias.mapValues { (_, referencia) -> transaction.get(referencia) }

            val descuentosStock = linkedMapOf<String, Pair<com.google.firebase.firestore.DocumentReference, Int>>()
            val productosConfirmados = mutableListOf<Map<String, Any>>()
            var totalConfirmado = 0.0

            productosSolicitud.forEach { producto ->
                val tipo = producto["tipo"]?.toString()?.uppercase(Locale.ROOT).orEmpty()
                val cantidad = (producto["cantidad"] as? Number)?.toInt() ?: 0
                check(cantidad in 1..100) { "Cantidad inválida" }
                val coleccion = when (tipo) {
                    "MENU" -> "menu"
                    "CENA" -> "cenas"
                    "ENTRADA" -> "entradas"
                    "EXTRA" -> "extras"
                    else -> error("Tipo de producto no reconocido")
                }
                val id = producto["id"]?.toString().orEmpty()
                val clave = "$coleccion/$id"
                val referencia = referencias[clave] ?: error("Producto inválido")
                val snapshot = catalogo[clave] ?: error("Producto inválido")
                check(snapshot.exists()) { "Producto no disponible" }
                check(snapshot.getBoolean("disponible") != false) {
                    "El producto $id ya no está disponible"
                }
                val nombre = snapshot.getString("nombre")
                    ?: snapshot.getString("name")
                    ?: producto["nombre"]?.toString()
                    ?: "Producto"
                val precioCatalogo = (snapshot.get("precio") as? Number)?.toDouble()
                    ?: if (tipo == "ENTRADA") PRECIO_ENTRADA else error("Precio no disponible")
                check(precioCatalogo.isFinite() && precioCatalogo >= 0.0) { "Precio inválido" }

                var precioUnitario = precioCatalogo
                val confirmado = linkedMapOf<String, Any>(
                    "id" to id,
                    "nombre" to nombre,
                    "tipo" to tipo,
                    "cantidad" to cantidad,
                    "precio" to precioCatalogo
                )

                if (tipo != "EXTRA") {
                    val stock = snapshot.getLong("stock")?.toInt() ?: error("Stock no disponible")
                    check(stock >= cantidad) { "Stock insuficiente para $nombre" }
                    val descuentoAnterior = descuentosStock[clave]?.second ?: 0
                    descuentosStock[clave] = referencia to (descuentoAnterior + cantidad)
                }
                if (tipo == "ENTRADA") {
                    check(snapshot.getBoolean("disponible") != false) {
                        "La entrada $nombre ya no está disponible"
                    }
                }

                if (tipo == "MENU") {
                    val entradasSolicitadas = producto["entradas"] as? List<*> ?: emptyList<Any>()
                    val entradasConfirmadas = mutableListOf<Map<String, Any>>()
                    var cantidadEntradasPorMenu = 0
                    entradasSolicitadas.forEach { entradaAny ->
                        val entrada = entradaAny as? Map<*, *> ?: error("Entrada inválida")
                        val entradaId = entrada["id"]?.toString().orEmpty()
                        val cantidadEntrada = (entrada["cantidad"] as? Number)?.toInt() ?: 0
                        check(cantidadEntrada in 1..100) { "Cantidad de entrada inválida" }
                        val entradaClave = "entradas/$entradaId"
                        val entradaRef = referencias[entradaClave] ?: error("Entrada inválida")
                        val entradaSnapshot = catalogo[entradaClave] ?: error("Entrada inválida")
                        check(entradaSnapshot.exists()) { "Entrada no disponible" }
                        check(entradaSnapshot.getBoolean("disponible") != false) {
                            "Una entrada solicitada ya no está disponible"
                        }
                        val entradasStock = entradaSnapshot.getLong("stock")?.toInt() ?: error("Stock de entrada no disponible")
                        val requerido = cantidadEntrada * cantidad
                        check(entradasStock >= requerido) { "Stock insuficiente para una entrada" }
                        val descuentoAnterior = descuentosStock[entradaClave]?.second ?: 0
                        descuentosStock[entradaClave] = entradaRef to (descuentoAnterior + requerido)
                        cantidadEntradasPorMenu += cantidadEntrada
                        entradasConfirmadas += mapOf(
                            "id" to entradaId,
                            "nombre" to (entradaSnapshot.getString("nombre") ?: entrada["nombre"]?.toString().orEmpty()),
                            "cantidad" to cantidadEntrada,
                            "precio" to ((entradaSnapshot.get("precio") as? Number)?.toDouble() ?: 2.0)
                        )
                    }
                    precioUnitario = if (cantidadEntradasPorMenu == 0) {
                        (precioCatalogo - 1.0).coerceAtLeast(0.0)
                    } else {
                        precioCatalogo + 5.0 * (cantidadEntradasPorMenu - 1)
                    }
                    confirmado["entradas"] = entradasConfirmadas
                }

                confirmado["precio"] = precioUnitario
                totalConfirmado += precioUnitario * cantidad
                productosConfirmados += confirmado
            }

            check(totalConfirmado.isFinite() && totalConfirmado > 0.0) { "Total inválido" }
            descuentosStock.forEach { (clave, descuento) ->
                val stockActual = catalogo[clave]?.getLong("stock")?.toInt()
                    ?: error("Stock inválido")
                check(stockActual >= descuento.second) {
                    "Stock insuficiente para completar la solicitud"
                }
            }
            val numeroPedido = (contadorSnapshot.getLong("ultimoNumero") ?: 0L) + 1L
            val pedidoRef = db.collection("pedidos").document()
            descuentosStock.forEach { (clave, descuento) ->
                val referencia = descuento.first
                val snapshot = catalogo[clave] ?: error("Stock inválido")
                val stockActual = snapshot.getLong("stock")?.toInt() ?: error("Stock inválido")
                transaction.update(referencia, "stock", stockActual - descuento.second)
            }
            transaction.set(contadorRef, mapOf("ultimoNumero" to numeroPedido))
            transaction.set(
                pedidoRef,
                mapOf(
                    "numeroPedido" to numeroPedido,
                    "fecha" to FieldValue.serverTimestamp(),
                    "usuarioId" to (solicitudSnapshot.getString("usuarioId") ?: ""),
                    "nombreUsuario" to (solicitudSnapshot.getString("nombreUsuario") ?: "Cliente"),
                    "correo" to (solicitudSnapshot.getString("correo") ?: ""),
                    "total" to kotlin.math.round(totalConfirmado * 100.0) / 100.0,
                    "tipoEntrega" to (solicitudSnapshot.getString("tipoEntrega") ?: "Recojo en tienda"),
                    "direccion" to (solicitudSnapshot.getString("direccion") ?: ""),
                    "referencia" to (solicitudSnapshot.getString("referencia") ?: ""),
                    "telefono" to (solicitudSnapshot.getString("telefono") ?: ""),
                    "latitud" to solicitudSnapshot.getDouble("latitud"),
                    "longitud" to solicitudSnapshot.getDouble("longitud"),
                    "ubicacionMaps" to (solicitudSnapshot.getString("ubicacionMaps") ?: ""),
                    "observacion" to (solicitudSnapshot.getString("observacion") ?: ""),
                    "metodoPago" to (solicitudSnapshot.getString("metodoPago") ?: "Contra entrega"),
                    "estadoPago" to "Pendiente",
                    "estadoPedido" to "Pendiente",
                    "productos" to productosConfirmados
                ).filterValues { it != null }
            )
            transaction.update(
                solicitudRef,
                mapOf(
                    "estado" to "Aprobada",
                    "numeroPedido" to numeroPedido,
                    "totalConfirmado" to kotlin.math.round(totalConfirmado * 100.0) / 100.0,
                    "fechaRevision" to FieldValue.serverTimestamp()
                )
            )
            numeroPedido
        }.addOnSuccessListener { numeroPedido ->
            Toast.makeText(this, getString(R.string.solicitud_admin_aprobada, numeroPedido), Toast.LENGTH_LONG).show()
        }.addOnFailureListener { error -> mostrarErrorRevision(error) }
    }

    private fun mostrarErrorRevision(error: Exception) {
        Log.e("PEDIDOS_ADMIN", "No se pudo revisar la solicitud", error)
        val mensaje = error.message?.takeIf { it.isNotBlank() }
            ?: getString(R.string.solicitud_admin_error_productos)
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    private fun convertirProductos(
        valor: Any?
    ): List<Map<String, Any>> {

        val productos =
            valor as? List<*>
                ?: return emptyList()

        return productos.mapNotNull { producto ->

            val mapa =
                producto as? Map<*, *>
                    ?: return@mapNotNull null

            mapa.entries.mapNotNull { entrada ->

                val clave =
                    entrada.key as? String
                        ?: return@mapNotNull null

                val contenido =
                    entrada.value
                        ?: return@mapNotNull null

                clave to contenido
            }.toMap()
        }
    }

    // =========================================================
    // CAMBIAR ESTADO
    // =========================================================

    private fun cambiarEstadoPedido(
        pedido: PedidoAdmin,
        nuevoEstado: String
    ) {

        val referenciaPedido =
            db.collection("pedidos")
                .document(pedido.id)

        db.runTransaction { transaction ->

            val snapshot =
                transaction.get(
                    referenciaPedido
                )

            if (!snapshot.exists()) {

                throw IllegalStateException(
                    "El pedido ya no existe"
                )
            }

            val estadoActual =
                normalizarEstado(
                    snapshot.getString("estadoPedido")
                        ?: "Pendiente"
                )

            val tipoEntrega =
                snapshot.getString("tipoEntrega")
                    ?: pedido.tipoEntrega

            val metodoPago =
                snapshot.getString("metodoPago")
                    ?: pedido.metodoPago

            val siguienteEstado =
                obtenerSiguienteEstado(
                    estadoActual = estadoActual,
                    tipoEntrega = tipoEntrega
                )

            if (nuevoEstado != siguienteEstado) {

                throw IllegalStateException(
                    "Primero debes cambiar el pedido a $siguienteEstado"
                )
            }

            val actualizaciones =
                hashMapOf(
                    "estadoPedido" to nuevoEstado,
                    "fechaActualizacion" to FieldValue.serverTimestamp()
                )

            /*
             * Contra entrega se considera pagado
             * únicamente cuando el pedido se entrega.
             */
            if (nuevoEstado == "Entregado") {

                if (
                    metodoPago.equals(
                        "Contra entrega",
                        ignoreCase = true
                    )
                ) {

                    actualizaciones["estadoPago"] =
                        "Pagado"
                }

                actualizaciones["fechaEntrega"] =
                    FieldValue.serverTimestamp()

                actualizaciones["expiraEn"] =
                    obtenerFechaExpiracion()
            }

            transaction.update(
                referenciaPedido,
                actualizaciones
            )

            null
        }
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Pedido actualizado: $nuevoEstado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->

                Log.e(
                    "PEDIDOS_ADMIN",
                    "Error actualizando el pedido",
                    error
                )

                Toast.makeText(
                    this,
                    error.message
                        ?: "No se pudo actualizar el pedido",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * Conserva el pedido durante siete días calendario y lo hace
     * vencer a las 00:00, usando siempre la zona horaria de Perú.
     */
    private fun obtenerFechaExpiracion(): Timestamp {

        val zonaPeru =
            TimeZone.getTimeZone("America/Lima")

        val calendario =
            Calendar.getInstance(zonaPeru).apply {

                add(
                    Calendar.DAY_OF_YEAR,
                    DIAS_DE_RETENCION
                )

                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

        return Timestamp(
            calendario.time
        )
    }

    // =========================================================
    // SIGUIENTE ESTADO
    // =========================================================

    private fun obtenerSiguienteEstado(
        estadoActual: String,
        tipoEntrega: String
    ): String {

        return when (estadoActual) {

            "Pendiente" ->
                "Confirmado"

            "Confirmado" ->
                "En preparación"

            "En preparación" -> {

                if (
                    tipoEntrega.equals(
                        "Delivery",
                        ignoreCase = true
                    )
                ) {
                    "En camino"
                } else {
                    "Listo para recoger"
                }
            }

            "En camino",
            "Listo para recoger" ->
                "Entregado"

            "Entregado" ->
                "Entregado"

            else ->
                "Confirmado"
        }
    }

    // =========================================================
    // NORMALIZAR ESTADOS ANTIGUOS
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
            //noinspection SpellCheckingInspection
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

    // =========================================================
    // ESTADO VACÍO
    // =========================================================

    private fun actualizarEstadoVacio() {

        if (listaPedidos.isEmpty()) {

            rvPedidos.visibility =
                View.GONE

            tvSinPedidos.visibility =
                View.VISIBLE

        } else {

            rvPedidos.visibility =
                View.VISIBLE

            tvSinPedidos.visibility =
                View.GONE
        }
    }

    // =========================================================
    // CICLO DE VIDA
    // =========================================================

    override fun onStart() {
        super.onStart()

        escucharPedidos()
        escucharSolicitudes()
    }

    override fun onStop() {

        pedidosListener?.remove()
        solicitudesListener?.remove()

        pedidosListener =
            null

        solicitudesListener = null

        super.onStop()
    }

    companion object {

        private const val DIAS_DE_RETENCION = 7
        //noinspection SpellCheckingInspection
        private const val ESTADO_SOLICITUD_PENDIENTE = "Pendiente de aprobacion"
        private const val MAX_DOCUMENTOS_REVISION = 450
        private const val PRECIO_ENTRADA = 2.0
    }
}
