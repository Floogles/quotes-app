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

Each day's quote is drawn from a shuffled "deck": every quote is shown once, in
a random order, before any of them repeats. New quotes you add are mixed into
the current deck so they show up soon.

## Backing up / restoring your quotes

Use the **⋮ menu** (top-right):

- **Export / back up quotes** → saves all your quotes to a `.json` file
  (choose a location like Downloads or Google Drive).
- **Import quotes** → pick a previously exported `.json` file to add its quotes
  back in. Importing *merges* — it won't create duplicates, so it's safe to run
  more than once.

## Updating to a new version

From version 1.1 onward the app is signed with a fixed key, so you can install a
newer APK **right over the top** of the existing app — your quotes are kept
automatically. Just download the new `daily-quote-apk` and tap it to install.

> **One-time note:** the very first 1.1 install may report a signature mismatch
> with an older build and refuse to install over it. If that happens, use
> **Export** first (if your current version has the ⋮ menu), then uninstall,
> install 1.1, and **Import**. After that, future updates are seamless.

## Building it yourself (optional)

If you have Android Studio, just open this folder and press Run. The project
uses Gradle 8.9, AGP 8.5.2, Kotlin 1.9.24, min SDK 26.
