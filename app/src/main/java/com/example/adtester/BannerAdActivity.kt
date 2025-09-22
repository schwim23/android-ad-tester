package com.example.adtester

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.adtester.services.OpenRTBService
import com.example.adtester.views.NativeAdRenderer
import android.widget.EditText
import kotlinx.coroutines.launch

class BannerAdActivity : AppCompatActivity() {
    
    private lateinit var etBidResponse: EditText
    private lateinit var btnLoadAd: Button
    private lateinit var btnBack: Button
    private lateinit var btnSampleBanner1: Button
    private lateinit var nativeAdRenderer: NativeAdRenderer
    private lateinit var tvAdStatus: TextView
    private lateinit var openRTBService: OpenRTBService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banner_ad)
        
        initializeViews()
        initializeOpenRTB()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        etBidResponse = findViewById(R.id.etAdUnitId) // Reusing existing input field
        btnLoadAd = findViewById(R.id.btnLoadBannerAd) // Reusing existing button
        btnBack = findViewById(R.id.btnBack)
        btnSampleBanner1 = findViewById(R.id.btnSampleBanner1)
        nativeAdRenderer = findViewById(R.id.adView)
        tvAdStatus = findViewById(R.id.tvAdStatus)
    }
    
    private fun initializeOpenRTB() {
        openRTBService = OpenRTBService(this)
        tvAdStatus.text = "Ready to parse OpenRTB bid response and render native ads."
        Log.d("BannerAdActivity", "OpenRTB service initialized")
    }
    
    private fun setupClickListeners() {
        btnLoadAd.setOnClickListener {
            val bidResponseJson = etBidResponse.text.toString().trim()
            if (bidResponseJson.isNotEmpty()) {
                parseAndRenderAd(bidResponseJson)
            } else {
                Toast.makeText(this, "Please enter a valid OpenRTB bid response JSON", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnBack.setOnClickListener {
            finish()
        }
        
        btnSampleBanner1.setOnClickListener {
            val sampleResponse = """
{
  "id": "1234567890",
  "seatbid": [
    {
      "seat": "555",
      "bid": [
        {
          "id": "ABCDEF0123",
          "impid": "1",
          "price": 0.50,
          "adid": "ad_12345",
          "nurl": "https://example.com/win_notice?bidid=${'$'}{AUCTION_ID}&price=${'$'}{AUCTION_PRICE}",
          "adomain": [
            "example.com"
          ],
          "crid": "creative_XYZ",
          "adm": "{\"native\":{\"ver\":\"1.2\",\"assets\":[{\"id\":1,\"title\":{\"text\":\"Sample Native Ad Title\"},\"required\":1},{\"id\":2,\"img\":{\"url\":\"https://example.com/images/main_image.jpg\",\"w\":1200,\"h\":628},\"required\":1},{\"id\":3,\"data\":{\"type\":1,\"value\":\"This is a compelling description of the ad.\"}},{\"id\":4,\"data\":{\"type\":2,\"value\":\"Advertiser Name\"}},{\"id\":5,\"link\":{\"url\":\"https://example.com/click\"},\"ext\":{\"clicktrackers\":[\"https://example.com/clicktracker\"]}}],\"eventtrackers\":[{\"event\":1,\"methods\":[1,2],\"url\":\"https://example.com/impressiontracker\"}]}}"
        }
      ]
    }
  ],
  "cur": "USD"
}
            """.trimIndent()
            etBidResponse.setText(sampleResponse)
        }
    }
    
    private fun parseAndRenderAd(bidResponseJson: String) {
        lifecycleScope.launch {
            try {
                Log.d("BannerAdActivity", "Parsing OpenRTB bid response JSON")
                tvAdStatus.text = "Parsing bid response JSON..."
                
                // Parse the bid response directly from JSON
                val bidResult = openRTBService.parseBidResponseFromJson(bidResponseJson)
                
                bidResult.fold(
                    onSuccess = { bidResponse ->
                        Log.d("BannerAdActivity", "Bid response parsed: ${bidResponse.id}")
                        tvAdStatus.text = "Bid response parsed, extracting native ADM..."
                        
                        // Process the adm field from the bid response
                        val nativeResult = openRTBService.processAdResponse(bidResponse)
                        
                        nativeResult.fold(
                            onSuccess = { nativeResponse ->
                                Log.d("BannerAdActivity", "Native ADM parsed successfully")
                                tvAdStatus.text = "Rendering native ad..."
                                
                                // Render the native ad
                                nativeAdRenderer.renderNativeAd(nativeResponse) { clickUrl ->
                                    Log.d("BannerAdActivity", "Native ad clicked: $clickUrl")
                                    tvAdStatus.text = "Ad clicked!"
                                    Toast.makeText(this@BannerAdActivity, "Ad clicked!", Toast.LENGTH_SHORT).show()
                                }
                                
                                tvAdStatus.text = "Native ad rendered successfully! 🎉"
                                Toast.makeText(this@BannerAdActivity, "Native ad loaded from ADM!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { error ->
                                Log.e("BannerAdActivity", "Failed to parse native ADM", error)
                                tvAdStatus.text = "Failed to parse native ADM: ${error.message}"
                                Toast.makeText(this@BannerAdActivity, "Failed to parse ADM: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    onFailure = { error ->
                        Log.e("BannerAdActivity", "Failed to parse bid response JSON", error)
                        tvAdStatus.text = "Failed to parse JSON: ${error.message}"
                        Toast.makeText(this@BannerAdActivity, "Failed to parse JSON: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                )
            } catch (e: Exception) {
                Log.e("BannerAdActivity", "Error parsing bid response", e)
                tvAdStatus.text = "Error parsing response: ${e.message}"
                Toast.makeText(this@BannerAdActivity, "Error parsing response: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroy() {
        nativeAdRenderer.clearAd()
        super.onDestroy()
    }
}