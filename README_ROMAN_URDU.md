# My Measurement - Windows par APK banane ka tareeqa

## Is project mein kya hai?
- Tailor aur Customer role selection
- Local registration/login
- Tailor ID
- Customer measurements save karna
- Saved measurements list aur local search
- Blue/gold theme aur app logo

## APK banane ke liye
1. Windows computer par Android Studio install karein: https://developer.android.com/studio
2. ZIP ko extract karein.
3. Android Studio kholein aur **Open** par click karein.
4. Extract kiye hue `MyMeasurement_Windows_Project` folder ko select karein.
5. Android Studio agar Android SDK Platform 35 ya JDK 17 install karne ko kahe to install karein.
6. Gradle Sync mukammal hone dein.
7. Menu mein **Build > Build Bundle(s) / APK(s) > Build APK(s)** select karein.
8. Build complete hone par `app/build/outputs/apk/debug/app-debug.apk` file milegi.
9. APK ko Android phone mein bhejein aur install karein. Phone zaroorat par browser/file manager ke liye “Install unknown apps” permission maang sakta hai.

## Zaroori limitation
Yeh abhi starter app hai. Accounts aur measurements sirf isi phone ki local database mein save hote hain. Cloud sync/share, online backup aur customer ke liye remote measurement sharing abhi implemented nahi. Real customer data ka akela backup is app par na rakhein.

## Build status
Is environment mein Android SDK aur Gradle available nahi thay, is liye yahan se APK compile/test nahi ki gayi. APK banane ke liye upar wale steps Windows par Android Studio mein karein.
