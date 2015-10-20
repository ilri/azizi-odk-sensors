/*
 Note that this application has been custom made by and for use by the ILRI Azizi Biorepository team. (C) 2015 Jason Rogena <j.rogena@cgiar.org>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
              /**  return raw.substring(raw.length() - 15, raw.length()); */
              /** testing for RT100 V8 Scanner */
              return raw.substring(0, 15);
              
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
