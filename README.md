# Android GPS Tracker User Guide


## what is GPS?
see [Wikipedia](https://en.wikipedia.org/wiki/Global_Positioning_System)

>The Global Positioning System (GPS) is a space-based radionavigation system owned by the United States government and operated by the United States Air Force. It is a global navigation satellite system that provides geolocation and time information to a GPS receiver anywhere on or near the Earth where there is an unobstructed line of sight to four or more GPS satellites.


## The Setup/Build
---
1. Ensure you have android v16 (Ice Cream Sandwich) or later for the application  
a. [Find Android Version] (http://www.androidcentral.com/android-101-how-check-your-os-version)  
2. Ensure you have a linux machine to run the server  
3. Clone the repository using the following link:

>https://github.com/COMP4985-AndroidGPS/AndroidGPS.git   

## The Server 

>The server is a Node.js program that uses socket-io to establish a socket connection to a client and receive GPS coordinates from the client's Android device.   
The server also serves as an http server to display a google map and plot the coordinates received from the client's via express library.

#### Dependencies  

Below are the libraries (and their minimum versions) required to build nodejs:   

* body-parser 1.17.1
* express 4.15.2
* express-session 1.15.2
* fs 0.0.1-security
* lodash-node 3.10.2
* socket.io 1.7.3 

#### To Build on Linux

1. Execute the following to install the dependency libraries:   
a. $ cd Server   
b. $ npm install   

#### Running the Server

1. After libraries are installed, execute the following:   
a. $ node app.js
    

## The Android Application

>The Android Application uses socket-io to communicate with and transmit location data to a remote server.  Using wireless networks as well as the android device's on-board GPS chip, the application obtains the devices latitude and longitudinal coordinates and sends them to the server to be stored and displayed in the web browser. 

##### To build the Application

1. Install the most recent version of [Android Studio] (https://developer.android.com/studio/index.html)
2. Run Android Studio
3. Open the AndroidGPS project in the client directory of the cloned repository
4. Click **Build APK** under the **Build** toolbar

##### Install the Application using Android Studio

1. Connect your android device to the computer running Android studio
2. When prompted, click enable USB debugging when the option appears on your device
3. In Android Studio, click **Run App**
4. Select your device from the Select Deployment Target window and click OK


##### Install the Application using apk

1. Enable the [installation of 3rd party apps] (http://www.android.pk/blog/tutorials/how-to-enable-third-party-apps-installation-on-android-phones/)
2. After the project is built, transfer the generated APK from your computer to the device  
a. APK created in: AndroidGPS\app\build\outputs\apk
2. Install the application   

#### Running the Application

1. Launch the installed application
2. When Prompted input:   
a. The Server's IP address  
b. Specified Port  
c. Login information provided to you by the service provider
3. Accept all permissions requested
4. Once the map is displayed, just start walking and watch the map update!
