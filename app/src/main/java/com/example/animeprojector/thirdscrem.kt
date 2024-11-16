package com.example.animeprojector

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.animeprojector.ui.theme.AnimeProjectorTheme
import androidx.core.view.WindowCompat
import android.view.ViewGroup
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import java.io.BufferedReader
import java.io.InputStreamReader
import android.content.Context


class thirdscrem : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar el ajuste de ventanas para pantalla completa
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AnimeProjectorTheme {
                // Establecer un fondo negro para toda la pantalla
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    // Incluir el contenido de la WebView en un Surface
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        WebViewScreen2()
                    }
                }
            }
        }
    }
}

@Composable
fun WebViewScreen2() {
    var url by remember { mutableStateOf("https://repelisplus.lat/") }

    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val adHosts by remember { mutableStateOf(loadEasyList(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        AndroidView(
            factory = { context ->
                WebView(context).apply {


                    // Habilitar cookies
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    // Configurar el WebView para videos HTML5
                    settings.apply {
                        javaScriptEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }

                    // Habilitar la aceleración por hardware
                    setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

                    webChromeClient = object : WebChromeClient() {
                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            // Handle fullscreen video view
                            (context as? ComponentActivity)?.apply {
                                val decorView = window.decorView as ViewGroup
                                val fullScreenView = view ?: return

                                // Add full screen view to decor view
                                decorView.addView(fullScreenView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                fullScreenView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

                                // Hide the action bar
                                actionBar?.hide()
                            }
                            callback?.onCustomViewHidden()
                        }

                        override fun onHideCustomView() {
                            // Handle hiding fullscreen video view
                            (context as? ComponentActivity)?.apply {
                                val decorView = window.decorView as ViewGroup
                                decorView.removeAllViews()

                                // Show the action bar
                                actionBar?.show()
                            }
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            val url = request.url.toString()
                            // Manejar esquemas de URL personalizados si es necesario
                            if (url.startsWith("mailto:") || url.startsWith("tel:")) {
                                val intent = Intent(Intent.ACTION_VIEW, request.url)
                                context.startActivity(intent)
                                return true
                            }
                            // Bloquear anuncios
                            if (isAdUrl2(url, adHosts)) {
                                return true // Bloquear la carga de la URL
                            }
                            return false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            // Ajustar el contenido del video después de que la página se haya cargado
                            view?.loadUrl("javascript:(function() { " +
                                    "var videos = document.getElementsByTagName('video');" +
                                    "for(var i=0;i<videos.length;i++){" +
                                    "videos[i].style.objectFit = 'fill';" +
                                    "}" +
                                    "})()")
                        }
                    }

                    loadUrl(url)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Enter URL") },
                text = {
                    Column {
                        TextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("URL") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                    }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

// Función para determinar si una URL es un anuncio

fun loadEasyList(context: Context): List<String> {
    val adHosts = mutableListOf<String>()
    try {
        context.assets.open("easylist.txt").use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.forEachLine { line ->
                    if (line.isNotBlank() && !line.startsWith("!") && !line.startsWith("[")) {
                        adHosts.add(line.trim())
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return adHosts
}

fun isAdUrl2(url: String, adHosts: List<String>): Boolean {
    return adHosts.any { url.contains(it, ignoreCase = true) }
}