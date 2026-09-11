package com.example.appzetar.Usuario.Pagos

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appzetar.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.appzetar.Usuario.Carrito.PedidoManager

class ActivityPagoYape : AppCompatActivity() {

    // =========================================================
    // COMPONENTES
    // =========================================================

    private lateinit var tvTotal: TextView

    private lateinit var etOperacion: TextInputEditText

    private lateinit var btnYaPague: MaterialButton


    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_pago_yape
        )

        initComponent()

        mostrarTotal()

        configurarBoton()
    }


    // =========================================================
    // INICIALIZAR COMPONENTES
    // =========================================================

    private fun initComponent() {

        tvTotal =
            findViewById(
                R.id.tvTotal
            )

        etOperacion =
            findViewById(
                R.id.etOperacion
            )

        btnYaPague =
            findViewById(
                R.id.btnYaPague
            )
    }


    // =========================================================
    // MOSTRAR TOTAL
    // =========================================================

    private fun mostrarTotal() {

        val total =
            PedidoManager.pedido.sumOf {

                it.precio * it.cantidad
            }

        tvTotal.text =
            "Total: S/ %.2f".format(
                total
            )
    }


    // =========================================================
    // BOTÓN YA PAGUÉ
    // =========================================================

    private fun configurarBoton() {

        btnYaPague.setOnClickListener {

            val operacion =
                etOperacion.text
                    ?.toString()
                    ?.trim()
                    ?: ""


            // -------------------------------------------------
            // VALIDAR OPERACIÓN
            // -------------------------------------------------

            if (operacion.isEmpty()) {

                etOperacion.error =
                    "Ingresa el número de operación"

                etOperacion.requestFocus()

                return@setOnClickListener
            }


            // -------------------------------------------------
            // POR AHORA
            // -------------------------------------------------

            Toast.makeText(
                this,
                "Yape estará disponible muy pronto 🚀",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}