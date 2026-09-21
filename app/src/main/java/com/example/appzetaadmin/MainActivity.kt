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
import com.google.firebase.firestore.Source

class MainActivity : AppCompatActivity() {

    private lateinit var etCorreoAdmin: TextInputEditText
    private lateinit var etContrasenaAdmin: TextInputEditText
    private lateinit var btnIngresarAdmin: MaterialButton
    private lateinit var progressAdmin: View
    private lateinit var tvMensajeAdmin: TextView

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    private val db by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iniciarComponentes()
        configurarEventos()
        comprobarSesionExistente()
    }

    private fun iniciarComponentes() {
        etCorreoAdmin = findViewById(R.id.etCorreoAdmin)
        etContrasenaAdmin = findViewById(R.id.etContrasenaAdmin)
        btnIngresarAdmin = findViewById(R.id.btnIngresarAdmin)
        progressAdmin = findViewById(R.id.progressAdmin)
        tvMensajeAdmin = findViewById(R.id.tvMensajeAdmin)
    }

    private fun configurarEventos() {
        btnIngresarAdmin.setOnClickListener {
            iniciarSesionAdmin()
        }
    }

    private fun comprobarSesionExistente() {
        val usuarioActual = auth.currentUser ?: return

        mostrarCarga(true)
        verificarPermisoAdministrador(usuarioActual.uid)
    }

    private fun iniciarSesionAdmin() {
        val correo = etCorreoAdmin.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val contrasena = etContrasenaAdmin.text
            ?.toString()
            .orEmpty()

        limpiarErrores()

        if (correo.isBlank()) {
            etCorreoAdmin.error = "Ingresa el correo administrativo"
            etCorreoAdmin.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreoAdmin.error = "Ingresa un correo válido"
            etCorreoAdmin.requestFocus()
            return
        }

        if (contrasena.isBlank()) {
            etContrasenaAdmin.error = "Ingresa la contraseña"
            etContrasenaAdmin.requestFocus()
            return
        }

        ocultarTeclado()
        mostrarCarga(true)

        auth.signInWithEmailAndPassword(correo, contrasena)
            .addOnSuccessListener { resultado ->
                val usuario = resultado.user

                if (usuario == null) {
                    auth.signOut()
                    mostrarError("No se pudo verificar la cuenta")
                    return@addOnSuccessListener
                }

                verificarPermisoAdministrador(usuario.uid)
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error iniciando sesión", error)
                mostrarError("Correo o contraseña incorrectos")
            }
    }

    private fun verificarPermisoAdministrador(uid: String) {
        db.collection(COLECCION_ADMINISTRADORES)
            .document(uid)
            .get(Source.SERVER)
            .addOnSuccessListener { documento ->
                val activo = documento.getBoolean(CAMPO_ACTIVO) == true
                val rolCorrecto = documento.getString(CAMPO_ROL) == ROL_ADMIN

                if (documento.exists() && activo && rolCorrecto) {
                    abrirMenuAdministrador()
                } else {
                    rechazarAcceso()
                }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error verificando administrador", error)
                auth.signOut()
                mostrarError("No se pudo verificar el acceso administrativo")
            }
    }

    private fun rechazarAcceso() {
        auth.signOut()
        etContrasenaAdmin.text?.clear()
        mostrarError("Esta cuenta no tiene permiso de administrador")
    }

    private fun limpiarErrores() {
        etCorreoAdmin.error = null
        etContrasenaAdmin.error = null
        tvMensajeAdmin.visibility = View.GONE
    }

    private fun mostrarCarga(mostrando: Boolean) {
        progressAdmin.visibility = if (mostrando) View.VISIBLE else View.GONE
        btnIngresarAdmin.isEnabled = !mostrando
        etCorreoAdmin.isEnabled = !mostrando
        etContrasenaAdmin.isEnabled = !mostrando
    }

    private fun mostrarError(mensaje: String) {
        mostrarCarga(false)
        tvMensajeAdmin.text = mensaje
        tvMensajeAdmin.visibility = View.VISIBLE

        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun ocultarTeclado() {
        val teclado = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        teclado.hideSoftInputFromWindow(
            currentFocus?.windowToken,
            0
        )

        currentFocus?.clearFocus()
    }

    private fun abrirMenuAdministrador() {
        mostrarCarga(false)

        val intent = Intent(this, ActivityMenu::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    private companion object {
        const val TAG = "LOGIN_ADMIN"
        const val COLECCION_ADMINISTRADORES = "administradores"
        const val CAMPO_ACTIVO = "activo"
        const val CAMPO_ROL = "rol"
        const val ROL_ADMIN = "admin"
    }
}