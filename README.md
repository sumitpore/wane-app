<p align="center">
  <img src="logo-and-icons/Logo.png" alt="Wane App Icon" width="120" />
</p>

# Wane

A focus session app for Android that helps you stay off your phone by blocking distracting apps during timed sessions. Features a calming water animation that recedes as your session progresses.

**Min SDK:** Android 9 (API 28) | **Target SDK:** Android 16 (API 36)

## Demo

https://github.com/user-attachments/assets/ac324ff5-3538-4403-8ad1-f63cd2cf6a8b

## Install Without Cloning

You can install the app directly on your Android device without cloning the repo or building from source.

1. On your Android device, open this link to download the APK:

   **[Download app-debug.apk](https://github.com/sumitpore/wane-app/raw/main/app/build/outputs/apk/debug/app-debug.apk)**

2. When prompted by your browser, tap **Download** (or **OK**)
3. Once downloaded, open the APK from your notification shade or file manager
4. If prompted, allow installation from unknown sources (your browser will guide you through this)
5. Tap **Install**
6. After installation, follow [Step 4](#step-4-enable-the-accessibility-service) and [Step 5](#step-5-grant-notification-access) below to grant the required permissions

## Building from Source

### Prerequisites

- **Android Studio** Ladybug or later (with Android SDK 36)
- **JDK 17**
- An Android device or emulator running Android 9+
- **USB cable** (for physical device installation)

### Build the APK

```bash
# Clone the repository
git clone https://github.com/sumitpore/wane-app.git
cd wane-app

# Build the debug APK
./gradlew assembleDebug
```

The APK will be generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Installing via USB (Sideload)

If you built from source or downloaded the APK to your computer, you can install via USB.

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
> The Accessibility Service monitors which app is in the foreground. During an active focus session, if you open a blocked app, Wane redirects you back to its session screen. The service only reads package names from window-state events — it cannot read screen content, passwords, or messages, and it does not collect or transmit any data.

### Step 5: Grant Notification Access

For notification filtering during focus sessions:

1. Open **Settings > Apps & notifications > Special app access > Notification access**
2. Enable **Wane**

> **Why does Wane need Notification access?**
> During a focus session, Wane silences distracting notifications by snoozing them until the session ends. Calls and messages from your phone/SMS apps are never snoozed so you don't miss anything urgent. Wane only reads each notification's package name and category to decide whether to snooze it — it does not read notification text or content, and it does not collect or transmit any data.

## Running from Android Studio

1. Open the project in Android Studio
2. Select your connected device from the device dropdown
3. Click **Run** (green play button) or press `Shift+F10`

## Uninstalling

```bash
adb uninstall com.unclutteredapps.wane.debug
```

Or uninstall from the device: **Settings > Apps > Wane > Uninstall**

## License

This project is licensed under the [MIT License](LICENSE).
