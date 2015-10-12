## Azizi ODK Sensors [![Build Status](https://travis-ci.org/ilri/azizi-odk-sensors.svg?branch=master)](https://travis-ci.org/ilri/azizi-odk-sensors)

This application can be used by ODK Collect to enter data from bluetoot devices into text fields.
Refer to [ODK External Apps](https://opendatakit.org/help/form-design/external-apps/).

Testing was done using the following devices:

*   DataMars GES3S RFID Reader (Connection to Android device via Bluetooth)
*   Allflex RS320-3-6 ISO RFID Stick Reader

### Usage

ODK Sensors can be called by an ODK form running on ODK Collect using an intent. As of now the intent is:

    org.cgiar.ilri.odk.sensors.action.GET_SENSOR_DATA(sensor='bluetooth', data_types='rfid')

The intent contains two variables i.e *sensor* and *data_type*.

Supported sensors include:

*   bluetooth

Supported data types include:

*   rfid

Observe character case when defining the variables. Also ensure you append 'ex:' to the intent as specified [here](https://opendatakit.org/help/form-design/external-apps/).

[sample_form.xls](https://raw.githubusercontent.com/ilri/azizi-odk-sensors/master/sample_form.xls) illustrates how ODK Sensors can be used with ODK Collect.

### Building the project

To buid this [Gradle](https://gradle.org/) project, run the following commands:

    ./gradlew clean
    ./gradlew build --debug

### Signing the release APK

Although it is suffient to building the application in debug mode, it is recommended to build and sign the application in release mode. To do this, first make sure you have a release signing key:

```
cd ~/.android
keytool -genkey -v -keystore release.keystore -alias androidreleasekey -keyalg RSA -keysize 2048 -validity 10000
```

Then add the following lines in your local.properties file in the project's root directory:

```
STORE_FILE=/home/[username]/.android/release.keystore
STORE_PASSWORD=your_key_store_pw
KEY_ALIAS=androidreleasekey
KEY_PASSWORD=your_release_key_pw
```

You can now build and sign the application in release mode:

```
./gradlew clean
./gradlew aR
```

To install the signed application run:

```
adb install -r app/build/outputs/apk/app-release.apk
```

For your convenience, a signed APK for this project is available [here](https://raw.githubusercontent.com/ilri/azizi-odk-sensors/master/app/build/outputs/apk/app-release.apk)

### License

This code is released under the [GNU General Public License v3](http://www.gnu.org/licenses/agpl-3.0.html). Please read LICENSE.txt for more details.
