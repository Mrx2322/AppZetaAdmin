package com.example.appzetaadmin.AdminMenu

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ActivityHistorialPedidosAdmin : AppCompatActivity() {

    private val db =
        FirebaseFirestore.getInstance()

    private var listenerHistorial: ListenerRegistration? = null

    private lateinit var rvHistorialPedidos: RecyclerView
    private lateinit var tvSinHistorial: TextView

    private val listaHistorial =
        mutableListOf<PedidoAdmin>()

    private lateinit var adapter: PedidoAdminAdapter

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContentView(
            R.layout.activity_historial_pedidos_admin
        )

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

        initComponent()
        initUI()
        escucharHistorial()
    }

    override fun onDestroy() {

        super.onDestroy()

        listenerHistorial?.remove()
    }

    private fun initComponent() {

        rvHistorialPedidos =
            findViewById(
                R.id.rvHistorialPedidos
            )

        tvSinHistorial =
            findViewById(
                R.id.tvSinHistorial
            )
    }

    private fun initUI() {

        adapter = PedidoAdminAdapter(
            listaHistorial,
            { _, _ -> },
            modoHistorial = true
        )

        rvHistorialPedidos.layoutManager =
            LinearLayoutManager(this)

        rvHistorialPedidos.adapter =
            adapter
    }

    private fun escucharHistorial() {

        listenerHistorial =
            db.collection("pedidos")
                .addSnapshotListener { resultado, error ->

                    if (error != null) {

                        Log.e(
                            "HISTORIAL_ADMIN",
                            "Error escuchando historial",
                            error
                        )

                        Toast.makeText(
                            this,
                            "Error al cargar historial",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@addSnapshotListener
                    }

                    if (resultado == null) {
                        return@addSnapshotListener
                    }

                    listaHistorial.clear()

                    for (documento in resultado.documents) {

                        val estadoPedido =
                            documento.getString(
                                "estadoPedido"
                            ) ?: "Pendiente"

                        // Solo mostrar pedidos entregados.
                        if (
                            !estadoPedido.equals(
                                "Entregado",
                                ignoreCase = true
                            )
                        ) {
                            continue
                        }

                        val numeroPedido =
                            documento.getLong(
                                "numeroPedido"
                            ) ?: 0L

                        val fecha =
                            documento.getTimestamp(
                                "fecha"
                            )

                        val pedido =
                            PedidoAdmin(

                                id =
                                    documento.id,

                                numeroPedido =
                                    numeroPedido,

                                fecha =
                                    fecha,

                                nombreUsuario =
                                    documento.getString(
                                        "nombreUsuario"
                                    ) ?: "Cliente",

                                correo =
                                    documento.getString(
                                        "correo"
                                    ) ?: "",

                                total =
                                    documento.getDouble(
                                        "total"
                                    )
                                        ?: documento.getLong(
                                            "total"
                                        )?.toDouble()
                                        ?: 0.0,

                                tipoEntrega =
                                    documento.getString(
                                        "tipoEntrega"
                                    ) ?: "Delivery",

                                direccion =
                                    documento.getString(
                                        "direccion"
                                    ) ?: "",

                                referencia =
                                    documento.getString(
                                        "referencia"
                                    ) ?: "",

                                telefono =
                                    documento.getString(
                                        "telefono"
                                    ) ?: "",

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
                                    documento.get(
                                        "productos"
                                    ) as? List<Map<String, Any>>
                                        ?: emptyList()
                            )

                        listaHistorial.add(
                            pedido
                        )
                    }

                    // Ordenar por fecha real.
                    // Los pedidos más recientes aparecen primero.
                    listaHistorial.sortByDescending { pedido ->

                        pedido.fecha
                            ?.toDate()
                            ?.time
                            ?: 0L
                    }

                    adapter.notifyDataSetChanged()

                    actualizarEstadoVacio()

                    Log.d(
                        "HISTORIAL_ADMIN",
                        "Pedidos entregados: " +
                                listaHistorial.size
                    )
                }
    }

    private fun actualizarEstadoVacio() {

        if (listaHistorial.isEmpty()) {

            rvHistorialPedidos.visibility =
                View.GONE

            tvSinHistorial.visibility =
                View.VISIBLE

        } else {

            rvHistorialPedidos.visibility =
                View.VISIBLE

            tvSinHistorial.visibility =
                View.GONE
        }
    }
}