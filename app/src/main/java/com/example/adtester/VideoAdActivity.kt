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
import android.widget.EditText

class VideoAdActivity : AppCompatActivity() {
    
    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private lateinit var etAdTagUrl: EditText
    private lateinit var btnLoadAd: Button
    private lateinit var btnBack: Button
    private lateinit var btnSampleVast1: Button
    private lateinit var btnSampleVast2: Button
    private var adsLoader: ImaAdsLoader? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("VideoAdActivity", "onCreate started")
        
        try {
            setContentView(R.layout.activity_video_ad)
            Log.d("VideoAdActivity", "Layout set successfully")
            
            initializeViews()
            Log.d("VideoAdActivity", "Views initialized successfully")
            
            initializePlayer()
            Log.d("VideoAdActivity", "Player initialized successfully")
            
            setupClickListeners()
            Log.d("VideoAdActivity", "Click listeners set up successfully")
            
        } catch (e: Exception) {
            Log.e("VideoAdActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing video screen: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun initializeViews() {
        try {
            playerView = findViewById(R.id.playerView)
            Log.d("VideoAdActivity", "PlayerView found: ${playerView != null}")
            
            etAdTagUrl = findViewById(R.id.etAdTagUrl)
            Log.d("VideoAdActivity", "EditText found: ${etAdTagUrl != null}")
            
            btnLoadAd = findViewById(R.id.btnLoadAd)
            Log.d("VideoAdActivity", "Load button found: ${btnLoadAd != null}")
            
            btnBack = findViewById(R.id.btnBack)
            Log.d("VideoAdActivity", "Back button found: ${btnBack != null}")
            
            btnSampleVast1 = findViewById(R.id.btnSampleVast1)
            Log.d("VideoAdActivity", "Sample button 1 found: ${btnSampleVast1 != null}")
            
            btnSampleVast2 = findViewById(R.id.btnSampleVast2)
            Log.d("VideoAdActivity", "Sample button 2 found: ${btnSampleVast2 != null}")
            
            // Make views explicitly visible
            etAdTagUrl.visibility = android.view.View.VISIBLE
            btnLoadAd.visibility = android.view.View.VISIBLE
            btnSampleVast1.visibility = android.view.View.VISIBLE
            btnSampleVast2.visibility = android.view.View.VISIBLE
            playerView.visibility = android.view.View.VISIBLE
            
            Log.d("VideoAdActivity", "All views set to visible")
            
        } catch (e: Exception) {
            Log.e("VideoAdActivity", "Error initializing views", e)
            throw e
        }
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
        
        // IMPORTANT: Set player to adsLoader before preparing
        adsLoader?.setPlayer(player)
        
        // Add comprehensive player listener
        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e("VideoAdActivity", "Player error: ${error.message}", error)
                Toast.makeText(this@VideoAdActivity, "Playback error: ${error.message}", Toast.LENGTH_LONG).show()
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    androidx.media3.common.Player.STATE_IDLE -> {
                        Log.d("VideoAdActivity", "Player state: IDLE")
                    }
                    androidx.media3.common.Player.STATE_BUFFERING -> {
                        Log.d("VideoAdActivity", "Player state: BUFFERING")
                        Toast.makeText(this@VideoAdActivity, "Loading...", Toast.LENGTH_SHORT).show()
                    }
                    androidx.media3.common.Player.STATE_READY -> {
                        Log.d("VideoAdActivity", "Player state: READY")
                        Toast.makeText(this@VideoAdActivity, "Ready to play", Toast.LENGTH_SHORT).show()
                    }
                    androidx.media3.common.Player.STATE_ENDED -> {
                        Log.d("VideoAdActivity", "Player state: ENDED")
                        Toast.makeText(this@VideoAdActivity, "Playback finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d("VideoAdActivity", "Is playing: $isPlaying")
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
            // Use the new moloco demo VAST tag
            val vastTag = "https://pubads.g.doubleclick.net/gampad/ads?iu=/10236567/moloco_demo&description_url=http%3A%2F%2Fwww.google.com&tfcd=0&npa=0&sz=640x480%7C728x90&gdfp_req=1&unviewed_position_start=1&output=vast&env=vp&impl=s&correlator=&vad_type=linear"
            etAdTagUrl.setText(vastTag)
        }
        
        btnSampleVast2.setOnClickListener {
            // Linear ad sample
            val vastTag = "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_ad_samples&sz=400x300&cust_params=sample_ct%3Dlinear&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=${System.currentTimeMillis()}"
            etAdTagUrl.setText(vastTag)
        }
    }
    
    private fun loadVastAd(adTagUrl: String) {
        try {
            Log.d("VideoAdActivity", "Starting to load VAST ad from: $adTagUrl")
            
            // Replace cachebuster placeholder with current timestamp
            val finalAdTagUrl = adTagUrl.replace("%%CACHEBUSTER%%", System.currentTimeMillis().toString())
            Log.d("VideoAdActivity", "Final VAST tag URL: $finalAdTagUrl")
            
            // Create a sample content video URL - this will play after the ad
            val contentVideoUrl = "https://storage.googleapis.com/gvabox/media/samples/stock.mp4"
            
            // Reset player first
            player.stop()
            player.clearMediaItems()
            
            // Create media item with ad tag configuration
            val mediaItem = MediaItem.Builder()
                .setUri(contentVideoUrl)
                .setAdsConfiguration(
                    MediaItem.AdsConfiguration.Builder(android.net.Uri.parse(finalAdTagUrl))
                        .build()
                )
                .build()
            
            Log.d("VideoAdActivity", "Media item created with VAST tag, setting to player")
            
            // Set the media item and prepare the player
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
            
            Log.d("VideoAdActivity", "Player prepared and set to play - VAST ad should load first")
            Toast.makeText(this, "Loading VAST ad... Watch for preroll ad!", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Log.e("VideoAdActivity", "Error loading VAST ad", e)
            e.printStackTrace()
            Toast.makeText(this, "Error loading ad: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        adsLoader?.let { adsLoader ->
            adsLoader.setPlayer(null)
            adsLoader.release()
        }
        player.release()
    }
    
    override fun onPause() {
        super.onPause()
        player.pause()
    }
    
    override fun onResume() {
        super.onResume()
        if (player.playbackState != androidx.media3.common.Player.STATE_IDLE) {
            player.play()
        }
    }
}