# Azizi ODK Sensors
This tool can be used by ODK Collect to enter data from bluetoot devices into text fields.
Refer to [ODK External Apps](http://opendatakit.org/help/form-design/external-apps/).

This tool has been tested using the following devices:

*   DataMars GES3S RFID Reader (Connection to Android device via Bluetooth)
*   Allflex RS320-3-6 ISO RFID Stick Reader


## Usage
This tool can be used by ODK collect by calling it using and intent. As of now the intent is:

    org.cgiar.ilri.odk.sensors.action.GET_SENSOR_DATA(sensor='bluetooth', data_types='rfid')

As shown in the above intent call, you provide the intent with two variables i.e sensor and data_type.
Supported sensors include:

*   bluetooth

Supported data types include:

*   rfid

Make sure the variable names and values are provided as is (observe case sensitivity). Also ensure you 
append 'ex:' to the intent as specified [here](http://opendatakit.org/help/form-design/external-apps/).


## Building the project
 1. Create the local.properties file in the project's root directory and specify Android SDK location there like this 
    sdk.dir=/android/SDK/location

 2. Run the following command(s) in the project's root directory to build the project
    ./gradlew build 

 3. You can also import the Project as a local Android Studios project on your machine