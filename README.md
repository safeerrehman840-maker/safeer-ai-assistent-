# Safeer AI Assistant (سفیر)

**Safeer AI Assistant** is a smart personal assistant Android application developed for Urdu, Roman Urdu, and English users.

---

## 👤 App Ownership & Branding

- **Application Name**: Safeer AI Assistant
- **Tagline**: Personal AI Assistant
- **Owner**: Safeer Ur Rehman
- **Contact Number**: `03293408114`
- **Website**: Coming soon
- **GitHub Repository**: [https://github.com/safeerrehman840-maker/safeer-ai-assistant](https://github.com/safeerrehman840-maker/safeer-ai-assistant)
- **Package ID**: `com.safeer.aiassistant` (Namespace: `com.example`)

Owner details are prominently accessible within:
1. **App Settings → About Safeer AI Assistant** (`AboutOwnerScreen`)
2. **App Settings footer branding**
3. **Interactive contact actions**:
   - **Copy Number**: One-tap clipboard copy.
   - **Call Owner**: Opens Android phone dialer with `tel:03293408114` pre-filled.
   - **WhatsApp Contact**: Prepares a verified WhatsApp chat message to `+923293408114`.

---

## 🛠️ Diagnostics & Error Reporting

Located at **Settings → System Diagnostics**:
- **Application & Build Information**: Package name, version code, Android SDK level, and device manufacturer/model.
- **Permissions Health Check**: Live verification of `RECORD_AUDIO` (microphone), `POST_NOTIFICATIONS`, and `SCHEDULE_EXACT_ALARM`.
- **AI Intelligence Engine Status**: Displays whether live Gemini 3.5 Flash is active or operating in built-in multilingual NLP fallback mode.
- **Diagnostics Log**: Safe, privacy-preserving report viewer.
- **Copy Error Details**: Easily copies the diagnostics report to clipboard for troubleshooting.
- **Retry / Recheck**: Refreshes connectivity and permission states dynamically.

---

## 🚀 Building & Running the App

### 1. Build Debug APK via Gradle
```bash
gradle assembleDebug
```
The APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 2. Run Automated Unit Tests
```bash
gradle :app:testDebugUnitTest
```
Runs Robolectric and JVM unit tests covering multilingual NLP parsing and application logic.

### 3. Installing on a Physical Android Device
1. Transfer `app-debug.apk` to your phone via USB cable or download it directly from Google Drive / storage.
2. Tap the APK file on your device.
3. If prompted, toggle **"Install unknown apps"** in Android Settings for your file manager.
4. Open **Safeer AI Assistant** and grant Microphone & Notification permissions when prompted.
