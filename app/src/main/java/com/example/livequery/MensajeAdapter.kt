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
    private val context: Context,
    private var mensajes: MutableList<ParseObject>
) : RecyclerView.Adapter<MensajeAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMensaje: TextView = view.findViewById(R.id.txtMensaje)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_mensaje, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mensaje = mensajes[position]
        holder.txtMensaje.text = mensaje.getString("texto")

        holder.txtMensaje.setOnClickListener {
            val editText = EditText(context)
            editText.setText(mensaje.getString("texto"))

            AlertDialog.Builder(context)
                .setTitle("Editar mensaje")
                .setView(editText)
                .setPositiveButton("Guardar") { _, _ ->
                    val nuevoTexto = editText.text.toString()
                    mensaje.put("texto", nuevoTexto)
                    mensaje.saveInBackground { e ->
                        if (e == null) {
                            Toast.makeText(context, "✅ Actualizado", Toast.LENGTH_SHORT).show()
                            notifyItemChanged(position)
                        } else {
                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun getItemCount(): Int = mensajes.size

    fun updateList(newList: MutableList<ParseObject>) {
        mensajes = newList
        notifyDataSetChanged()
    }
}
