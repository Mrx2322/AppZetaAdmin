package com.example.appzetaadmin.adminmenu

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetaadmin.MainActivity
import com.example.appzetaadmin.R
import com.example.appzetaadmin.adminmenu.cenasadmin.CenasAdminAdapter
import com.example.appzetaadmin.adminmenu.cenasadmin.TaskCenas
import com.example.appzetaadmin.adminmenu.entradasadmin.EntradasAdapter
import com.example.appzetaadmin.adminmenu.entradasadmin.TaskEntradas
import com.example.appzetaadmin.adminmenu.extrasadmin.ExtraAdminAdapter
import com.example.appzetaadmin.adminmenu.pedidosadmin.ActivityPedidosAdmin
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale

class ActivityMenu : AppCompatActivity() {

    // =========================================================
    // FIREBASE
    // =========================================================

    private val db =
        FirebaseFirestore.getInstance()

    private var listenerMenu: ListenerRegistration? = null
    private var listenerEntradas: ListenerRegistration? = null
    private var listenerExtras: ListenerRegistration? = null
    private var listenerCenas: ListenerRegistration? = null


    // =========================================================
    // PROGRESS BAR
    // =========================================================

    private lateinit var progressBarMenu: ProgressBar


    // =========================================================
    // ENTRADAS
    // =========================================================

    private val entradas =
        mutableListOf<TaskEntradas>()

    private lateinit var rvEntradas: RecyclerView
    private lateinit var entradasAdapter: EntradasAdapter


    // =========================================================
    // MENÚ
    // =========================================================

    private val listaMenu =
        mutableListOf<TaskMenu>()

    private lateinit var rvMenu: RecyclerView
    private lateinit var menuAdapter: MenuAdapter


    // =========================================================
    // EXTRAS
    // =========================================================

    private val listaExtras =
        mutableListOf<ExtraItem>()

    private lateinit var rvExtras: RecyclerView
    private lateinit var extraAdminAdapter: ExtraAdminAdapter


    // =========================================================
    // CENAS
    // =========================================================

    private val listaCenas =
        mutableListOf<TaskCenas>()

    private lateinit var rvCenas: RecyclerView
    private lateinit var cenasAdminAdapter: CenasAdminAdapter


    // =========================================================
    // BOTONES
    // =========================================================

    private lateinit var fabAgregarMenu: FloatingActionButton
    private lateinit var fabPedidos: FloatingActionButton
    private lateinit var btnCerrarSesion: MaterialButton


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

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            hide(
                WindowInsetsCompat.Type.statusBars()
            )

            systemBarsBehavior =
                WindowInsetsControllerCompat
                    .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContentView(R.layout.activity_menu)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        initComponent()
        initUI()

        cargarDatosDesdeFirebase()
        cargarEntradasDesdeFirebase()
        cargarExtrasDesdeFirebase()
        cargarCenasDesdeFirebase()
    }


    // =========================================================
    // ON DESTROY
    // =========================================================

    override fun onDestroy() {
        super.onDestroy()

        listenerMenu?.remove()
        listenerEntradas?.remove()
        listenerExtras?.remove()
        listenerCenas?.remove()
    }


    // =========================================================
    // INICIALIZAR COMPONENTES
    // =========================================================

    private fun initComponent() {

        progressBarMenu =
            findViewById(R.id.progressBarMenu)

        rvEntradas =
            findViewById(R.id.rvEntradas)

        rvExtras =
            findViewById(R.id.rvExtras)

        rvMenu =
            findViewById(R.id.rvMenu)

        rvCenas =
            findViewById(R.id.rvCenas)

        fabAgregarMenu =
            findViewById(R.id.fabAgregarMenu)

        fabPedidos =
            findViewById(R.id.fabPedidos)

        btnCerrarSesion =
            findViewById(R.id.btnCerrarSesion)
    }


    // =========================================================
    // CONFIGURAR INTERFAZ
    // =========================================================

    private fun initUI() {

        // ENTRADAS

        entradasAdapter =
            EntradasAdapter(entradas) { posicion ->
                mostrarDialogoEdicionEntrada(posicion)
            }

        rvEntradas.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        rvEntradas.adapter = entradasAdapter


        // EXTRAS

        extraAdminAdapter =
            ExtraAdminAdapter(
                listaExtras,

                onEditClick = { posicion ->
                    mostrarDialogoEdicionExtra(posicion)
                },

                onDeleteClick = { posicion ->
                    eliminarExtra(posicion)
                }
            )

        rvExtras.layoutManager =
            LinearLayoutManager(this)

        rvExtras.adapter = extraAdminAdapter


        // MENÚ

        menuAdapter =
            MenuAdapter(
                listaMenu,

                onEditClick = { posicion ->
                    mostrarDialogoEdicionMenu(posicion)
                },

                onDeleteClick = { posicion ->
                    eliminarElementoMenu(posicion)
                }
            )

        rvMenu.layoutManager =
            LinearLayoutManager(this)

        rvMenu.adapter = menuAdapter


        // CENAS

        cenasAdminAdapter =
            CenasAdminAdapter(
                listaCenas,

                onEditClick = { posicion ->
                    mostrarDialogoEdicionCena(posicion)
                },

                onDeleteClick = { posicion ->
                    eliminarCena(posicion)
                }
            )

        rvCenas.layoutManager =
            LinearLayoutManager(this)

        rvCenas.adapter = cenasAdminAdapter


        // BOTÓN AGREGAR

        fabAgregarMenu.setOnClickListener {
            mostrarDialogoSeleccionarTipo()
        }


        // BOTÓN PEDIDOS

        fabPedidos.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ActivityPedidosAdmin::class.java
                )
            )
        }


        // CERRAR SESIÓN

        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }


    // =========================================================
    // CERRAR SESIÓN
    // =========================================================

    private fun cerrarSesion() {

        val autenticacion =
            FirebaseAuth.getInstance()

        autenticacion.signOut()

        if (autenticacion.currentUser != null) {
            Toast.makeText(
                this,
                R.string.admin_error_cerrar_sesion,
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        Toast.makeText(
            this,
            R.string.admin_sesion_cerrada,
            Toast.LENGTH_SHORT
        ).show()

        val intent =
            Intent(
                this,
                MainActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        startActivity(intent)
        finish()
    }


    // =========================================================
    // SELECCIONAR TIPO DE PRODUCTO
    // =========================================================

    private fun mostrarDialogoSeleccionarTipo() {

        val vista =
            layoutInflater.inflate(
                R.layout.dialog_seleccionar_tipo,
                null
            )

        val dialogo =
            AlertDialog.Builder(this)
                .setView(vista)
                .create()

        vista.findViewById<MaterialButton>(
            R.id.btnTipoPlato
        ).setOnClickListener {
            dialogo.dismiss()
            mostrarDialogoAgregarMenu()
        }

        vista.findViewById<MaterialButton>(
            R.id.btnTipoEntrada
        ).setOnClickListener {
            dialogo.dismiss()
            mostrarDialogoAgregarEntrada()
        }

        vista.findViewById<MaterialButton>(
            R.id.btnTipoExtra
        ).setOnClickListener {
            dialogo.dismiss()
            mostrarDialogoAgregarExtra()
        }

        // CENAS:
        // Se conectará automáticamente cuando exista
        // btnTipoCena en dialog_seleccionar_tipo.xml.

        // CENAS
        vista.findViewById<MaterialButton>(
            R.id.btnTipoCena
        ).setOnClickListener {
            dialogo.dismiss()
            mostrarDialogoAgregarCena()
        }

        vista.findViewById<MaterialButton>(
            R.id.btnCancelarTipo
        ).setOnClickListener {
            dialogo.dismiss()
        }

        dialogo.setOnShowListener {
            dialogo.window?.setBackgroundDrawable(
                Color.TRANSPARENT.toDrawable()
            )
        }

        dialogo.show()
    }


    // =========================================================
    // AGREGAR MENÚ
    // =========================================================

    private fun mostrarDialogoAgregarMenu() {

        val dialogView =
            LayoutInflater.from(this).inflate(
                R.layout.dialog_agregar_menu,
                null
            )

        val etNombre =
            dialogView.findViewById<TextInputEditText>(
                R.id.etNombrePlato
            )

        val etPrecio =
            dialogView.findViewById<TextInputEditText>(
                R.id.etPrecioPlato
            )

        val etStock =
            dialogView.findViewById<TextInputEditText>(
                R.id.etStockPlato
            )

        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.admin_agregar, null)
                .setNegativeButton(R.string.admin_cancelar, null)
                .create()

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener {

                val nombre =
                    etNombre.text.toString().trim()

                val textoPrecio =
                    etPrecio.text.toString().trim()

                val textoStock =
                    etStock.text.toString().trim()

                if (nombre.isBlank()) {
                    etNombre.error =
                        getString(R.string.admin_error_nombre_valido)
                    return@setOnClickListener
                }

                val precio =
                    textoPrecio
                        .replace(",", ".")
                        .toDoubleOrNull()

                if (precio == null || precio <= 1) {
                    etPrecio.error =
                        getString(R.string.admin_error_precio_menu)
                    return@setOnClickListener
                }

                val stock =
                    textoStock.toIntOrNull()

                if (stock == null || stock <= 0) {
                    etStock.error =
                        getString(R.string.admin_error_stock_mayor_cero)
                    return@setOnClickListener
                }

                val nuevoId =
                    (listaMenu.maxOfOrNull { it.id } ?: 0) + 1

                val datos = hashMapOf(
                    "id" to nuevoId,
                    "nombre" to nombre,
                    "precio" to precio,
                    "stock" to stock
                )

                db.collection("menu")
                    .document(nuevoId.toString())
                    .set(datos)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            getString(
                                R.string.admin_plato_agregado_stock,
                                stock
                            ),
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            "FIREBASE",
                            "Error agregando plato",
                            error
                        )

                        Toast.makeText(
                            this,
                            R.string.admin_error_guardar_plato,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }


    // =========================================================
    // EDITAR MENÚ
    // =========================================================

    private fun mostrarDialogoEdicionMenu(posicion: Int) {

        if (posicion !in listaMenu.indices) return

        val item = listaMenu[posicion]

        val dialogView =
            LayoutInflater.from(this).inflate(
                R.layout.dialog_editar_menu,
                null
            )

        val etNombre =
            dialogView.findViewById<TextInputEditText>(
                R.id.etNombrePlato
            )

        val etPrecio =
            dialogView.findViewById<TextInputEditText>(
                R.id.etPrecioPlato
            )

        val etStock =
            dialogView.findViewById<TextInputEditText>(
                R.id.etStockPlato
            )

        etNombre.setText(item.name)

        etPrecio.setText(
            String.format(Locale.US, "%.2f", item.precio)
        )

        etStock.setText(item.stock.toString())

        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.admin_guardar, null)
                .setNegativeButton(R.string.admin_cancelar, null)
                .create()

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener {

                val nombre =
                    etNombre.text.toString().trim()

                val precio =
                    etPrecio.text.toString()
                        .replace(",", ".")
                        .toDoubleOrNull()

                val stock =
                    etStock.text.toString()
                        .trim()
                        .toIntOrNull()

                if (nombre.isBlank()) {
                    etNombre.error =
                        getString(R.string.admin_error_nombre)
                    return@setOnClickListener
                }

                if (precio == null || precio <= 1) {
                    etPrecio.error =
                        getString(R.string.admin_error_precio_menu)
                    return@setOnClickListener
                }

                if (stock == null || stock < 0) {
                    etStock.error =
                        getString(R.string.admin_error_cantidad_invalida)
                    return@setOnClickListener
                }

                val datos = hashMapOf(
                    "id" to item.id,
                    "nombre" to nombre,
                    "precio" to precio,
                    "stock" to stock
                )

                db.collection("menu")
                    .document(item.id.toString())
                    .set(datos)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            R.string.admin_plato_actualizado,
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            "FIREBASE",
                            "Error actualizando plato",
                            error
                        )

                        Toast.makeText(
                            this,
                            R.string.admin_error_actualizar,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }


    // =========================================================
    // ELIMINAR MENÚ
    // =========================================================

    private fun eliminarElementoMenu(posicion: Int) {

        if (posicion !in listaMenu.indices) return

        val plato = listaMenu[posicion]

        AlertDialog.Builder(this)
            .setTitle(R.string.admin_eliminar_plato)
            .setMessage(
                getString(
                    R.string.admin_confirmar_eliminar,
                    plato.name
                )
            )
            .setNegativeButton(R.string.admin_cancelar, null)
            .setPositiveButton(R.string.admin_eliminar) { _, _ ->

                db.collection("menu")
                    .document(plato.id.toString())
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            R.string.admin_plato_eliminado,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            "FIREBASE",
                            "Error eliminando plato",
                            error
                        )

                        Toast.makeText(
                            this,
                            R.string.admin_error_eliminar,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .show()
    }


    // =========================================================
    // CARGAR MENÚ DESDE FIRESTORE
    // =========================================================

    private fun cargarDatosDesdeFirebase() {

        progressBarMenu.visibility = View.VISIBLE

        listenerMenu =
            db.collection("menu")
                .addSnapshotListener { resultado, error ->

                    if (error != null) {
                        Log.e(
                            "FIREBASE",
                            "Error escuchando menú",
                            error
                        )

                        progressBarMenu.visibility = View.GONE
                        return@addSnapshotListener
                    }

                    if (resultado == null) {
                        progressBarMenu.visibility = View.GONE
                        return@addSnapshotListener
                    }

                    val nuevosPlatos =
                        mutableListOf<TaskMenu>()

                    for (documento in resultado) {

                        val id =
                            documento.getLong("id")?.toInt()
                                ?: documento.id.toIntOrNull()
                                ?: 0

                        val nombre =
                            documento.getString("nombre") ?: ""

                        val precio =
                            (documento.get("precio") as? Number)
                                ?.toDouble() ?: 0.0

                        val stock =
                            documento.getLong("stock")?.toInt()
                                ?: 0

                        if (id > 0 && nombre.isNotEmpty()) {
                            nuevosPlatos.add(
                                TaskMenu(
                                    id = id,
                                    name = nombre,
                                    precio = precio,
                                    stock = stock
                                )
                            )
                        }
                    }

                    actualizarLista(
                        destino = listaMenu,
                        nuevosElementos = nuevosPlatos,
                        adapter = menuAdapter
                    )

                    progressBarMenu.visibility = View.GONE

                    Log.d(
                        "FIREBASE",
                        "Menú actualizado: ${listaMenu.size}"
                    )
                }
    }


    // =========================================================
    // AGREGAR ENTRADA
    // =========================================================

    private fun mostrarDialogoAgregarEntrada() {

        val dialogView =
            LayoutInflater.from(this).inflate(
                R.layout.dialog_agregar_entrada,
                null
            )

        val etNombre =
            dialogView.findViewById<TextInputEditText>(
                R.id.etNombreEntrada
            )

        val etStock =
            dialogView.findViewById<TextInputEditText>(
                R.id.etStockEntrada
            )

        val switchDisponible =
            dialogView.findViewById<SwitchMaterial>(
                R.id.switchDisponible
            )

        switchDisponible.isChecked = true

        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.admin_agregar, null)
                .setNegativeButton(R.string.admin_cancelar, null)
                .create()

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener {

                val nombre =
                    etNombre.text.toString().trim()

                val textoStock =
                    etStock.text.toString().trim()

                if (nombre.isBlank()) {
                    etNombre.error =
                        getString(R.string.admin_error_nombre_valido)
                    return@setOnClickListener
                }

                val stock = textoStock.toIntOrNull()

                if (stock == null || stock <= 0) {
                    etStock.error =
                        getString(R.string.admin_error_stock_mayor_cero)
                    return@setOnClickListener
                }

                val nuevoId =
                    (entradas.maxOfOrNull { it.id } ?: 0) + 1

                val datos = hashMapOf(
                    "id" to nuevoId,
                    "nombre" to nombre,
                    "disponible" to switchDisponible.isChecked,
                    "stock" to stock
                )

                db.collection("entradas")
                    .document(nuevoId.toString())
                    .set(datos)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            getString(
                                R.string.admin_entrada_agregada_stock,
                                stock
                            ),
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            "FIREBASE",
                            "Error guardando entrada",
                            error
                        )

                        Toast.makeText(
                            this,
                            R.string.admin_error_guardar_entrada,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }


    // =========================================================
    // CARGAR ENTRADAS DESDE FIRESTORE
    // =========================================================

    private fun cargarEntradasDesdeFirebase() {

        listenerEntradas =
            db.collection("entradas")
                .addSnapshotListener { resultado, error ->

                    if (error != null) {
                        Log.e(
                            "FIREBASE",
                            "Error escuchando entradas",
                            error
                        )
                        return@addSnapshotListener
                    }

                    if (resultado == null) return@addSnapshotListener

                    val nuevasEntradas =
                        mutableListOf<TaskEntradas>()

                    for (documento in resultado) {

                        val id =
                            documento.getLong("id")?.toInt()
                                ?: documento.id.toIntOrNull()
                                ?: 0

                        val nombre =
                            documento.getString("nombre") ?: ""

                        val disponible =
                            documento.getBoolean("disponible") ?: true

                        val stock =
                            documento.getLong("stock")?.toInt()
                                ?: 0

                        if (id > 0 && nombre.isNotEmpty()) {

                            val entrada =
                                when (id) {
                                    1 -> TaskEntradas.Ceviche(
                                        id = id,
                                        nombre = nombre,
                                        disponible = disponible,
                                        stock = stock
                                    )

                                    2 -> TaskEntradas.Huancaina(
                                        id = id,
                                        nombre = nombre,
                                        disponible = disponible,
                                        stock = stock
                                    )

                                    else -> TaskEntradas.Otros(
                                        id = id,
                                        nombre = nombre,
                                        disponible = disponible,
                                        stock = stock
                                    )
                                }

                            nuevasEntradas.add(entrada)
                        }
                    }

                    actualizarLista(
                        destino = entradas,
                        nuevosElementos = nuevasEntradas,
                        adapter = entradasAdapter
                    )

                    Log.d(
                        "FIREBASE",
                        "Entradas actualizadas: ${entradas.size}"
                    )
                }
    }


    // =========================================================
    // EDITAR ENTRADA
    // =========================================================

    private fun mostrarDialogoEdicionEntrada(posicion: Int) {

        if (posicion !in entradas.indices) return

        val entrada = entradas[posicion]

        val dialogView =
            LayoutInflater.from(this).inflate(
                R.layout.dialog_editar_entrada,
                null
            )

        val etNombre =
            dialogView.findViewById<TextInputEditText>(
                R.id.etNombreEntrada
            )

        val etStock =
            dialogView.findViewById<TextInputEditText>(
                R.id.etStockEntrada
            )

        val switchDisponible =
            dialogView.findViewById<SwitchMaterial>(
                R.id.switchDisponible
            )

        etNombre.setText(entrada.nombre)
        etStock.setText(entrada.stock.toString())
        switchDisponible.isChecked = entrada.disponible

        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.admin_guardar, null)
                .setNegativeButton(R.string.admin_eliminar, null)
                .create()

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener {

                val nombre =
                    etNombre.text.toString().trim()

                val stock =
                    etStock.text.toString().trim().toIntOrNull()

                if (nombre.isBlank()) {
                    etNombre.error =
                        getString(R.string.admin_error_nombre_valido)
                    return@setOnClickListener
                }

                if (stock == null || stock < 0) {
                    etStock.error =
                        getString(R.string.admin_error_cantidad_invalida)
                    return@setOnClickListener
                }

                val datos = hashMapOf(
                    "id" to entrada.id,
                    "nombre" to nombre,
                    "disponible" to switchDisponible.isChecked,
                    "stock" to stock
                )

                db.collection("entradas")
                    .document(entrada.id.toString())
                    .set(datos)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            getString(
                                R.string.admin_entrada_actualizada_stock,
                                stock
                            ),
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            "FIREBASE",
                            "Error actualizando entrada",
                            error
                        )

                        Toast.makeText(
                            this,
                            R.string.admin_error_actualizar,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setOnClickListener {

                AlertDialog.Builder(this)
                    .setTitle(R.string.admin_eliminar_entrada)
                    .setMessage(
                        getString(
                            R.string.admin_confirmar_eliminar,
                            entrada.nombre
                        )
                    )
                    .setNegativeButton(R.string.admin_cancelar, null)
                    .setPositiveButton(R.string.admin_eliminar) { _, _ ->

                        db.collection("entradas")
                            .document(entrada.id.toString())
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    R.string.admin_entrada_eliminada,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { error ->
                                Log.e(
                                    "FIREBASE",
                                    "Error eliminando entrada",
                                    error
                                )

                                Toast.makeText(
                                    this,
                                    R.string.admin_error_eliminar,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        dialog.dismiss()
                    }
                    .show()
            }
    }


    // =========================================================
    // AGREGAR EXTRA
    // =========================================================

    private fun mostrarDialogoAgregarExtra() {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                resources.getDimensionPixelSize(
                    R.dimen.admin_dialog_padding_horizontal
                ),
                resources.getDimensionPixelSize(
                    R.dimen.admin_dialog_padding_top
                ),
                resources.getDimensionPixelSize(
                    R.dimen.admin_dialog_padding_horizontal
                ),
                resources.getDimensionPixelSize(
                    R.dimen.admin_dialog_padding_bottom
                )
            )
        }

        val etNombre = TextInputEditText(this).apply {
            hint = getString(R.string.admin_nombre_extra)
        }
        layout.addView(etNombre)

        val etPrecio = TextInputEditText(this).apply {
            hint = getString(R.string.admin_precio)
            inputType = InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        layout.addView(etPrecio)

        val spinnerCategoria = Spinner(this)

        val categorias =
            resources.getStringArray(
                R.array.admin_categorias_extras
            )

        spinnerCategoria.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categorias
            )

        layout.addView(spinnerCategoria)

        val dialog =
            AlertDialog.Builder(this)
                .setTitle(R.string.admin_agregar_extra)
                .setView(layout)
                .setPositiveButton(R.string.admin_guardar, null)
                .setNegativeButton(R.string.admin_cancelar, null)
                .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener {

                val nombre = etNombre.text.toString().trim()

                val precio =
                    etPrecio.text.toString()
                        .replace(",", ".")
                        .toDoubleOrNull()

                if (nombre.isBlank()) {
                    etNombre.error =
                        getString(R.string.admin_error_nombre)
                    return@setOnClickListener
                }

                if (precio == null || precio <= 0) {
                    etPrecio.error =
                        getString(R.string.admin_error_precio_valido)
                    return@setOnClickListener
                }

                val categoriaId =
                    spinnerCategoria.selectedItemPosition + 1

                val icono =
                    obtenerIconoPorCategoria(categoriaId)

                val nuevoId =
                    (listaExtras.maxOfOrNull { it.id } ?: 0) + 1

                val datos = hashMapOf(
                    "id" to nuevoId,
                    "nombre" to nombre,
                    "precio" to precio,
                    "categoriaId" to categoriaId,
                    "icono" to icono
                )

                db.collection("extras")
                    .document(nuevoId.toString())
                    .set(datos)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            R.string.admin_extra_agregado,
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            "FIREBASE",
                            "Error agregando extra",
                            error
                        )

                        Toast.makeText(
                            this,
                            R.string.admin_error_guardar_extra,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }


    // =========================================================
    // ICONO SEGÚN CATEGORÍA
    // =========================================================

    private fun obtenerIconoPorCategoria(categoriaId: Int): Int {

        return when (categoriaId) {
            1 -> R.drawable.ic_gaseosa
            2 -> R.drawable.ic_torta
            3 -> R.drawable.ic_plato
            else -> R.drawable.ic_plato
        }
    }


    // =========================================================
    // CARGAR EXTRAS
    // =========================================================

    private fun cargarExtrasDesdeFirebase() {

        listenerExtras =
            db.collection("extras")
                .addSnapshotListener { resultado, error ->

                    if (error != null) {
                        Log.e(
                            "FIREBASE",
                            "Error escuchando extras",
                            error
                        )
                        return@addSnapshotListener
                    }

                    if (resultado == null) return@addSnapshotListener

                    val nuevosExtras =
                        mutableListOf<ExtraItem>()

                    for (documento in resultado) {

                        val id =
                            documento.getLong("id")?.toInt()
                                ?: documento.id.toIntOrNull()
                                ?: 0

                        val nombre =
                            documento.getString("nombre") ?: ""

                        val precio =
                            (documento.get("precio") as? Number)
                                ?.toDouble() ?: 0.0

                        val categoriaId =
                            documento.getLong("categoriaId")?.toInt()
                                ?: 1

                        val icono =
                            obtenerIconoPorCategoria(categoriaId)

                        if (id > 0 && nombre.isNotEmpty()) {
                            nuevosExtras.add(
                                ExtraItem(
                                    id = id,
                                    nombre = nombre,
                                    precio = precio,
                                    categoriaId = categoriaId,
                                    icono = icono
                                )
                            )
                        }
                    }

                    actualizarLista(
                        destino = listaExtras,
                        nuevosElementos = nuevosExtras,
                        adapter = extraAdminAdapter
                    )

                    Log.d(
                        "FIREBASE",
                        "Extras actualizados: ${listaExtras.size}"
                    )
                }
    }


    // =========================================================
    // EDITAR EXTRA
    // =========================================================

    private fun mostrarDialogoEdicionExtra(posicion: Int) {

        if (posicion !in listaExtras.indices) return

        val extra = listaExtras[posicion]

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                resources.getDimensionPixelSize(
                    R.dimen.admin_dialog_padding_horizontal
                ),
                resources.getDimensionPixelSize(
                    R.dimen.admin_dialog_padding_top
                ),
                resources.getDimensionPixelSize(
                    R.dimen.admin_dialog_padding_horizontal
                ),
                resources.getDimensionPixelSize(
                    R.dimen.admin_dialog_padding_bottom
                )
            )
        }

        val etNombre = TextInputEditText(this).apply {
            hint = getString(R.string.admin_nombre_extra)
            setText(extra.nombre)
        }
        layout.addView(etNombre)

        val etPrecio = TextInputEditText(this).apply {
            hint = getString(R.string.admin_precio)
            inputType = InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(
                String.format(Locale.US, "%.2f", extra.precio)
            )
        }
        layout.addView(etPrecio)

        val categorias =
            resources.getStringArray(
                R.array.admin_categorias_extras
            )

        val spinnerCategoria = Spinner(this)

        spinnerCategoria.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categorias
            )

        val seleccion = extra.categoriaId - 1

        if (seleccion in categorias.indices) {
            spinnerCategoria.setSelection(seleccion)
        }

        layout.addView(spinnerCategoria)

        val dialog =
            AlertDialog.Builder(this)
                .setTitle(R.string.admin_editar_extra)
                .setView(layout)
                .setPositiveButton(R.string.admin_guardar, null)
                .setNegativeButton(R.string.admin_cancelar, null)
                .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener {

                val nombre = etNombre.text.toString().trim()

                val precio =
                    etPrecio.text.toString()
                        .replace(",", ".")
                        .toDoubleOrNull()

                if (nombre.isBlank()) {
                    etNombre.error =
                        getString(R.string.admin_error_nombre)
                    return@setOnClickListener
                }

                if (precio == null || precio <= 0) {
                    etPrecio.error =
                        getString(R.string.admin_error_precio_valido)
                    return@setOnClickListener
                }

                val categoriaId =
                    spinnerCategoria.selectedItemPosition + 1

                val datos = hashMapOf(
                    "id" to extra.id,
                    "nombre" to nombre,
                    "precio" to precio,
                    "categoriaId" to categoriaId,
                    "icono" to obtenerIconoPorCategoria(categoriaId)
                )

                db.collection("extras")
                    .document(extra.id.toString())
                    .set(datos)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            R.string.admin_extra_actualizado,
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            "FIREBASE",
                            "Error actualizando extra",
                            error
                        )

                        Toast.makeText(
                            this,
                            R.string.admin_error_actualizar,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }


    // =========================================================
    // ELIMINAR EXTRA
    // =========================================================

    private fun eliminarExtra(posicion: Int) {

        if (posicion !in listaExtras.indices) return

        val extra = listaExtras[posicion]

        AlertDialog.Builder(this)
            .setTitle(R.string.admin_eliminar_extra)
            .setMessage(
                getString(
                    R.string.admin_confirmar_eliminar,
                    extra.nombre
                )
            )
            .setNegativeButton(R.string.admin_cancelar, null)
            .setPositiveButton(R.string.admin_eliminar) { _, _ ->

                db.collection("extras")
                    .document(extra.id.toString())
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            R.string.admin_extra_eliminado,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            "FIREBASE",
                            "Error eliminando extra",
                            error
                        )

                        Toast.makeText(
                            this,
                            R.string.admin_error_eliminar,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .show()
    }


    // =========================================================
    // CENAS: AGREGAR
    // =========================================================

    private fun mostrarDialogoAgregarCena() {

        val dialogView =
            LayoutInflater.from(this).inflate(
                R.layout.dialog_agregar_cena,
                null
            )

        val etNombre =
            dialogView.findViewById<TextInputEditText>(
                R.id.etNombreCena
            )

        val etPrecio =
            dialogView.findViewById<TextInputEditText>(
                R.id.etPrecioCena
            )

        val etStock =
            dialogView.findViewById<TextInputEditText>(
                R.id.etStockCena
            )

        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(
                    R.string.admin_agregar,
                    null
                )
                .setNegativeButton(
                    R.string.admin_cancelar,
                    null
                )
                .create()

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.getButton(
            AlertDialog.BUTTON_POSITIVE
        ).setOnClickListener {

            val nombre =
                etNombre.text.toString().trim()

            val precio =
                etPrecio.text.toString()
                    .replace(",", ".")
                    .toDoubleOrNull()

            val stock =
                etStock.text.toString()
                    .trim()
                    .toIntOrNull()

            if (nombre.isBlank()) {
                etNombre.error =
                    getString(R.string.admin_error_nombre)
                etNombre.requestFocus()
                return@setOnClickListener
            }

            if (precio == null || !precio.isFinite() || precio <= 0) {
                etPrecio.error =
                    getString(R.string.admin_error_precio_valido)
                etPrecio.requestFocus()
                return@setOnClickListener
            }

            if (stock == null || stock <= 0) {
                etStock.error =
                    getString(R.string.admin_error_stock_mayor_cero)
                etStock.requestFocus()
                return@setOnClickListener
            }

            val nuevoId =
                (listaCenas.maxOfOrNull { it.id } ?: 0) + 1

            val datos = hashMapOf(
                "id" to nuevoId,
                "nombre" to nombre,
                "precio" to precio,
                "stock" to stock
            )

            db.collection("cenas")
                .document(nuevoId.toString())
                .set(datos)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        getString(
                            R.string.admin_cena_agregada_stock,
                            stock
                        ),
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                }
                .addOnFailureListener { error ->

                    Log.e(
                        "FIREBASE",
                        "Error agregando cena",
                        error
                    )

                    Toast.makeText(
                        this,
                        R.string.admin_error_guardar_cena,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


    // =========================================================
    // CENAS: CARGAR DESDE FIRESTORE
    // =========================================================

    private fun cargarCenasDesdeFirebase() {

        listenerCenas =
            db.collection("cenas")
                .addSnapshotListener { resultado, error ->

                    if (error != null) {
                        Log.e(
                            "FIREBASE",
                            "Error escuchando cenas",
                            error
                        )
                        return@addSnapshotListener
                    }

                    if (resultado == null) {
                        return@addSnapshotListener
                    }

                    val nuevasCenas =
                        mutableListOf<TaskCenas>()

                    for (documento in resultado) {

                        val id =
                            documento.getLong("id")?.toInt()
                                ?: documento.id.toIntOrNull()
                                ?: 0

                        val nombre =
                            documento.getString("nombre") ?: ""

                        val precio =
                            (documento.get("precio") as? Number)
                                ?.toDouble() ?: 0.0

                        val stock =
                            documento.getLong("stock")?.toInt()
                                ?: 0

                        if (id > 0 && nombre.isNotBlank()) {
                            nuevasCenas.add(
                                TaskCenas(
                                    id = id,
                                    name = nombre,
                                    precio = precio,
                                    stock = stock
                                )
                            )
                        }
                    }

                    actualizarLista(
                        destino = listaCenas,
                        nuevosElementos = nuevasCenas,
                        adapter = cenasAdminAdapter
                    )

                    Log.d(
                        "FIREBASE",
                        "Cenas actualizadas: ${listaCenas.size}"
                    )
                }
    }


    // =========================================================
    // CENAS: EDITAR
    // =========================================================

    private fun mostrarDialogoEdicionCena(posicion: Int) {

        if (posicion !in listaCenas.indices) return

        val cena = listaCenas[posicion]

        val layout =
            crearContenedorCamposCena()

        val etNombre =
            crearCampoCena(
                R.string.admin_nombre_cena,
                InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            )

        etNombre.setText(cena.name)
        layout.addView(etNombre)

        val etPrecio =
            crearCampoCena(
                R.string.admin_precio_cena,
                InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
            )

        etPrecio.setText(
            String.format(Locale.US, "%.2f", cena.precio)
        )
        layout.addView(etPrecio)

        val etStock =
            crearCampoCena(
                R.string.admin_stock_cena,
                InputType.TYPE_CLASS_NUMBER
            )

        etStock.setText(cena.stock.toString())
        layout.addView(etStock)

        val dialog =
            AlertDialog.Builder(this)
                .setTitle(R.string.admin_editar_cena)
                .setView(layout)
                .setPositiveButton(R.string.admin_guardar, null)
                .setNegativeButton(R.string.admin_cancelar, null)
                .create()

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener {

                val nombre =
                    etNombre.text.toString().trim()

                val precio =
                    etPrecio.text.toString()
                        .replace(",", ".")
                        .toDoubleOrNull()

                val stock =
                    etStock.text.toString()
                        .trim()
                        .toIntOrNull()

                if (nombre.isBlank()) {
                    etNombre.error =
                        getString(R.string.admin_error_nombre)
                    return@setOnClickListener
                }

                if (precio == null || precio <= 0) {
                    etPrecio.error =
                        getString(R.string.admin_error_precio_valido)
                    return@setOnClickListener
                }

                if (stock == null || stock < 0) {
                    etStock.error =
                        getString(R.string.admin_error_cantidad_invalida)
                    return@setOnClickListener
                }

                val datos = hashMapOf(
                    "id" to cena.id,
                    "nombre" to nombre,
                    "precio" to precio,
                    "stock" to stock
                )

                db.collection("cenas")
                    .document(cena.id.toString())
                    .set(datos)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            R.string.admin_cena_actualizada,
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            "FIREBASE",
                            "Error actualizando cena",
                            error
                        )

                        Toast.makeText(
                            this,
                            R.string.admin_error_actualizar_cena,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }


    // =========================================================
    // CENAS: ELIMINAR
    // =========================================================

    private fun eliminarCena(posicion: Int) {

        if (posicion !in listaCenas.indices) return

        val cena = listaCenas[posicion]

        AlertDialog.Builder(this)
            .setTitle(R.string.admin_eliminar_cena)
            .setMessage(
                getString(
                    R.string.admin_confirmar_eliminar_cena,
                    cena.name
                )
            )
            .setNegativeButton(R.string.admin_cancelar, null)
            .setPositiveButton(R.string.admin_eliminar) { _, _ ->

                db.collection("cenas")
                    .document(cena.id.toString())
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            R.string.admin_cena_eliminada,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            "FIREBASE",
                            "Error eliminando cena",
                            error
                        )

                        Toast.makeText(
                            this,
                            R.string.admin_error_eliminar_cena,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .show()
    }


    // =========================================================
    // CENAS: CREAR CAMPOS
    // =========================================================

    private fun crearContenedorCamposCena(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                resources.getDimensionPixelSize(
                    R.dimen.admin_dialog_padding_horizontal
                ),
                resources.getDimensionPixelSize(
                    R.dimen.admin_dialog_padding_top
                ),
                resources.getDimensionPixelSize(
                    R.dimen.admin_dialog_padding_horizontal
                ),
                resources.getDimensionPixelSize(
                    R.dimen.admin_dialog_padding_bottom
                )
            )
        }
    }

    private fun crearCampoCena(
        hintRes: Int,
        tipoEntrada: Int
    ): TextInputEditText {

        return TextInputEditText(this).apply {
            hint = getString(hintRes)
            inputType = tipoEntrada

            layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin =
                        resources.getDimensionPixelSize(
                            R.dimen.admin_dialog_padding_top
                        ) / 2
                }
        }
    }


    // =========================================================
    // ACTUALIZAR LISTAS
    // =========================================================

    private fun <T> actualizarLista(
        destino: MutableList<T>,
        nuevosElementos: List<T>,
        adapter: RecyclerView.Adapter<*>
    ) {

        val cantidadAnterior = destino.size

        if (cantidadAnterior > 0) {
            destino.clear()

            adapter.notifyItemRangeRemoved(
                0,
                cantidadAnterior
            )
        }

        if (nuevosElementos.isNotEmpty()) {
            destino.addAll(nuevosElementos)

            adapter.notifyItemRangeInserted(
                0,
                nuevosElementos.size
            )
        }
    }
}