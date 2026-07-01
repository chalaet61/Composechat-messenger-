<div align="center">
<img width="1200" height="475" alt="Composechat Messenger" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Composechat Messenger

A modern Android messaging application built with **Jetpack Compose** and powered by **Google's Gemini AI**.

## Features

- 💬 Real-time messaging interface
- 🤖 AI-powered chat assistance with Gemini API
- 🎨 Modern UI built with Jetpack Compose
- 📱 Android native development
- 🔐 Secure API key management via `.env`

## Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest version)
- JDK 11 or higher
- Android SDK 28+
- Gemini API key from [Google AI Studio](https://ai.studio/)

## Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/chalaet61/Composechat-messenger-.git
cd Composechat-messenger-
```

### 2. Open in Android Studio
- Launch Android Studio
- Select **File** → **Open** and choose the project directory
- Allow Android Studio to fix any incompatibilities as it imports the project

### 3. Configure Gemini API Key
- Create a `.env` file in the project root directory
- Add your Gemini API key (see `.env.example` for reference):
  ```
  GEMINI_API_KEY=your_api_key_here
  ```

### 4. Build Configuration
- Remove this line from `app/build.gradle.kts`:
  ```
  signingConfig = signingConfigs.getByName("debugConfig")
  ```

### 5. Run the App
- Select your emulator or physical device
- Click **Run** (or press `Shift + F10`)

## Project Structure

```
Composechat-messenger-/
├── app/                    # Main application module
├── build.gradle.kts        # Build configuration
└── .env.example           # Environment variables template
```

## Technology Stack

- **Kotlin** - Primary development language
- **Jetpack Compose** - Modern UI toolkit
- **Google Gemini API** - AI capabilities
- **Android Architecture Components** - MVVM pattern

## Getting Your Gemini API Key

1. Visit [Google AI Studio](https://ai.studio/)
2. Sign in with your Google account
3. Create a new API key
4. Copy and paste it into your `.env` file

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Build fails | Try `File → Sync Now` or rebuild the project |
| API key not found | Ensure `.env` file is in project root with correct key |
| Emulator issues | Use Android Studio's AVD Manager to create a new device |

## Contributing

Contributions are welcome! Feel free to:
- Fork the repository
- Create a feature branch (`git checkout -b feature/YourFeature`)
- Commit changes (`git commit -m 'Add YourFeature'`)
- Push to the branch (`git push origin feature/YourFeature`)
- Open a Pull Request

## License

This project is provided as-is. See LICENSE file for more details.

## Support

For issues or questions:
- Open an [issue](https://github.com/chalaet61/Composechat-messenger-/issues)
- Check existing documentation in the repo

---

**View your app in AI Studio:** https://ai.studio/apps/3bcebdfb-632a-4a86-b2eb-374ec17fc60c
