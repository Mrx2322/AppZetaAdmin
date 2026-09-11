package com.example.appzetar.Usuario

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appzetar.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ActivityDireccion : AppCompatActivity() {

    // =========================================================
    // COMPONENTES
    // =========================================================

    private lateinit var etDireccion: TextInputEditText
    private lateinit var etReferencia: TextInputEditText
    private lateinit var etTelefono: TextInputEditText

    private lateinit var btnContinuar: MaterialButton


    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_direccion
        )


        // =====================================================
        // INICIALIZAR COMPONENTES
        // =====================================================

        etDireccion =
            findViewById(
                R.id.etDireccion
            )

        etReferencia =
            findViewById(
                R.id.etReferencia
            )

        etTelefono =
            findViewById(
                R.id.etTelefono
            )

        btnContinuar =
            findViewById(
                R.id.btnContinuarDireccion
            )


        // =====================================================
        // BOTÓN CONTINUAR
        // =====================================================

        btnContinuar.setOnClickListener {

            continuarAConfirmacion()
        }
    }


    // =========================================================
    // CONTINUAR A CONFIRMAR PEDIDO
    // =========================================================

    private fun continuarAConfirmacion() {

        val direccion =
            etDireccion.text
                ?.toString()
                ?.trim()
                ?: ""


        val referencia =
            etReferencia.text
                ?.toString()
                ?.trim()
                ?: ""


        val telefono =
            etTelefono.text
                ?.toString()
                ?.trim()
                ?: ""


        // =====================================================
        // VALIDAR DIRECCIÓN
        // =====================================================

        if (direccion.isEmpty()) {

            etDireccion.error =
                "Ingresa tu dirección"

            etDireccion.requestFocus()

            return
        }


        // =====================================================
        // VALIDAR TELÉFONO
        // =====================================================

        if (telefono.isEmpty()) {

            etTelefono.error =
                "Ingresa tu número de teléfono"

            etTelefono.requestFocus()

            return
        }


        if (telefono.length != 9) {

            etTelefono.error =
                "El teléfono debe tener 9 dígitos"

            etTelefono.requestFocus()

            return
        }


        // =====================================================
        // IR A CONFIRMAR PEDIDO
        // =====================================================

        val intentConfirmacion =
            Intent(
                this,
                ActivityConfirmarPedido::class.java
            )


        // =====================================================
        // ENVIAR DATOS
        // =====================================================

        intentConfirmacion.putExtra(
            "tipoEntrega",
            "Delivery"
        )

        intentConfirmacion.putExtra(
            "metodoPago",
            "Contra entrega"
        )

        intentConfirmacion.putExtra(
            "direccion",
            direccion
        )

        intentConfirmacion.putExtra(
            "referencia",
            referencia
        )

        intentConfirmacion.putExtra(
            "telefono",
            telefono
        )


        // =====================================================
        // ABRIR CONFIRMACIÓN
        // =====================================================

        startActivity(
            intentConfirmacion
        )
    }
}