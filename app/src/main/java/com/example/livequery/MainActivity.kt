package com.example.livequery

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import java.net.URI

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: MensajeAdapter
    private val mensajes = mutableListOf<ParseObject>()
    private var liveQueryClient: ParseLiveQueryClient? = null

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

            // Asigna permisos de lectura pública para que LiveQuery reciba el evento CREATE
            val acl = com.parse.ParseACL()
            acl.publicReadAccess = true
            mensaje.acl = acl

            mensaje.saveInBackground { e ->
                if (e == null) {
                    Toast.makeText(this, "✅ Guardado", Toast.LENGTH_SHORT).show()
                    inputMensaje.text.clear()
                    // El LiveQuery se encargará de actualizar la UI
                } else {
                    Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cargarMensajesIniciales()
        iniciarLiveQuery()
    }

    private fun cargarMensajesIniciales() {
        val query = ParseQuery.getQuery<ParseObject>("Mensajes")
        query.orderByDescending("createdAt")
        query.findInBackground { lista, e ->
            if (e == null) {
                mensajes.clear()
                mensajes.addAll(lista)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Error cargando mensajes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun iniciarLiveQuery() {
        // Obtiene una instancia del cliente LiveQuery
        // Se inicializa explícitamente con la URL de LiveQuery proporcionada por Back4App
        val wssUri = URI("wss://livequeryuaa.b4a.io")
        liveQueryClient = ParseLiveQueryClient.Factory.getClient(wssUri)

        val query = ParseQuery.getQuery<ParseObject>("Mensajes")
        val subscription = liveQueryClient?.subscribe(query)

        subscription?.handleEvent(SubscriptionHandling.Event.CREATE) { _, obj ->
            Log.d("LIVEQUERY", "Evento CREATE recibido: ${obj.getString("texto")}")
            runOnUiThread {
                mensajes.add(0, obj) // Añade al principio de la lista
                adapter.notifyItemInserted(0)
                recycler.scrollToPosition(0)
            }
        }

        subscription?.handleEvent(SubscriptionHandling.Event.UPDATE) { _, obj ->
            Log.d("LIVEQUERY", "Evento UPDATE recibido: ${obj.getString("texto")}")
            runOnUiThread {
                val index = mensajes.indexOfFirst { it.objectId == obj.objectId }
                if (index != -1) {
                    mensajes[index] = obj
                    adapter.notifyItemChanged(index)
                }
            }
        }
    }
}
