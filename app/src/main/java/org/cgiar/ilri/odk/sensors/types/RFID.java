package org.cgiar.ilri.odk.sensors.types;

import android.util.Log;

/**
 * Created by Jason Rogena on 4th June 2014
 *
 * This class handles any data that is of type RFID
 */
public class RFID {

    private static final String TAG = "ODK Sensors RFID";

    public static final String KEY = "rfid";

    /**
     * This method will convert the provided raw string to the desired
     * RFID string.
     * For now, what is done is:
     *      - remove all the spaces from the string
     *      - get the last 15 characters in the string
     *
     * @param raw The raw RFID String
     *
     * @return The processed RFID String
     */
    public static String process(String raw){
        if(raw != null){
            raw = raw.replaceAll("\\s+", "");
            if(raw.length()>=15){
                return raw.substring(raw.length() - 15, raw.length());
            }
            else{
                Log.w(TAG, "The length of the provided RFID string is less than 15 (without the whitespaces). Cannot process this string");
            }
        }
        else{
            Log.w(TAG, "provided RFID string was null");
        }
        Log.w(TAG, "Process method returning the original string");
        return raw;
    }
}
