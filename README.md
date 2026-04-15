<p align="center">
  <img src="logo-and-icons/Logo.png" alt="Wane App Icon" width="120" />
</p>

# Wane

A focus session app for Android that helps you stay off your phone by blocking distracting apps during timed sessions. Features a calming water animation that recedes as your session progresses.

**Min SDK:** Android 9 (API 28) | **Target SDK:** Android 16 (API 36)

## Prerequisites

- **Android Studio** Ladybug or later (with Android SDK 36)
- **JDK 17**
- An Android device or emulator running Android 9+
- **USB cable** (for physical device installation)

## Building the App

```bash
# Clone the repository
git clone https://github.com/sumitpore/wane-app.git
cd limit-mobile-control-app

# Build the debug APK
./gradlew assembleDebug
```

The APK will be generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Installing on a Device (Sideload)

Since the app is not yet published on Google Play, you need to sideload it via USB.

### Step 1: Enable Developer Options on your Android device

1. Open **Settings > About phone**
2. Tap **Build number** 7 times until you see "You are now a developer"

### Step 2: Enable USB Debugging

1. Go to **Settings > Developer options**
2. Enable **USB debugging**
3. Connect your device to your computer via USB
4. When prompted on the device, tap **Allow** to authorize the computer

### Step 3: Install via ADB

```bash
# Verify your device is connected
adb devices

# Install the debug build
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or, build and install in one step:

```bash
./gradlew installDebug
```

### Step 4: Enable the Accessibility Service

Wane uses an Accessibility Service to block distracting apps during focus sessions. This must be enabled manually after installation:

1. Open **Settings > Accessibility** on your device
2. Find **Wane** in the list of services
3. Tap it and toggle **Use Wane** to ON
4. Confirm the permission dialog

> **Why does Wane need Accessibility access?**
> The Accessibility Service allows Wane to detect when you open a blocked app during a focus session and redirect you back. Wane does not read, collect, or transmit any personal data through this service.

### Step 5: Grant Notification Access

For notification filtering during focus sessions:

1. Open **Settings > Apps & notifications > Special app access > Notification access**
2. Enable **Wane**

## Running from Android Studio

1. Open the project in Android Studio
2. Select your connected device from the device dropdown
3. Click **Run** (green play button) or press `Shift+F10`

## Uninstalling

```bash
adb uninstall com.wane.app.debug
```

Or uninstall from the device: **Settings > Apps > Wane > Uninstall**

## Project Structure

```
app/src/main/kotlin/com/wane/app/
├── animation/       # Water GL animation (shaders, renderer, themes)
├── data/            # DataStore preferences, Room database, repositories
├── service/         # Foreground service, Accessibility service, auto-lock
├── shared/          # Domain models and route definitions
├── ui/
│   ├── components/  # Shared UI components
│   ├── home/        # Main screen (duration picker, start button)
│   ├── onboarding/  # First-launch onboarding flow
│   ├── session/     # Active focus session screen
│   ├── settings/    # App settings
│   └── theme/       # Colors, typography, motion
└── util/            # Accessibility, notification, and intent helpers
```

## License

This project is licensed under the [MIT License](LICENSE).
