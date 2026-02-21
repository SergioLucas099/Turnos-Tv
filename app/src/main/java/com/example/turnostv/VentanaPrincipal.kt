package com.example.turnostv

import android.os.*
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.turnostv.model.Multimedia
import com.example.turnostv.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class VentanaPrincipal : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var txtHora: TextView
    private lateinit var txtFecha: TextView

    private var currentVideoUrl: String? = null
    private var multimediaActual: Multimedia? = null

    private val handler = Handler(Looper.getMainLooper())

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
        txtHora = findViewById(R.id.txtHora)
        txtFecha = findViewById(R.id.txtFecha)

        handler.post(relojRunnable)

        iniciarVerificacionVideo()
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

                    if (currentVideoUrl != fullUrl) {

                        currentVideoUrl = fullUrl

                        reproducirVideo(fullUrl)
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
}