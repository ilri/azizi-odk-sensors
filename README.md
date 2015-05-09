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

### Building the project

To buid this [Gradle](https://gradle.org/) project, run the following commands:

    ./gradlew clean
    ./gradlew build --debug

### License

This code is released under the [GNU General Public License v3](http://www.gnu.org/licenses/agpl-3.0.html). Please read LICENSE.txt for more details.
