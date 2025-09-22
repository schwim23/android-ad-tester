# Android Ad Tester

A comprehensive Android application for testing both native ads (via OpenRTB bid responses) and VAST video ads. This app allows developers and ad operations teams to test ad integrations with real-world ad formats.

## Features

### Main Features
- **Ad Type Selection**: Choose between Native Ads and Video Ads from the main screen
- **Native Ad Testing**: Test native ads by parsing OpenRTB bid responses with native ADM
- **Video Ad Testing**: Test VAST video ads using Media3 ExoPlayer with Google IMA SDK
- **Direct JSON Input**: Paste OpenRTB bid responses directly for immediate testing
- **Real-time Feedback**: Status updates and comprehensive error handling

### Native Ads (OpenRTB)
- Direct OpenRTB bid response JSON parsing
- Native ADM extraction and rendering
- Custom native ad renderer with support for:
  - Title, description, advertiser name
  - Main image and icon assets
  - Call-to-action buttons
  - Click tracking and impression tracking
- Sample bid response with realistic native ad data

### Video Ads (VAST)
- Media3 ExoPlayer with Google IMA SDK integration
- VAST tag parsing and ad rendering
- Support for skippable preroll ads
- Cachebuster functionality
- Content video playback after ads
- Comprehensive playback state tracking

## Dependencies

- **Media3 ExoPlayer 1.2.1**: For video playback
- **Media3 IMA Extension 1.2.1**: For VAST ad support
- **Google IMA SDK**: For interactive media ads
- **Retrofit 2.9.0**: For network requests (OpenRTB)
- **Gson**: For JSON parsing
- **OkHttp**: For HTTP logging and networking
- **Material Design Components**: For modern UI

## Usage

### Getting Started
1. Launch the app
2. Choose between "Video Ads (VAST)" or "Banner Ads" (now Native Ad Tester)

### Testing Native Ads
1. Select "Banner Ads" from the main screen (opens Native Ad Tester)
2. Paste a complete OpenRTB bid response JSON in the text area
3. Or tap "Use Sample Native Ad Response" for a working example
4. Tap "Parse and Render Native Ad"
5. Watch the native ad render with all assets (title, image, description, CTA)

### Testing Video Ads
1. Select "Video Ads (VAST)" from the main screen
2. Enter a VAST tag URL (pre-filled with working sample)
3. Or use the sample buttons: "Skippable Preroll" or "Linear Ad"
4. Tap "Load VAST Ad"
5. Watch the preroll ad play, followed by content video

## Sample Data

### Native Ads (Sample OpenRTB Bid Response)
```json
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
          "nurl": "https://example.com/win_notice?bidid=${AUCTION_ID}&price=${AUCTION_PRICE}",
          "adomain": ["example.com"],
          "crid": "creative_XYZ",
          "adm": "{\"native\":{\"ver\":\"1.2\",\"assets\":[{\"id\":1,\"title\":{\"text\":\"Sample Native Ad Title\"},\"required\":1},{\"id\":2,\"img\":{\"url\":\"https://example.com/images/main_image.jpg\",\"w\":1200,\"h\":628},\"required\":1},{\"id\":3,\"data\":{\"type\":1,\"value\":\"This is a compelling description of the ad.\"}},{\"id\":4,\"data\":{\"type\":2,\"value\":\"Advertiser Name\"}},{\"id\":5,\"link\":{\"url\":\"https://example.com/click\"},\"ext\":{\"clicktrackers\":[\"https://example.com/clicktracker\"]}}],\"eventtrackers\":[{\"event\":1,\"methods\":[1,2],\"url\":\"https://example.com/impressiontracker\"}]}}"
        }
      ]
    }
  ],
  "cur": "USD"
}
```

### Video Ads (Sample VAST URL)
```
https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_preroll_skippable&sz=400x300&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=%%CACHEBUSTER%%
```

## Build Instructions

1. Clone this repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run on an Android device or emulator (API 21+)

## Architecture

### Activities
- **MainActivity**: Main menu for selecting ad type
- **BannerAdActivity**: Native ad testing with OpenRTB parsing
- **VideoAdActivity**: Video ad testing with VAST support

### Key Components
- **OpenRTBService**: Handles OpenRTB bid response parsing and native ADM extraction
- **NativeAdRenderer**: Custom view for rendering native ad assets
- **Media3 ExoPlayer with IMA extension**: Handles VAST ad parsing and playbook
- **Material Design components**: Modern UI experience

### Models
- **OpenRTBModels**: Data classes for OpenRTB bid request/response structures
- Support for native ad assets, tracking, and metadata

## Permissions

- `INTERNET`: Required for loading ads and tracking
- `ACCESS_NETWORK_STATE`: Used by ad SDKs for optimization

## Testing Notes

- **Native Ads**: Use real OpenRTB bid response JSON for testing
- **Video Ads**: Sample VAST URLs provided for immediate testing  
- **Real-time feedback**: Status updates help debug ad loading issues
- **Comprehensive logging**: Detailed logs for troubleshooting
- **Safe for development**: Uses test URLs that won't affect production metrics

## Screenshots

The app provides:
- Clean, Material Design interface
- Real-time status updates during ad loading
- Proper error handling and user feedback
- Sample data for quick testing

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is intended for educational and testing purposes.