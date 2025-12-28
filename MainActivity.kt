package com.optv.player

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      val red = colorResource(R.color.red)
      val deepRed = colorResource(R.color.deep_red)
      val black = colorResource(R.color.black)
      val gray = colorResource(R.color.gray)
      val white = colorResource(R.color.white)

      MaterialTheme(colorScheme = darkColorScheme(
        primary = red, secondary = deepRed, background = black, surface = Color(0xFF1A1A1A),
        onPrimary = white, onBackground = white, onSurface = white
      )) {
        AppRoot(gray = gray)
      }
    }
  }

  @Composable
  private fun AppRoot(gray: Color) {
    var server by remember { mutableStateOf("http://kodytv.ddns.net:8080") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ready") }
    var loggedIn by remember { mutableStateOf(false) }
    var api: XtreamApi? by remember { mutableStateOf(null) }

    val scope = rememberCoroutineScope()

    Surface(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
      if (!loggedIn) {
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.Center) {
          Text("OPTV Login", fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineMedium)
          Spacer(Modifier.height(14.dp))

          OutlinedTextField(value = server, onValueChange = { server = it }, label = { Text("Server URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          Spacer(Modifier.height(10.dp))
          OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          Spacer(Modifier.height(10.dp))
          OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, singleLine = true, modifier = Modifier.fillMaxWidth())

          Spacer(Modifier.height(14.dp))
          Button(
            onClick = {
              scope.launch {
                status = "Checking..."
                val a = XtreamApi(server.trim(), user.trim(), pass.trim())
                val ok = withContext(Dispatchers.IO) { a.auth().user_info != null }
                if (ok) {
                  api = a
                  loggedIn = true
                } else {
                  status = "Login failed"
                }
              }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
          ) { Text("CONNECT", fontWeight = FontWeight.Black) }

          Spacer(Modifier.height(10.dp))
          Text(status, color = gray)
        }
      } else {
        Dashboard(api = api!!, onPlay = { title, url ->
          startActivity(Intent(this@MainActivity, PlayerActivity::class.java).apply {
            putExtra("title", title)
            putExtra("url", url)
          })
        })
      }
    }
  }

  @Composable
  private fun Dashboard(api: XtreamApi, onPlay: (String, String) -> Unit) {
    var tab by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
      Text("Dashboard", fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
      Spacer(Modifier.height(10.dp))

      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Tile("LIVE TV", selected = tab == 0) { tab = 0 }
        Tile("MOVIES", selected = tab == 1) { tab = 1 }
        Tile("SERIES", selected = tab == 2) { tab = 2 }
      }

      Spacer(Modifier.height(12.dp))
      when (tab) {
        0 -> ListLive(api, onPlay)
        1 -> ListMovies(api, onPlay)
        2 -> ListSeries(api)
      }
    }
  }

  @Composable
  private fun Tile(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF1F1F1F)
    Button(
      onClick = onClick,
      colors = ButtonDefaults.buttonColors(containerColor = bg),
      modifier = Modifier.weight(1f).height(54.dp),
      shape = RoundedCornerShape(16.dp)
    ) { Text(text, fontWeight = FontWeight.Black) }
  }

  @Composable
  private fun ListLive(api: XtreamApi, onPlay: (String, String) -> Unit) {
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<StreamItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
      scope.launch(Dispatchers.IO) {
        val data = api.liveStreams().take(200)
        withContext(Dispatchers.Main) { items = data; loading = false }
      }
    }

    if (loading) Text("Loading...", color = Color(0xFF676767))
    else StreamList(items.map { it.name.orEmpty() to api.liveUrl(it.stream_id) }, onPlay)
  }

  @Composable
  private fun ListMovies(api: XtreamApi, onPlay: (String, String) -> Unit) {
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<VodItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
      scope.launch(Dispatchers.IO) {
        val data = api.vodStreams().take(200)
        withContext(Dispatchers.Main) { items = data; loading = false }
      }
    }

    if (loading) Text("Loading...", color = Color(0xFF676767))
    else StreamList(items.map { it.name.orEmpty() to api.vodUrl(it.stream_id) }, onPlay)
  }

  @Composable
  private fun ListSeries(api: XtreamApi) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var count by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
      scope.launch(Dispatchers.IO) {
        val data = api.series()
        withContext(Dispatchers.Main) { count = data.size; loading = false }
      }
    }
    Text(if (loading) "Loading..." else "Series loaded: $count", color = Color(0xFF676767))
  }

  @Composable
  private fun StreamList(items: List<Pair<String, String>>, onPlay: (String, String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
      items.forEach { (title, url) ->
        Card(
          onClick = { onPlay(title, url) },
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)))
            Spacer(Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}