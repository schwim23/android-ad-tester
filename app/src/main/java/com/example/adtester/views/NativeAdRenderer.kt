package com.example.adtester.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.adtester.R
import com.example.adtester.models.Asset
import com.example.adtester.models.NativeResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class NativeAdRenderer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val titleTextView: TextView
    private val descriptionTextView: TextView
    private val sponsoredTextView: TextView
    private val ctaButton: TextView
    private val mainImageView: ImageView
    private val iconImageView: ImageView
    private val adContainer: LinearLayout
    
    private var currentNativeResponse: NativeResponse? = null
    private var onAdClickListener: ((String) -> Unit)? = null
    
    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.native_ad_layout, this, true)
        
        titleTextView = findViewById(R.id.tvAdTitle)
        descriptionTextView = findViewById(R.id.tvAdDescription)
        sponsoredTextView = findViewById(R.id.tvAdSponsored)
        ctaButton = findViewById(R.id.btnAdCta)
        mainImageView = findViewById(R.id.ivAdMainImage)
        iconImageView = findViewById(R.id.ivAdIcon)
        adContainer = findViewById(R.id.adContainer)
    }
    
    fun renderNativeAd(nativeResponse: NativeResponse, onAdClick: ((String) -> Unit)? = null) {
        currentNativeResponse = nativeResponse
        onAdClickListener = onAdClick
        
        // Clear previous content
        clearAd()
        
        // Check if assets exist
        val assets = nativeResponse.assets
        if (assets.isNullOrEmpty()) {
            Log.w("NativeAdRenderer", "No assets found in native response")
            // Still show the container but with placeholder content
            titleTextView.text = "Native Ad (No Assets)"
            descriptionTextView.text = "No ad assets available to display"
            advertiserTextView.text = "Unknown Advertiser"
            ctaButton.text = "Learn More"
            adContainer.visibility = View.VISIBLE
            return
        }
        
        // Parse and render assets
        assets.forEach { asset ->
            renderAsset(asset)
        }
        
        // Set up click handling
        setupClickHandling(nativeResponse)
        
        // Fire impression trackers
        fireImpressionTrackers(nativeResponse)
        
        // Show the ad container
        adContainer.visibility = View.VISIBLE
        
        Log.d("NativeAdRenderer", "Native ad rendered successfully with ${assets.size} assets")
    }
    
    private fun renderAsset(asset: Asset) {
        when {
            asset.title != null -> {
                titleTextView.text = asset.title.text
                titleTextView.visibility = View.VISIBLE
                Log.d("NativeAdRenderer", "Rendered title: ${asset.title.text}")
            }
            asset.data != null -> {
                when (asset.data.type) {
                    1 -> { // Sponsored by
                        sponsoredTextView.text = asset.data.value
                        sponsoredTextView.visibility = View.VISIBLE
                        Log.d("NativeAdRenderer", "Rendered sponsored text: ${asset.data.value}")
                    }
                    2 -> { // Description
                        descriptionTextView.text = asset.data.value
                        descriptionTextView.visibility = View.VISIBLE
                        Log.d("NativeAdRenderer", "Rendered description: ${asset.data.value}")
                    }
                    12 -> { // Call to action
                        ctaButton.text = asset.data.value
                        ctaButton.visibility = View.VISIBLE
                        Log.d("NativeAdRenderer", "Rendered CTA: ${asset.data.value}")
                    }
                }
            }
            asset.image != null -> {
                when (asset.image.type) {
                    1 -> { // Icon
                        loadImageWithGlide(asset.image.url, iconImageView)
                        iconImageView.visibility = View.VISIBLE
                        Log.d("NativeAdRenderer", "Rendered icon: ${asset.image.url}")
                    }
                    3 -> { // Main image
                        loadImageWithGlide(asset.image.url, mainImageView)
                        mainImageView.visibility = View.VISIBLE
                        Log.d("NativeAdRenderer", "Rendered main image: ${asset.image.url}")
                    }
                }
            }
        }
    }
    
    private fun loadImageWithGlide(imageUrl: String, imageView: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(imageView)
    }
    
    private fun setupClickHandling(nativeResponse: NativeResponse) {
        val clickUrl = nativeResponse.link?.url
        
        if (!clickUrl.isNullOrEmpty()) {
            val clickListener = View.OnClickListener {
                handleAdClick(clickUrl, nativeResponse.link?.clickTrackers ?: emptyList())
            }
            
            // Set click listeners on clickable elements
            adContainer.setOnClickListener(clickListener)
            titleTextView.setOnClickListener(clickListener)
            mainImageView.setOnClickListener(clickListener)
            ctaButton.setOnClickListener(clickListener)
            
            // Make elements focusable and clickable
            adContainer.isClickable = true
            adContainer.isFocusable = true
            titleTextView.isClickable = true
            mainImageView.isClickable = true
            ctaButton.isClickable = true
        }
    }
    
    private fun handleAdClick(clickUrl: String, clickTrackers: List<String>) {
        Log.d("NativeAdRenderer", "Ad clicked: $clickUrl")
        
        // Fire click trackers
        CoroutineScope(Dispatchers.IO).launch {
            clickTrackers.forEach { trackerUrl ->
                fireTracker(trackerUrl, "click")
            }
        }
        
        // Handle click via callback or open URL
        if (onAdClickListener != null) {
            onAdClickListener?.invoke(clickUrl)
        } else {
            // Default behavior: open URL in browser
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl))
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("NativeAdRenderer", "Failed to open click URL: $clickUrl", e)
            }
        }
    }
    
    private fun fireImpressionTrackers(nativeResponse: NativeResponse) {
        CoroutineScope(Dispatchers.IO).launch {
            // Fire impression trackers
            nativeResponse.impressionTrackers?.forEach { trackerUrl ->
                fireTracker(trackerUrl, "impression")
            }
            
            // Fire event trackers for impression
            nativeResponse.eventTrackers?.filter { it.event == 1 }?.forEach { eventTracker ->
                eventTracker.url?.let { trackerUrl ->
                    fireTracker(trackerUrl, "impression")
                }
            }
        }
    }
    
    private suspend fun fireTracker(url: String, type: String) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()
                
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("NativeAdRenderer", "$type tracker fired successfully: $url")
                } else {
                    Log.w("NativeAdRenderer", "$type tracker failed: ${response.code} - $url")
                }
            }
        } catch (e: IOException) {
            Log.e("NativeAdRenderer", "Failed to fire $type tracker: $url", e)
        }
    }
    
    fun clearAd() {
        titleTextView.text = ""
        titleTextView.visibility = View.GONE
        descriptionTextView.text = ""
        descriptionTextView.visibility = View.GONE
        sponsoredTextView.text = ""
        sponsoredTextView.visibility = View.GONE
        ctaButton.text = ""
        ctaButton.visibility = View.GONE
        mainImageView.setImageDrawable(null)
        mainImageView.visibility = View.GONE
        iconImageView.setImageDrawable(null)
        iconImageView.visibility = View.GONE
        adContainer.visibility = View.GONE
        
        // Clear click listeners
        adContainer.setOnClickListener(null)
        titleTextView.setOnClickListener(null)
        mainImageView.setOnClickListener(null)
        ctaButton.setOnClickListener(null)
    }
    
    fun hideAd() {
        adContainer.visibility = View.GONE
    }
    
    fun showAd() {
        if (currentNativeResponse != null) {
            adContainer.visibility = View.VISIBLE
        }
    }
}