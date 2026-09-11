package com.example.appzetar.Usuario

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appzetar.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import android.annotation.SuppressLint
import com.example.appzetar.Usuario.Carrito.ActivityPedido
import com.example.appzetar.Usuario.Carrito.PedidoManager
import com.google.firebase.firestore.FirebaseFirestore

class ActivityPerfilUsuario : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var tvInicialPerfil: TextView
    private lateinit var tvNombrePerfil: TextView
    private lateinit var tvCorreoPerfil: TextView
    private lateinit var tvCantidadCarrito: TextView

    private lateinit var opcionEditarPerfil: LinearLayout
    private lateinit var opcionPedidosPerfil: LinearLayout
    private lateinit var btnCerrarSesion: MaterialButton

    private lateinit var navInicio: LinearLayout
    private lateinit var navExtras: LinearLayout
    private lateinit var navPedidos: LinearLayout
    private lateinit var navCarrito: LinearLayout
    private lateinit var navPerfil: LinearLayout

    private var nombreActual = ""
    private var correoActual = ""
    private var animacionInicialEjecutada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContentView(R.layout.activity_perfil_usuario)

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
        prepararAnimaciones()
        cargarDatosUsuario()
    }

    override fun onResume() {
        super.onResume()
        actualizarContadorCarrito()
    }

    private fun initComponent() {
        tvInicialPerfil = findViewById(R.id.tvInicialPerfil)
        tvNombrePerfil = findViewById(R.id.tvNombrePerfil)
        tvCorreoPerfil = findViewById(R.id.tvCorreoPerfil)
        tvCantidadCarrito = findViewById(R.id.tvCantidadCarrito)

        opcionEditarPerfil = findViewById(R.id.opcionEditarPerfil)
        opcionPedidosPerfil = findViewById(R.id.opcionPedidosPerfil)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        navInicio = findViewById(R.id.navInicio)
        navExtras = findViewById(R.id.navExtras)
        navPedidos = findViewById(R.id.navPedidos)
        navCarrito = findViewById(R.id.navCarrito)
        navPerfil = findViewById(R.id.navPerfil)
    }

    private fun initUI() {
        opcionEditarPerfil.setOnClickListener {
            mostrarDialogEditarNombre()
        }

        opcionPedidosPerfil.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ActivityPedidosUsuario::class.java
                )
            )
        }

        btnCerrarSesion.setOnClickListener {
            mostrarConfirmacionCerrarSesion()
        }

        configurarNavegacion()
        configurarAnimacionesBarra()
        marcarPerfilActivo()
        actualizarContadorCarrito()
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
            startActivity(
                Intent(this, ActivityPedido::class.java)
            )
        }

        navPerfil.setOnClickListener {
            // Ya estás en Perfil.
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

                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        vista.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationY(0f)
                            .setDuration(180)
                            .setInterpolator(DecelerateInterpolator())
                            .start()

                        vista.performClick()
                        true
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        vista.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationY(0f)
                            .setDuration(180)
                            .setInterpolator(DecelerateInterpolator())
                            .start()

                        true
                    }

                    else -> true
                }
            }
        }
    }

    private fun marcarPerfilActivo() {
        navPerfil.post {
            navPerfil.animate()
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

    private fun prepararAnimaciones() {
        tvInicialPerfil.alpha = 0f
        tvInicialPerfil.scaleX = 0.85f
        tvInicialPerfil.scaleY = 0.85f

        tvNombrePerfil.alpha = 0f
        tvNombrePerfil.translationY = 18f

        tvCorreoPerfil.alpha = 0f
        tvCorreoPerfil.translationY = 18f

        opcionEditarPerfil.alpha = 0f
        opcionEditarPerfil.translationY = 25f

        opcionPedidosPerfil.alpha = 0f
        opcionPedidosPerfil.translationY = 25f

        btnCerrarSesion.alpha = 0f
        btnCerrarSesion.translationY = 25f
    }

    private fun ejecutarAnimacionInicial() {
        if (animacionInicialEjecutada) {
            return
        }

        animacionInicialEjecutada = true

        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(
                    tvInicialPerfil,
                    View.ALPHA,
                    0f,
                    1f
                ),
                ObjectAnimator.ofFloat(
                    tvInicialPerfil,
                    View.SCALE_X,
                    0.85f,
                    1f
                ),
                ObjectAnimator.ofFloat(
                    tvInicialPerfil,
                    View.SCALE_Y,
                    0.85f,
                    1f
                )
            )

            duration = 450
            startDelay = 80
            interpolator = DecelerateInterpolator()

            start()
        }

        animarEntrada(tvNombrePerfil, 160)
        animarEntrada(tvCorreoPerfil, 230)
        animarEntrada(opcionEditarPerfil, 320)
        animarEntrada(opcionPedidosPerfil, 400)
        animarEntrada(btnCerrarSesion, 480)
    }

    private fun animarEntrada(
        vista: View,
        retraso: Long
    ) {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(
                    vista,
                    View.ALPHA,
                    0f,
                    1f
                ),
                ObjectAnimator.ofFloat(
                    vista,
                    View.TRANSLATION_Y,
                    vista.translationY,
                    0f
                )
            )

            duration = 420
            startDelay = retraso
            interpolator = DecelerateInterpolator()

            start()
        }
    }

    private fun cargarDatosUsuario() {
        val usuario = auth.currentUser

        if (usuario == null) {
            regresarLogin()
            return
        }

        correoActual = usuario.email ?: ""

        db.collection("usuarios")
            .document(usuario.uid)
            .get()
            .addOnSuccessListener { documento ->

                if (documento.exists()) {
                    nombreActual =
                        documento.getString("nombre")
                            ?: "Usuario"

                    correoActual =
                        documento.getString("correo")
                            ?: usuario.email
                                    ?: ""
                } else {
                    nombreActual =
                        usuario.displayName
                            ?: "Usuario"

                    correoActual =
                        usuario.email
                            ?: ""
                }

                mostrarDatos()
                ejecutarAnimacionInicial()
            }
            .addOnFailureListener {
                nombreActual =
                    usuario.displayName
                        ?: "Usuario"

                correoActual =
                    usuario.email
                        ?: ""

                mostrarDatos()
                ejecutarAnimacionInicial()

                Toast.makeText(
                    this,
                    "No se pudieron cargar todos los datos",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun mostrarDatos() {
        tvNombrePerfil.text = nombreActual
        tvCorreoPerfil.text = correoActual

        val inicial = nombreActual
            .trim()
            .firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            ?: "U"

        tvInicialPerfil.text = inicial
    }

    private fun mostrarDialogEditarNombre() {
        val dialogView = layoutInflater.inflate(
            R.layout.dialog_editar_perfil,
            null
        )

        val etNombreEditar =
            dialogView.findViewById<TextInputEditText>(
                R.id.etNombreEditar
            )

        val btnCancelar =
            dialogView.findViewById<MaterialButton>(
                R.id.btnCancelarEditarPerfil
            )

        val btnGuardar =
            dialogView.findViewById<MaterialButton>(
                R.id.btnGuardarEditarPerfil
            )

        etNombreEditar.setText(nombreActual)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.show()

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val nuevoNombre = etNombreEditar.text
                ?.toString()
                ?.trim()
                ?: ""

            if (nuevoNombre.isEmpty()) {
                etNombreEditar.error = "Ingresa tu nombre"
                return@setOnClickListener
            }

            actualizarNombre(
                nuevoNombre,
                dialog,
                btnGuardar
            )
        }
    }

    private fun actualizarNombre(
        nuevoNombre: String,
        dialog: AlertDialog,
        btnGuardar: MaterialButton
    ) {
        val usuario = auth.currentUser

        if (usuario == null) {
            regresarLogin()
            return
        }

        btnGuardar.isEnabled = false

        db.collection("usuarios")
            .document(usuario.uid)
            .update("nombre", nuevoNombre)
            .addOnSuccessListener {
                nombreActual = nuevoNombre
                mostrarDatos()

                Toast.makeText(
                    this,
                    "Perfil actualizado",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
            .addOnFailureListener {
                btnGuardar.isEnabled = true

                Toast.makeText(
                    this,
                    "No se pudo actualizar el perfil",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun mostrarConfirmacionCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Deseas cerrar tu sesión?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Cerrar sesión") { _, _ ->
                cerrarSesion()
            }
            .show()
    }

    private fun cerrarSesion() {
        auth.signOut()
        regresarLogin()
    }

    private fun regresarLogin() {
        val intent = Intent(
            this,
            LoginActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}