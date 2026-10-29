# My Measurement — Android starter project

This is the first development phase of My Measurement. It includes the supplied logo, blue/gold styling, Tailor/Customer role selection, local registration/login, salted PBKDF2 password hashes, unique-per-local-database Tailor IDs, local SQLite storage, measurement entry, auto serial numbers, tailor measurement list, and four-field local search.

## Important limitations of this first phase
- No APK has been built in this environment because Android SDK/Gradle are not installed here.
- Tailor IDs are unique only inside this local database. Cross-device/server-side uniqueness needs a configured backend.
- Accounts and measurements are local to one device. Cloud sync, backup/recovery, secure server authorization, internal share requests, gallery attachment persistence, PDF/text export, customer saved records, profile photos, Material date picker, and multi-device/reinstall recovery are not implemented yet.
- The local search currently looks up measurements in the same device database and is not a secure substitute for authenticated cloud sharing.
- This is a starter implementation, not a production-ready release. Do not use it as the only storage for real customer measurements.

## Build
1. Install Android Studio with Android SDK Platform 35 and JDK 17.
2. Open this folder (`MyMeasurement`) in Android Studio.
3. Let Gradle sync/download the Android Gradle Plugin and Kotlin plugin.
4. Use **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. For command line builds after Gradle is installed: `gradle assembleDebug`.
6. Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Package
`com.mhabibtailors.mymeasurement`
