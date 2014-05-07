# SyntroNavView

This is a version of SyntroNavView for Android. It provides an OpenGL-based display of IMU data from SyntroPiNav apps.

Check out www.richards-tech.com for more details.

### Build

Set the Android SDK Eclipse workspace to be where the SyntroAndroid repo was cloned. Then import the SyntroNavView project as an existing project. Create a launch configuration and then it should be possible to download and run the app.

### Running

Press the settings icon and then press "Select nav stream". This will display a list of available nav data streams. After starting the SyntroNavView app it may take a few seconds to connect to a SyntroControl and obtain the SyntroNet directory so, if the list is empty, try again a few seconds later.

Once a valid stream is selected, it should update the pose of the OpenGL object and also display the pose data as Euler angles in degrees. For the OpenGL object, the red cone indicates the roll axis, the green cone the pitch axis and the blue cone the yaw axis.




