# Dhaka Traffic Survival — Android project

This Android Studio project wraps the supplied single-file HTML game in a native Android WebView and adds a native Google Mobile Ads banner.

## Included
- App name: ঢাকা ট্রাফিক সারভাইভাল
- Package ID: `com.mizanurmunna.dhakatrafficsurvival`
- Portrait orientation
- Offline game assets (`index.html` is stored in `app/src/main/assets/`)
- WebView JavaScript + DOM storage for game logic and saved progress
- Android bridge for saving the PNG result card and sharing the result image
- Google Mobile Ads SDK with Google's sample app ID and sample banner ad unit ID
- External website links open outside the game WebView

## Build the APK
1. Install Android Studio on a computer.
2. Extract this ZIP.
3. In Android Studio choose **Open**, then select the `DhakaTrafficSurvival-Android` folder.
4. Allow Gradle sync and install any SDK components Android Studio requests.
5. Use **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. Android Studio will show the APK location when the build finishes.

The project source is prepared, but an APK has not been compiled or device-tested in this environment.

## AdMob before publishing
- The project currently uses Google's official sample AdMob App ID and test banner ID. Test ads are safe for development only.
- Create/register the app in your own AdMob account, then replace the sample App ID in `app/src/main/AndroidManifest.xml` and replace `TEST_BANNER_AD_UNIT_ID` in `MainActivity.java` with your own banner ad unit ID.
- Keep test ads enabled while developing. Never click live ads to test them.
- Before publishing, complete the required privacy, consent, app-content, store listing, and AdMob setup steps for your audience and region.
- AdMob approval/serving is separate from building the APK.

## Notes
- This is a source project, not a ready-to-install APK.
- The Facebook share feature in the HTML still needs a public game URL to share a playable link; set `PUBLIC_GAME_URL` in `index.html` if you host the game online.


## Build from an Android phone using GitHub Actions

This ZIP includes `.github/workflows/build-apk.yml` so the APK can be built in GitHub's cloud runner without a computer.

1. On your phone, sign in to GitHub and create a **new repository**. For the simplest setup, set it to **Private**.
2. Upload the contents of this ZIP into the repository (upload the files/folders inside the ZIP, not the ZIP itself).
3. Open the repository's **Actions** tab and enable Actions if asked.
4. Select **Build Dhaka Traffic Survival APK** from the workflow list, tap **Run workflow**, then run it.
5. When the run finishes successfully, open that run and download the artifact named `Dhaka-Traffic-Survival-debug-APK`.
6. Extract the downloaded artifact ZIP on your phone. It contains `app-debug.apk`. Tap it to install; Android may ask you to allow installs from your browser or file manager.

If the build fails, open the failed run and share the error log so it can be fixed. A debug APK is for testing; before release, replace the sample Google AdMob App ID and banner ad unit ID in the project with your own IDs. Never click your own live ads.
