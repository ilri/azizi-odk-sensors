package org.cgiar.ilri.odk.sensors.handlers;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Jason Rogena (j.rogena@cgiar.org) on 3rd June 2014.
 *
 * Refer to http://developer.android.com/guide/topics/connectivity/bluetooth.html
 * for further reference
 */
public class BluetoothHandler {

    public static final int REQUEST_ENABLE_BT = 3321;
    public static final String KEY = "bluetooth";

    private static final String TAG = "ODK Sensors BluetoothHandler";

    private final Activity activity;
    private final BluetoothAdapter bluetoothAdapter;
    private final BroadcastReceiver broadcastReceiver;
    private final List<BluetoothDevice> availableDevices;
    private final DeviceFoundListener deviceFoundListener;

    /**
     * The constructor. If you don't know about constructors, well.., take more programming lessons then
     * come back ;)
     *
     * @param activity
     */
    public BluetoothHandler(Activity activity, final DeviceFoundListener deviceFoundListener){
        this.activity = activity;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        availableDevices = new ArrayList<BluetoothDevice>();

        this.deviceFoundListener = deviceFoundListener;

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    availableDevices.add(device);
                    deviceFoundListener.onDeviceFound(device);
                }

                else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                    availableDevices.clear();
                    deviceFoundListener.onSearchStart();
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    deviceFoundListener.onSearchStop();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        activity.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * This method checks whether the device has supported bluetooth hardware
     *
     * @return true if bluetooth hardware available in device else false
     */
    public boolean isBluetootSupported(){
        if(bluetoothAdapter == null) return false;
        return true;
    }

    /**
     * This method checks whether bluetooth is on.
     * It's advisable to first check if the device has supported BT hardware before checking if
     * bluetooth is on
     *
     * @return true if bluetooth is on and false if not on or device does not have supported BT hardware
     */
    public boolean isBluetootEnabled(){
        if(bluetoothAdapter != null){
            return bluetoothAdapter.isEnabled();
        }

        return false;
    }

    /**
     * This method requests user to enable bluetooth if:
     *   - the device has supported hardware
     *   - bluetooth is off
     *
     * This method should be used with the onActivityResult() callback
     * for determining when bluetooth is enabled by the user
     *
     */
    public void requestEnableBluetooth(){
        if(isBluetootSupported() && !isBluetootEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }
    }

    /**
     * This method checks whether the given bluetooth device is paired with this android device
     *
     * @param device The bluetooth device that you want to determine the pairing status
     *
     * @return true if the device has already been paired
     */
    public boolean isDevicePaired(BluetoothDevice device){

        if(isBluetootEnabled()){
            if(device != null){
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                for(BluetoothDevice currDevice : pairedDevices){
                    if(currDevice.getAddress().equals(device.getAddress())){
                        Log.d(TAG, "The device has already been paired");
                        return true;
                    }
                }
            }
            else{
                Log.w(TAG, "Provided device is null. Returning false in isDevicePaired");
            }
        }
        else {
            Log.w(TAG, "BluetoothHandler is either not enabled or the device does not have supported bluetooth hardware. Returning null for getAvailablePairedDevice()");
        }

        return false;
    }

    /**
     * This method tries to start the actual scanning for bluetooth devices
     *
     * @return true is scanning started successfully
     */
    public boolean startScan(){
        if(isBluetootEnabled()){
            if(broadcastReceiver != null){
                return bluetoothAdapter.startDiscovery();
            }
            else{
                Log.w(TAG, "For some reason broadcast receiver is null. Returning false for startScan()");
            }
        }
        else {
            Log.w(TAG, "BluetoothHandler is not enabled or device does not have supported hardware. Returning false for startScan()");
        }

        return false;
    }

    /**
     * This method gracefully unregisters any broadcast receiver created by this class.
     * Make sure you call this method whenever the parent activity goes to sleep (in onPause)
     *  if you want to save the devices battery
     */
    public void unregisterReceiver(){
        try{//sand boxed because there is really no way to check if receiver is still registered
            activity.unregisterReceiver(broadcastReceiver);
            Log.i(TAG, "broadcastReceiver unregistered");
        }
        catch (Exception e){
            Log.w(TAG, "broadcastReceiver was already unregistered");
        }
    }

    /**
     * This method stops the bluetoothAdapter from scanning for bluetooth devices.
     * Note that the adapter might have already have stopped scanning. Refer to:
     *      - http://developer.android.com/reference/android/bluetooth/BluetoothAdapter.html#startDiscovery()
     *
     * Call this method when you want to do anything else with bluetooth. It'll make that process much faster
     */
    public void stopScan(){
        bluetoothAdapter.cancelDiscovery();
    }

    /**
     * This method returns a list of bluetooth devices found so far in the current/last scan.
     * Note that this list is set to 0 whenever the scan is restarted and not when the current scan is complete
     *
     * @return The list of discoverable bluetooth devices found in the current/last scan
     */
    public List<BluetoothDevice> getAvailableDevices(){
        return availableDevices;
    }

    /**
     * This method returns the first supported UUID for the bluetooth device
     *
     * @param device The device we are using the get a UUID
     *
     * @return The first supported UUID gotten from the device
     */
    private UUID getUUID(BluetoothDevice device){
        return device.getUuids()[0].getUuid();//Reason why the minimum sdk is 15
    }

    /**
     * This method initiates the process of getting data from the connected bluetooth device
     * Please make sure you call this method from a thread that is asynchronous to the UI thread.
     * Refer to:
     *      - http://developer.android.com/reference/android/os/AsyncTask.html
     *
     * @param device The device we are getting data from
     * @param sessionListener The listener that the UI thread will use when socket is started
     *
     * @return true if we are indeed able to make the initial connection to the device
     */
    public boolean getDataFromDevice(BluetoothDevice device, BluetoothSessionListener sessionListener){
        if(device !=null){
            stopScan();

            AsClientConnectionThread clientConnectionThread = new AsClientConnectionThread(device, sessionListener);
            clientConnectionThread.run();
        }
        else{
            Log.w(TAG, "The bluetooth device provided to initiateConnectionAsClient is null. initiateConnectionAsClient returning false");
        }
        return false;
    }

    /**
     * This class initialises a synchronous thread that does the connection to the bluetooth device and opens up the socket
     */
    private class AsClientConnectionThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
        private final BluetoothSessionListener sessionListener;

        /**
         * The constructor.
         *
         * @param device The device to connect to
         * @param sessionListener The session listener being used by the UI thread to receive updates (since I assume we are running asynchronously here)
         */
        public AsClientConnectionThread (BluetoothDevice device, BluetoothSessionListener sessionListener){
            this.device = device;
            this.sessionListener = sessionListener;
            BluetoothSocket tmpSocket = null;

            try {
                tmpSocket = device.createRfcommSocketToServiceRecord(getUUID(device));
            }

            catch (IOException e) {
                Log.e(TAG, "Was unable to initiate a socket with Bluetooth server in AsClientConnectionThread");
                e.printStackTrace();
            }

            socket = tmpSocket;
            sessionListener.onSocketOpened(device);
        }

        /**
         * This method holds the code to be run in the thread being initialized.
         * Note that this thread is not asynchronous
         * Refer to:
         *      - http://developer.android.com/guide/components/processes-and-threads.html#Threads
         */
        @Override
        public void run() {
            super.run();

            stopScan();//Stop scan (in case the user started it again after getDataFromDevice was called)
            try {
                socket.connect();//This right here blocks the thread until a connection is gotten or a timeout is reached
                sessionListener.onConnected(device);

                getData(device, socket, sessionListener);
            }
            catch (IOException e){
                Log.e(TAG, "Was unable to connect to socket with Bluetooth server in AsClientConnectionThread");
                e.printStackTrace();

                try {
                    socket.close();
                }
                catch (IOException closeE){
                    Log.e(TAG, "Was unable to close bluetooth socket with Bluetooth server in AsClientConnectionThread");
                    closeE.printStackTrace();
                }
            }
        }
    }

    /**
     * This method initiated the process of getting the actual data from the bluetooth device
     *
     * @param device The device to get data from
     * @param socket The socket to use to get the data
     * @param sessionListener The session listener being used by the UI thread to receive updates
     */
    private void getData(BluetoothDevice device, BluetoothSocket socket, BluetoothSessionListener sessionListener){
        TransferThread transferThread = new TransferThread(device, socket, sessionListener);
        transferThread.run();
    }

    /**
     * This class initializes a thread for getting the data from the connected bluetooth device
     */
    private class TransferThread extends Thread {
        private final BluetoothDevice device;
        private final BluetoothSocket socket;

        private final InputStream inputStream;
        private final OutputStream outputStream;//not used as of now
        private final BluetoothSessionListener sessionListener;

        /**
         * The constructor.
         *
         * @param device The device to get data from
         * @param socket The socket to use to get the data
         * @param sessionListener The session listener being used by the UI thread to receive updates
         */
        public TransferThread(BluetoothDevice device, BluetoothSocket socket, BluetoothSessionListener sessionListener){
            this.device = device;
            this.socket = socket;
            this.sessionListener = sessionListener;

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try{
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            }
            catch (IOException e){
                Log.e(TAG, "IOException thrown while tying to create an input and output stream to bluetooth device");
                e.printStackTrace();
            }

            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        /**
         * This method holds the code to be run in the thread being initialized.
         * Note that this thread is not asynchronous.
         * Refer to:
         *      - http://developer.android.com/guide/components/processes-and-threads.html#Threads
         */
        @Override
        public void run() {
            super.run();

            String message = convertStreamToString(inputStream);//this method will block the thread until something is gotten

            sessionListener.onActualMessageGotten(device, message);

            closeSocket();
        }

        /**
         * This method closes the socket to the bluetooth device.
         * Make sure it's called after the InputStream and OutputStream are closed
         */
        private void closeSocket(){
            try{
                socket.close();
                sessionListener.onSocketClosed(device);
                Log.i(TAG, "Bluetooth socket successfully closed");
            }
            catch (Exception e){
                Log.e(TAG, "Something went wrong while trying to close the bluetooth socket with device");
                e.printStackTrace();
            }
        }

        /**
         * This method converts the provided inputStream into a string.
         * Note that some lines of code in this method block the thread until something is returned from
         *  the other side.
         * Also note that it's not doing a conversion but rather extraction (for lack of a better word)
         *
         * @param inputStream The input stream to be converted into a string
         *
         * @return The string
         */
        private String convertStreamToString(InputStream inputStream){
            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;

                boolean confirmed = false;

                //do until the first line gotten from the input stream matches the second
                while(confirmed == false){
                    /*
                    The first line should be discarded afterward since some devices returned a cached value first before returning the
                    actual scan value. Observed in:
                            - Allflex RFID Stick Reader Model No. RS320-3-60
                     */

                    String firstScan = reader.readLine();//this line of code blocks the thread until something is returned
                    Log.d(TAG, firstScan);

                    sessionListener.onFirstMessageGotten(device);

                    line = reader.readLine();//Process this string and not firstScan. This line of code also blocks the thread
                    Log.d(TAG, line);

                    if(firstScan.equals(line)){
                        confirmed = true;
                    }
                }

                inputStream.close();

                return line;
            }
            catch (Exception e){
                Log.e(TAG, "An error occurred while trying to convert input stream to string");
            }

            return null;
        }
    }

    /**
     * This interface describes a listener for device discovery
     */
    public interface DeviceFoundListener {
        void onSearchStart();
        void onDeviceFound(BluetoothDevice device);
        void onSearchStop();
    }

    /**
     * This interface describes a listener for connection with a bluetooth device
     * and transfer of data from that device
     */
    public interface BluetoothSessionListener {
        void onConnected(BluetoothDevice device);
        void onSocketOpened(BluetoothDevice device);
        void onFirstMessageGotten(BluetoothDevice device);
        void onActualMessageGotten(BluetoothDevice device, String message);
        void onSocketClosed(BluetoothDevice device);
    }

}
