package com.example.adtester.services

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.webkit.WebSettings
import com.example.adtester.models.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.*
import java.util.concurrent.TimeUnit

interface OpenRTBApiService {
    @POST
    suspend fun sendBidRequest(@Url url: String, @Body bidRequest: BidRequest): Response<BidResponse>
}

class OpenRTBService(private val context: Context) {
    
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://example.com/") // Base URL required but we use full URLs
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    private val apiService = retrofit.create(OpenRTBApiService::class.java)
    
    suspend fun sendBidRequest(bidUrl: String, requestData: Map<String, Any> = emptyMap()): Result<BidResponse> {
        return try {
            val bidRequest = createBidRequest(requestData)
            val response = apiService.sendBidRequest(bidUrl, bidRequest)
            
            if (response.isSuccessful) {
                response.body()?.let { bidResponse ->
                    Result.success(bidResponse)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createBidRequest(customData: Map<String, Any>): BidRequest {
        val requestId = UUID.randomUUID().toString()
        val impressionId = UUID.randomUUID().toString()
        
        return BidRequest(
            id = requestId,
            imp = listOf(createImpression(impressionId, customData)),
            app = createApp(),
            device = createDevice(),
            user = createUser(),
            test = if (customData["test"] as? Boolean == true) 1 else 0
        )
    }
    
    private fun createImpression(impressionId: String, customData: Map<String, Any>): Impression {
        // Create native request for native ads
        val nativeRequest = createNativeRequest()
        val nativeRequestJson = gson.toJson(nativeRequest)
        
        return Impression(
            id = impressionId,
            native = Native(
                request = nativeRequestJson,
                version = "1.2"
            ),
            banner = Banner(
                width = customData["width"] as? Int ?: 320,
                height = customData["height"] as? Int ?: 50,
                formats = listOf(
                    Format(320, 50),
                    Format(728, 90),
                    Format(300, 250)
                ),
                mimeTypes = listOf("image/jpeg", "image/png", "image/gif")
            ),
            bidFloor = customData["bidFloor"] as? Double ?: 0.10,
            bidFloorCur = "USD"
        )
    }
    
    private fun createNativeRequest(): NativeRequest {
        return NativeRequest(
            version = "1.2",
            context = 1, // Content-centric context
            contextSubType = 10, // General or mixed content
            placementType = 1, // In the feed of content
            assets = listOf(
                // Title asset
                NativeAssetRequest(
                    id = 1,
                    required = 1,
                    title = TitleAssetRequest(len = 90)
                ),
                // Main image asset
                NativeAssetRequest(
                    id = 2,
                    required = 1,
                    img = ImageAssetRequest(
                        type = 3, // Main image
                        wMin = 150,
                        hMin = 50,
                        mimes = listOf("image/jpeg", "image/png")
                    )
                ),
                // Description/body text asset
                NativeAssetRequest(
                    id = 3,
                    required = 0,
                    data = DataAssetRequest(
                        type = 2, // Description/body
                        len = 140
                    )
                ),
                // Sponsored/brand text asset
                NativeAssetRequest(
                    id = 4,
                    required = 0,
                    data = DataAssetRequest(
                        type = 1, // Sponsored
                        len = 25
                    )
                ),
                // Call to action text asset
                NativeAssetRequest(
                    id = 5,
                    required = 0,
                    data = DataAssetRequest(
                        type = 12, // Call to action
                        len = 15
                    )
                ),
                // Icon image asset
                NativeAssetRequest(
                    id = 6,
                    required = 0,
                    img = ImageAssetRequest(
                        type = 1, // Icon image
                        wMin = 50,
                        hMin = 50,
                        mimes = listOf("image/jpeg", "image/png")
                    )
                )
            ),
            eventTrackers = listOf(
                EventTrackerRequest(
                    event = 1, // Impression
                    methods = listOf(1, 2) // Image and JS tracking
                ),
                EventTrackerRequest(
                    event = 2, // Viewable MRC 50%
                    methods = listOf(1, 2)
                )
            )
        )
    }
    
    private fun createApp(): App {
        return App(
            id = "adtester-app",
            name = "Ad Tester",
            bundle = context.packageName,
            version = "1.0",
            categories = listOf("IAB24"), // Uncategorized
            privacyPolicy = 1
        )
    }
    
    private fun createDevice(): Device {
        val displayMetrics = context.resources.displayMetrics
        val userAgent = WebSettings.getDefaultUserAgent(context)
        
        return Device(
            userAgent = userAgent,
            deviceType = 1, // Mobile/Tablet
            make = Build.MANUFACTURER,
            model = Build.MODEL,
            os = "Android",
            osVersion = Build.VERSION.RELEASE,
            height = displayMetrics.heightPixels,
            width = displayMetrics.widthPixels,
            ppi = displayMetrics.densityDpi,
            pixelRatio = displayMetrics.density,
            javascript = 1,
            language = Locale.getDefault().language
        )
    }
    
    private fun createUser(): User {
        return User(
            id = UUID.randomUUID().toString()
        )
    }
    
    fun parseNativeResponse(adMarkup: String): Result<NativeResponse> {
        return try {
            if (adMarkup.isBlank()) {
                return Result.failure(Exception("Ad markup is empty or blank"))
            }
            
            // Parse the native response from the adm JSON
            val nativeResponse = gson.fromJson(adMarkup, NativeResponse::class.java)
                ?: return Result.failure(Exception("Failed to parse native response - result is null"))
            
            Result.success(nativeResponse)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse native response JSON: ${e.message}", e))
        }
    }
    
    fun parseBidResponseFromJson(bidResponseJson: String): Result<BidResponse> {
        return try {
            if (bidResponseJson.isBlank()) {
                return Result.failure(Exception("Bid response JSON is empty or blank"))
            }
            
            val bidResponse = gson.fromJson(bidResponseJson, BidResponse::class.java)
                ?: return Result.failure(Exception("Failed to parse bid response - result is null"))
                
            Result.success(bidResponse)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse bid response JSON: ${e.message}", e))
        }
    }
    
    fun processAdResponse(bidResponse: BidResponse): Result<NativeResponse> {
        return try {
            // Check if seatBid list exists and is not empty
            if (bidResponse.seatBid.isNullOrEmpty()) {
                return Result.failure(Exception("No seat bids found in response"))
            }
            
            // Get the first seatbid
            val seatBid = bidResponse.seatBid.firstOrNull()
                ?: return Result.failure(Exception("No seat bid found in response"))
            
            // Check if bids list exists and is not empty
            if (seatBid.bids.isNullOrEmpty()) {
                return Result.failure(Exception("No bids found in seat bid"))
            }
            
            // Get the first bid
            val bid = seatBid.bids.firstOrNull()
                ?: return Result.failure(Exception("No bid found in bids list"))
            
            // Extract the adm (ad markup) field
            val adMarkup = bid.adMarkup
                ?: return Result.failure(Exception("No ad markup found in bid"))
            
            // Parse the native response from adm
            parseNativeResponse(adMarkup)
        } catch (e: Exception) {
            Result.failure(Exception("Error processing ad response: ${e.message}", e))
        }
    }
}

// Native request models (for creating the native request JSON)
data class NativeRequest(
    @com.google.gson.annotations.SerializedName("ver") val version: String,
    @com.google.gson.annotations.SerializedName("context") val context: Int,
    @com.google.gson.annotations.SerializedName("contextsubtype") val contextSubType: Int,
    @com.google.gson.annotations.SerializedName("plcmttype") val placementType: Int,
    @com.google.gson.annotations.SerializedName("assets") val assets: List<NativeAssetRequest>,
    @com.google.gson.annotations.SerializedName("eventtrackers") val eventTrackers: List<EventTrackerRequest>? = null
)

data class NativeAssetRequest(
    @com.google.gson.annotations.SerializedName("id") val id: Int,
    @com.google.gson.annotations.SerializedName("required") val required: Int = 0,
    @com.google.gson.annotations.SerializedName("title") val title: TitleAssetRequest? = null,
    @com.google.gson.annotations.SerializedName("img") val img: ImageAssetRequest? = null,
    @com.google.gson.annotations.SerializedName("data") val data: DataAssetRequest? = null
)

data class TitleAssetRequest(
    @com.google.gson.annotations.SerializedName("len") val len: Int
)

data class ImageAssetRequest(
    @com.google.gson.annotations.SerializedName("type") val type: Int,
    @com.google.gson.annotations.SerializedName("wmin") val wMin: Int? = null,
    @com.google.gson.annotations.SerializedName("hmin") val hMin: Int? = null,
    @com.google.gson.annotations.SerializedName("w") val w: Int? = null,
    @com.google.gson.annotations.SerializedName("h") val h: Int? = null,
    @com.google.gson.annotations.SerializedName("mimes") val mimes: List<String>? = null
)

data class DataAssetRequest(
    @com.google.gson.annotations.SerializedName("type") val type: Int,
    @com.google.gson.annotations.SerializedName("len") val len: Int? = null
)

data class EventTrackerRequest(
    @com.google.gson.annotations.SerializedName("event") val event: Int,
    @com.google.gson.annotations.SerializedName("methods") val methods: List<Int>
)