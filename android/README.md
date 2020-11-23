# Background Stylizer Android App

This is an Android sample app that stylize the background of an image.  

Please read the detailed instructions on how to create the application in this blog post on Medium (TODO: add link)

## TensorFlow Lite Model
The TensorFlow Lite Models used in the Android app can be found under the [/ml](../ml) folder.

## Requirements
* Install Android Studio 4.1.1 from [here](https://developer.android.com/studio).
* Android device in developer ode with USB debugging enabled.
* USB cable to connect an Android device to computer.

## Build and run
* Clone the project repo:  
`git clone https://github.com/margaretmz/segmentation-style-transfer.git`  
* Open the Android code under /android in Android Studio.
* Connect your Android device to computer then click on `"Run -> Run 'app'`.
* Once the app is launched on device, grant camera permission.
* Take a selfie or choose a photo from gallery, and wait for the TensorFlow Lite model to process the photo for background segmentation.
* On the second screen, choose a thumbnail image for stylizing the background.

## Download the app apk
If you are not familar with building the app in Android and would like to just try out the app, please download the apk file from the [build/outputs/apk/debug/](app/build/outputs/apk/debug) folder.   

Note: you will need to grant permission to install unknown apps from your Android device security settings. 
