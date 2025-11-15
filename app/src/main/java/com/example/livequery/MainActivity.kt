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

    private lateinit var listaMensajesView: RecyclerView
    private lateinit var mensajesAdapter: MensajeAdapter
    private val listaMensajes = mutableListOf<ParseObject>()
    private var clienteLiveQuery: ParseLiveQueryClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val campoEntradaMensaje = findViewById<EditText>(R.id.inputMensaje)
        val botonEnviar = findViewById<Button>(R.id.btnEnviar)
        listaMensajesView = findViewById(R.id.recyclerMensajes)

        mensajesAdapter = MensajeAdapter(this, listaMensajes)
        listaMensajesView.layoutManager = LinearLayoutManager(this)
        listaMensajesView.adapter = mensajesAdapter

        botonEnviar.setOnClickListener {
            val textoMensaje = campoEntradaMensaje.text.toString().trim()
            if (textoMensaje.isEmpty()) {
                Toast.makeText(this, "Escribe un mensaje", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val objetoMensaje = ParseObject("Mensajes")
            objetoMensaje.put("texto", textoMensaje)

            val permisosACL = com.parse.ParseACL()
            permisosACL.publicReadAccess = true
            objetoMensaje.acl = permisosACL

            objetoMensaje.saveInBackground { e ->
                if (e == null) {
                    Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show()
                    campoEntradaMensaje.text.clear()
                } else {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cargarMensajesIniciales()
        iniciarLiveQuery()
    }

    private fun cargarMensajesIniciales() {
        val consulta = ParseQuery.getQuery<ParseObject>("Mensajes")
        consulta.orderByDescending("createdAt")
        consulta.findInBackground { listaRecibida, e ->
            if (e == null) {
                listaMensajes.clear()
                listaMensajes.addAll(listaRecibida)
                mensajesAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Error cargando mensajes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun iniciarLiveQuery() {
        val uriWss = URI("wss://livequeryuaa.b4a.io")
        clienteLiveQuery = ParseLiveQueryClient.Factory.getClient(uriWss)

        val consulta = ParseQuery.getQuery<ParseObject>("Mensajes")
        val suscripcion = clienteLiveQuery?.subscribe(consulta)

        suscripcion?.handleEvent(SubscriptionHandling.Event.CREATE) { _, objetoParse ->
            Log.d("LIVEQUERY", "Evento CREATE recibido: ${objetoParse.getString("texto")}")
            runOnUiThread {
                listaMensajes.add(0, objetoParse)
                mensajesAdapter.notifyItemInserted(0)
                listaMensajesView.scrollToPosition(0)
            }
        }

        suscripcion?.handleEvent(SubscriptionHandling.Event.UPDATE) { _, objetoParse ->
            Log.d("LIVEQUERY", "Evento UPDATE recibido: ${objetoParse.getString("texto")}")
            runOnUiThread {
                val indice = listaMensajes.indexOfFirst { it.objectId == objetoParse.objectId }
                if (indice != -1) {
                    listaMensajes[indice] = objetoParse
                    mensajesAdapter.notifyItemChanged(indice)
                }
            }
        }
    }
}