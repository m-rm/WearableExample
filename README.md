# Demo for Android Wear 2.x
This demo shows the current heart rate and step count in an Activity and additionally writes it to a CSV file.

## Prerequisites
* Android Studio with Android SDK (https://developer.android.com/studio/)

## Getting started
* When opening the project for the first time use the import option instead of open
* The error message "Failed to find Build Tools revision 28.0.3" may appear. Click "Install Build Tools 28.0.3 and sync project" to install the dependency and continue.
* Wait until Android Studio is has finished loading the project
* If the watch was already used to develop an app skip ahead, otherwise follow the next points
  * Enable developer options on the watch (https://developer.android.com/training/wearables/apps/debugging)
  * Enable ADB debugging in the developer options
  * Connect the watch to the PC using the USB cable
  * Allow debugging in the dialog shown on the watch
* Push the run button in Android Studio
* Select the watch in the next dialog on press "OK"

## Additional resources
* Permission request documentation (https://developer.android.com/training/permissions/requesting)
* Step counter sensor documentation (https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-stepcounter)
* Sensor class documentation containing list of all possible sensors (https://developer.android.com/reference/android/hardware/Sensor)
