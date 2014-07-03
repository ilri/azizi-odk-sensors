Azizi ODK Tools
==========================
This application holds tools to be used by ODK Collect on Android. Tools so far include

1. Sensor Tool
--------------
This tool can be used by ODK Collect to enter data from bluetoot devices into text fields.
Refer to [ODK External Apps](http://opendatakit.org/help/form-design/external-apps/)


Building the project
--------------------
 1. Create the local.properties file in the project's root directory and specify Android SDK location there like this 
    sdk.dir=/android/SDK/location

 2. Run the following command(s) in the project's root directory to build the project
    ./gradlew build 

 3. You can also import the Project as a local Android Studios project on your machine   
