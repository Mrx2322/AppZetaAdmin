package com.example.appzetaadmin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appzetaadmin.AdminMenu.ActivityMenu
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var etCorreoAdmin: TextInputEditText
    private lateinit var etContrasenaAdmin: TextInputEditText
    private lateinit var btnIngresarAdmin: MaterialButton
    private lateinit var progressAdmin: View
    private lateinit var tvMensajeAdmin: TextView

    private val auth =
        FirebaseAuth.getInstance()

    private val db =
        FirebaseFirestore.getInstance()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        iniciarComponentes()
        configurarEventos()
        comprobarSesionExistente()
    }

    private fun iniciarComponentes() {
        etCorreoAdmin =
            findViewById(R.id.etCorreoAdmin)

        etContrasenaAdmin =
            findViewById(R.id.etContrasenaAdmin)

        btnIngresarAdmin =
            findViewById(R.id.btnIngresarAdmin)

        progressAdmin =
            findViewById(R.id.progressAdmin)

        tvMensajeAdmin =
            findViewById(R.id.tvMensajeAdmin)
    }

    private fun configurarEventos() {
        btnIngresarAdmin.setOnClickListener {
            iniciarSesionAdmin()
        }
    }

    private fun comprobarSesionExistente() {
        val usuarioActual =
            auth.currentUser ?: return

        mostrarCarga(true)

        verificarPermisoAdministrador(
            uid = usuarioActual.uid,
            cerrarSesionSiFalla = true
        )
    }

    private fun iniciarSesionAdmin() {
        val correo =
            etCorreoAdmin.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val contrasena =
            etContrasenaAdmin.text
                ?.toString()
                .orEmpty()

        etCorreoAdmin.error = null
        etContrasenaAdmin.error = null
        tvMensajeAdmin.visibility = View.GONE

        if (correo.isEmpty()) {
            etCorreoAdmin.error =
                "Ingresa el correo administrativo"

            etCorreoAdmin.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreoAdmin.error =
                "Ingresa un correo válido"

            etCorreoAdmin.requestFocus()
            return
        }

        if (contrasena.isEmpty()) {
            etContrasenaAdmin.error =
                "Ingresa la contraseña"

            etContrasenaAdmin.requestFocus()
            return
        }

        ocultarTeclado()
        mostrarCarga(true)

        auth.signInWithEmailAndPassword(
            correo,
            contrasena
        )
            .addOnSuccessListener { resultado ->

                val usuario =
                    resultado.user

                if (usuario == null) {
                    mostrarError(
                        "No se pudo verificar la cuenta"
                    )
                    return@addOnSuccessListener
                }

                verificarPermisoAdministrador(
                    uid = usuario.uid,
                    cerrarSesionSiFalla = true
                )
            }
            .addOnFailureListener { error ->

                Log.e(
                    "LOGIN_ADMIN",
                    "Error iniciando sesión",
                    error
                )

                mostrarError(
                    "Correo o contraseña incorrectos"
                )
            }
    }

    private fun verificarPermisoAdministrador(
        uid: String,
        cerrarSesionSiFalla: Boolean
    ) {
        db.collection("admins")
            .document(uid)
            .get()
            .addOnSuccessListener { documento ->

                val esAdministrador =
                    documento.exists() &&
                            documento.getBoolean("activo") == true

                if (esAdministrador) {
                    abrirMenuAdministrador()
                } else {
                    if (cerrarSesionSiFalla) {
                        auth.signOut()
                    }

                    mostrarError(
                        "Esta cuenta no tiene permiso de administrador"
                    )
                }
            }
            .addOnFailureListener { error ->

                Log.e(
                    "LOGIN_ADMIN",
                    "Error verificando administrador",
                    error
                )

                if (cerrarSesionSiFalla) {
                    auth.signOut()
                }

                mostrarError(
                    "No se pudo verificar el acceso administrativo"
                )
            }
    }

    private fun mostrarCarga(
        mostrando: Boolean
    ) {
        progressAdmin.visibility =
            if (mostrando) {
                View.VISIBLE
            } else {
                View.GONE
            }

        btnIngresarAdmin.isEnabled =
            !mostrando

        etCorreoAdmin.isEnabled =
            !mostrando

        etContrasenaAdmin.isEnabled =
            !mostrando
    }

    private fun mostrarError(
        mensaje: String
    ) {
        mostrarCarga(false)

        tvMensajeAdmin.text =
            mensaje

        tvMensajeAdmin.visibility =
            View.VISIBLE

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun ocultarTeclado() {
        val teclado =
            getSystemService(
                INPUT_METHOD_SERVICE
            ) as InputMethodManager

        teclado.hideSoftInputFromWindow(
            currentFocus?.windowToken,
            0
        )

        currentFocus?.clearFocus()
    }

    private fun abrirMenuAdministrador() {
        mostrarCarga(false)

        val intent =
            Intent(
                this,
                ActivityMenu::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        startActivity(intent)
        finish()
    }
}