# BubbleBot Stealth

Scaffold Android (Kotlin), Xposed/LSPosed trigger, and Firebase Cloud Functions for the BubbleBot app.

## Requirements

- JDK 17 or newer and Android SDK platform/build-tools 35.
- Root access on the Android device for `su screencap`.
- An Xposed-compatible framework (for example LSPosed) with the module enabled for SystemUI.
- A Firebase project with Anonymous Authentication, App Check/Play Integrity, and Cloud Functions enabled.
- Google Cloud Vertex AI access for the configured project and region.

## Configure Firebase

1. Download the Android app's `google-services.json` from your Firebase project and place it at `app/google-services.json` (the `.example` file is a placeholder only; do not use it for a live backend).
2. Set the GitHub Actions secret `GOOGLE_SERVICES_JSON` to the contents of that file to enable the APK workflow.
3. Update `.firebaserc` to the Firebase project you own before deploying functions.
4. Enable the Vertex AI API and configure credentials/permissions for the Cloud Functions runtime service account.

Do not commit a real `google-services.json` or credentials. The app sends captured screen images to the configured Vertex AI function; use it only with the device owner's consent and where screen capture and AI assistance are permitted.

## Build

```sh
./gradlew :app:assembleDebug :xposed:assembleDebug
```

The build expects a real `app/google-services.json`. APKs are written under each module's `build/outputs/apk/debug/` directory.

## Cloud Functions

```sh
cd functions
npm install
npm run lint
cd ..
firebase deploy --only functions
```

The callable function requires an authenticated Firebase user and valid App Check token. It invokes Gemini on Vertex AI in `us-central1`.
