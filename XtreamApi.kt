package com.optv.player

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request

class XtreamApi(
  private val baseUrl: String,
  private val username: String,
  private val password: String
) {
  private val http = OkHttpClient()
  private val gson = Gson()

  private fun apiUrl(action: String? = null): String {
    val a = if (action == null) "" else "&action=$action"
    return "$baseUrl/player_api.php?username=$username&password=$password$a"
  }

  fun auth(): AuthResponse {
    val req = Request.Builder().url(apiUrl(null)).build()
    http.newCall(req).execute().use { res ->
      val body = res.body?.string().orEmpty()
      return gson.fromJson(body, AuthResponse::class.java)
    }
  }

  fun liveStreams(): List<StreamItem> {
    val req = Request.Builder().url(apiUrl("get_live_streams")).build()
    http.newCall(req).execute().use { res ->
      val body = res.body?.string().orEmpty()
      val type = object : TypeToken<List<StreamItem>>() {}.type
      return gson.fromJson(body, type) ?: emptyList()
    }
  }

  fun vodStreams(): List<VodItem> {
    val req = Request.Builder().url(apiUrl("get_vod_streams")).build()
    http.newCall(req).execute().use { res ->
      val body = res.body?.string().orEmpty()
      val type = object : TypeToken<List<VodItem>>() {}.type
      return gson.fromJson(body, type) ?: emptyList()
    }
  }

  fun series(): List<SeriesItem> {
    val req = Request.Builder().url(apiUrl("get_series")).build()
    http.newCall(req).execute().use { res ->
      val body = res.body?.string().orEmpty()
      val type = object : TypeToken<List<SeriesItem>>() {}.type
      return gson.fromJson(body, type) ?: emptyList()
    }
  }

  fun liveUrl(streamId: Long): String =
    "$baseUrl/live/$username/$password/$streamId.ts"

  fun vodUrl(streamId: Long): String =
    "$baseUrl/movie/$username/$password/$streamId.mp4"
}