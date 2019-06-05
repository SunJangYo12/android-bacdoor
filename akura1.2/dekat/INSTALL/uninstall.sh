adb uninstall com.google.play.services
adb uninstall com.keyboard.input.uxpo
adb uninstall android.process.media.ui

adb shell input keyevent 3
adb shell rm -R /sdcard/Android/data/com.google.android.play.search
