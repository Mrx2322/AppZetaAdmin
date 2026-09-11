package com.example.appzetar.Usuario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appzetar.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNombre: TextInputEditText
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var etConfirmarContrasena: TextInputEditText

    private lateinit var btnRegistrarse: Button

    private lateinit var auth: FirebaseAuth

    private val db =
        FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_registro
        )


        auth =
            FirebaseAuth.getInstance()


        etNombre =
            findViewById(R.id.etNombre)

        etCorreo =
            findViewById(R.id.etCorreo)

        etContrasena =
            findViewById(R.id.etContrasena)

        etConfirmarContrasena =
            findViewById(R.id.etConfirmarContrasena)

        btnRegistrarse =
            findViewById(R.id.btnRegistrarse)


        btnRegistrarse.setOnClickListener {

            registrarUsuario()
        }
    }


    // =========================================================
    // REGISTRAR
    // =========================================================

    private fun registrarUsuario() {

        val nombre =
            etNombre.text
                ?.toString()
                ?.trim()
                ?: ""

        val correo =
            etCorreo.text
                ?.toString()
                ?.trim()
                ?: ""

        val contrasena =
            etContrasena.text
                ?.toString()
                ?.trim()
                ?: ""

        val confirmar =
            etConfirmarContrasena.text
                ?.toString()
                ?.trim()
                ?: ""


        // -----------------------------------------------------
        // VALIDACIONES
        // -----------------------------------------------------

        if (nombre.isEmpty()) {

            etNombre.error =
                "Ingresa tu nombre"

            return
        }


        if (correo.isEmpty()) {

            etCorreo.error =
                "Ingresa tu correo"

            return
        }


        if (contrasena.isEmpty()) {

            etContrasena.error =
                "Ingresa una contraseña"

            return
        }


        if (contrasena.length < 6) {

            etContrasena.error =
                "Mínimo 6 caracteres"

            return
        }


        if (confirmar.isEmpty()) {

            etConfirmarContrasena.error =
                "Confirma tu contraseña"

            return
        }


        if (contrasena != confirmar) {

            etConfirmarContrasena.error =
                "Las contraseñas no coinciden"

            return
        }


        // -----------------------------------------------------
        // DESACTIVAR BOTÓN
        // -----------------------------------------------------

        btnRegistrarse.isEnabled = false


        // -----------------------------------------------------
        // CREAR USUARIO FIREBASE
        // -----------------------------------------------------

        auth.createUserWithEmailAndPassword(
            correo,
            contrasena
        )
            .addOnSuccessListener {

                val usuario =
                    auth.currentUser


                if (usuario == null) {

                    btnRegistrarse.isEnabled = true

                    return@addOnSuccessListener
                }


                val uid =
                    usuario.uid


                // -------------------------------------------------
                // DATOS DEL USUARIO
                // -------------------------------------------------

                val datos =
                    hashMapOf(
                        "uid" to uid,
                        "nombre" to nombre,
                        "correo" to correo
                    )


                // -------------------------------------------------
                // FIRESTORE
                // -------------------------------------------------

                db.collection("usuarios")
                    .document(uid)
                    .set(datos)

                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            "Cuenta creada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()


                        val intent =
                            Intent(
                                this,
                                ActivityMenuUsuario::class.java
                            )


                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK


                        startActivity(intent)

                        finish()
                    }

                    .addOnFailureListener {

                        btnRegistrarse.isEnabled = true

                        Toast.makeText(
                            this,
                            "Cuenta creada, pero hubo un error guardando los datos",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }

            .addOnFailureListener { error ->

                btnRegistrarse.isEnabled = true


                Toast.makeText(
                    this,
                    error.message
                        ?: "No se pudo crear la cuenta",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}