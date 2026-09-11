package com.example.appzetar.Usuario

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetar.R
import com.example.appzetar.Usuario.Carrito.ActivityPedido
import com.example.appzetar.Usuario.Carrito.PedidoManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ActivityPedidosUsuario : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var pedidosListener: ListenerRegistration? = null

    private lateinit var rvPedidos: RecyclerView
    private lateinit var progressBarPedidos: ProgressBar
    private lateinit var layoutSinPedidos: LinearLayout
    private lateinit var tvCantidadCarrito: TextView

    private lateinit var navInicio: LinearLayout
    private lateinit var navExtras: LinearLayout
    private lateinit var navPedidos: LinearLayout
    private lateinit var navCarrito: LinearLayout
    private lateinit var navPerfil: LinearLayout

    private lateinit var pedidoAdapter: PedidoUsuarioAdapter

    private val listaPedidos = mutableListOf<PedidoUsuarioItem>()

    private val handler = Handler(Looper.getMainLooper())
    private val intervaloRevision = 60_000L

    private val revisarPedidosRunnable = object : Runnable {
        override fun run() {
            revisarPedidosVisibles()

            handler.postDelayed(
                this,
                intervaloRevision
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pedidos_usuario)

        inicializarComponentes()
        configurarRecyclerView()
        configurarNavegacion()
        configurarAnimacionesBarra()
        marcarPedidosActivo()
        actualizarContadorCarrito()
    }

    override fun onResume() {
        super.onResume()
        actualizarContadorCarrito()
    }

    private fun inicializarComponentes() {
        rvPedidos = findViewById(R.id.rvPedidos)
        progressBarPedidos = findViewById(R.id.progressBarPedidos)
        layoutSinPedidos = findViewById(R.id.layoutSinPedidos)
        tvCantidadCarrito = findViewById(R.id.tvCantidadCarrito)

        navInicio = findViewById(R.id.navInicio)
        navExtras = findViewById(R.id.navExtras)
        navPedidos = findViewById(R.id.navPedidos)
        navCarrito = findViewById(R.id.navCarrito)
        navPerfil = findViewById(R.id.navPerfil)
    }

    private fun configurarRecyclerView() {
        pedidoAdapter = PedidoUsuarioAdapter()

        rvPedidos.apply {
            layoutManager = LinearLayoutManager(
                this@ActivityPedidosUsuario
            )

            adapter = pedidoAdapter
            setHasFixedSize(false)
            itemAnimator = null
        }
    }

    private fun configurarNavegacion() {
        navInicio.setOnClickListener {
            startActivity(
                Intent(this, ActivityMenuUsuario::class.java).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            )
        }

        navExtras.setOnClickListener {
            startActivity(
                Intent(this, ActivityExtras::class.java)
            )
        }

        navPedidos.setOnClickListener {
            // Ya estás en Pedidos.
        }

        navCarrito.setOnClickListener {
            startActivity(
                Intent(this, ActivityPedido::class.java)
            )
        }

        navPerfil.setOnClickListener {
            startActivity(
                Intent(this, ActivityPerfilUsuario::class.java)
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configurarAnimacionesBarra() {
        val opciones = listOf(
            navInicio,
            navExtras,
            navPedidos,
            navCarrito,
            navPerfil
        )

        opciones.forEach { opcion ->
            opcion.setOnTouchListener { vista, evento ->

                when (evento.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        vista.animate()
                            .scaleX(1.12f)
                            .scaleY(1.12f)
                            .translationY(-9f)
                            .setDuration(150)
                            .setInterpolator(DecelerateInterpolator())
                            .start()
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        vista.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationY(0f)
                            .setDuration(180)
                            .setInterpolator(DecelerateInterpolator())
                            .start()
                    }
                }

                false
            }
        }
    }

    private fun marcarPedidosActivo() {
        navPedidos.post {
            navPedidos.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .translationY(-5f)
                .setDuration(280)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun actualizarContadorCarrito() {
        val cantidad = PedidoManager.cantidadTotal()

        tvCantidadCarrito.text = cantidad.toString()

        tvCantidadCarrito.visibility =
            if (cantidad > 0) View.VISIBLE else View.GONE
    }

    private fun escucharPedidos() {
        val usuarioActual = auth.currentUser

        if (usuarioActual == null) {
            Toast.makeText(
                this,
                "Debes iniciar sesión para ver tus pedidos",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        mostrarCargando()

        pedidosListener?.remove()

        pedidosListener = db.collection("pedidos")
            .whereEqualTo(
                "usuarioId",
                usuarioActual.uid
            )
            .addSnapshotListener { resultado, error ->

                progressBarPedidos.visibility = View.GONE

                if (error != null) {
                    Log.e(
                        "PEDIDOS_USUARIO",
                        "Error escuchando pedidos",
                        error
                    )

                    limpiarPedidos()
                    mostrarSinPedidos()

                    Toast.makeText(
                        this,
                        "No se pudieron cargar los pedidos",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addSnapshotListener
                }

                if (resultado == null) {
                    limpiarPedidos()
                    mostrarSinPedidos()
                    return@addSnapshotListener
                }

                listaPedidos.clear()

                val ahora = System.currentTimeMillis()

                for (documento in resultado.documents) {
                    val pedido = convertirDocumentoEnPedido(
                        documento
                    )

                    if (pedido.debeMostrarse(ahora)) {
                        listaPedidos.add(pedido)
                    }
                }

                listaPedidos.sortByDescending { pedido ->
                    pedido.fecha
                }

                actualizarLista()
            }
    }

    private fun convertirDocumentoEnPedido(
        documento: DocumentSnapshot
    ): PedidoUsuarioItem {

        val numeroPedido =
            documento.getLong("numeroPedido") ?: 0L

        val estado = documento.getString("estadoPedido")
            ?.trim()
            .orEmpty()
            .ifBlank {
                "Pendiente"
            }

        val tipoEntrega = documento.getString("tipoEntrega")
            ?.trim()
            .orEmpty()
            .ifBlank {
                "Delivery"
            }

        val total = documento.getDouble("total")
            ?: documento.getLong("total")?.toDouble()
            ?: 0.0

        val fecha = obtenerTimestamp(
            documento,
            "fecha"
        )

        val fechaEntrega = obtenerTimestamp(
            documento,
            "fechaEntrega"
        )

        val fechaActualizacion = obtenerTimestamp(
            documento,
            "fechaActualizacion"
        )

        return PedidoUsuarioItem(
            id = documento.id,
            numeroPedido = numeroPedido,
            estado = estado,
            tipoEntrega = tipoEntrega,
            total = total,
            fecha = fecha,
            fechaEntrega = fechaEntrega,
            fechaActualizacion = fechaActualizacion
        )
    }

    private fun obtenerTimestamp(
        documento: DocumentSnapshot,
        campo: String
    ): Long {
        return documento.getTimestamp(campo)
            ?.toDate()
            ?.time
            ?: 0L
    }

    private fun revisarPedidosVisibles() {
        if (listaPedidos.isEmpty()) {
            return
        }

        val ahora = System.currentTimeMillis()

        val seEliminoAlgunPedido =
            listaPedidos.removeAll { pedido ->
                !pedido.debeMostrarse(ahora)
            }

        if (seEliminoAlgunPedido) {
            actualizarLista()

            Log.d(
                "PEDIDOS_USUARIO",
                "Se ocultaron pedidos vencidos"
            )
        }
    }

    private fun actualizarLista() {
        pedidoAdapter.actualizarPedidos(listaPedidos)
        actualizarEstadoPantalla()
    }

    private fun mostrarCargando() {
        progressBarPedidos.visibility = View.VISIBLE
        rvPedidos.visibility = View.GONE
        layoutSinPedidos.visibility = View.GONE
    }

    private fun actualizarEstadoPantalla() {
        progressBarPedidos.visibility = View.GONE

        if (listaPedidos.isEmpty()) {
            mostrarSinPedidos()
        } else {
            rvPedidos.visibility = View.VISIBLE
            layoutSinPedidos.visibility = View.GONE
        }
    }

    private fun mostrarSinPedidos() {
        progressBarPedidos.visibility = View.GONE
        rvPedidos.visibility = View.GONE
        layoutSinPedidos.visibility = View.VISIBLE
    }

    private fun limpiarPedidos() {
        listaPedidos.clear()
        pedidoAdapter.actualizarPedidos(emptyList())
    }

    override fun onStart() {
        super.onStart()

        escucharPedidos()

        handler.removeCallbacks(revisarPedidosRunnable)
        handler.post(revisarPedidosRunnable)
    }

    override fun onStop() {
        pedidosListener?.remove()
        pedidosListener = null

        handler.removeCallbacks(revisarPedidosRunnable)

        super.onStop()
    }
}