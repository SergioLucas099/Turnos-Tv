package com.example.turnostv

import android.media.MediaPlayer
import android.os.*
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.turnostv.model.Multimedia
import com.example.turnostv.model.TextoGuardado
import com.example.turnostv.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ImageView
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.turnostv.adapter.LlamandoTurnoAdapter
import com.example.turnostv.adapter.TurnosAdapter
import com.example.turnostv.model.Turnos
import android.speech.tts.TextToSpeech
import java.util.Locale

class VentanaPrincipal : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var imageView: ImageView
    private lateinit var txtHora: TextView
    private lateinit var txtFecha: TextView
    private lateinit var carruselText: TextView
    private lateinit var RevTurnos: RecyclerView
    private var currentTexto: String? = null
    private var currentVideoUrl: String? = null
    private var multimediaActual: Multimedia? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var turnosAdapter: TurnosAdapter
    private lateinit var contenedorLlamado: View
    private lateinit var llamadoAdapter: LlamandoTurnoAdapter
    private lateinit var RevLlamadoCentro: RecyclerView
    private var ultimoTurnoLlamado: String? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var textToSpeech: TextToSpeech
    private var ttsListo = false
    private val relojRunnable = object : Runnable {
        override fun run() {
            val horaActual =
                SimpleDateFormat("h:mm a", Locale.getDefault())
                    .format(Date())

            val fechaActual =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date())

            txtHora.text = horaActual
            txtFecha.text = fechaActual

            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventana_principal)

        videoView = findViewById(R.id.videoView)
        imageView = findViewById(R.id.imageView)
        txtHora = findViewById(R.id.txtHora)
        txtFecha = findViewById(R.id.txtFecha)
        carruselText = findViewById(R.id.carruselText)
        RevTurnos = findViewById(R.id.RevTurnos)

        handler.post(relojRunnable)

        turnosAdapter = TurnosAdapter(mutableListOf()) { }

        RevTurnos.adapter = turnosAdapter
        RevTurnos.layoutManager = LinearLayoutManager(this)

        contenedorLlamado = findViewById(R.id.contenedorLlamado)
        RevLlamadoCentro = findViewById(R.id.RevLlamadoCentro)

        llamadoAdapter = LlamandoTurnoAdapter(mutableListOf()) { }

        RevLlamadoCentro.adapter = llamadoAdapter
        RevLlamadoCentro.layoutManager = LinearLayoutManager(this)

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {

                val result = textToSpeech.setLanguage(Locale("es", "ES"))
                textToSpeech.setPitch(1.1f)   // tono
                textToSpeech.setSpeechRate(0.9f) // velocidad

                ttsListo = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }

        iniciarVerificacionVideo()
        obtenerTurnos()
        iniciarTextoCarrusel()
        iniciarTurnos()
    }

    private fun iniciarVerificacionVideo() {

        lifecycleScope.launch {

            while (true) {

                try {

                    val response = ApiClient.client.get(
                        "${ApiClient.BASE_URL}/multimedia/activo"
                    )

                    println("STATUS: ${response.status}")

                    multimediaActual = response.body()

                    println("VIDEO URL: ${multimediaActual?.url}")

                    val fullUrl =
                        "${ApiClient.BASE_URL}/${multimediaActual?.url}"

                    if (multimediaActual?.tipo == "VIDEO") {

                        imageView.visibility = View.GONE
                        videoView.visibility = View.VISIBLE

                        if (currentVideoUrl != fullUrl) {
                            currentVideoUrl = fullUrl
                            reproducirVideo(fullUrl)
                        }

                    } else if (multimediaActual?.tipo == "IMG") {

                        videoView.stopPlayback()
                        videoView.visibility = View.GONE
                        imageView.visibility = View.VISIBLE

                        if (currentVideoUrl != fullUrl) {
                            currentVideoUrl = fullUrl
                            cargarImagen(fullUrl)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                delay(5000)
            }
        }
    }

    private fun reproducirVideo(url: String) {

        videoView.setVideoPath(url)

        videoView.setOnPreparedListener { mp ->

            mp.isLooping = true

            // 🔊 Control dinámico de sonido
            if (multimediaActual?.sonido == true) {
                mp.setVolume(1f, 1f)
            } else {
                mp.setVolume(0f, 0f)
            }

            videoView.start()
        }

        videoView.setOnErrorListener { _, what, extra ->
            println("ERROR VIDEO: $what - $extra")
            true
        }
    }

    private fun cargarImagen(url: String) {

        Glide.with(this)
            .load(url)
            .into(imageView)
    }

    private fun obtenerTurnos() {

        lifecycleScope.launch {

            try {

                val lista: List<Turnos> =
                    ApiClient.client.get("${ApiClient.BASE_URL}/turnos").body()

                // 🔥 1. FILTRAR APROBADOS (lado derecho)
                val aprobados = lista.filter { it.estado == "APROBADO" }
                turnosAdapter.actualizarLista(aprobados)

                // 🔥 2. FILTRAR LLAMADOS (centro)
                val llamados = lista
                    .filter { it.llamandoTurno == true }
                    .sortedBy { it.fecha }

                if (llamados.isNotEmpty()) {

                    contenedorLlamado.visibility = View.VISIBLE
                    llamadoAdapter.actualizarLista(llamados)

                    val turnoActual = llamados.first()._id

                    // 🔥 evitar repetir sonido
                    if (ultimoTurnoLlamado != turnoActual) {
                        ultimoTurnoLlamado = turnoActual
                        reproducirLlamadoConVoz(llamados.first())
                    }

                } else {

                    contenedorLlamado.visibility = View.GONE

                    mediaPlayer?.release()
                    mediaPlayer = null

                    ultimoTurnoLlamado = null
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun iniciarTextoCarrusel() {

        lifecycleScope.launch {

            while (true) {

                try {

                    val texto: TextoGuardado =
                        ApiClient.client.get("${ApiClient.BASE_URL}/textos/activo").body()

                    if (currentTexto != texto.texto) {

                        currentTexto = texto.texto
                        carruselText.text = texto.texto

                        carruselText.isSelected = true // 🔥 activa marquee
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                delay(5000)
            }
        }
    }

    private fun iniciarTurnos() {

        lifecycleScope.launch {

            while (true) {

                obtenerTurnos()
                delay(3000)

            }
        }
    }

    private fun reproducirSonidoLlamado() {

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.llamado)
            mediaPlayer?.start()
        }
    }

    private fun reproducirLlamadoConVoz(turno: Turnos) {

        val media = MediaPlayer.create(this, R.raw.llamado)

        media.setOnCompletionListener {

            media.release()

            if (ttsListo) {

                val mensaje = "Llamando turno ${turno.numeroTurno} para ${turno.nombreAtraccion}"

                textToSpeech.speak(
                    mensaje,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            }
        }

        media.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}