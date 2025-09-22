package com.example.adtester

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ima.ImaAdsLoader
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import android.widget.EditText

class VideoAdActivity : AppCompatActivity() {
    
    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private lateinit var etAdTagUrl: EditText
    private lateinit var btnLoadAd: Button
    private lateinit var btnBack: Button
    private lateinit var btnSampleVast1: Button
    private var adsLoader: ImaAdsLoader? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_ad)
        
        initializeViews()
        initializePlayer()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        playerView = findViewById(R.id.playerView)
        etAdTagUrl = findViewById(R.id.etAdTagUrl)
        btnLoadAd = findViewById(R.id.btnLoadAd)
        btnBack = findViewById(R.id.btnBack)
        btnSampleVast1 = findViewById(R.id.btnSampleVast1)
    }
    
    private fun initializePlayer() {
        adsLoader = ImaAdsLoader.Builder(this).build()
        
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)
        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
            .setAdsLoaderProvider { adsLoader }
            .setAdViewProvider(playerView)
        
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        
        adsLoader?.setPlayer(player)
        
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(this@VideoAdActivity, "Playback error: ${error.message}", Toast.LENGTH_LONG).show()
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Toast.makeText(this@VideoAdActivity, "Loading...", Toast.LENGTH_SHORT).show()
                    }
                    Player.STATE_READY -> {
                        Toast.makeText(this@VideoAdActivity, "Ready to play", Toast.LENGTH_SHORT).show()
                    }
                    Player.STATE_ENDED -> {
                        Toast.makeText(this@VideoAdActivity, "Playback finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        
        playerView.player = player
    }
    
    private fun setupClickListeners() {
        btnLoadAd.setOnClickListener {
            val adTagUrl = etAdTagUrl.text.toString().trim()
            if (adTagUrl.isNotEmpty()) {
                loadVastAd(adTagUrl)
            } else {
                Toast.makeText(this, "Please enter a valid VAST ad tag URL", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnBack.setOnClickListener {
            finish()
        }
        
        btnSampleVast1.setOnClickListener {
            val vastTag = "https://pubads.g.doubleclick.net/gampad/ads?iu=/10236567/moloco_demo&tfcd=0&npa=0&sz=640x480%7C728x90&gdfp_req=1&unviewed_position_start=1&output=vast&env=vp&impl=s&correlator=%%CACHEBUSTER%%&vad_type=linear"
            etAdTagUrl.setText(vastTag)
        }
    }
    
    private fun loadVastAd(adTagUrl: String) {
        try {
            // Replace cache buster macro with current timestamp
            val finalAdTagUrl = adTagUrl.replace("%%CACHEBUSTER%%", System.currentTimeMillis().toString())
            
            val contentVideoUrl = "https://storage.googleapis.com/gvabox/media/samples/stock.mp4"
            
            player.stop()
            player.clearMediaItems()
            
            val mediaItem = MediaItem.Builder()
                .setUri(contentVideoUrl)
                .setAdsConfiguration(
                    MediaItem.AdsConfiguration.Builder(android.net.Uri.parse(finalAdTagUrl))
                        .build()
                )
                .build()
            
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
            
            Toast.makeText(this, "Loading VAST ad with cache buster: ${System.currentTimeMillis()}", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading ad: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        adsLoader?.setPlayer(null)
        adsLoader?.release()
        player.release()
    }
    
    override fun onPause() {
        super.onPause()
        player.pause()
    }
    
    override fun onResume() {
        super.onResume()
        player.play()
    }
}