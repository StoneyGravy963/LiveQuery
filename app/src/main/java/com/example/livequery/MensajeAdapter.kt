package com.example.livequery

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.parse.ParseObject

class MensajeAdapter(
    private val contexto: Context,
    private var listaMensajes: MutableList<ParseObject>
) : RecyclerView.Adapter<MensajeAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textoMensajeView: TextView = view.findViewById(R.id.txtMensaje)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(contexto).inflate(R.layout.item_mensaje, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val objetoMensaje = listaMensajes[position]
        holder.textoMensajeView.text = objetoMensaje.getString("texto")

        holder.textoMensajeView.setOnClickListener {
            val campoEditarTexto = EditText(contexto)
            campoEditarTexto.setText(objetoMensaje.getString("texto"))

            AlertDialog.Builder(contexto)
                .setTitle("Editar mensaje")
                .setView(campoEditarTexto)
                .setPositiveButton("Guardar") { _, _ ->
                    val textoNuevo = campoEditarTexto.text.toString()
                    objetoMensaje.put("texto", textoNuevo)
                    objetoMensaje.saveInBackground { e ->
                        if (e == null) {
                            Toast.makeText(contexto, "Actualizado", Toast.LENGTH_SHORT).show()
                            notifyItemChanged(position)
                        } else {
                            Toast.makeText(contexto, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun getItemCount(): Int = listaMensajes.size
}