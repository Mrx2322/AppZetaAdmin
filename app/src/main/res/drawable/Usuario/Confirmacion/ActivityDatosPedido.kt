package com.example.appzetar.Usuario.Confirmacion

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appzetar.R

class ActivityDatosPedido : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etObservacion: EditText
    private lateinit var btnConfirmar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContentView(R.layout.activity_datos_pedido)

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
    }

    // ---------------------------------------------------------
    // COMPONENTES
    // ---------------------------------------------------------

    private fun initComponent() {

        etNombre =
            findViewById(R.id.etNombre)

        etTelefono =
            findViewById(R.id.etTelefono)

        etDireccion =
            findViewById(R.id.etDireccion)

        etObservacion =
            findViewById(R.id.etObservacion)

        btnConfirmar =
            findViewById(R.id.btnConfirmar)
    }

    // ---------------------------------------------------------
    // UI
    // ---------------------------------------------------------

    private fun initUI() {

        btnConfirmar.setOnClickListener {

            val nombre =
                etNombre.text.toString().trim()

            val telefono =
                etTelefono.text.toString().trim()

            val direccion =
                etDireccion.text.toString().trim()

            val observacion =
                etObservacion.text.toString().trim()

            // ---------------------------------------------
            // VALIDACIONES
            // ---------------------------------------------

            if (nombre.isEmpty()) {

                etNombre.error =
                    "Ingresa tu nombre"

                etNombre.requestFocus()

                return@setOnClickListener
            }

            if (telefono.isEmpty()) {

                etTelefono.error =
                    "Ingresa tu teléfono"

                etTelefono.requestFocus()

                return@setOnClickListener
            }

            if (direccion.isEmpty()) {

                etDireccion.error =
                    "Ingresa tu dirección"

                etDireccion.requestFocus()

                return@setOnClickListener
            }

            // ---------------------------------------------
            // IR AL RESUMEN DEL PEDIDO
            // ---------------------------------------------

            val intent =
                Intent(
                    this,
                    ActivityResumenPedido::class.java
                )

            intent.putExtra(
                "nombre",
                nombre
            )

            intent.putExtra(
                "telefono",
                telefono
            )

            intent.putExtra(
                "direccion",
                direccion
            )

            intent.putExtra(
                "observacion",
                observacion
            )

            startActivity(intent)
        }
    }
}