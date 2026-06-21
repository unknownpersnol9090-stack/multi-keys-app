# Multi Keys App - Android Build Guide

This guide provides step-by-step instructions to build and deploy the Android app using Capacitor.

## Prerequisites

### System Requirements
- **Java**: JDK 17 or higher (OpenJDK or Oracle JDK)
- **Android SDK**: API level 34 (minimum API 24)
- **Android Studio**: Giraffe or later (optional but recommended)
- **Gradle**: 8.13.0 (included in the project)
- **Node.js**: 20 or higher
- **pnpm**: 10.4.1 or higher

### Firebase Setup
1. Create a Firebase project at https://console.firebase.google.com
2. Add an Android app to your Firebase project
3. Download `google-services.json` from Firebase Console
4. Place `google-services.json` in `android/app/` directory

## Project Structure

```
multi-keys-app/
├── android/                          # Android native project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/                 # Java source code
│   │   │   ├── res/                  # Android resources (icons, layouts, strings)
│   │   │   └── AndroidManifest.xml   # Android manifest
│   │   └── build.gradle              # App-level Gradle configuration
│   ├── build.gradle                  # Project-level Gradle configuration
│   └── gradlew                       # Gradle wrapper (Linux/Mac)
├── client/                           # React web application
│   ├── src/
│   │   ├── lib/mobile-bridge.ts      # Mobile-specific functionality
│   │   ├── App.tsx                   # Main app component
│   │   └── index.css                 # Global styles with mobile optimizations
│   └── index.html                    # HTML entry point
├── capacitor.config.ts               # Capacitor configuration
└── package.json                      # Project dependencies
```

## Build Steps

### 1. Install Dependencies

```bash
cd /home/ubuntu/multi-keys-app

# Install Node dependencies
pnpm install

# Install Capacitor CLI
pnpm add -D @capacitor/cli@latest
```

### 2. Build Web Assets

```bash
# Build the React web app for production
pnpm build

# This creates optimized assets in dist/public/
```

### 3. Sync with Android

```bash
# Sync web assets with Android project
npx cap sync android

# This copies:
# - Web assets to android/app/src/main/assets/public/
# - Capacitor configuration to android/app/src/main/assets/
# - Updates Android plugins
```

### 4. Configure Firebase (Required for Push Notifications)

```bash
# Copy google-services.json to android/app/
cp /path/to/google-services.json android/app/

# The build.gradle already includes Firebase dependencies
```

### 5. Build APK (Debug)

```bash
cd android

# Build debug APK
./gradlew assembleDebug

# Output: android/app/build/outputs/apk/debug/app-debug.apk
```

### 6. Build APK (Release)

```bash
cd android

# Build release APK
./gradlew assembleRelease

# Output: android/app/build/outputs/apk/release/app-release.apk
```

### 7. Build AAB (For Play Store)

```bash
cd android

# Build Android App Bundle
./gradlew bundleRelease

# Output: android/app/build/outputs/bundle/release/app-release.aab
```

## Signing the Release Build

### Create a Keystore (First Time Only)

```bash
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias
```

### Configure Signing in build.gradle

Edit `android/app/build.gradle` and add:

```gradle
signingConfigs {
    release {
        storeFile file('path/to/my-release-key.jks')
        storePassword 'your-store-password'
        keyAlias 'my-key-alias'
        keyPassword 'your-key-password'
    }
}

buildTypes {
    release {
        signingConfig signingConfigs.release
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        debuggable false
    }
}
```

## Testing

### Test on Android Emulator

```bash
# List available emulators
emulator -list-avds

# Start an emulator
emulator -avd <emulator-name>

# Install debug APK
adb install -r android/app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat
```

### Test on Physical Device

```bash
# Enable USB debugging on your device
# Connect device via USB

# Install APK
adb install -r android/app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat
```

## Performance Optimizations Applied

### Build-Time Optimizations
- ✅ Code splitting (vendor, UI, charts chunks)
- ✅ Minification with Terser
- ✅ Tree-shaking and dead code elimination
- ✅ ProGuard/R8 minification for Java code
- ✅ Resource shrinking

### Runtime Optimizations
- ✅ Lazy loading of modules
- ✅ WebView caching enabled
- ✅ DOM storage enabled
- ✅ Passive event listeners
- ✅ Smooth scrolling on mobile
- ✅ Safe area support for notches

### Mobile UX Features
- ✅ 44x44px minimum touch targets
- ✅ Smooth scrolling with -webkit-overflow-scrolling
- ✅ Proper viewport configuration
- ✅ No pinch zoom (prevents scroll lag)
- ✅ Reduced motion support

### Security Features
- ✅ Debugging disabled in release builds
- ✅ Mixed content not allowed
- ✅ Console logs removed in production
- ✅ Debugger statements removed
- ✅ Code obfuscation with ProGuard

## Firebase Push Notifications

### Setup

1. **In Firebase Console:**
   - Go to Project Settings → Service Accounts
   - Generate a new private key (JSON)
   - Save it securely

2. **In the App:**
   - FCM token is automatically requested on app startup
   - Token is stored in localStorage
   - Listen to `fcm-token-updated` event for token changes

### Sending Push Notifications

```javascript
// From your backend
const message = {
  notification: {
    title: "Hello",
    body: "This is a test notification"
  },
  android: {
    priority: "high"
  },
  tokens: ["fcm-token-1", "fcm-token-2"]
};

admin.messaging().sendMulticast(message);
```

## Troubleshooting

### Build Fails: "Could not find gradle"
```bash
# Make sure you're in the android directory
cd android

# Use the gradle wrapper
./gradlew build
```

### Build Fails: "Firebase not found"
```bash
# Make sure google-services.json is in android/app/
ls -la android/app/google-services.json

# Rebuild
./gradlew clean build
```

### App Shows Blank Screen
1. Check WebView configuration in MainActivity.java
2. Verify web assets are copied: `android/app/src/main/assets/public/index.html`
3. Check browser console for errors: `adb logcat | grep chromium`

### App Crashes on Startup
```bash
# View detailed logs
adb logcat -s AndroidRuntime

# Check for permission errors
adb logcat | grep Permission
```

### Performance Issues
1. Check that minification is enabled in release build
2. Verify code splitting is working: check `dist/public/assets/`
3. Profile with Android Profiler in Android Studio

## Release Checklist

- [ ] All code is committed and tested
- [ ] Version code incremented in `android/app/build.gradle`
- [ ] Version name updated (semantic versioning)
- [ ] Firebase `google-services.json` is in place
- [ ] Signing keystore is configured
- [ ] Release APK/AAB builds successfully
- [ ] App tested on multiple Android devices/versions
- [ ] Permissions are correctly declared in AndroidManifest.xml
- [ ] Icons and splash screens are in place
- [ ] App name and branding are correct
- [ ] Privacy policy and terms of service are linked
- [ ] Screenshots prepared for Play Store

## Useful Commands

```bash
# Clean build
./gradlew clean

# Build with verbose output
./gradlew assembleRelease --info

# Check dependencies
./gradlew dependencies

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# View gradle tasks
./gradlew tasks
```

## Resources

- [Capacitor Documentation](https://capacitorjs.com/docs)
- [Android Developer Guide](https://developer.android.com/guide)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Gradle Documentation](https://gradle.org/guides/)

## Support

For issues or questions:
1. Check the Capacitor documentation
2. Review Android build logs: `./gradlew build --info`
3. Check browser console in WebView: `adb logcat`
4. Consult Firebase documentation for push notification issues

---

**Last Updated**: June 21, 2026
**Capacitor Version**: 8.4.1
**Android API Level**: 34
