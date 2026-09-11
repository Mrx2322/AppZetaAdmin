package com.example.appzetar.Usuario

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appzetar.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ActivityEntrega : AppCompatActivity() {

    private lateinit var cardDelivery: MaterialCardView
    private lateinit var cardRecojo: MaterialCardView

    private lateinit var rbDelivery: RadioButton
    private lateinit var rbRecojo: RadioButton

    private lateinit var cardContraEntrega: MaterialCardView
    private lateinit var cardYape: MaterialCardView

    private lateinit var rbContraEntrega: RadioButton
    private lateinit var rbYape: RadioButton

    private lateinit var btnContinuar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_entrega)

        initComponent()
        initUI()
    }

    private fun initComponent() {

        cardDelivery = findViewById(R.id.cardDelivery)
        cardRecojo = findViewById(R.id.cardRecojo)

        rbDelivery = findViewById(R.id.rbDelivery)
        rbRecojo = findViewById(R.id.rbRecojo)

        cardContraEntrega = findViewById(R.id.cardContraEntrega)
        cardYape = findViewById(R.id.cardYape)

        rbContraEntrega = findViewById(R.id.rbContraEntrega)
        rbYape = findViewById(R.id.rbYape)

        btnContinuar = findViewById(R.id.btnContinuarEntrega)
    }

    private fun initUI() {

        rbDelivery.isChecked = false
        rbRecojo.isChecked = false

        rbContraEntrega.isChecked = true
        rbYape.isChecked = false

        actualizarSeleccionEntrega()
        actualizarSeleccionPago()

        cardDelivery.setOnClickListener {
            seleccionarDelivery()
        }

        rbDelivery.setOnClickListener {
            seleccionarDelivery()
        }

        cardRecojo.setOnClickListener {
            seleccionarRecojo()
        }

        rbRecojo.setOnClickListener {
            seleccionarRecojo()
        }

        cardContraEntrega.setOnClickListener {
            seleccionarContraEntrega()
        }

        rbContraEntrega.setOnClickListener {
            seleccionarContraEntrega()
        }

        cardYape.setOnClickListener {
            mostrarYapeProximamente()
        }

        rbYape.setOnClickListener {
            mostrarYapeProximamente()
        }

        btnContinuar.setOnClickListener {
            continuar()
        }
    }

    private fun seleccionarDelivery() {

        rbDelivery.isChecked = true
        rbRecojo.isChecked = false

        actualizarSeleccionEntrega()
    }

    private fun seleccionarRecojo() {

        rbDelivery.isChecked = false
        rbRecojo.isChecked = true

        actualizarSeleccionEntrega()
    }

    private fun seleccionarContraEntrega() {

        rbContraEntrega.isChecked = true
        rbYape.isChecked = false

        actualizarSeleccionPago()
    }

    private fun actualizarSeleccionEntrega() {

        cardDelivery.strokeWidth =
            if (rbDelivery.isChecked) 2 else 0

        cardRecojo.strokeWidth =
            if (rbRecojo.isChecked) 2 else 0
    }

    private fun actualizarSeleccionPago() {

        cardContraEntrega.strokeWidth =
            if (rbContraEntrega.isChecked) 2 else 0

        cardYape.strokeWidth = 0
    }

    private fun mostrarYapeProximamente() {

        rbContraEntrega.isChecked = true
        rbYape.isChecked = false

        actualizarSeleccionPago()

        Toast.makeText(
            this,
            "Yape estará disponible muy pronto. Por ahora puedes pagar contra entrega.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun continuar() {

        if (!rbContraEntrega.isChecked) {
            mostrarYapeProximamente()
            return
        }

        if (rbDelivery.isChecked) {

            val intent = Intent(
                this,
                ActivityDireccion::class.java
            )

            intent.putExtra(
                "metodoPago",
                "Contra entrega"
            )

            intent.putExtra(
                "tipoEntrega",
                "Delivery"
            )

            startActivity(intent)

            return
        }

        if (rbRecojo.isChecked) {

            val intent = Intent(
                this,
                ActivityConfirmarPedido::class.java
            )

            intent.putExtra(
                "metodoPago",
                "Contra entrega"
            )

            intent.putExtra(
                "tipoEntrega",
                "Recojo en tienda"
            )

            startActivity(intent)

            return
        }

        Toast.makeText(
            this,
            "Selecciona cómo quieres recibir tu pedido.",
            Toast.LENGTH_SHORT
        ).show()
    }
}