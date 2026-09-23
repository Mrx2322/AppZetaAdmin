package com.example.appzetaadmin.adminmenu.pedidosadmin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetaadmin.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ActivityHistorialPedidosAdmin : AppCompatActivity() {

    private enum class FiltroFecha { TODOS, HOY, AYER }

    private val db = FirebaseFirestore.getInstance()
    private val zonaPeru = TimeZone.getTimeZone("America/Lima")
    private val localePeru = Locale.forLanguageTag("es-PE")
    private val handlerMedianoche = Handler(Looper.getMainLooper())

    private var listenerHistorial: ListenerRegistration? = null
    private var filtroFechaActual = FiltroFecha.TODOS
    private var textoBusqueda = ""

    private lateinit var rvHistorialPedidos: RecyclerView
    private lateinit var tvSinHistorial: TextView
    private lateinit var etBuscarHistorial: EditText
    private lateinit var btnFiltroTodos: MaterialButton
    private lateinit var btnFiltroHoy: MaterialButton
    private lateinit var btnFiltroAyer: MaterialButton

    private val historialCompleto = mutableListOf<PedidoAdmin>()
    private val historialVisible = mutableListOf<PedidoAdmin>()
    private lateinit var adapter: PedidoAdminAdapter

    private val actualizarAlCambiarDia: Runnable = Runnable {
        aplicarFiltros()
        programarActualizacionDeMedianoche()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_historial_pedidos_admin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        inicializarComponentes()
        configurarInterfaz()
        escucharHistorial()
    }

    override fun onStart() {
        super.onStart()
        programarActualizacionDeMedianoche()
    }

    override fun onStop() {
        handlerMedianoche.removeCallbacks(actualizarAlCambiarDia)
        super.onStop()
    }

    override fun onDestroy() {
        listenerHistorial?.remove()
        super.onDestroy()
    }

    private fun inicializarComponentes() {
        rvHistorialPedidos = findViewById(R.id.rvHistorialPedidos)
        tvSinHistorial = findViewById(R.id.tvSinHistorial)
        etBuscarHistorial = findViewById(R.id.etBuscarHistorial)
        btnFiltroTodos = findViewById(R.id.btnFiltroTodos)
        btnFiltroHoy = findViewById(R.id.btnFiltroHoy)
        btnFiltroAyer = findViewById(R.id.btnFiltroAyer)
    }

    private fun configurarInterfaz() {
        adapter = PedidoAdminAdapter(
            listaPedidos = historialVisible,
            onCambiarEstado = { _, _ -> },
            onEliminarPedido = ::confirmarEliminacion,
            modoHistorial = true
        )

        rvHistorialPedidos.layoutManager = LinearLayoutManager(this)
        rvHistorialPedidos.adapter = adapter

        etBuscarHistorial.doAfterTextChanged { editable ->
            textoBusqueda = editable?.toString()?.trim().orEmpty()
            aplicarFiltros()
        }

        btnFiltroTodos.setOnClickListener { seleccionarFiltro(FiltroFecha.TODOS) }
        btnFiltroHoy.setOnClickListener { seleccionarFiltro(FiltroFecha.HOY) }
        btnFiltroAyer.setOnClickListener { seleccionarFiltro(FiltroFecha.AYER) }

        actualizarBotonesFiltro()
    }

    private fun confirmarEliminacion(pedido: PedidoAdmin) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.admin_history_delete_title)
            .setMessage(
                getString(
                    R.string.admin_history_delete_message,
                    formatearNumeroPedido(pedido)
                )
            )
            .setNegativeButton(R.string.admin_history_delete_cancel, null)
            .setPositiveButton(R.string.admin_history_delete_confirm) { _, _ ->
                eliminarPedido(pedido)
            }
            .show()
    }

    private fun eliminarPedido(pedido: PedidoAdmin) {
        if (pedido.id.isBlank()) {
            Toast.makeText(
                this,
                R.string.admin_history_delete_error,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        db.collection("pedidos")
            .document(pedido.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    R.string.admin_history_delete_success,
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error eliminando el pedido ${pedido.id}", error)
                Toast.makeText(
                    this,
                    R.string.admin_history_delete_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun formatearNumeroPedido(pedido: PedidoAdmin): String =
        if (pedido.numeroPedido > 0L) {
            String.format(localePeru, "#%04d", pedido.numeroPedido)
        } else {
            getString(R.string.admin_history_order_without_number)
        }

    private fun seleccionarFiltro(filtro: FiltroFecha) {
        filtroFechaActual = filtro
        actualizarBotonesFiltro()
        aplicarFiltros()
    }

    private fun actualizarBotonesFiltro() {
        btnFiltroTodos.isChecked = filtroFechaActual == FiltroFecha.TODOS
        btnFiltroHoy.isChecked = filtroFechaActual == FiltroFecha.HOY
        btnFiltroAyer.isChecked = filtroFechaActual == FiltroFecha.AYER
    }

    private fun escucharHistorial() {
        listenerHistorial = db.collection("pedidos")
            .addSnapshotListener { resultado, error ->
                if (error != null) {
                    Log.e(TAG, "Error escuchando historial", error)
                    Toast.makeText(
                        this,
                        R.string.admin_history_load_error,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                val documentos = resultado?.documents ?: return@addSnapshotListener
                val limiteHistorial = obtenerInicioDelHistorial()

                val pedidosRecientes = documentos.mapNotNull { documento ->
                    val estadoPedido = documento.getString("estadoPedido") ?: "Pendiente"

                    if (!estadoPedido.equals("Entregado", ignoreCase = true)) {
                        return@mapNotNull null
                    }

                    val fechaHistorial = documento.getTimestamp("fechaEntrega")
                        ?: documento.getTimestamp("fecha")
                        ?: return@mapNotNull null

                    if (fechaHistorial.toDate().before(limiteHistorial.time)) {
                        return@mapNotNull null
                    }

                    PedidoAdmin(
                        id = documento.id,
                        numeroPedido = documento.getLong("numeroPedido") ?: 0L,
                        fecha = fechaHistorial,
                        nombreUsuario = documento.getString("nombreUsuario")
                            ?: getString(R.string.admin_history_default_customer),
                        correo = documento.getString("correo").orEmpty(),
                        total = documento.getDouble("total")
                            ?: documento.getLong("total")?.toDouble()
                            ?: 0.0,
                        tipoEntrega = documento.getString("tipoEntrega")
                            ?: getString(R.string.admin_history_default_delivery),
                        direccion = documento.getString("direccion").orEmpty(),
                        referencia = documento.getString("referencia").orEmpty(),
                        telefono = documento.getString("telefono").orEmpty(),
                        metodoPago = documento.getString("metodoPago")
                            ?: getString(R.string.admin_history_default_payment),
                        estadoPago = documento.getString("estadoPago")
                            ?: getString(R.string.admin_history_default_pending),
                        estadoPedido = estadoPedido,
                        productos = convertirProductos(documento.get("productos"))
                    )
                }.sortedByDescending { pedido ->
                    pedido.fecha?.seconds ?: 0L
                }

                historialCompleto.clear()
                historialCompleto.addAll(pedidosRecientes)
                aplicarFiltros()

                Log.d(TAG, "Pedidos entregados de los últimos 7 días: ${historialCompleto.size}")
            }
    }

    private fun aplicarFiltros() {
        val pedidosFiltrados = historialCompleto.filter { pedido ->
            estaDentroDeLosUltimosSieteDias(pedido.fecha) &&
                    coincideConFiltroFecha(pedido.fecha) &&
                    coincideConBusqueda(pedido)
        }

        adapter.actualizarPedidos(pedidosFiltrados)
        actualizarEstadoVacio()
    }

    private fun coincideConFiltroFecha(fecha: Timestamp?): Boolean {
        fecha ?: return false

        return when (filtroFechaActual) {
            FiltroFecha.TODOS -> true
            FiltroFecha.HOY -> esMismoDia(fecha, diasRespectoHoy = 0)
            FiltroFecha.AYER -> esMismoDia(fecha, diasRespectoHoy = -1)
        }
    }

    private fun estaDentroDeLosUltimosSieteDias(fecha: Timestamp?): Boolean {
        fecha ?: return false
        return !fecha.toDate().before(obtenerInicioDelHistorial().time)
    }

    private fun coincideConBusqueda(pedido: PedidoAdmin): Boolean {
        if (textoBusqueda.isBlank()) return true

        val consulta = textoBusqueda.lowercase(localePeru)
        return pedido.numeroPedido.toString().contains(consulta) ||
                pedido.nombreUsuario.lowercase(localePeru).contains(consulta) ||
                pedido.telefono.lowercase(localePeru).contains(consulta)
    }

    private fun esMismoDia(fecha: Timestamp, diasRespectoHoy: Int): Boolean {
        val fechaPedido = Calendar.getInstance(zonaPeru).apply {
            time = fecha.toDate()
        }
        val fechaComparada = Calendar.getInstance(zonaPeru).apply {
            add(Calendar.DAY_OF_YEAR, diasRespectoHoy)
        }

        return fechaPedido.get(Calendar.YEAR) == fechaComparada.get(Calendar.YEAR) &&
                fechaPedido.get(Calendar.DAY_OF_YEAR) ==
                fechaComparada.get(Calendar.DAY_OF_YEAR)
    }

    /** Incluye hoy y los seis días calendario anteriores. */
    private fun obtenerInicioDelHistorial(): Calendar =
        Calendar.getInstance(zonaPeru).apply {
            add(Calendar.DAY_OF_YEAR, -6)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    private fun programarActualizacionDeMedianoche() {
        handlerMedianoche.removeCallbacks(actualizarAlCambiarDia)

        val ahora = Calendar.getInstance(zonaPeru)
        val proximaMedianoche = Calendar.getInstance(zonaPeru).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 1)
            set(Calendar.MILLISECOND, 0)
        }

        val demora = (proximaMedianoche.timeInMillis - ahora.timeInMillis)
            .coerceAtLeast(1_000L)
        handlerMedianoche.postDelayed(actualizarAlCambiarDia, demora)
    }

    private fun convertirProductos(valor: Any?): List<Map<String, Any>> {
        val productos = valor as? List<*> ?: return emptyList()

        return productos.mapNotNull { producto ->
            val mapa = producto as? Map<*, *> ?: return@mapNotNull null

            mapa.entries.mapNotNull { entrada ->
                val clave = entrada.key as? String ?: return@mapNotNull null
                val contenido = entrada.value ?: return@mapNotNull null
                clave to contenido
            }.toMap()
        }
    }

    private fun actualizarEstadoVacio() {
        val historialVacio = historialVisible.isEmpty()
        rvHistorialPedidos.visibility = if (historialVacio) View.GONE else View.VISIBLE
        tvSinHistorial.visibility = if (historialVacio) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG = "HISTORIAL_ADMIN"
    }
}