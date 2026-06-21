# Quick Start - Build Android App

## One-Command Build (After Firebase Setup)

```bash
# 1. Install dependencies
pnpm install

# 2. Build web assets
pnpm build

# 3. Sync with Android
npx cap sync android

# 4. Build APK
cd android
./gradlew assembleDebug    # Debug APK
# or
./gradlew assembleRelease  # Release APK (requires signing)
```

## Required Setup

### Firebase (For Push Notifications)
1. Go to https://console.firebase.google.com
2. Create a new project or use existing one
3. Add Android app with package name: `app.lovable.multihackskey`
4. Download `google-services.json`
5. Copy to: `android/app/google-services.json`

### Signing (For Release Build)
```bash
# Create keystore (one time)
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias

# Edit android/app/build.gradle and add signing config
# See ANDROID_BUILD_GUIDE.md for details
```

## Output Files

- **Debug APK**: `android/app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `android/app/build/outputs/apk/release/app-release.apk`
- **Play Store Bundle**: `android/app/build/outputs/bundle/release/app-release.aab`

## Test on Device

```bash
# Install debug APK
adb install -r android/app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Build fails | Run `./gradlew clean` then rebuild |
| Firebase not found | Ensure `google-services.json` is in `android/app/` |
| Blank screen on startup | Check WebView logs: `adb logcat \| grep chromium` |
| Slow performance | Verify minification is enabled in release build |

## Documentation

- **Full Guide**: See `ANDROID_BUILD_GUIDE.md`
- **Implementation Details**: See `IMPLEMENTATION_SUMMARY.md`
- **Original Instructions**: See `CAPACITOR_BUILD.md`

---

**Ready to build?** Start with: `pnpm install && pnpm build && npx cap sync android`
