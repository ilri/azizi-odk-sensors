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

package org.cgiar.ilri.odk.sensors;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.util.Log;

import org.cgiar.ilri.odk.sensors.handlers.BluetoothHandler;
import org.cgiar.ilri.odk.sensors.storage.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Jason Rogena j.rogena@cgiar.org
 * This Activity presents the App's settings
 */
public class SettingsActivity extends PreferenceActivity
                              implements Preference.OnPreferenceChangeListener{

    private static final String TAG = "SettingsActivity";

    private PreferenceCategory bluetoothPC;
    private ListPreference btRFIDDefaultDevLP;

    private List<CharSequence> pairedBTDeviceNames;
    private List<CharSequence> pairedBTDeviceAddresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.activity_settings);

        bluetoothPC = (PreferenceCategory)findPreference("bluetooth_pc");
        btRFIDDefaultDevLP = (ListPreference)findPreference("bt_rfid_default_dev_lp");
        btRFIDDefaultDevLP.setOnPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadPairedBTDevices();
        loadSetPreferences();
    }

    private void loadPairedBTDevices() {
        //load the paired bluetooth devices
        BluetoothHandler bluetoothHandler = new BluetoothHandler(this, null, null);
        Set<BluetoothDevice> pairedDevices = bluetoothHandler.getPairedDevices();

        pairedBTDeviceNames = new ArrayList<CharSequence>();
        pairedBTDeviceAddresses = new ArrayList<CharSequence>();

        pairedBTDeviceNames.add(getString(R.string.none));
        pairedBTDeviceAddresses.add(BluetoothHandler.DEFAULT_BT_MAC_ADDRESS);

        for(BluetoothDevice currDevice : pairedDevices){
            pairedBTDeviceNames.add(currDevice.getName());
            pairedBTDeviceAddresses.add(currDevice.getAddress());
        }

        btRFIDDefaultDevLP.setEntries(pairedBTDeviceNames.toArray(new CharSequence[pairedBTDeviceNames.size()]));
        btRFIDDefaultDevLP.setEntryValues(pairedBTDeviceAddresses.toArray(new CharSequence[pairedBTDeviceAddresses.size()]));
    }

    private void loadSetPreferences(){
        String defaultBTRFIDDevice = SharedPreferenceManager.getSharedPreference(this, SharedPreferenceManager.SP_DEFAULT_BT_RFID_DEVICE_ADDRESS, BluetoothHandler.DEFAULT_BT_MAC_ADDRESS);
        String btRFIDDefaultDevName = null;

        btRFIDDefaultDevLP.setValue(defaultBTRFIDDevice);

        if(!defaultBTRFIDDevice.equals(BluetoothHandler.DEFAULT_BT_MAC_ADDRESS)){
            if(pairedBTDeviceAddresses != null && pairedBTDeviceNames != null){
                for(int i = 0; i < pairedBTDeviceAddresses.size(); i++){
                    if(pairedBTDeviceAddresses.get(i).equals(defaultBTRFIDDevice)){
                        btRFIDDefaultDevName = pairedBTDeviceNames.get(i).toString();
                    }
                }

                if(btRFIDDefaultDevName == null){//means that the default rfid device is no longer paired with this device.
                    //set the default device to no device
                    defaultBTRFIDDevice = BluetoothHandler.DEFAULT_BT_MAC_ADDRESS;
                    SharedPreferenceManager.setSharedPreference(this, SharedPreferenceManager.SP_DEFAULT_BT_RFID_DEVICE_ADDRESS, BluetoothHandler.DEFAULT_BT_MAC_ADDRESS);

                    Log.w(TAG, "The saved default bluetooth RFID device is no longer paired with this device. Setting the default bluetooth RFID device to nothing");
                }
            }
        }

        btRFIDDefaultDevLP.setValue(defaultBTRFIDDevice);
        if(btRFIDDefaultDevName != null) {
            btRFIDDefaultDevLP.setSummary(btRFIDDefaultDevName);
        }
        else {
            btRFIDDefaultDevLP.setSummary(getString(R.string.pref_bt_rfid_default_dev_summary));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference == btRFIDDefaultDevLP){
            String defaultDeviceAddress = newValue.toString();
            String defaultDeviceName = "";
            for(int i = 0; i < pairedBTDeviceAddresses.size(); i++){
                if(pairedBTDeviceAddresses.get(i).equals(defaultDeviceAddress)){
                    defaultDeviceName = pairedBTDeviceNames.get(i).toString();
                }
            }

            btRFIDDefaultDevLP.setSummary(defaultDeviceName);
            SharedPreferenceManager.setSharedPreference(this, SharedPreferenceManager.SP_DEFAULT_BT_RFID_DEVICE_ADDRESS, defaultDeviceAddress);
        }
        return true;
    }
}
