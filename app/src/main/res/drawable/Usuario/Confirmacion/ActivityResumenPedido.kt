package com.example.appzetar.Usuario.Confirmacion

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetar.R
import com.example.appzetar.Usuario.ActivityConfirmarPedido
import com.example.appzetar.Usuario.Carrito.PedidoManager

class ActivityResumenPedido : AppCompatActivity() {

    // =========================================================
    // COMPONENTES
    // =========================================================

    private lateinit var rvResumenPedido: RecyclerView
    private lateinit var tvNombreCliente: TextView
    private lateinit var tvTelefonoCliente: TextView
    private lateinit var tvDireccionCliente: TextView
    private lateinit var tvObservacionCliente: TextView
    private lateinit var tvTotalProductos: TextView
    private lateinit var btnConfirmarPedido: Button

    private lateinit var resumenAdapter: PedidoResumenAdapter

    // =========================================================
    // DATOS RECIBIDOS
    // =========================================================

    private var nombre = ""
    private var telefono = ""
    private var direccion = ""
    private var observacion = ""

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

        setContentView(R.layout.activity_resumen_pedido)

        aplicarInsets()
        obtenerDatos()
        initComponent()
        initUI()
    }

    // =========================================================
    // BARRAS DEL SISTEMA
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
    // OBTENER DATOS
    // =========================================================

    private fun obtenerDatos() {

        nombre =
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
    }

    // =========================================================
    // COMPONENTES
    // =========================================================

    private fun initComponent() {

        rvResumenPedido =
            findViewById(R.id.rvResumenPedido)

        tvNombreCliente =
            findViewById(R.id.tvNombreCliente)

        tvTelefonoCliente =
            findViewById(R.id.tvTelefonoCliente)

        tvDireccionCliente =
            findViewById(R.id.tvDireccionCliente)

        tvObservacionCliente =
            findViewById(R.id.tvObservacionCliente)

        tvTotalProductos =
            findViewById(R.id.tvTotalProductos)

        btnConfirmarPedido =
            findViewById(R.id.btnConfirmarPedido)
    }

    // =========================================================
    // INTERFAZ
    // =========================================================

    private fun initUI() {

        mostrarDatosCliente()
        configurarListaPedido()
        mostrarCantidadProductos()
        configurarBotonConfirmar()
    }

    // =========================================================
    // MOSTRAR DATOS DEL CLIENTE
    // =========================================================

    private fun mostrarDatosCliente() {

        tvNombreCliente.text =
            nombre

        tvTelefonoCliente.text =
            telefono

        tvDireccionCliente.text =
            direccion

        if (observacion.isBlank()) {

            tvObservacionCliente.visibility =
                View.GONE

        } else {

            tvObservacionCliente.visibility =
                View.VISIBLE

            tvObservacionCliente.text =
                observacion
        }
    }

    // =========================================================
    // LISTA DEL PEDIDO
    // =========================================================

    private fun configurarListaPedido() {

        resumenAdapter =
            PedidoResumenAdapter(
                PedidoManager.pedido
            )

        rvResumenPedido.apply {

            layoutManager =
                LinearLayoutManager(
                    this@ActivityResumenPedido
                )

            adapter =
                resumenAdapter
        }
    }

    // =========================================================
    // CANTIDAD TOTAL
    // =========================================================

    private fun mostrarCantidadProductos() {

        val cantidad =
            PedidoManager.cantidadTotal()

        tvTotalProductos.text =
            if (cantidad == 1) {
                "1 producto"
            } else {
                "$cantidad productos"
            }
    }

    // =========================================================
    // BOTÓN CONFIRMAR
    // =========================================================

    private fun configurarBotonConfirmar() {

        btnConfirmarPedido.setOnClickListener {
            confirmarPedido()
        }
    }

    // =========================================================
    // CONTINUAR A LA CONFIRMACIÓN FINAL
    // =========================================================

    private fun confirmarPedido() {

        if (PedidoManager.pedido.isEmpty()) {

            Toast.makeText(
                this,
                "El carrito está vacío",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val intent = Intent(
            this,
            ActivityConfirmarPedido::class.java
        ).apply {

            putExtra("nombre", nombre)
            putExtra("telefono", telefono)
            putExtra("direccion", direccion)
            putExtra("observacion", observacion)

            // Opciones activas actualmente
            putExtra("tipoEntrega", "Delivery")
            putExtra("metodoPago", "Contra entrega")
        }

        startActivity(intent)
    }
}