package com.example.appzetar.Usuario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.example.appzetar.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: TextInputEditText
    private lateinit var etContrasena: TextInputEditText

    private lateinit var btnIniciarSesion: Button
    private lateinit var btnGoogle: MaterialButton
    private lateinit var tvRegistrarse: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)

        btnIniciarSesion = findViewById(R.id.btnIniciarSesion)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvRegistrarse = findViewById(R.id.tvRegistrarse)

        btnIniciarSesion.setOnClickListener {
            iniciarSesion()
        }

        btnGoogle.setOnClickListener {
            iniciarSesionConGoogle()
        }

        tvRegistrarse.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RegistroActivity::class.java
                )
            )
        }
    }

    private fun iniciarSesion() {

        val correo = etCorreo.text?.toString()?.trim() ?: ""
        val contrasena = etContrasena.text?.toString()?.trim() ?: ""

        if (correo.isEmpty()) {
            etCorreo.error = "Ingresa tu correo"
            return
        }

        if (contrasena.isEmpty()) {
            etContrasena.error = "Ingresa tu contraseña"
            return
        }

        btnIniciarSesion.isEnabled = false

        auth.signInWithEmailAndPassword(
            correo,
            contrasena
        )
            .addOnSuccessListener {
                abrirMenuUsuario()
            }
            .addOnFailureListener {
                btnIniciarSesion.isEnabled = true

                Toast.makeText(
                    this,
                    "Correo o contraseña incorrectos",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun iniciarSesionConGoogle() {

        btnGoogle.isEnabled = false

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(
                getString(R.string.default_web_client_id)
            )
            .build()

        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        credentialManager.getCredentialAsync(
            this,
            solicitud,
            null,
            ContextCompat.getMainExecutor(this),
            object :
                CredentialManagerCallback<
                        GetCredentialResponse,
                        GetCredentialException
                        > {

                override fun onResult(
                    resultado: GetCredentialResponse
                ) {
                    procesarCredencialGoogle(
                        resultado.credential
                    )
                }

                override fun onError(
                    error: GetCredentialException
                ) {
                    btnGoogle.isEnabled = true

                    Toast.makeText(
                        this@LoginActivity,
                        "No se pudo iniciar sesión con Google",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun procesarCredencialGoogle(
        credential: androidx.credentials.Credential
    ) {

        if (
            credential is CustomCredential &&
            credential.type ==
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential =
                GoogleIdTokenCredential.createFrom(
                    credential.data
                )

            autenticarConFirebase(
                googleCredential.idToken
            )
        } else {
            btnGoogle.isEnabled = true

            Toast.makeText(
                this,
                "No se pudo obtener la cuenta de Google",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun autenticarConFirebase(
        idToken: String
    ) {

        val credential =
            GoogleAuthProvider.getCredential(
                idToken,
                null
            )

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                crearPerfilGoogleSiNoExiste()
            }
            .addOnFailureListener {
                btnGoogle.isEnabled = true

                Toast.makeText(
                    this,
                    "No se pudo iniciar sesión con Google",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun crearPerfilGoogleSiNoExiste() {

        val usuario = auth.currentUser ?: return

        val referencia =
            db.collection("usuarios")
                .document(usuario.uid)

        referencia.get()
            .addOnSuccessListener { documento ->

                if (documento.exists()) {
                    abrirMenuUsuario()
                    return@addOnSuccessListener
                }

                val datos = hashMapOf(
                    "uid" to usuario.uid,
                    "nombre" to (
                            usuario.displayName ?: "Cliente"
                            ),
                    "correo" to (
                            usuario.email ?: ""
                            )
                )

                referencia.set(datos)
                    .addOnSuccessListener {
                        abrirMenuUsuario()
                    }
                    .addOnFailureListener {
                        btnGoogle.isEnabled = true

                        Toast.makeText(
                            this,
                            "La cuenta ingresó, pero no se pudo crear el perfil",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener {
                btnGoogle.isEnabled = true

                Toast.makeText(
                    this,
                    "No se pudo verificar el perfil",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun abrirMenuUsuario() {

        Toast.makeText(
            this,
            "Inicio de sesión correcto",
            Toast.LENGTH_SHORT
        ).show()

        val intent = Intent(
            this,
            ActivityMenuUsuario::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }
}