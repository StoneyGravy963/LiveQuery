package com.example.livequery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.parse.ParseObject
import com.parse.ParseQuery

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: MensajeAdapter
    private val mensajes = mutableListOf<ParseObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputMensaje = findViewById<EditText>(R.id.inputMensaje)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        recycler = findViewById(R.id.recyclerMensajes)

        adapter = MensajeAdapter(this, mensajes)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        btnEnviar.setOnClickListener {
            val texto = inputMensaje.text.toString().trim()
            if (texto.isEmpty()) {
                Toast.makeText(this, "Escribe un mensaje", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val mensaje = ParseObject("Mensajes")
            mensaje.put("texto", texto)
            mensaje.saveInBackground { e ->
                if (e == null) {
                    Toast.makeText(this, "✅ Guardado", Toast.LENGTH_SHORT).show()
                    inputMensaje.text.clear()
                    cargarMensajes()
                } else {
                    Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cargarMensajes()
    }

    private fun cargarMensajes() {
        val query = ParseQuery.getQuery<ParseObject>("Mensajes")
        query.orderByDescending("createdAt")
        query.findInBackground { lista, e ->
            if (e == null) {
                mensajes.clear()
                mensajes.addAll(lista)
                adapter.updateList(mensajes)
            } else {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
