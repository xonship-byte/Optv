package com.optv.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView

class PlayerActivity : ComponentActivity() {

  private var player: ExoPlayer? = null
  private lateinit var playerView: PlayerView
  private lateinit var trackSelector: DefaultTrackSelector

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    playerView = PlayerView(this).apply {
      useController = true
      // resize modes etc can be exposed via custom buttons later
    }
    setContentView(playerView)

    val url = intent.getStringExtra("url").orEmpty()

    trackSelector = DefaultTrackSelector(this)

    player = ExoPlayer.Builder(this)
      .setTrackSelector(trackSelector)
      .build().also { exo ->
        playerView.player = exo

        // If your stream is HLS m3u8, set mimeType to APPLICATION_M3U8 (Media3 HLS doc) [web:282]
        val item = MediaItem.Builder()
          .setUri(url)
          .setMimeType(MimeTypes.VIDEO_MP4) // TS/MP4 guess; if m3u8 then change to APPLICATION_M3U8
          .build()

        exo.setMediaItem(item)
        exo.prepare()
        exo.play()
      }
  }

  override fun onStop() {
    super.onStop()
    playerView.player = null
    player?.release()
    player = null
  }
}