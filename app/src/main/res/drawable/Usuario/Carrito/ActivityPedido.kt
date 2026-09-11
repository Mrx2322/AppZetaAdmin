package com.example.appzetar.Usuario.Carrito

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetar.R
import com.example.appzetar.Usuario.ActivityEntrega
import com.example.appzetar.Usuario.ActivityExtras
import com.example.appzetar.Usuario.ActivityMenuUsuario
import com.example.appzetar.Usuario.ActivityPedidosUsuario
import com.example.appzetar.Usuario.ActivityPerfilUsuario
import com.google.android.material.button.MaterialButton

class ActivityPedido : AppCompatActivity() {

    private lateinit var rvPedido: RecyclerView
    private lateinit var tvTotalProductos: TextView
    private lateinit var tvTotalPedido: TextView
    private lateinit var tvCantidadCarrito: TextView
    private lateinit var tvMensajeVacio: View
    private lateinit var btnContinuar: MaterialButton

    private lateinit var navInicio: LinearLayout
    private lateinit var navExtras: LinearLayout
    private lateinit var navPedidos: LinearLayout
    private lateinit var navCarrito: LinearLayout
    private lateinit var navPerfil: LinearLayout

    private lateinit var pedidoAdapter: PedidoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContentView(R.layout.activity_pedido)

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

        initComponent()
        initUI()
        actualizarPedido()
    }

    private fun initComponent() {
        rvPedido = findViewById(R.id.rvPedido)
        tvTotalProductos = findViewById(R.id.tvTotalProductos)
        tvTotalPedido = findViewById(R.id.tvTotalPedido)
        tvCantidadCarrito = findViewById(R.id.tvCantidadCarrito)
        tvMensajeVacio = findViewById(R.id.tvMensajeVacio)
        btnContinuar = findViewById(R.id.btnContinuar)

        navInicio = findViewById(R.id.navInicio)
        navExtras = findViewById(R.id.navExtras)
        navPedidos = findViewById(R.id.navPedidos)
        navCarrito = findViewById(R.id.navCarrito)
        navPerfil = findViewById(R.id.navPerfil)
    }

    private fun initUI() {
        pedidoAdapter = PedidoAdapter(
            PedidoManager.pedido,

            onAumentar = { item ->
                aumentarProducto(item)
            },

            onDisminuir = { item ->
                disminuirProducto(item)
            },

            onEliminar = { item ->
                eliminarProducto(item)
            }
        )

        rvPedido.layoutManager = LinearLayoutManager(this)
        rvPedido.adapter = pedidoAdapter

        btnContinuar.setOnClickListener {
            continuarCompra()
        }

        configurarNavegacion()
        configurarAnimacionesBarra()
        marcarCarritoActivo()
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
            startActivity(
                Intent(this, ActivityPedidosUsuario::class.java)
            )
        }

        navCarrito.setOnClickListener {
            // Ya estás en Carrito.
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

    private fun marcarCarritoActivo() {
        navCarrito.post {
            navCarrito.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .translationY(-5f)
                .setDuration(280)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun aumentarProducto(item: PedidoItem) {
        PedidoManager.aumentarCantidad(item)
        actualizarPedido()
    }

    private fun disminuirProducto(item: PedidoItem) {
        PedidoManager.disminuirCantidad(item)
        actualizarPedido()
    }

    private fun eliminarProducto(item: PedidoItem) {
        PedidoManager.eliminarProducto(item)
        actualizarPedido()
    }

    private fun continuarCompra() {
        if (PedidoManager.pedido.isEmpty()) {
            return
        }

        startActivity(
            Intent(this, ActivityEntrega::class.java)
        )
    }

    private fun actualizarPedido() {
        pedidoAdapter.notifyDataSetChanged()

        val cantidad = PedidoManager.cantidadTotal()

        tvTotalProductos.text =
            if (cantidad == 1) {
                "1 producto"
            } else {
                "$cantidad productos"
            }

        tvCantidadCarrito.text = cantidad.toString()

        tvCantidadCarrito.visibility =
            if (cantidad > 0) View.VISIBLE else View.GONE

        val total = PedidoManager.totalPedido()

        tvTotalPedido.text =
            "S/ %.2f".format(total)

        if (PedidoManager.pedido.isEmpty()) {
            rvPedido.visibility = View.GONE
            tvMensajeVacio.visibility = View.VISIBLE

            btnContinuar.isEnabled = false
            btnContinuar.alpha = 0.5f
        } else {
            rvPedido.visibility = View.VISIBLE
            tvMensajeVacio.visibility = View.GONE

            btnContinuar.isEnabled = true
            btnContinuar.alpha = 1f
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarPedido()
    }
}