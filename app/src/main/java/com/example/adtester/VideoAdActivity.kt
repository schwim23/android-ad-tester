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
import android.os.Handler
import android.os.Looper

class VideoAdActivity : AppCompatActivity() {
    
    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private lateinit var etAdTagUrl: EditText
    private lateinit var btnLoadAd: Button
    private lateinit var btnBack: Button
    private lateinit var btnSampleVast1: Button
    private var adsLoader: ImaAdsLoader? = null
    private val handler = Handler(Looper.getMainLooper())
    private var playbackMonitor: Runnable? = null
    
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
        // Create ads loader first
        adsLoader = ImaAdsLoader.Builder(this).build()
        
        // Create data source factory
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)
        
        // Create media source factory with ads support
        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
            .setAdsLoaderProvider { adsLoader }
            .setAdViewProvider(playerView)
        
        // Create ExoPlayer with the media source factory
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        
        // CRITICAL: Set the player to ads loader BEFORE setting up any media
        adsLoader?.setPlayer(player)
        
        // Connect player to the view LAST
        playerView.player = player
        
        android.util.Log.d("VideoAdActivity", "Player and ads loader initialized")
        
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                android.util.Log.e("VideoAdActivity", "Player error: ${error.message}", error)
                Toast.makeText(this@VideoAdActivity, "Playback error: ${error.message}", Toast.LENGTH_LONG).show()
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateString = when (playbackState) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING" 
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
                android.util.Log.d("VideoAdActivity", "Player state changed to: $stateString")
                
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Toast.makeText(this@VideoAdActivity, "Loading...", Toast.LENGTH_SHORT).show()
                    }
                    Player.STATE_READY -> {
                        val isAd = player.isPlayingAd
                        val currentWindowIndex = player.currentWindowIndex
                        android.util.Log.d("VideoAdActivity", "Player ready - isPlayingAd: $isAd, currentWindow: $currentWindowIndex")
                        Toast.makeText(this@VideoAdActivity, if (isAd) "Ad ready to play" else "Content ready to play", Toast.LENGTH_SHORT).show()
                    }
                    Player.STATE_ENDED -> {
                        android.util.Log.d("VideoAdActivity", "Playback ended")
                        Toast.makeText(this@VideoAdActivity, "Playback finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                android.util.Log.d("VideoAdActivity", "Is playing changed: $isPlaying")
                if (isPlaying) {
                    startPlaybackMonitoring()
                } else {
                    stopPlaybackMonitoring()
                }
            }
            
            override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                val reasonString = when (reason) {
                    Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED -> "PLAYLIST_CHANGED"
                    Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE -> "SOURCE_UPDATE"
                    else -> "OTHER"
                }
                android.util.Log.d("VideoAdActivity", "Timeline changed (reason: $reasonString) - window count: ${timeline.windowCount}")
                
                for (i in 0 until timeline.windowCount) {
                    val window = androidx.media3.common.Timeline.Window()
                    timeline.getWindow(i, window)
                    android.util.Log.d("VideoAdActivity", "Window $i: duration=${window.durationMs}ms, defaultPositionMs=${window.defaultPositionMs}")
                }
                
                // Check if we have multiple windows which might indicate ads
                if (timeline.windowCount > 1) {
                    android.util.Log.d("VideoAdActivity", "Multiple windows detected - ads may be present")
                    Toast.makeText(this@VideoAdActivity, "Ads detected in timeline", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    
    private fun startPlaybackMonitoring() {
        stopPlaybackMonitoring() // Stop any existing monitoring
        playbackMonitor = object : Runnable {
            override fun run() {
                if (player.isPlaying) {
                    val isPlayingAd = player.isPlayingAd
                    val currentWindowIndex = player.currentWindowIndex
                    val currentPosition = player.currentPosition
                    android.util.Log.d("VideoAdActivity", "Monitoring - isAd: $isPlayingAd, window: $currentWindowIndex, position: ${currentPosition}ms")
                    handler.postDelayed(this, 2000) // Check every 2 seconds
                }
            }
        }
        handler.post(playbackMonitor!!)
    }
    
    private fun stopPlaybackMonitoring() {
        playbackMonitor?.let { handler.removeCallbacks(it) }
        playbackMonitor = null
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
            val vastTag = "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_preroll_skippable&sz=640x360&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=%%CACHEBUSTER%%"
            etAdTagUrl.setText(vastTag)
        }
    }
    
    private fun loadVastAd(adTagUrl: String) {
        try {
            // Replace cache buster macro with current timestamp
            val timestamp = System.currentTimeMillis().toString()
            val finalAdTagUrl = adTagUrl.replace("%%CACHEBUSTER%%", timestamp)
            
            android.util.Log.d("VideoAdActivity", "Original URL: $adTagUrl")
            android.util.Log.d("VideoAdActivity", "Final URL with cache buster: $finalAdTagUrl")
            android.util.Log.d("VideoAdActivity", "Cache buster timestamp: $timestamp")
            
            val contentVideoUrl = "https://storage.googleapis.com/gvabox/media/samples/stock.mp4"
            
            // Stop and reset player state
            player.stop()
            player.clearMediaItems()
            
            // Create MediaItem with ads configuration
            val adsConfiguration = MediaItem.AdsConfiguration.Builder(android.net.Uri.parse(finalAdTagUrl))
                .build()
            
            val mediaItem = MediaItem.Builder()
                .setUri(contentVideoUrl)
                .setAdsConfiguration(adsConfiguration)
                .build()
            
            android.util.Log.d("VideoAdActivity", "MediaItem created with content: $contentVideoUrl")
            android.util.Log.d("VideoAdActivity", "Ad tag URL: $finalAdTagUrl")
            android.util.Log.d("VideoAdActivity", "Setting MediaItem to player...")
            
            // Set media item and prepare
            player.setMediaItem(mediaItem)
            player.prepare()
            
            // Wait a moment before setting playWhenReady to allow ads loader to initialize
            handler.postDelayed({
                android.util.Log.d("VideoAdActivity", "Starting playback after delay...")
                player.playWhenReady = true
            }, 500) // 500ms delay
            
            Toast.makeText(this, "Loading VAST ad (${timestamp})", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            android.util.Log.e("VideoAdActivity", "Error loading VAST ad", e)
            Toast.makeText(this, "Error loading ad: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopPlaybackMonitoring()
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