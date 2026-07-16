# Daily Quote — Android app

A tiny dark-themed app: add your own quotes, and each day it surfaces a random
one from your list via a home-screen **widget** and a daily **notification**
(fixed at **8:00 AM**). The quote is the same in the widget and the
notification, and it changes every day.

## Getting the app onto your phone (no developer tools needed)

The APK is built for you automatically by GitHub Actions.

1. Go to the **Actions** tab of this repository on GitHub.
2. Open the most recent **Build APK** run that has a green check.
3. Scroll to the **Artifacts** section at the bottom and download
   **`daily-quote-apk`** (a zip).
4. Unzip it — inside is `app-debug.apk`.
5. Copy that APK to your Android phone (email it to yourself, USB, Google
   Drive, etc.) and tap it to install. You'll need to allow
   "install from unknown sources" for whichever app you open it from — Android
   will prompt you.

> The first time you open the app, Android will ask permission to send
> notifications — tap **Allow** so the daily notification works.

## Using it

The app comes with a handful of starter quotes so it isn't empty on day one —
you can edit or delete any of them.

- **Add quote** button → type the quote and (optionally) an author → **Save**.
- Tap any quote in the list to edit it; the trash icon deletes it.
- Add the **widget**: long-press your home screen → **Widgets** → find
  **Daily Quote** → drag it onto the screen.

## Building it yourself (optional)

If you have Android Studio, just open this folder and press Run. The project
uses Gradle 8.9, AGP 8.5.2, Kotlin 1.9.24, min SDK 26.
