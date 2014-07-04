package org.cgiar.ilri.odk.sensors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.cgiar.ilri.odk.sensors.handlers.BluetoothHandler;
import org.cgiar.ilri.odk.sensors.types.RFID;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason Rogena (j.rogena@cgiar.org) on 3rd June 2014
 */
public class MainActivity
        extends Activity
        implements BluetoothHandler.DeviceFoundListener,
                    ListView.OnItemClickListener,
                    BluetoothHandler.BluetoothSessionListener{

    private static String TAG = "ODK Sensors Main Activity";
    private static String KEY_SENSOR = "sensor";
    /*
    Supported sensors include
        - bluetooth
     */
    private static String KEY_DATA_TYPE = "data_type";
    /*
    Supported data types include
        - rfid
     */

    private ListView devicesLV;

    private BluetoothHandler bluetoothHandler;

    private List<String> deviceNames;
    private List<BluetoothDevice> bluetoothDevices;
    private String sensorToUse;
    private String returnDataType;

    private ProgressDialog progressDialog;

    /**
     * This method is what is called first when the activity starts
     * But you probably already knew that
     *
     * @param savedInstanceState Holds data in cases where the activity previously saved that data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);

        devicesLV = (ListView) this.findViewById(R.id.devices_lv);
        ArrayAdapter<String> deviceArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<String>());
        devicesLV.setAdapter(deviceArrayAdapter);
        devicesLV.setOnItemClickListener(this);

        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null){
            sensorToUse = bundle.getString(KEY_SENSOR);
            if(sensorToUse != null) sensorToUse = sensorToUse.toLowerCase();

            returnDataType = bundle.getString(KEY_DATA_TYPE);
            if(returnDataType != null) returnDataType = returnDataType.toLowerCase();

            Log.i(TAG, "Gotten data from parent activity");
        }
        else{
            Log.w(TAG, "Was unable to get data from previous activity. Probably because the activity was called from the launcher");
        }
        Log.i(TAG, "onCreated finished");
    }

    /**
     * This method is called second after onCreate
     * Initiate all hardware resources her and not onCreate.
     * Because all the hardware is released when onPause is called and onCreate might not be called
     * if activity resumes eg from lock screen
     *
     *      {App Launched for the 1st time} > onCreate > onResume > {screen times out} > onPause > {user turn screen back on} > onResume
     */
    @Override
    protected void onResume() {
        super.onResume();

        if(bluetoothHandler == null) {
            bluetoothHandler = new BluetoothHandler(this, this);
        }
        else {
            Log.i(TAG, "Bluetooth Handler is not null, not reinitializing it");
        }

        Log.i(TAG, "onResume finished");
    }

    /**
     * This is the first method to be called when activity becomes invisible to the user.
     * Please unlink from all hardware resources here and not in onDestroy or any other exit method
     *  to avoid eating up the devices battery
     */
    @Override
    protected void onPause() {
        super.onPause();

        stopBluetoothHandler();
        bluetoothHandler = null;

        if(progressDialog != null) progressDialog.dismiss();
        progressDialog = null;
        Log.i(TAG, "onPause finished");
    }

    /**
     * Called when the menu in the action bar has been initialized.
     * Call any method that requires stuff in the actionbar here and not in
     *  onCreate or onResume to avoid getting NullPointerExceptions
     *
     * @param menu
     *
     * @return true if actionbar (panel) is to be displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //workingMenuItem = menu.findItem(R.id.action_working);

        initBluetoothSearch();
        return true;
    }

    /**
     * This method is called when a menu item is clicked
     *
     * @param item
     * @return true if you are done with processing or false to perform normal menu handling
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_scan) {
            initBluetoothSearch();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when Bluetooth Handler starts scanning for devices
     */
    @Override
    public void onSearchStart() {
        Log.i(TAG, "Search for bluetooth devices started");

        this.runOnUiThread(new Runnable() {//done in case method is called from a thread that is not the UI thread
            @Override
            public void run() {
                //show the spinning thingy on the action bar
                setProgressBarIndeterminateVisibility(Boolean.TRUE);
            }
        });
    }

    /**
     * This method is called when a device is found by Bluetooth Handler
     *
     * @param device The BluetoothDevice found
     */
    @Override
    public void onDeviceFound(BluetoothDevice device) {
        Log.i(TAG, "onDeviceFound called. New device is : "+device.getName());

        //Get all the found devices so far
        List<BluetoothDevice> availableDevices = bluetoothHandler.getAvailableDevices();
        if(availableDevices != null){
            deviceNames = new ArrayList<String>(availableDevices.size());
            bluetoothDevices = new ArrayList<BluetoothDevice>(availableDevices.size());

            for(int index = 0; index < availableDevices.size(); index++){
                BluetoothDevice currDevice = availableDevices.get(index);

                deviceNames.add(currDevice.getName());
                bluetoothDevices.add(currDevice);// use this instead of bluetoothDevices = bluetoothHandler.getAvailableDevices() to avoid passing by reference
            }

            //repopulate the devices list view
            ((ArrayAdapter)devicesLV.getAdapter()).clear();
            ((ArrayAdapter)devicesLV.getAdapter()).addAll(deviceNames);
            ((ArrayAdapter)devicesLV.getAdapter()).notifyDataSetChanged();
        }
        else{
            Log.w(TAG, "Returned Available device list from bluetooth handler is null ");
        }
    }

    /**
     * This method is called when Bluetooth handler stops scanning for devices
     */
    @Override
    public void onSearchStop() {
        this.runOnUiThread(new Runnable() {//done in case the method is called in a thread that is not the UI thread
            @Override
            public void run() {
                //hide the spinning thingy in the action bar
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }
        });
    }

    /**
     * This method is called when a sub-activity called using startActivityForResults is called
     * Refer to:
     *      - http://developer.android.com/reference/android/app/Activity.html#startActivityForResult(android.content.Intent, int)
     *
     * @param requestCode The code used by this activity to call the sub-activity
     * @param resultCode Can either be RESULT_OK if the result is fine or RESULT_CANCEL if the sub-activity was unable to get a result
     * @param data The data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult called");
        if(requestCode == BluetoothHandler.REQUEST_ENABLE_BT){
            if(resultCode == RESULT_OK){//bluetooth was successfully enabled
                Log.d(TAG, "Bluetooth was successfully enabled");

                //Make sure you reinitialize the bluetooth handler because it's null
                bluetoothHandler = new BluetoothHandler(this, this);

                startBluetoothSearch();
            }
            else{//means that user did not enable bluetooth
                Log.d(TAG, "Bluetooth was not enabled");
                Toast.makeText(this, this.getText(R.string.bluetooth_was_not_enabled), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * This method initiates the process of getting available bluetooth devices
     * The process is:
     *      - Checking if bluetooth is enabled
     *      - Searching for available bluetooth devices
     */
    private void initBluetoothSearch(){
        Log.i(TAG, "initBluetoothSearch called");

        if(bluetoothHandler.isBluetootSupported()){
            if(bluetoothHandler.isBluetootEnabled()){
                Log.d(TAG, "Bluetooth is on");
                startBluetoothSearch();
            }
            else{
                Log.d(TAG, "Bluetooth is off");
                bluetoothHandler.requestEnableBluetooth();
            }
        }
        else{
            //TODO: tell user in dialog that bluetooth is not supported
        }
    }

    /**
     * This method tells BluetoothHandler to start the actual search
     */
    private void startBluetoothSearch(){
        Log.i(TAG, "Bluetooth search started");
        bluetoothHandler.startScan();
    }

    /**
     * This method kills everything in Bluetooth Handler that needs to be killed before bluetooth
     *  handler is set to null. This includes releasing the bluetooth module and unregistering any
     *  receiver
     */
    private void stopBluetoothHandler(){
        bluetoothHandler.stopScan();
        bluetoothHandler.unregisterReceiver();
    }

    /**
     * Handles item clicks in any AdapterView that is registered with this listener using
     *      setOnItemClickedListener
     * @param adapterView The parent view eg ListView of which view is a child of
     * @param view The child view clicked in adapterView
     * @param i Index of view in AdapterView
     * @param l Row id of the view in the parent
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(adapterView == devicesLV){
            if(i < bluetoothDevices.size()){
                BluetoothDevice selectedDevice = bluetoothDevices.get(i);
                if(bluetoothHandler.isDevicePaired(selectedDevice)){
                    BluetoothSocketThread bluetoothSocketThread = new BluetoothSocketThread();
                    bluetoothSocketThread.execute(selectedDevice);
                }
                else{
                    Toast.makeText(this, getResources().getString(R.string.device_not_paired), Toast.LENGTH_LONG).show();
                }
            }
            else {
                Log.e(TAG, "It appears like the clicked item in devicesLV is out of index in bluetoothDevices list in onItemClick");
            }
        }
        else{
            Log.w(TAG, "Unable to determine parent of clicked child in onItemClick");
        }
    }

    /**
     * This method is called when BluetoothHandler is able to connect to a bluetooth device
     * @param device The device connected to
     */
    @Override
    public void onConnected(final BluetoothDevice device) {
        this.runOnUiThread(new Runnable() {//Used in case the method is called from a thread that is not the UI thread
            @Override
            public void run() {
                if (progressDialog == null) {
                    progressDialog = ProgressDialog.show(MainActivity.this, "", getResources().getString(R.string.connected_to) + " " + device.getName());
                } else {
                    progressDialog.setMessage(getResources().getString(R.string.connected_to) + device.getName());
                    progressDialog.show();
                }
            }
        });
    }

    /**
     * This method is called when Bluetooth Handler is able to create a socket to the bluetooth device
     *
     * @param device The device on the other end of the socket
     */
    @Override
    public void onSocketOpened(final BluetoothDevice device) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog == null){
                    progressDialog = ProgressDialog.show(MainActivity.this, "",getResources().getString(R.string.socket_initialised_for) + " " + device.getName());
                }
                else{
                    progressDialog.setMessage(getResources().getString(R.string.connected_to) + device.getName());
                    progressDialog.show();
                }
            }
        });
    }

    /**
     * This method is called when the first message is gotten from the bluetooth device.
     * The first message is ignored because it might be a message that was cached on the
     *  bluetooth device before it was connected to this (android) device. Observed in:
     *      - Allflex RFID Stick Reader Model No. RS320-3-60
     *
     * @param device The device that sent the message
     */
    @Override
    public void onFirstMessageGotten(final BluetoothDevice device) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog == null){
                    progressDialog = ProgressDialog.show(MainActivity.this, "",getResources().getString(R.string.scan_again));
                }
                else{
                    progressDialog.setMessage(getResources().getString(R.string.scan_again));
                    progressDialog.show();
                }
            }
        });
    }

    /**
     * This method is called when the second message is gotten from the bluetooth device.
     * Note that the message here is identical to the first message if we have come this far
     *
     * @param device The device that sent the message
     * @param message The message from the bluetooth device
     */
    @Override
    public void onActualMessageGotten(final BluetoothDevice device, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog != null) progressDialog.dismiss();
                progressDialog = null;//do a bit of house cleaning

                if(message != null){
                    Log.d(TAG, "Message from "+device.getName()+" is "+message);

                    //Test whether the activity was called from the launcher or by ODK Collect as a sub activity
                    if(MainActivity.this.getCallingActivity() == null){//activity called from the launcher
                        Toast.makeText(MainActivity.this, "Message from "+device.getName()+" is " + message + ". App not called by other app", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "Activity not called by another activity. Result just displayed");
                    }
                    else{//activity called by odk
                        Log.i(TAG, "Activity called by "+MainActivity.this.getCallingActivity().getClassName() + " sending message there");

                        Intent intent = new Intent();
                        /*
                            "value" on the next line is specific to ODK, If you use anything else, ODK will not insert the message into the textfield
                            Refer to http://opendatakit.org/help/form-design/external-apps/
                         */

                        if(returnDataType != null){
                            if(returnDataType.equals(RFID.KEY)){
                                String value = RFID.process(message);
                                intent.putExtra("value", value);
                                setResult(RESULT_OK, intent);
                            }
                            else{
                                setResult(RESULT_CANCELED, intent);

                                Log.e(TAG, "Was unable to determine the return data type returning nothing");
                                Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.something_wrong_odk), Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            setResult(RESULT_CANCELED, intent);

                            Log.e(TAG, "The return data type is null. Probably because ODK form did not provide activity with one");
                            Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.something_wrong_odk), Toast.LENGTH_LONG).show();
                        }

                        finish();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Message from "+device.getName()+" is null ", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "Message from " + device.getName() + " is null");
                }
            }
        });
    }

    /**
     * Called when the bluetooth socket to the device is successfully closed by Bluetooth Handler
     *
     * @param device The device on the other end of the socket (now closed)
     */
    @Override
    public void onSocketClosed(final BluetoothDevice device) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.closed_socket_with)+ " " + device.getName(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This class creates an asynchronous thread for fetching data from bluetooth since getting values from
     *  InputStreams block a thread.
     *  Refer to:
     *      - http://developer.android.com/reference/android/os/AsyncTask.html
     *      - http://developer.android.com/guide/topics/connectivity/bluetooth.html
     */
    private class BluetoothSocketThread extends AsyncTask<BluetoothDevice, Integer, Boolean>{

        @Override
        protected Boolean doInBackground(BluetoothDevice... bluetoothDevices) {
            Boolean result = bluetoothHandler.getDataFromDevice(bluetoothDevices[0], MainActivity.this);
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result == null || result == false){
                Log.w(TAG, "Unable to initiate connection with bluetooth device");
            }
            else{
                Log.i(TAG, "Async task with bluetooth device successfully initiated");
            }
        }
    }
}
