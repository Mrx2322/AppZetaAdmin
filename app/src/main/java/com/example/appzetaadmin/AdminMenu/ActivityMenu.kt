package com.example.appzetaadmin.AdminMenu

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
import com.example.appzetaadmin.AdminMenu.EntradasAdmin.EntradasAdapter
import com.example.appzetaadmin.AdminMenu.EntradasAdmin.TaskEntradas
import com.example.appzetaadmin.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class ActivityMenu : AppCompatActivity() {

    // =========================================================
    // FIREBASE
    // =========================================================

    private val db =
        FirebaseFirestore.getInstance()


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
    // FAB
    // =========================================================

    private lateinit var fabAgregarMenu: FloatingActionButton

    private lateinit var fabPedidos: FloatingActionButton


    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

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


        setContentView(
            R.layout.activity_menu
        )


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
    }


    // =========================================================
    // COMPONENTES
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

        fabAgregarMenu =
            findViewById(R.id.fabAgregarMenu)

        fabPedidos =
            findViewById(R.id.fabPedidos)
    }


    // =========================================================
    // UI
    // =========================================================

    private fun initUI() {

        // =====================================================
        // ENTRADAS
        // =====================================================

        entradasAdapter =
            EntradasAdapter(
                entradas
            ) { posicion ->

                mostrarDialogoEdicionEntrada(
                    posicion
                )
            }

        rvEntradas.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        rvEntradas.adapter =
            entradasAdapter


        // =====================================================
        // EXTRAS
        // =====================================================

        extraAdminAdapter =
            ExtraAdminAdapter(
                listaExtras,

                onEditClick = { posicion ->

                    mostrarDialogoEdicionExtra(
                        posicion
                    )
                },

                onDeleteClick = { posicion ->

                    eliminarExtra(
                        posicion
                    )
                }
            )

        rvExtras.layoutManager =
            LinearLayoutManager(this)

        rvExtras.adapter =
            extraAdminAdapter


        // =====================================================
        // MENÚ
        // =====================================================

        menuAdapter =
            MenuAdapter(
                listaMenu,

                onEditClick = { posicion ->

                    mostrarDialogoEdicionMenu(
                        posicion
                    )
                },

                onDeleteClick = { posicion ->

                    eliminarElementoMenu(
                        posicion
                    )
                }
            )

        rvMenu.layoutManager =
            LinearLayoutManager(this)

        rvMenu.adapter =
            menuAdapter


        // =====================================================
        // FAB
        // =====================================================

        fabAgregarMenu.setOnClickListener {

            mostrarDialogoSeleccionarTipo()
        }


        // =====================================================
        // PEDIDOS
        // =====================================================

        fabPedidos.setOnClickListener {

            val intent =
                Intent(
                    this,
                    ActivityPedidosAdmin::class.java
                )

            startActivity(intent)
        }
    }


    // =========================================================
    // SELECCIONAR QUÉ AGREGAR
    // =========================================================

    private fun mostrarDialogoSeleccionarTipo() {

        val opciones =
            arrayOf(
                "Plato del menú",
                "Entrada",
                "Extra"
            )

        AlertDialog.Builder(this)
            .setTitle(
                "¿Qué deseas agregar?"
            )
            .setItems(
                opciones
            ) { _, cual ->

                when (cual) {

                    0 ->
                        mostrarDialogoAgregarMenu()

                    1 ->
                        mostrarDialogoAgregarEntrada()

                    2 ->
                        mostrarDialogoAgregarExtra()
                }
            }
            .setNegativeButton(
                "Cancelar",
                null
            )
            .show()
    }


    // =========================================================
    // AGREGAR PLATO
    // =========================================================

    private fun mostrarDialogoAgregarMenu() {

        val dialogView =
            LayoutInflater.from(this)
                .inflate(
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
                .setPositiveButton(
                    "Agregar",
                    null
                )
                .setNegativeButton(
                    "Cancelar",
                    null
                )
                .create()


        dialog.show()


        dialog.window?.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )


        dialog.getButton(
            AlertDialog.BUTTON_POSITIVE
        ).setOnClickListener {

            val nombre =
                etNombre.text
                    .toString()
                    .trim()


            val textoPrecio =
                etPrecio.text
                    .toString()
                    .trim()


            val textoStock =
                etStock.text
                    .toString()
                    .trim()


            if (nombre.isEmpty()) {

                etNombre.error =
                    "Escribe un nombre válido"

                return@setOnClickListener
            }


            val precio =
                textoPrecio
                    .replace(",", ".")
                    .toDoubleOrNull()


            if (
                precio == null ||
                precio <= 1
            ) {

                etPrecio.error =
                    "Ingresa el precio con una entrada (mayor a S/ 1)"

                return@setOnClickListener
            }


            val stock =
                textoStock.toIntOrNull()


            if (
                stock == null ||
                stock <= 0
            ) {

                etStock.error =
                    "Ingresa una cantidad mayor a 0"

                return@setOnClickListener
            }


            val nuevoId =
                (
                        listaMenu.maxOfOrNull {
                            it.id
                        } ?: 0
                        ) + 1


            val datos =
                hashMapOf(
                    "id" to nuevoId,
                    "nombre" to nombre,
                    "precio" to precio,
                    "stock" to stock
                )


            db.collection("menu")
                .document(
                    nuevoId.toString()
                )
                .set(datos)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Plato agregado. Stock: $stock",
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
                        "Error al guardar el plato",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


    // =========================================================
    // EDITAR MENÚ
    // =========================================================

    private fun mostrarDialogoEdicionMenu(
        posicion: Int
    ) {

        if (
            posicion < 0 ||
            posicion >= listaMenu.size
        ) {
            return
        }


        val item =
            listaMenu[posicion]


        val dialogView =
            LayoutInflater.from(this)
                .inflate(
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


        etNombre.setText(
            item.name
        )


        etPrecio.setText(
            String.format(
                Locale.US,
                "%.2f",
                item.precio
            )
        )


        etStock.setText(
            item.stock.toString()
        )


        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(
                    "Guardar",
                    null
                )
                .setNegativeButton(
                    "Cancelar",
                    null
                )
                .create()


        dialog.show()


        dialog.window?.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )


        dialog.getButton(
            AlertDialog.BUTTON_POSITIVE
        ).setOnClickListener {

            val nombre =
                etNombre.text
                    .toString()
                    .trim()


            val precio =
                etPrecio.text
                    .toString()
                    .replace(",", ".")
                    .toDoubleOrNull()


            val stock =
                etStock.text
                    .toString()
                    .trim()
                    .toIntOrNull()


            if (nombre.isEmpty()) {

                etNombre.error =
                    "Escribe un nombre"

                return@setOnClickListener
            }


            if (
                precio == null ||
                precio <= 1
            ) {

                etPrecio.error =
                    "Ingresa el precio con una entrada (mayor a S/ 1)"

                return@setOnClickListener
            }


            if (
                stock == null ||
                stock < 0
            ) {

                etStock.error =
                    "Cantidad inválida"

                return@setOnClickListener
            }


            val datos =
                hashMapOf(
                    "id" to item.id,
                    "nombre" to nombre,
                    "precio" to precio,
                    "stock" to stock
                )


            db.collection("menu")
                .document(
                    item.id.toString()
                )
                .set(datos)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Plato actualizado",
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
                        "No se pudo actualizar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


    // =========================================================
    // ELIMINAR MENÚ
    // =========================================================

    private fun eliminarElementoMenu(
        posicion: Int
    ) {

        if (
            posicion < 0 ||
            posicion >= listaMenu.size
        ) {
            return
        }


        val plato =
            listaMenu[posicion]


        AlertDialog.Builder(this)
            .setTitle(
                "Eliminar plato"
            )
            .setMessage(
                "¿Eliminar \"${plato.name}\"?"
            )
            .setNegativeButton(
                "Cancelar",
                null
            )
            .setPositiveButton(
                "Eliminar"
            ) { _, _ ->

                db.collection("menu")
                    .document(
                        plato.id.toString()
                    )
                    .delete()
                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            "Plato eliminado",
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
                            "No se pudo eliminar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .show()
    }


    // =========================================================
    // CARGAR MENÚ FIREBASE
    // =========================================================

    private fun cargarDatosDesdeFirebase() {

        progressBarMenu.visibility =
            View.VISIBLE


        db.collection("menu")
            .addSnapshotListener { resultado, error ->

                if (error != null) {

                    Log.e(
                        "FIREBASE",
                        "Error escuchando menú",
                        error
                    )

                    progressBarMenu.visibility =
                        View.GONE

                    return@addSnapshotListener
                }


                if (resultado == null) {

                    progressBarMenu.visibility =
                        View.GONE

                    return@addSnapshotListener
                }


                listaMenu.clear()


                for (documento in resultado) {

                    val id =
                        documento
                            .getLong("id")
                            ?.toInt()
                            ?: documento.id.toIntOrNull()
                            ?: 0


                    val nombre =
                        documento
                            .getString("nombre")
                            ?: ""


                    val precio =
                        documento
                            .getDouble("precio")
                            ?: 0.0


                    val stock =
                        documento
                            .getLong("stock")
                            ?.toInt()
                            ?: 0


                    if (
                        id > 0 &&
                        nombre.isNotEmpty()
                    ) {

                        listaMenu.add(
                            TaskMenu(
                                id = id,
                                name = nombre,
                                precio = precio,
                                stock = stock
                            )
                        )
                    }
                }


                menuAdapter.notifyDataSetChanged()


                progressBarMenu.visibility =
                    View.GONE


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
            LayoutInflater.from(this)
                .inflate(
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
            dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(
                R.id.switchDisponible
            )


        switchDisponible.isChecked =
            true


        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(
                    "Agregar",
                    null
                )
                .setNegativeButton(
                    "Cancelar",
                    null
                )
                .create()


        dialog.show()


        dialog.window?.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )


        dialog.getButton(
            AlertDialog.BUTTON_POSITIVE
        ).setOnClickListener {

            val nombre =
                etNombre.text
                    .toString()
                    .trim()


            val textoStock =
                etStock.text
                    .toString()
                    .trim()


            if (nombre.isEmpty()) {

                etNombre.error =
                    "Escribe un nombre válido"

                return@setOnClickListener
            }


            val stock =
                textoStock.toIntOrNull()


            if (
                stock == null ||
                stock <= 0
            ) {

                etStock.error =
                    "Ingresa una cantidad mayor a 0"

                return@setOnClickListener
            }


            val nuevoId =
                (
                        entradas.maxOfOrNull {
                            it.id
                        } ?: 0
                        ) + 1


            val datos =
                hashMapOf(
                    "id" to nuevoId,
                    "nombre" to nombre,
                    "disponible" to switchDisponible.isChecked,
                    "stock" to stock
                )


            db.collection("entradas")
                .document(
                    nuevoId.toString()
                )
                .set(datos)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Entrada agregada. Stock: $stock",
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
                        "Error al guardar entrada",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


    // =========================================================
    // CARGAR ENTRADAS FIREBASE
    // =========================================================

    private fun cargarEntradasDesdeFirebase() {

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


                if (resultado == null) {
                    return@addSnapshotListener
                }


                entradas.clear()


                for (documento in resultado) {

                    val id =
                        documento
                            .getLong("id")
                            ?.toInt()
                            ?: documento.id.toIntOrNull()
                            ?: 0


                    val nombre =
                        documento
                            .getString("nombre")
                            ?: ""


                    val disponible =
                        documento
                            .getBoolean("disponible")
                            ?: true


                    val stock =
                        documento
                            .getLong("stock")
                            ?.toInt()
                            ?: 0


                    if (
                        id > 0 &&
                        nombre.isNotEmpty()
                    ) {

                        val entrada =
                            when (id) {

                                1 ->
                                    TaskEntradas.Ceviche(
                                        id = id,
                                        nombre = nombre,
                                        disponible = disponible,
                                        stock = stock
                                    )

                                2 ->
                                    TaskEntradas.Huancaina(
                                        id = id,
                                        nombre = nombre,
                                        disponible = disponible,
                                        stock = stock
                                    )

                                else ->
                                    TaskEntradas.Otros(
                                        id = id,
                                        nombre = nombre,
                                        disponible = disponible,
                                        stock = stock
                                    )
                            }


                        entradas.add(
                            entrada
                        )
                    }
                }


                entradasAdapter.notifyDataSetChanged()


                Log.d(
                    "FIREBASE",
                    "Entradas actualizadas: ${entradas.size}"
                )
            }
    }


    // =========================================================
    // EDITAR ENTRADA
    // =========================================================

    private fun mostrarDialogoEdicionEntrada(
        posicion: Int
    ) {

        if (
            posicion < 0 ||
            posicion >= entradas.size
        ) {
            return
        }


        val entrada =
            entradas[posicion]


        val dialogView =
            LayoutInflater.from(this)
                .inflate(
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
            dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(
                R.id.switchDisponible
            )


        etNombre.setText(
            entrada.nombre
        )


        etStock.setText(
            entrada.stock.toString()
        )


        switchDisponible.isChecked =
            entrada.disponible


        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(
                    "Guardar",
                    null
                )
                .setNegativeButton(
                    "Eliminar",
                    null
                )
                .create()


        dialog.show()


        dialog.window?.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )


        dialog.getButton(
            AlertDialog.BUTTON_POSITIVE
        ).setOnClickListener {

            val nombre =
                etNombre.text
                    .toString()
                    .trim()


            val stock =
                etStock.text
                    .toString()
                    .trim()
                    .toIntOrNull()


            if (nombre.isEmpty()) {

                etNombre.error =
                    "Escribe un nombre válido"

                return@setOnClickListener
            }


            if (
                stock == null ||
                stock < 0
            ) {

                etStock.error =
                    "Cantidad inválida"

                return@setOnClickListener
            }


            val datos =
                hashMapOf(
                    "id" to entrada.id,
                    "nombre" to nombre,
                    "disponible" to switchDisponible.isChecked,
                    "stock" to stock
                )


            db.collection("entradas")
                .document(
                    entrada.id.toString()
                )
                .set(datos)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Entrada actualizada. Stock: $stock",
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
                        "No se pudo actualizar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }


        dialog.getButton(
            AlertDialog.BUTTON_NEGATIVE
        ).setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle(
                    "Eliminar entrada"
                )
                .setMessage(
                    "¿Eliminar \"${entrada.nombre}\"?"
                )
                .setNegativeButton(
                    "Cancelar",
                    null
                )
                .setPositiveButton(
                    "Eliminar"
                ) { _, _ ->

                    db.collection("entradas")
                        .document(
                            entrada.id.toString()
                        )
                        .delete()
                        .addOnSuccessListener {

                            Toast.makeText(
                                this,
                                "Entrada eliminada",
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
                                "No se pudo eliminar",
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

        val layout =
            LinearLayout(this)

        layout.orientation =
            LinearLayout.VERTICAL

        layout.setPadding(
            50,
            20,
            50,
            10
        )


        val etNombre =
            TextInputEditText(this)

        etNombre.hint =
            "Nombre del extra"

        layout.addView(
            etNombre
        )


        val etPrecio =
            TextInputEditText(this)

        etPrecio.hint =
            "Precio"

        etPrecio.inputType =
            InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL

        layout.addView(
            etPrecio
        )


        val spinnerCategoria =
            Spinner(this)


        val categorias =
            arrayOf(
                "Gaseosas",
                "Tortas",
                "Platos"
            )


        val categoriaAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categorias
            )


        spinnerCategoria.adapter =
            categoriaAdapter


        layout.addView(
            spinnerCategoria
        )


        val dialog =
            AlertDialog.Builder(this)
                .setTitle(
                    "Agregar extra"
                )
                .setView(layout)
                .setPositiveButton(
                    "Guardar",
                    null
                )
                .setNegativeButton(
                    "Cancelar",
                    null
                )
                .create()


        dialog.show()


        dialog.getButton(
            AlertDialog.BUTTON_POSITIVE
        ).setOnClickListener {

            val nombre =
                etNombre.text
                    .toString()
                    .trim()


            val precio =
                etPrecio.text
                    .toString()
                    .replace(",", ".")
                    .toDoubleOrNull()


            if (nombre.isEmpty()) {

                etNombre.error =
                    "Escribe un nombre"

                return@setOnClickListener
            }


            if (
                precio == null ||
                precio <= 0
            ) {

                etPrecio.error =
                    "Ingresa un precio válido"

                return@setOnClickListener
            }


            val categoriaId =
                spinnerCategoria.selectedItemPosition + 1


            val icono =
                obtenerIconoPorCategoria(
                    categoriaId
                )


            val nuevoId =
                (
                        listaExtras.maxOfOrNull {
                            it.id
                        } ?: 0
                        ) + 1


            val datos =
                hashMapOf(
                    "id" to nuevoId,
                    "nombre" to nombre,
                    "precio" to precio,
                    "categoriaId" to categoriaId,
                    "icono" to icono
                )


            db.collection("extras")
                .document(
                    nuevoId.toString()
                )
                .set(datos)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Extra agregado",
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
                        "No se pudo guardar el extra",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


    // =========================================================
    // ICONO SEGÚN CATEGORÍA
    // =========================================================

    private fun obtenerIconoPorCategoria(
        categoriaId: Int
    ): Int {

        return when (categoriaId) {

            1 ->
                R.drawable.ic_gaseosa

            2 ->
                R.drawable.ic_torta

            3 ->
                R.drawable.ic_plato

            else ->
                R.drawable.ic_plato
        }
    }


    // =========================================================
    // CARGAR EXTRAS
    // =========================================================

    private fun cargarExtrasDesdeFirebase() {

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


                if (resultado == null) {
                    return@addSnapshotListener
                }


                listaExtras.clear()


                for (documento in resultado) {

                    val id =
                        documento
                            .getLong("id")
                            ?.toInt()
                            ?: documento.id.toIntOrNull()
                            ?: 0


                    val nombre =
                        documento
                            .getString("nombre")
                            ?: ""


                    val precio =
                        documento
                            .getDouble("precio")
                            ?: 0.0


                    val categoriaId =
                        documento
                            .getLong("categoriaId")
                            ?.toInt()
                            ?: 1


                    val icono =
                        obtenerIconoPorCategoria(
                            categoriaId
                        )


                    if (
                        id > 0 &&
                        nombre.isNotEmpty()
                    ) {

                        listaExtras.add(
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


                extraAdminAdapter
                    .notifyDataSetChanged()


                Log.d(
                    "FIREBASE",
                    "Extras actualizados: ${listaExtras.size}"
                )
            }
    }


    // =========================================================
    // EDITAR EXTRA
    // =========================================================

    private fun mostrarDialogoEdicionExtra(
        posicion: Int
    ) {

        if (
            posicion < 0 ||
            posicion >= listaExtras.size
        ) {
            return
        }


        val extra =
            listaExtras[posicion]


        val layout =
            LinearLayout(this)

        layout.orientation =
            LinearLayout.VERTICAL

        layout.setPadding(
            50,
            20,
            50,
            10
        )


        val etNombre =
            TextInputEditText(this)

        etNombre.hint =
            "Nombre del extra"

        etNombre.setText(
            extra.nombre
        )

        layout.addView(
            etNombre
        )


        val etPrecio =
            TextInputEditText(this)

        etPrecio.hint =
            "Precio"

        etPrecio.inputType =
            InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL

        etPrecio.setText(
            String.format(
                Locale.US,
                "%.2f",
                extra.precio
            )
        )

        layout.addView(
            etPrecio
        )


        val spinnerCategoria =
            Spinner(this)


        val categorias =
            arrayOf(
                "Gaseosas",
                "Tortas",
                "Platos"
            )


        val categoriaAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categorias
            )


        spinnerCategoria.adapter =
            categoriaAdapter


        val categoriaSeleccionada =
            extra.categoriaId - 1


        if (
            categoriaSeleccionada >= 0 &&
            categoriaSeleccionada < categorias.size
        ) {

            spinnerCategoria.setSelection(
                categoriaSeleccionada
            )
        }


        layout.addView(
            spinnerCategoria
        )


        val dialog =
            AlertDialog.Builder(this)
                .setTitle(
                    "Editar extra"
                )
                .setView(layout)
                .setPositiveButton(
                    "Guardar",
                    null
                )
                .setNegativeButton(
                    "Cancelar",
                    null
                )
                .create()


        dialog.show()


        dialog.getButton(
            AlertDialog.BUTTON_POSITIVE
        ).setOnClickListener {

            val nombre =
                etNombre.text
                    .toString()
                    .trim()


            val precio =
                etPrecio.text
                    .toString()
                    .replace(",", ".")
                    .toDoubleOrNull()


            if (nombre.isEmpty()) {

                etNombre.error =
                    "Escribe un nombre"

                return@setOnClickListener
            }


            if (
                precio == null ||
                precio <= 0
            ) {

                etPrecio.error =
                    "Ingresa un precio válido"

                return@setOnClickListener
            }


            val categoriaId =
                spinnerCategoria.selectedItemPosition + 1


            val icono =
                obtenerIconoPorCategoria(
                    categoriaId
                )


            val datos =
                hashMapOf(
                    "id" to extra.id,
                    "nombre" to nombre,
                    "precio" to precio,
                    "categoriaId" to categoriaId,
                    "icono" to icono
                )


            db.collection("extras")
                .document(
                    extra.id.toString()
                )
                .set(datos)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Extra actualizado",
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
                        "No se pudo actualizar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


    // =========================================================
    // ELIMINAR EXTRA
    // =========================================================

    private fun eliminarExtra(
        posicion: Int
    ) {

        if (
            posicion < 0 ||
            posicion >= listaExtras.size
        ) {
            return
        }


        val extra =
            listaExtras[posicion]


        AlertDialog.Builder(this)
            .setTitle(
                "Eliminar extra"
            )
            .setMessage(
                "¿Deseas eliminar \"${extra.nombre}\"?"
            )
            .setNegativeButton(
                "Cancelar",
                null
            )
            .setPositiveButton(
                "Eliminar"
            ) { _, _ ->

                db.collection("extras")
                    .document(
                        extra.id.toString()
                    )
                    .delete()
                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            "Extra eliminado",
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
                            "No se pudo eliminar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .show()
    }
}
