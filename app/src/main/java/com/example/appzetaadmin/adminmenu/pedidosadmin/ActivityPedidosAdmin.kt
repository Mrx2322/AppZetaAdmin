package com.example.appzetaadmin.adminmenu.pedidosadmin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
                    false
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

                    adapter.actualizarPedidos(
                        nuevosPedidos
                    )

                    actualizarEstadoVacio()
                }
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
    }

    override fun onStop() {

        pedidosListener?.remove()

        pedidosListener =
            null

        super.onStop()
    }

    companion object {

        private const val DIAS_DE_RETENCION = 7
    }
}