package com.example.adtester.models

import com.google.gson.annotations.SerializedName

// OpenRTB Bid Request Models
data class BidRequest(
    @SerializedName("id") val id: String,
    @SerializedName("imp") val imp: List<Impression>,
    @SerializedName("site") val site: Site? = null,
    @SerializedName("app") val app: App? = null,
    @SerializedName("device") val device: Device,
    @SerializedName("user") val user: User? = null,
    @SerializedName("test") val test: Int = 0,
    @SerializedName("at") val auctionType: Int = 1,
    @SerializedName("tmax") val timeoutMs: Int = 3000,
    @SerializedName("cur") val currency: List<String> = listOf("USD")
)

data class Impression(
    @SerializedName("id") val id: String,
    @SerializedName("native") val native: Native? = null,
    @SerializedName("banner") val banner: Banner? = null,
    @SerializedName("bidfloor") val bidFloor: Double = 0.0,
    @SerializedName("bidfloorcur") val bidFloorCur: String = "USD"
)

data class Native(
    @SerializedName("request") val request: String, // JSON encoded native request
    @SerializedName("ver") val version: String = "1.2",
    @SerializedName("api") val api: List<Int>? = null,
    @SerializedName("battr") val blockedAttributes: List<Int>? = null
)

data class Banner(
    @SerializedName("w") val width: Int,
    @SerializedName("h") val height: Int,
    @SerializedName("format") val formats: List<Format>? = null,
    @SerializedName("mimes") val mimeTypes: List<String>? = null
)

data class Format(
    @SerializedName("w") val width: Int,
    @SerializedName("h") val height: Int
)

data class Site(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("domain") val domain: String,
    @SerializedName("cat") val categories: List<String>? = null,
    @SerializedName("page") val page: String,
    @SerializedName("ref") val ref: String? = null,
    @SerializedName("search") val search: String? = null,
    @SerializedName("mobile") val mobile: Int? = null
)

data class App(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("bundle") val bundle: String,
    @SerializedName("domain") val domain: String? = null,
    @SerializedName("storeurl") val storeUrl: String? = null,
    @SerializedName("cat") val categories: List<String>? = null,
    @SerializedName("ver") val version: String? = null,
    @SerializedName("privacypolicy") val privacyPolicy: Int? = null
)

data class Device(
    @SerializedName("ua") val userAgent: String,
    @SerializedName("geo") val geo: Geo? = null,
    @SerializedName("dnt") val doNotTrack: Int? = null,
    @SerializedName("lmt") val limitAdTracking: Int? = null,
    @SerializedName("ip") val ip: String? = null,
    @SerializedName("devicetype") val deviceType: Int = 1, // Mobile/Tablet
    @SerializedName("make") val make: String,
    @SerializedName("model") val model: String,
    @SerializedName("os") val os: String,
    @SerializedName("osv") val osVersion: String,
    @SerializedName("hwv") val hardwareVersion: String? = null,
    @SerializedName("h") val height: Int,
    @SerializedName("w") val width: Int,
    @SerializedName("ppi") val ppi: Int? = null,
    @SerializedName("pxratio") val pixelRatio: Float? = null,
    @SerializedName("js") val javascript: Int = 1,
    @SerializedName("connectiontype") val connectionType: Int? = null,
    @SerializedName("language") val language: String? = null
)

data class Geo(
    @SerializedName("lat") val latitude: Float? = null,
    @SerializedName("lon") val longitude: Float? = null,
    @SerializedName("type") val type: Int? = null,
    @SerializedName("accuracy") val accuracy: Int? = null,
    @SerializedName("lastfix") val lastFix: Int? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("regionfips104") val regionFips: String? = null,
    @SerializedName("metro") val metro: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("zip") val zip: String? = null,
    @SerializedName("utcoffset") val utcOffset: Int? = null
)

data class User(
    @SerializedName("id") val id: String? = null,
    @SerializedName("buyeruid") val buyerUid: String? = null,
    @SerializedName("yob") val yearOfBirth: Int? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("keywords") val keywords: String? = null,
    @SerializedName("customdata") val customData: String? = null,
    @SerializedName("geo") val geo: Geo? = null,
    @SerializedName("data") val data: List<Data>? = null
)

data class Data(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("segment") val segment: List<Segment>? = null
)

data class Segment(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("value") val value: String? = null
)

// OpenRTB Bid Response Models
data class BidResponse(
    @SerializedName("id") val id: String,
    @SerializedName("seatbid") val seatBid: List<SeatBid>,
    @SerializedName("bidid") val bidId: String? = null,
    @SerializedName("cur") val currency: String = "USD",
    @SerializedName("customdata") val customData: String? = null,
    @SerializedName("nbr") val noBidReason: Int? = null
)

data class SeatBid(
    @SerializedName("bid") val bids: List<Bid>,
    @SerializedName("seat") val seat: String? = null,
    @SerializedName("group") val group: Int = 0
)

data class Bid(
    @SerializedName("id") val id: String,
    @SerializedName("impid") val impressionId: String,
    @SerializedName("price") val price: Double,
    @SerializedName("adid") val adId: String? = null,
    @SerializedName("nurl") val noticeUrl: String? = null,
    @SerializedName("burl") val billingUrl: String? = null,
    @SerializedName("lurl") val lossUrl: String? = null,
    @SerializedName("adm") val adMarkup: String? = null,
    @SerializedName("adomain") val advertiserDomains: List<String>? = null,
    @SerializedName("bundle") val bundle: String? = null,
    @SerializedName("iurl") val imageUrl: String? = null,
    @SerializedName("cid") val campaignId: String? = null,
    @SerializedName("crid") val creativeId: String? = null,
    @SerializedName("tactic") val tactic: String? = null,
    @SerializedName("cat") val categories: List<String>? = null,
    @SerializedName("attr") val attributes: List<Int>? = null,
    @SerializedName("api") val api: Int? = null,
    @SerializedName("protocol") val protocol: Int? = null,
    @SerializedName("qagmediarating") val mediaRating: Int? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("dealid") val dealId: String? = null,
    @SerializedName("w") val width: Int? = null,
    @SerializedName("h") val height: Int? = null,
    @SerializedName("wratio") val widthRatio: Int? = null,
    @SerializedName("hratio") val heightRatio: Int? = null,
    @SerializedName("exp") val expiration: Int? = null,
    @SerializedName("ext") val extensions: Map<String, Any>? = null
)

// Native Ad Models (for parsing native responses)
data class NativeResponse(
    @SerializedName("ver") val version: String? = null,
    @SerializedName("assets") val assets: List<Asset>,
    @SerializedName("link") val link: Link? = null,
    @SerializedName("imptrackers") val impressionTrackers: List<String>? = null,
    @SerializedName("jstracker") val jsTracker: String? = null,
    @SerializedName("eventtrackers") val eventTrackers: List<EventTracker>? = null
)

data class Asset(
    @SerializedName("id") val id: Int,
    @SerializedName("required") val required: Int? = 0,
    @SerializedName("title") val title: TitleAsset? = null,
    @SerializedName("img") val image: ImageAsset? = null,
    @SerializedName("data") val data: DataAsset? = null,
    @SerializedName("link") val link: Link? = null
)

data class TitleAsset(
    @SerializedName("text") val text: String,
    @SerializedName("len") val length: Int? = null
)

data class ImageAsset(
    @SerializedName("url") val url: String,
    @SerializedName("w") val width: Int? = null,
    @SerializedName("h") val height: Int? = null,
    @SerializedName("type") val type: Int? = null
)

data class DataAsset(
    @SerializedName("type") val type: Int,
    @SerializedName("len") val length: Int? = null,
    @SerializedName("value") val value: String
)

data class Link(
    @SerializedName("url") val url: String,
    @SerializedName("clicktrackers") val clickTrackers: List<String>? = null,
    @SerializedName("fallback") val fallback: String? = null,
    @SerializedName("ext") val extensions: Map<String, Any>? = null
)

data class EventTracker(
    @SerializedName("event") val event: Int,
    @SerializedName("method") val method: Int,
    @SerializedName("url") val url: String? = null,
    @SerializedName("customdata") val customData: Map<String, Any>? = null
)