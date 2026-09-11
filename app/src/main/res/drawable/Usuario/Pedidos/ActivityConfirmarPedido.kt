package com.example.appzetar.Usuario

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appzetar.R
import com.example.appzetar.Usuario.Carrito.PedidoItem
import com.example.appzetar.Usuario.Carrito.PedidoManager
import com.example.appzetar.Usuario.Carrito.TipoPedido
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ActivityConfirmarPedido : AppCompatActivity() {

    private data class SolicitudStock(
        val coleccion: String,
        val idProducto: Int,
        val nombre: String,
        val cantidad: Int
    )

    // =========================================================
    // FIREBASE
    // =========================================================

    private val auth =
        FirebaseAuth.getInstance()

    private val db =
        FirebaseFirestore.getInstance()

    // =========================================================
    // COMPONENTES
    // =========================================================

    private lateinit var tvNombreUsuario: TextView
    private lateinit var tvCorreoUsuario: TextView
    private lateinit var tvResumenProductos: TextView
    private lateinit var tvTotalPedido: TextView

    private lateinit var radioGroupEntrega: RadioGroup
    private lateinit var radioDelivery: RadioButton
    private lateinit var radioRecojo: RadioButton

    private lateinit var tvDireccion: TextView
    private lateinit var tvReferencia: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var tvMetodoPago: TextView

    private lateinit var btnContinuarPago: MaterialButton

    // =========================================================
    // DATOS DEL PEDIDO
    // =========================================================

    private var nombreUsuario =
        ""

    private var tipoEntrega =
        "Delivery"

    private var metodoPago =
        "Contra entrega"

    private var direccion =
        ""

    private var referencia =
        ""

    private var telefono =
        ""

    private var observacion =
        ""

    private var confirmandoPedido =
        false

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
            R.layout.activity_confirmar_pedido
        )

        aplicarInsets()
        initComponent()
        recibirDatos()
        cargarDatosUsuario()
        cargarResumenPedido()
        mostrarDatosEntrega()
        configurarEntrega()
        configurarBoton()
    }

    // =========================================================
    // INSETS
    // =========================================================

    private fun aplicarInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars = insets.getInsets(
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

        tvNombreUsuario =
            findViewById(R.id.tvNombreUsuario)

        tvCorreoUsuario =
            findViewById(R.id.tvCorreoUsuario)

        tvResumenProductos =
            findViewById(R.id.tvResumenProductos)

        tvTotalPedido =
            findViewById(R.id.tvTotalPedido)

        radioGroupEntrega =
            findViewById(R.id.radioGroupEntrega)

        radioDelivery =
            findViewById(R.id.radioDelivery)

        radioRecojo =
            findViewById(R.id.radioRecojo)

        tvDireccion =
            findViewById(R.id.tvDireccion)

        tvReferencia =
            findViewById(R.id.tvReferencia)

        tvTelefono =
            findViewById(R.id.tvTelefono)

        tvMetodoPago =
            findViewById(R.id.tvMetodoPago)

        btnContinuarPago =
            findViewById(R.id.btnContinuarPago)
    }

    // =========================================================
    // RECIBIR DATOS
    // =========================================================

    private fun recibirDatos() {

        nombreUsuario =
            intent.getStringExtra("nombre")
                ?.trim()
                .orEmpty()

        telefono =
            intent.getStringExtra("telefono")
                ?.trim()
                .orEmpty()

        direccion =
            intent.getStringExtra("direccion")
                ?.trim()
                .orEmpty()

        observacion =
            intent.getStringExtra("observacion")
                ?.trim()
                .orEmpty()

        referencia =
            intent.getStringExtra("referencia")
                ?.trim()
                .orEmpty()

        tipoEntrega =
            intent.getStringExtra("tipoEntrega")
                ?.trim()
                .orEmpty()
                .ifBlank {
                    "Delivery"
                }

        // Único método habilitado actualmente
        metodoPago =
            "Contra entrega"
    }

    // =========================================================
    // DATOS DEL USUARIO
    // =========================================================

    private fun cargarDatosUsuario() {

        val usuario =
            auth.currentUser

        if (usuario == null) {

            Toast.makeText(
                this,
                "No hay una sesión activa",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        tvCorreoUsuario.text =
            usuario.email ?: "Sin correo"

        if (nombreUsuario.isNotBlank()) {

            tvNombreUsuario.text =
                nombreUsuario

            return
        }

        db.collection("usuarios")
            .document(usuario.uid)
            .get()
            .addOnSuccessListener { documento ->

                nombreUsuario =
                    documento.getString("nombre")
                        ?.trim()
                        .orEmpty()
                        .ifBlank {
                            "Cliente"
                        }

                tvNombreUsuario.text =
                    nombreUsuario
            }
            .addOnFailureListener {

                nombreUsuario =
                    "Cliente"

                tvNombreUsuario.text =
                    nombreUsuario
            }
    }

    // =========================================================
    // RESUMEN
    // =========================================================

    private fun cargarResumenPedido() {

        val pedido =
            PedidoManager.pedido

        if (pedido.isEmpty()) {

            Toast.makeText(
                this,
                "El carrito está vacío",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        val cantidad =
            PedidoManager.cantidadTotal()

        tvResumenProductos.text =
            if (cantidad == 1) {
                "1 producto"
            } else {
                "$cantidad productos"
            }

        val total = PedidoManager.totalPedido()

        tvTotalPedido.text =
            "S/ %.2f".format(total)
    }

    // =========================================================
    // MOSTRAR ENTREGA
    // =========================================================

    private fun mostrarDatosEntrega() {

        if (tipoEntrega.equals("Delivery", ignoreCase = true)) {

            radioDelivery.isChecked =
                true

            mostrarDatosDelivery()

        } else {

            radioRecojo.isChecked =
                true

            mostrarDatosRecojo()
        }

        tvMetodoPago.text =
            "Contra entrega"
    }

    private fun mostrarDatosDelivery() {

        tvDireccion.text =
            direccion.ifBlank {
                "No especificada"
            }

        /*
         * Si todavía no tienes un campo separado para referencia,
         * aquí se muestra la observación.
         */
        tvReferencia.text =
            when {
                referencia.isNotBlank() -> referencia
                observacion.isNotBlank() -> observacion
                else -> "Sin observaciones"
            }

        tvTelefono.text =
            telefono.ifBlank {
                "No especificado"
            }
    }

    private fun mostrarDatosRecojo() {

        tvDireccion.text =
            "Recojo en tienda"

        tvReferencia.text =
            observacion.ifBlank {
                "Sin observaciones"
            }

        tvTelefono.text =
            telefono.ifBlank {
                "No especificado"
            }
    }

    // =========================================================
    // TIPO DE ENTREGA
    // =========================================================

    private fun configurarEntrega() {

        radioGroupEntrega.setOnCheckedChangeListener {
                _,
                checkedId ->

            when (checkedId) {

                R.id.radioDelivery -> {

                    tipoEntrega =
                        "Delivery"

                    mostrarDatosDelivery()
                }

                R.id.radioRecojo -> {

                    tipoEntrega =
                        "Recojo en tienda"

                    mostrarDatosRecojo()
                }
            }
        }
    }

    // =========================================================
    // BOTÓN
    // =========================================================

    private fun configurarBoton() {

        btnContinuarPago.setOnClickListener {

            confirmarPedido()
        }
    }

    // =========================================================
    // CONFIRMAR PEDIDO
    // =========================================================

    private fun confirmarPedido() {

        if (confirmandoPedido) {
            return
        }

        val usuario =
            auth.currentUser

        if (usuario == null) {

            Toast.makeText(
                this,
                "No hay una sesión activa",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val pedido =
            PedidoManager.pedido

        if (pedido.isEmpty()) {

            Toast.makeText(
                this,
                "El carrito está vacío",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val opcionSeleccionada =
            radioGroupEntrega.checkedRadioButtonId

        if (opcionSeleccionada == -1) {

            Toast.makeText(
                this,
                "Selecciona cómo deseas recibir tu pedido",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        tipoEntrega =
            when (opcionSeleccionada) {

                R.id.radioDelivery ->
                    "Delivery"

                R.id.radioRecojo ->
                    "Recojo en tienda"

                else ->
                    return
            }

        if (tipoEntrega == "Delivery") {

            if (direccion.isBlank()) {

                Toast.makeText(
                    this,
                    "No se encontró la dirección de entrega",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            if (telefono.isBlank()) {

                Toast.makeText(
                    this,
                    "No se encontró el teléfono",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }
        }

        bloquearBoton()

        val productos = pedido.map { item ->
            hashMapOf<String, Any>(
                "id" to item.id,
                "nombre" to item.nombre,
                "precio" to item.precio,
                "cantidad" to item.cantidad,
                "tipo" to item.tipo.name,
                "precioBaseMenu" to (item.precioBaseMenu ?: 0.0),
                "entradas" to item.entradas.map { entrada ->
                    hashMapOf<String, Any>(
                        "id" to entrada.id,
                        "nombre" to entrada.nombre,
                        "cantidad" to entrada.cantidad,
                        "precioUnitario" to entrada.precioUnitario
                    )
                }
            )
        }

        val total = PedidoManager.totalPedido()

        buscarReferenciasStock(
            pedido = pedido,

            onSuccess = { referencias ->

                guardarPedidoYDescontarStock(
                    usuarioId = usuario.uid,
                    correo = usuario.email.orEmpty(),
                    productos = productos,
                    total = total,
                    referenciasStock = referencias
                )
            },

            onError = { exception ->

                finalizarConError(exception)
            }
        )
    }

    // =========================================================
    // BLOQUEAR BOTÓN
    // =========================================================

    private fun bloquearBoton() {

        confirmandoPedido =
            true

        btnContinuarPago.isEnabled =
            false

        btnContinuarPago.alpha =
            0.5f

        btnContinuarPago.text =
            "CONFIRMANDO..."
    }

    // =========================================================
    // BUSCAR REFERENCIAS DE STOCK
    // =========================================================

    private fun buscarReferenciasStock(
        pedido: List<PedidoItem>,
        onSuccess: (Map<SolicitudStock, DocumentReference>) -> Unit,
        onError: (Exception) -> Unit
    ) {

        val solicitudesAgrupadas =
            mutableMapOf<Pair<String, Int>, SolicitudStock>()

        fun agregarSolicitud(
            coleccion: String,
            id: Int,
            nombre: String,
            cantidad: Int
        ) {
            if (cantidad <= 0) return

            val clave = coleccion to id
            val existente = solicitudesAgrupadas[clave]

            solicitudesAgrupadas[clave] = SolicitudStock(
                coleccion = coleccion,
                idProducto = id,
                nombre = nombre,
                cantidad = (existente?.cantidad ?: 0) + cantidad
            )
        }

        pedido.forEach { item ->
            when (item.tipo) {
                TipoPedido.MENU -> {
                    agregarSolicitud("menu", item.id, item.nombre, item.cantidad)

                    item.entradas.forEach { entrada ->
                        agregarSolicitud(
                            coleccion = "entradas",
                            id = entrada.id,
                            nombre = entrada.nombre,
                            cantidad = entrada.cantidad * item.cantidad
                        )
                    }
                }

                // Compatibilidad temporal con entradas antiguas independientes.
                TipoPedido.ENTRADA ->
                    agregarSolicitud("entradas", item.id, item.nombre, item.cantidad)

                TipoPedido.EXTRA -> Unit
            }
        }

        val productosConStock = solicitudesAgrupadas.values.toList()
        val referencias = mutableMapOf<SolicitudStock, DocumentReference>()

        if (productosConStock.isEmpty()) {

            onSuccess(referencias)
            return
        }

        buscarReferenciaRecursiva(
            productos = productosConStock,
            posicion = 0,
            referencias = referencias,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    // =========================================================
    // BÚSQUEDA RECURSIVA
    // =========================================================

    private fun buscarReferenciaRecursiva(
        productos: List<SolicitudStock>,
        posicion: Int,
        referencias: MutableMap<SolicitudStock, DocumentReference>,
        onSuccess: (Map<SolicitudStock, DocumentReference>) -> Unit,
        onError: (Exception) -> Unit
    ) {

        if (posicion >= productos.size) {

            onSuccess(referencias)
            return
        }

        val item =
            productos[posicion]

        db.collection(item.coleccion)
            .whereEqualTo("id", item.idProducto)
            .limit(1)
            .get()
            .addOnSuccessListener { resultado ->

                if (resultado.isEmpty) {

                    onError(
                        IllegalStateException(
                            "No se encontró '${item.nombre}' en ${item.coleccion}."
                        )
                    )

                    return@addOnSuccessListener
                }

                referencias[item] =
                    resultado.documents
                        .first()
                        .reference

                buscarReferenciaRecursiva(
                    productos = productos,
                    posicion = posicion + 1,
                    referencias = referencias,
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
            .addOnFailureListener { exception ->

                onError(exception)
            }
    }

    // =========================================================
    // GUARDAR PEDIDO Y DESCONTAR STOCK
    // =========================================================

    private fun guardarPedidoYDescontarStock(
        usuarioId: String,
        correo: String,
        productos: List<HashMap<String, Any>>,
        total: Double,
        referenciasStock: Map<SolicitudStock, DocumentReference>
    ) {

        val referenciaPedido =
            db.collection("pedidos")
                .document()

        val contadorReferencia =
            db.collection("configuracion")
                .document("contadorPedidos")

        db.runTransaction { transaction ->

            val contadorSnapshot =
                transaction.get(contadorReferencia)

            val ultimoNumero =
                contadorSnapshot.getLong("ultimoNumero")
                    ?: 0L

            val numeroPedido =
                ultimoNumero + 1

            val stocksActuales = mutableMapOf<SolicitudStock, Long>()

            // Primero se realizan todas las lecturas.
            for ((item, referenciaStock) in referenciasStock) {

                val snapshot =
                    transaction.get(referenciaStock)

                if (!snapshot.exists()) {

                    throw IllegalStateException(
                        "El producto '${item.nombre}' ya no existe."
                    )
                }

                val stock =
                    snapshot.getLong("stock")
                        ?: throw IllegalStateException(
                            "El producto '${item.nombre}' no tiene un stock válido."
                        )

                if (stock < item.cantidad) {

                    throw IllegalStateException(
                        "SIN_STOCK:${item.nombre}:$stock:${item.cantidad}"
                    )
                }

                stocksActuales[item] =
                    stock
            }

            // Después se realizan las escrituras.
            for ((item, referenciaStock) in referenciasStock) {

                val stockActual =
                    stocksActuales[item]
                        ?: 0L

                val nuevoStock =
                    stockActual - item.cantidad

                transaction.update(
                    referenciaStock,
                    "stock",
                    nuevoStock
                )
            }

            val datosPedido =
                hashMapOf<String, Any>(

                    "numeroPedido" to numeroPedido,
                    "usuarioId" to usuarioId,

                    "nombreUsuario" to
                            nombreUsuario.ifBlank {
                                "Cliente"
                            },

                    "correo" to correo,
                    "productos" to productos,
                    "total" to total,

                    "tipoEntrega" to tipoEntrega,

                    "direccion" to
                            if (tipoEntrega == "Delivery") {
                                direccion
                            } else {
                                "Recojo en tienda"
                            },

                    "referencia" to referencia,
                    "telefono" to telefono,
                    "observacion" to observacion,

                    // Único método disponible actualmente
                    "metodoPago" to "Contra entrega",
                    "estadoPago" to "Pendiente",
                    "estadoPedido" to "Pendiente",

                    "fecha" to
                            FieldValue.serverTimestamp()
                )

            transaction.set(
                referenciaPedido,
                datosPedido
            )

            transaction.set(
                contadorReferencia,
                hashMapOf(
                    "ultimoNumero" to numeroPedido
                )
            )
        }
            .addOnSuccessListener {

                pedidoConfirmado()
            }
            .addOnFailureListener { exception ->

                finalizarConError(exception)
            }
    }

    // =========================================================
    // PEDIDO CONFIRMADO
    // =========================================================

    private fun pedidoConfirmado() {

        PedidoManager.limpiar()

        Toast.makeText(
            this,
            "¡Pedido confirmado correctamente! 🚀",
            Toast.LENGTH_LONG
        ).show()

        val intent = Intent(
            this,
            ActivityMenuUsuario::class.java
        ).apply {

            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    // =========================================================
    // MANEJO DE ERRORES
    // =========================================================

    private fun finalizarConError(exception: Exception) {

        confirmandoPedido =
            false

        btnContinuarPago.isEnabled =
            true

        btnContinuarPago.alpha =
            1f

        btnContinuarPago.text =
            "CONFIRMAR PEDIDO"

        val mensaje =
            if (
                exception.message
                    ?.startsWith("SIN_STOCK:") == true
            ) {

                val partes =
                    exception.message
                        ?.split(":")

                val producto =
                    partes?.getOrNull(1)
                        ?: "este producto"

                val disponible =
                    partes?.getOrNull(2)
                        ?: "0"

                val solicitado =
                    partes?.getOrNull(3)
                        ?: "0"

                "No hay stock suficiente de $producto. " +
                        "Disponible: $disponible. " +
                        "Solicitado: $solicitado."

            } else {

                exception.message
                    ?: "No se pudo confirmar el pedido. Inténtalo nuevamente."
            }

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }
}
