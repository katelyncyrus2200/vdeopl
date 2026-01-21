MyVideoPlayer (Media3/ExoPlayer) - No Android Studio, No Gradle Wrapper

Package: com.example.myvideoplayer

How to build ONLINE (GitHub Actions):
1) Create a GitHub repo and upload the contents of this ZIP.
2) Go to Actions -> run "Build APK (no Android Studio)".
3) Download artifact "app-debug-apk" -> install APK on your phone.

Notes:
- Workflow downloads Gradle 8.6 automatically, so you don't need gradlew/gradle-wrapper.jar.
- App opens system picker, then plays selected local video fullscreen (immersive) in landscape.
