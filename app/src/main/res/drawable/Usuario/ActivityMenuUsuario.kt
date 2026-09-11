package com.example.appzetar.Usuario

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
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
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetar.AdminMenu.EntradasAdmin.TaskEntradas
import com.example.appzetar.AdminMenu.TaskMenu
import com.example.appzetar.R
import com.example.appzetar.Usuario.Carrito.ActivityPedido
import com.example.appzetar.Usuario.Carrito.EntradaPedido
import com.example.appzetar.Usuario.Carrito.PedidoItem
import com.example.appzetar.Usuario.Carrito.PedidoManager
import com.example.appzetar.Usuario.Carrito.ReglasPrecioPedido
import com.example.appzetar.Usuario.Carrito.TipoPedido
import com.example.appzetar.Usuario.Entradas.EntradasUsuarioAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs


class ActivityMenuUsuario : AppCompatActivity() {

    // =========================================================
    // FIREBASE
    // =========================================================

    private val db =
        FirebaseFirestore.getInstance()

    private val auth =
        FirebaseAuth.getInstance()


    // =========================================================
    // CATEGORÍAS
    // =========================================================

    private lateinit var rvCategorias: RecyclerView
    private lateinit var categoriaAdapter: CategoriaAdapter

    private val categorias =
        mutableListOf<CategoriaItem>()

    private var categoriaSeleccionadaId = 0


    // =========================================================
    // EXTRAS
    // =========================================================

    private lateinit var rvExtras: RecyclerView
    private lateinit var extraAdapter: ExtraAdapter

    private val todosLosExtras =
        mutableListOf<ExtraItem>()

    private val listaExtras =
        mutableListOf<ExtraItem>()


    // =========================================================
    // ENTRADAS
    // =========================================================

    private val entradas =
        mutableListOf<TaskEntradas>()

    private lateinit var rvEntradas: RecyclerView
    private lateinit var entradasAdapter: EntradasUsuarioAdapter


    // =========================================================
    // MENÚ
    // =========================================================

    private val listaMenu =
        mutableListOf<TaskMenu>()

    private lateinit var rvMenu: RecyclerView
    private lateinit var menuAdapter: MenuUsuarioAdapter


    // =========================================================
    // UI
    // =========================================================

    private lateinit var progressBarMenu: ProgressBar
    private lateinit var tvCantidadCarrito: TextView
    private lateinit var btnCarrito: FloatingActionButton
    private lateinit var tvSaludo: TextView


    // =========================================================
    // ELEMENTOS DE ANIMACIÓN
    // =========================================================

    private lateinit var tvSubtituloMenu: TextView
    private lateinit var tvTituloEntradas: TextView
    private lateinit var layoutTituloMenu: LinearLayout
    private lateinit var bottomNavigation: View
    private lateinit var contenedorCarrito: View

    private var animacionInicialEjecutada = false


    // =========================================================
    // NAVEGACIÓN INFERIOR
    // =========================================================

    private lateinit var navInicio: LinearLayout
    private lateinit var navExtras: LinearLayout
    private lateinit var navPedidos: LinearLayout
    private lateinit var navCarrito: LinearLayout
    private lateinit var navPerfil: LinearLayout


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
            R.layout.activity_menu_usuario
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

        prepararAnimaciones()

        cargarNombreUsuario()
        cargarCategorias()
        cargarDatosDesdeFirebase()
    }


    // =========================================================
    // COMPONENTES
    // =========================================================

    private fun initComponent() {

        rvCategorias =
            findViewById(R.id.rvCategorias)

        rvEntradas =
            findViewById(R.id.rvEntradas)

        rvExtras =
            findViewById(R.id.rvExtras)

        rvMenu =
            findViewById(R.id.rvMenu)

        progressBarMenu =
            findViewById(R.id.progressBarMenu)

        tvCantidadCarrito =
            findViewById(R.id.tvCantidadCarrito)

        btnCarrito =
            findViewById(R.id.btnCarrito)

        tvSaludo =
            findViewById(R.id.tvSaludo)


        // Elementos de animación

        tvSubtituloMenu =
            findViewById(R.id.tvSubtituloMenu)

        tvTituloEntradas =
            findViewById(R.id.tvTituloEntradas)

        layoutTituloMenu =
            findViewById(R.id.layoutTituloMenu)

        bottomNavigation =
            findViewById(R.id.bottomNavigation)

        contenedorCarrito =
            findViewById(R.id.contenedorCarrito)


        // Navegación

        navInicio =
            findViewById(R.id.navInicio)

        navExtras =
            findViewById(R.id.navExtras)

        navPedidos =
            findViewById(R.id.navPedidos)

        navCarrito =
            findViewById(R.id.navCarrito)

        navPerfil =
            findViewById(R.id.navPerfil)
    }


    // =========================================================
    // PREPARAR ANIMACIONES
    // =========================================================

    private fun prepararAnimaciones() {

        tvSaludo.alpha = 0f
        tvSaludo.translationY = 25f

        tvSubtituloMenu.alpha = 0f
        tvSubtituloMenu.translationY = 20f

        tvTituloEntradas.alpha = 0f
        tvTituloEntradas.translationY = 20f

        rvEntradas.alpha = 0f
        rvEntradas.translationY = 25f

        layoutTituloMenu.alpha = 0f
        layoutTituloMenu.translationY = 20f

        rvMenu.alpha = 0f
        rvMenu.translationY = 30f

        bottomNavigation.alpha = 0f
        bottomNavigation.translationY = 90f
    }


    // =========================================================
    // ANIMACIÓN PRINCIPAL
    // =========================================================

    private fun ejecutarAnimacionInicial() {

        if (animacionInicialEjecutada) {
            return
        }

        animacionInicialEjecutada = true


        val saludoAlpha =
            ObjectAnimator.ofFloat(
                tvSaludo,
                View.ALPHA,
                0f,
                1f
            )

        val saludoMovimiento =
            ObjectAnimator.ofFloat(
                tvSaludo,
                View.TRANSLATION_Y,
                25f,
                0f
            )


        val subtituloAlpha =
            ObjectAnimator.ofFloat(
                tvSubtituloMenu,
                View.ALPHA,
                0f,
                1f
            )

        val subtituloMovimiento =
            ObjectAnimator.ofFloat(
                tvSubtituloMenu,
                View.TRANSLATION_Y,
                20f,
                0f
            )


        val tituloEntradasAlpha =
            ObjectAnimator.ofFloat(
                tvTituloEntradas,
                View.ALPHA,
                0f,
                1f
            )

        val tituloEntradasMovimiento =
            ObjectAnimator.ofFloat(
                tvTituloEntradas,
                View.TRANSLATION_Y,
                20f,
                0f
            )


        val entradasAlpha =
            ObjectAnimator.ofFloat(
                rvEntradas,
                View.ALPHA,
                0f,
                1f
            )

        val entradasMovimiento =
            ObjectAnimator.ofFloat(
                rvEntradas,
                View.TRANSLATION_Y,
                25f,
                0f
            )


        val tituloMenuAlpha =
            ObjectAnimator.ofFloat(
                layoutTituloMenu,
                View.ALPHA,
                0f,
                1f
            )

        val tituloMenuMovimiento =
            ObjectAnimator.ofFloat(
                layoutTituloMenu,
                View.TRANSLATION_Y,
                20f,
                0f
            )


        val menuAlpha =
            ObjectAnimator.ofFloat(
                rvMenu,
                View.ALPHA,
                0f,
                1f
            )

        val menuMovimiento =
            ObjectAnimator.ofFloat(
                rvMenu,
                View.TRANSLATION_Y,
                30f,
                0f
            )


        val barraAlpha =
            ObjectAnimator.ofFloat(
                bottomNavigation,
                View.ALPHA,
                0f,
                1f
            )

        val barraMovimiento =
            ObjectAnimator.ofFloat(
                bottomNavigation,
                View.TRANSLATION_Y,
                90f,
                0f
            )


        val animadores =
            listOf(
                saludoAlpha,
                saludoMovimiento,
                subtituloAlpha,
                subtituloMovimiento,
                tituloEntradasAlpha,
                tituloEntradasMovimiento,
                entradasAlpha,
                entradasMovimiento,
                tituloMenuAlpha,
                tituloMenuMovimiento,
                menuAlpha,
                menuMovimiento,
                barraAlpha,
                barraMovimiento
            )


        animadores.forEach { animador ->

            animador.duration = 420

            animador.interpolator =
                DecelerateInterpolator()
        }


        crearAnimacion(
            saludoAlpha,
            saludoMovimiento,
            80
        )

        crearAnimacion(
            subtituloAlpha,
            subtituloMovimiento,
            160
        )

        crearAnimacion(
            tituloEntradasAlpha,
            tituloEntradasMovimiento,
            240
        )

        crearAnimacion(
            entradasAlpha,
            entradasMovimiento,
            320
        )

        crearAnimacion(
            tituloMenuAlpha,
            tituloMenuMovimiento,
            400
        )

        crearAnimacion(
            menuAlpha,
            menuMovimiento,
            480
        )

        crearAnimacion(
            barraAlpha,
            barraMovimiento,
            600
        )
    }


    // =========================================================
    // CREAR ANIMACIÓN
    // =========================================================

    private fun crearAnimacion(
        alpha: ObjectAnimator,
        movimiento: ObjectAnimator,
        retraso: Long
    ) {

        AnimatorSet().apply {

            playTogether(
                alpha,
                movimiento
            )

            startDelay = retraso

            start()
        }
    }


    // =========================================================
    // BADGE DEL CARRITO
    // =========================================================

    private fun animarBadgeCarrito() {

        if (
            tvCantidadCarrito.visibility !=
            View.VISIBLE
        ) {
            return
        }

        tvCantidadCarrito.animate()
            .scaleX(1.25f)
            .scaleY(1.25f)
            .setDuration(120)
            .withEndAction {

                tvCantidadCarrito.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }


    // =========================================================
    // NOMBRE DEL USUARIO
    // =========================================================

    private fun cargarNombreUsuario() {

        val usuarioActual =
            auth.currentUser

        if (usuarioActual == null) {

            tvSaludo.text =
                "¡Hola!"

            return
        }

        val uid =
            usuarioActual.uid

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { documento ->

                if (documento.exists()) {

                    val nombre =
                        documento.getString("nombre")

                    if (!nombre.isNullOrEmpty()) {

                        tvSaludo.text =
                            "¡Hola, $nombre!"

                    } else {

                        tvSaludo.text =
                            "¡Hola!"
                    }

                } else {

                    tvSaludo.text =
                        "¡Hola!"
                }
            }
            .addOnFailureListener { error ->

                Log.e(
                    "USUARIO_FIREBASE",
                    "Error obteniendo nombre",
                    error
                )

                tvSaludo.text =
                    "¡Hola!"
            }
    }


    // =========================================================
    // CONFIGURACIÓN DE LA UI
    // =========================================================

    private fun initUI() {

        // =====================================================
        // ENTRADAS
        // =====================================================

        entradasAdapter =
            EntradasUsuarioAdapter(
                entradas
            )

        rvEntradas.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        rvEntradas.adapter =
            entradasAdapter


        // =====================================================
        // MENÚ
        // =====================================================

        menuAdapter =
            MenuUsuarioAdapter(
                listaMenu
            ) { plato ->

                mostrarAlertaAgregar(
                    plato
                )
            }

        configurarCarruselMenu()

        rvMenu.adapter =
            menuAdapter


        // =====================================================
        // EXTRAS
        // =====================================================

        extraAdapter =
            ExtraAdapter(
                listaExtras
            ) { extra ->

                agregarExtraAlPedido(
                    extra
                )
            }

        rvExtras.layoutManager =
            LinearLayoutManager(this)

        rvExtras.adapter =
            extraAdapter


        // =====================================================
        // CARRITO
        // =====================================================

        btnCarrito.setOnClickListener {

            abrirCarrito()
        }


        // =====================================================
        // NAVEGACIÓN
        // =====================================================

        navInicio.setOnClickListener {

            // Ya estamos en Inicio.
        }

        navExtras.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ActivityExtras::class.java
                )
            )
        }

        navPedidos.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ActivityPedidosUsuario::class.java
                )
            )
        }

        navCarrito.setOnClickListener {

            abrirCarrito()
        }

        navPerfil.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ActivityPerfilUsuario::class.java
                )
            )
        }

        configurarAnimacionesBarra()
        marcarInicioActivo()

        actualizarContadorCarrito()
    }

    // =========================================================
    // ANIMACIÓN DE LA BARRA INFERIOR
    // =========================================================

    @SuppressLint("ClickableViewAccessibility")
    private fun configurarAnimacionesBarra() {

        listOf(
            navInicio,
            navExtras,
            navPedidos,
            navCarrito,
            navPerfil
        ).forEach { item ->

            item.setOnTouchListener { vista, evento ->

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

    private fun marcarInicioActivo() {

        navInicio.post {

            navInicio.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .translationY(-5f)
                .setDuration(280)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }


    // =========================================================
    // ABRIR CARRITO
    // =========================================================

    private fun abrirCarrito() {

        startActivity(
            Intent(
                this,
                ActivityPedido::class.java
            )
        )
    }


    // =========================================================
    // CARRUSEL DEL MENÚ
    // =========================================================

    private fun configurarCarruselMenu() {

        val layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        rvMenu.layoutManager =
            layoutManager


        // =====================================================
        // EFECTO PEEK
        // =====================================================

        rvMenu.clipToPadding =
            false

        rvMenu.clipChildren =
            false

        rvMenu.setPadding(
            90,
            0,
            90,
            0
        )

        rvMenu.overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER

        rvMenu.itemAnimator =
            null


        // =====================================================
        // SNAP
        // =====================================================

        val snapHelper =
            LinearSnapHelper()

        snapHelper.attachToRecyclerView(
            rvMenu
        )


        // =====================================================
        // SCROLL
        // =====================================================

        rvMenu.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {

                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {

                    super.onScrolled(
                        recyclerView,
                        dx,
                        dy
                    )

                    animarTarjetasCarrusel(
                        recyclerView
                    )
                }


                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int
                ) {

                    super.onScrollStateChanged(
                        recyclerView,
                        newState
                    )

                    if (
                        newState ==
                        RecyclerView.SCROLL_STATE_IDLE
                    ) {

                        animarTarjetaCentral(
                            recyclerView
                        )
                    }
                }
            }
        )


        // =====================================================
        // ANIMACIÓN INICIAL
        // =====================================================

        rvMenu.post {

            animarTarjetasCarrusel(
                rvMenu
            )
        }
    }


    // =========================================================
    // ANIMAR TARJETAS DEL CARRUSEL
    // =========================================================

    private fun animarTarjetasCarrusel(
        recyclerView: RecyclerView
    ) {

        if (recyclerView.width <= 0) {
            return
        }

        val centroX =
            recyclerView.width / 2f

        val distanciaMaxima =
            recyclerView.width / 2f

        if (distanciaMaxima <= 0f) {
            return
        }


        for (
        i in 0 until recyclerView.childCount
        ) {

            val tarjeta =
                recyclerView.getChildAt(i)

            val centroTarjeta =
                (
                        tarjeta.left +
                                tarjeta.right
                        ) / 2f

            val distancia =
                abs(
                    centroX -
                            centroTarjeta
                )

            val posicion =
                (
                        distancia /
                                distanciaMaxima
                        ).coerceIn(
                        0f,
                        1f
                    )


            // =================================================
            // ESCALA
            // =================================================
            //
            // Centro = 1.00
            // Laterales = hasta 0.86
            // =================================================

            val escala =
                1f -
                        (
                                posicion *
                                        0.14f
                                )

            tarjeta.scaleX =
                escala

            tarjeta.scaleY =
                escala


            // =================================================
            // OPACIDAD
            // =================================================

            tarjeta.alpha =
                1f -
                        (
                                posicion *
                                        0.25f
                                )


            // =================================================
            // MOVIMIENTO VERTICAL
            // =================================================

            tarjeta.translationY =
                posicion * 14f


            // =================================================
            // PROFUNDIDAD
            // =================================================

            tarjeta.translationZ =
                (
                        1f -
                                posicion
                        ) * 25f
        }
    }


    // =========================================================
    // POP TARJETA CENTRAL
    // =========================================================

    private fun animarTarjetaCentral(
        recyclerView: RecyclerView
    ) {

        if (recyclerView.width <= 0) {
            return
        }

        val centroX =
            recyclerView.width / 2f

        var tarjetaCentral: View? =
            null

        var distanciaMenor =
            Float.MAX_VALUE


        for (
        i in 0 until recyclerView.childCount
        ) {

            val tarjeta =
                recyclerView.getChildAt(i)

            val centroTarjeta =
                (
                        tarjeta.left +
                                tarjeta.right
                        ) / 2f

            val distancia =
                abs(
                    centroX -
                            centroTarjeta
                )

            if (
                distancia <
                distanciaMenor
            ) {

                distanciaMenor =
                    distancia

                tarjetaCentral =
                    tarjeta
            }
        }


        tarjetaCentral?.let { tarjeta ->

            tarjeta.animate()
                .cancel()

            tarjeta.animate()
                .scaleX(1.06f)
                .scaleY(1.06f)
                .translationY(-2f)
                .translationZ(30f)
                .setDuration(180)
                .setInterpolator(
                    DecelerateInterpolator()
                )
                .withEndAction {

                    tarjeta.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .translationY(0f)
                        .translationZ(25f)
                        .setDuration(220)
                        .setInterpolator(
                            DecelerateInterpolator()
                        )
                        .start()
                }
                .start()
        }
    }


    // =========================================================
    // CATEGORÍAS
    // =========================================================

    private fun cargarCategorias() {

        categorias.clear()


        categorias.add(
            CategoriaItem(
                1,
                "Gaseosas",
                R.drawable.ic_gaseosa
            )
        )

        categorias.add(
            CategoriaItem(
                2,
                "Tortas",
                R.drawable.ic_torta
            )
        )

        categorias.add(
            CategoriaItem(
                3,
                "Platos",
                R.drawable.ic_plato
            )
        )


        categoriaAdapter =
            CategoriaAdapter(
                categorias
            ) { categoria ->

                if (
                    categoriaSeleccionadaId ==
                    categoria.id &&
                    rvExtras.visibility ==
                    View.VISIBLE
                ) {

                    rvExtras.visibility =
                        View.GONE

                } else {

                    categoriaSeleccionadaId =
                        categoria.id

                    mostrarExtrasPorCategoria(
                        categoria.id
                    )
                }
            }


        rvCategorias.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        rvCategorias.adapter =
            categoriaAdapter
    }


    // =========================================================
    // FILTRAR EXTRAS
    // =========================================================

    private fun mostrarExtrasPorCategoria(
        categoriaId: Int
    ) {

        listaExtras.clear()

        listaExtras.addAll(
            todosLosExtras.filter {
                it.categoriaId ==
                        categoriaId
            }
        )

        extraAdapter.notifyDataSetChanged()

        rvExtras.visibility =
            if (listaExtras.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }

        Log.d(
            "USUARIO_FIREBASE",
            "Categoría $categoriaId: ${listaExtras.size} extras"
        )
    }


    // =========================================================
    // AGREGAR EXTRA
    // =========================================================

    private fun agregarExtraAlPedido(
        extra: ExtraItem
    ) {

        PedidoManager.agregarProducto(

            PedidoItem(
                id = extra.id,
                nombre = extra.nombre,
                precio = extra.precio,
                cantidad = 1,
                tipo = TipoPedido.EXTRA
            )
        )

        actualizarContadorCarrito()

        Toast.makeText(
            this,
            "${extra.nombre} agregado al pedido",
            Toast.LENGTH_SHORT
        ).show()
    }


    // =========================================================
    // DIALOG AGREGAR PLATO
    // =========================================================

    private fun mostrarAlertaAgregar(
        plato: TaskMenu
    ) {

        val cantidadMenuEnCarrito =
            PedidoManager.cantidadMenuEnPedido(
                plato.id
            )

        if (
            plato.stock <= 0 ||
            cantidadMenuEnCarrito >= plato.stock
        ) {

            Toast.makeText(
                this,
                "Ya no puedes agregar más unidades de este plato",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val seleccionActual =
            entradasAdapter
                .obtenerEntradasSeleccionadas()

        val cantidadEntradas =
            seleccionActual.sumOf { entrada ->
                entrada.cantidad
            }

        val precioSinEntrada =
            ReglasPrecioPedido.calcularPrecioMenu(
                precioMenuConEntrada = plato.precio,
                cantidadEntradas = 0
            )

        val precioConEntrada =
            ReglasPrecioPedido.calcularPrecioMenu(
                precioMenuConEntrada = plato.precio,
                cantidadEntradas = 1
            )

        val precioFinal =
            ReglasPrecioPedido.calcularPrecioMenu(
                precioMenuConEntrada = plato.precio,
                cantidadEntradas = cantidadEntradas
            )

        val dialogView =
            layoutInflater.inflate(
                R.layout.dialog_agregar_pedido,
                null
            )


        val tvNombrePlato =
            dialogView.findViewById<TextView>(
                R.id.tvNombrePlato
            )

        val tvPrecioSinEntrada =
            dialogView.findViewById<TextView>(
                R.id.tvPrecioSinEntrada
            )

        val tvPrecioConEntrada =
            dialogView.findViewById<TextView>(
                R.id.tvPrecioConEntrada
            )

        val tvPrecioEntradaAdicional =
            dialogView.findViewById<TextView>(
                R.id.tvPrecioEntradaAdicional
            )

        val tvEntradasSeleccionadas =
            dialogView.findViewById<TextView>(
                R.id.tvEntradasSeleccionadas
            )

        val tvTotalSeleccion =
            dialogView.findViewById<TextView>(
                R.id.tvTotalSeleccion
            )

        val tvAyudaSeleccion =
            dialogView.findViewById<TextView>(
                R.id.tvAyudaSeleccion
            )

        val btnCancelar =
            dialogView.findViewById<Button>(
                R.id.btnCancelarPedido
            )

        val btnAgregar =
            dialogView.findViewById<Button>(
                R.id.btnAgregarPedido
            )


        tvNombrePlato.text =
            plato.name

        tvPrecioSinEntrada.text =
            "S/ %.2f".format(
                precioSinEntrada
            )

        tvPrecioConEntrada.text =
            "S/ %.2f".format(
                precioConEntrada
            )

        tvPrecioEntradaAdicional.text =
            "Entradas adicionales: +S/ %.2f cada una".format(
                ReglasPrecioPedido.PRECIO_ENTRADA
            )

        tvEntradasSeleccionadas.text =
            descripcionSeleccionEntradas(
                seleccionActual
            )

        tvTotalSeleccion.text =
            "S/ %.2f".format(
                precioFinal
            )

        tvAyudaSeleccion.text =
            when (cantidadEntradas) {

                0 ->
                    "Sin entrada, este menú te costará S/ %.2f.".format(
                        precioFinal
                    )

                1 ->
                    "Con una entrada, este menú te costará S/ %.2f.".format(
                        precioFinal
                    )

                else ->
                    "Elegiste $cantidadEntradas entradas para este menú."
            }


        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

        dialog.show()


        dialog.window?.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )


        btnCancelar.setOnClickListener {

            dialog.dismiss()
        }


        btnAgregar.setOnClickListener {

            val cantidadActualEnCarrito =
                PedidoManager.cantidadMenuEnPedido(
                    plato.id
                )

            if (
                plato.stock <= 0 ||
                cantidadActualEnCarrito >= plato.stock
            ) {

                Toast.makeText(
                    this,
                    "Ya no puedes agregar más unidades de este plato",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()

                return@setOnClickListener
            }


            btnAgregar.isEnabled =
                false


            agregarAlPedido(

                plato = plato,
                seleccionEntradas = seleccionActual,
                precioFinal = precioFinal,

                onCompletado = {
                    dialog.dismiss()
                },

                onError = {

                    if (
                        !isFinishing &&
                        !isDestroyed
                    ) {

                        btnAgregar.isEnabled =
                            true
                    }
                }
            )
        }
    }


    // =========================================================
    // AGREGAR PLATO AL PEDIDO
    // =========================================================

    private fun agregarAlPedido(
        plato: TaskMenu,
        seleccionEntradas: List<EntradaPedido>,
        precioFinal: Double,
        onCompletado: () -> Unit,
        onError: () -> Unit
    ) {

        val cantidadMenuEnCarrito =
            PedidoManager.cantidadMenuEnPedido(
                plato.id
            )

        if (
            plato.stock <= 0 ||
            cantidadMenuEnCarrito >= plato.stock
        ) {

            Toast.makeText(
                this,
                "Ya no puedes agregar más unidades de este plato",
                Toast.LENGTH_SHORT
            ).show()

            onError()

            return
        }

        for (entradaSeleccionada in seleccionEntradas) {

            val entradaActual =
                entradas.find { entrada ->
                    entrada.id == entradaSeleccionada.id
                }

            val cantidadEnCarrito =
                PedidoManager.cantidadEntradaEnPedido(
                    entradaSeleccionada.id
                )

            val cantidadNecesaria =
                cantidadEnCarrito +
                        entradaSeleccionada.cantidad

            if (
                entradaActual == null ||
                !entradaActual.disponible ||
                cantidadNecesaria > entradaActual.stock
            ) {

                Toast.makeText(
                    this,
                    "No hay stock suficiente de ${entradaSeleccionada.nombre}",
                    Toast.LENGTH_SHORT
                ).show()

                entradasAdapter.notifyDataSetChanged()
                onError()

                return
            }
        }

        PedidoManager.agregarProducto(

            PedidoItem(
                id = plato.id,
                nombre = plato.name,
                precio = precioFinal,
                cantidad = 1,
                tipo = TipoPedido.MENU,
                precioBaseMenu = plato.precio,
                entradas = seleccionEntradas
            )
        )

        entradasAdapter.limpiarSeleccion()

        actualizarContadorCarrito()


        Toast.makeText(
            this,
            "${plato.name} agregado por S/ %.2f".format(
                precioFinal
            ),
            Toast.LENGTH_SHORT
        ).show()


        onCompletado()
    }


    // =========================================================
    // DESCRIPCIÓN DE LAS ENTRADAS SELECCIONADAS
    // =========================================================

    private fun descripcionSeleccionEntradas(
        seleccionEntradas: List<EntradaPedido>
    ): String {

        if (seleccionEntradas.isEmpty()) {
            return "Sin entrada"
        }

        val detalle =
            seleccionEntradas.joinToString(
                separator = ", "
            ) { entrada ->

                if (entrada.cantidad > 1) {
                    "${entrada.cantidad} x ${entrada.nombre}"
                } else {
                    entrada.nombre
                }
            }

        val cantidadTotal =
            seleccionEntradas.sumOf { entrada ->
                entrada.cantidad
            }

        return if (cantidadTotal == 1) {
            "Con entrada: $detalle"
        } else {
            "Con $cantidadTotal entradas: $detalle"
        }
    }


    // =========================================================
    // CONTADOR DEL CARRITO
    // =========================================================

    private fun actualizarContadorCarrito() {

        val cantidadTotal =
            PedidoManager.cantidadTotal()


        tvCantidadCarrito.text =
            cantidadTotal.toString()


        tvCantidadCarrito.visibility =
            if (cantidadTotal > 0) {
                View.VISIBLE
            } else {
                View.GONE
            }


        if (cantidadTotal > 0) {

            animarBadgeCarrito()
        }
    }


    // =========================================================
    // CARGAR DATOS
    // =========================================================

    private fun cargarDatosDesdeFirebase() {

        progressBarMenu.visibility =
            View.VISIBLE

        rvEntradas.visibility =
            View.GONE

        rvMenu.visibility =
            View.GONE

        rvExtras.visibility =
            View.GONE


        cargarEntradas()

        cargarMenu()

        cargarExtrasDesdeFirebase()
    }


    // =========================================================
    // EXTRAS - FIREBASE
    // =========================================================

    private fun cargarExtrasDesdeFirebase() {

        db.collection("extras")
            .addSnapshotListener { resultado, error ->

                if (error != null) {

                    Log.e(
                        "USUARIO_FIREBASE",
                        "Error escuchando extras",
                        error
                    )

                    return@addSnapshotListener
                }


                if (resultado == null) {
                    return@addSnapshotListener
                }


                todosLosExtras.clear()


                for (documento in resultado) {

                    val id =
                        documento
                            .getLong("id")
                            ?.toInt()
                            ?: documento.id
                                .toIntOrNull()
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
                            ?: 0


                    val icono =
                        when (categoriaId) {

                            1 ->
                                R.drawable.ic_gaseosa

                            2 ->
                                R.drawable.ic_torta

                            3 ->
                                R.drawable.ic_plato

                            else ->
                                R.drawable.ic_gaseosa
                        }


                    if (
                        id > 0 &&
                        nombre.isNotEmpty() &&
                        categoriaId > 0
                    ) {

                        todosLosExtras.add(

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


                if (
                    categoriaSeleccionadaId != 0
                ) {

                    mostrarExtrasPorCategoria(
                        categoriaSeleccionadaId
                    )
                }


                Log.d(
                    "USUARIO_FIREBASE",
                    "Extras actualizados: ${todosLosExtras.size}"
                )
            }
    }


    // =========================================================
    // ENTRADAS - FIREBASE
    // =========================================================

    private fun cargarEntradas() {

        db.collection("entradas")
            .addSnapshotListener { resultado, error ->

                if (error != null) {

                    Log.e(
                        "USUARIO_FIREBASE",
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
                            ?: documento.id
                                .toIntOrNull()
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


                entradasAdapter
                    .notifyDataSetChanged()


                Log.d(
                    "USUARIO_FIREBASE",
                    "Entradas actualizadas: ${entradas.size}"
                )
            }
    }


    // =========================================================
    // MENÚ - FIREBASE
    // =========================================================

    private fun cargarMenu() {

        db.collection("menu")
            .addSnapshotListener { resultado, error ->

                if (error != null) {

                    Log.e(
                        "USUARIO_FIREBASE",
                        "Error escuchando menú",
                        error
                    )

                    mostrarContenido()

                    return@addSnapshotListener
                }


                if (resultado == null) {
                    return@addSnapshotListener
                }


                listaMenu.clear()


                for (documento in resultado) {

                    val id =
                        documento
                            .getLong("id")
                            ?.toInt()
                            ?: documento.id
                                .toIntOrNull()
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


                menuAdapter
                    .notifyDataSetChanged()


                mostrarContenido()


                Log.d(
                    "USUARIO_FIREBASE",
                    "Menú actualizado: ${listaMenu.size}"
                )
            }
    }


    // =========================================================
    // MOSTRAR CONTENIDO
    // =========================================================

    private fun mostrarContenido() {

        progressBarMenu.visibility =
            View.GONE

        rvEntradas.visibility =
            View.VISIBLE

        rvMenu.visibility =
            View.VISIBLE


        ejecutarAnimacionInicial()


        rvMenu.post {

            animarTarjetasCarrusel(
                rvMenu
            )
        }
    }


    // =========================================================
    // AL REGRESAR
    // =========================================================

    override fun onResume() {

        super.onResume()

        actualizarContadorCarrito()
    }
}