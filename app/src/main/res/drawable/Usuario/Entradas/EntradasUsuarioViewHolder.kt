package com.example.appzetar.Usuario.Entradas

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appzetar.AdminMenu.EntradasAdmin.TaskEntradas
import com.example.appzetar.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class EntradasUsuarioViewHolder(
    view: View
) : RecyclerView.ViewHolder(view) {

    private val cardEntrada: MaterialCardView =
        view.findViewById(R.id.cardEntrada)

    private val tvEntradasName: TextView =
        view.findViewById(R.id.tvNombrePlato)

    private val tvStockEntrada: TextView =
        view.findViewById(R.id.tvStockEntrada)

    private val btnAgregarEntrada: MaterialButton =
        view.findViewById(R.id.btnAgregarEntrada)

    fun render(
        taskEntradas: TaskEntradas,
        cantidadSeleccionada: Int,
        maximoSeleccionable: Int,
        onAlternarClick: () -> Unit
    ) {
        tvEntradasName.text = taskEntradas.nombre

        val seleccionada = cantidadSeleccionada > 0

        mostrarEstadoSeleccion(seleccionada)

        val agotada =
            !taskEntradas.disponible ||
                    taskEntradas.stock <= 0

        when {
            agotada -> {
                tvStockEntrada.text = "Agotado"
                tvStockEntrada.setTextColor(
                    Color.parseColor("#D32F2F")
                )
            }

            taskEntradas.stock <= 3 -> {
                tvStockEntrada.text = when (taskEntradas.stock) {
                    1 -> "¡Última unidad!"
                    else -> "¡Últimas ${taskEntradas.stock} unidades!"
                }

                tvStockEntrada.setTextColor(
                    Color.parseColor("#E65100")
                )
            }

            else -> {
                tvStockEntrada.text = "Disponible"

                tvStockEntrada.setTextColor(
                    Color.parseColor("#757575")
                )
            }
        }

        // Si ya está seleccionada, siempre permitimos quitarla.
        val puedeInteractuar =
            seleccionada ||
                    (!agotada && maximoSeleccionable > 0)

        btnAgregarEntrada.isEnabled = puedeInteractuar
        btnAgregarEntrada.alpha =
            if (puedeInteractuar) 1f else 0.45f

        btnAgregarEntrada.setOnClickListener {
            onAlternarClick()
        }

        itemView.setOnClickListener(null)
    }

    private fun mostrarEstadoSeleccion(
        seleccionada: Boolean
    ) {
        if (seleccionada) {
            cardEntrada.setCardBackgroundColor(
                Color.parseColor("#FFF3E8")
            )

            cardEntrada.strokeColor =
                Color.parseColor("#E87520")

            cardEntrada.strokeWidth = dpToPx(2)

            btnAgregarEntrada.text = "Quitar"
            btnAgregarEntrada.setTextColor(
                Color.parseColor("#E87520")
            )

            btnAgregarEntrada.backgroundTintList =
                ColorStateList.valueOf(
                    Color.parseColor("#FFE0C2")
                )
        } else {
            cardEntrada.setCardBackgroundColor(
                Color.parseColor("#FFFFFF")
            )

            cardEntrada.strokeColor =
                Color.parseColor("#EEEEEE")

            cardEntrada.strokeWidth = dpToPx(1)

            btnAgregarEntrada.text = "Agregar"
            btnAgregarEntrada.setTextColor(
                Color.parseColor("#FFFFFF")
            )

            btnAgregarEntrada.backgroundTintList =
                ColorStateList.valueOf(
                    Color.parseColor("#E87520")
                )
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (
                dp * itemView.resources.displayMetrics.density
                ).toInt()
    }
}