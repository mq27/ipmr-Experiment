package com.example.exeriment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static android.widget.Toast.LENGTH_SHORT;
import static java.lang.String.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_COARSE_LOCATION_PERMISSION = 2;
    private static MyBluetoothService myBluetoothService;
    private BluetoothAdapter bluetoothAdapter;
    private String deviceOldName;
    private String deviceNameToSearch, myName;
    private ManageMyConnectedSocket manageMyConnectedSocket;
    private ProgressBar progressBar;
    private static String SEPARATOR, TERMINATOR;
    private boolean restartDiscoveryBoolean;
    Button  turnaround, forward, backward, hello, btnbye, goodJob,
            howAre,goBack,finish,student1,student2,student3,student4
            ,student5,student6,time_act;
    RadioButton reset,stopMtion;
    // Command String variables for expressions //
    final String FORWARD_MOVEMENT_CLASSIFIER = "F";
    final String BACKWARD_MOVEMENT_CLASSIFIER = "B";
    final String LEFT_MOVEMENT_CLASSIFIER = "L";
    final String RIGHT_MOVEMENT_CLASSIFIER = "R";
    final String STOP_MOVEMENT_CLASSIFIER = "S";
    final String SPEAK_OUT_CLASSIFIER = "G";
    final Handler handler = new Handler();
    final String EMOTION_CLASSIFIER = "E";
    final String TAIL_STRING = "000";
    final String VERBALIZE_EMOTION = "V";
    final String MODIFIER = "000_000";
    final String distance = "800";
    final String trunDistance = "2500";
    TextView tvHello, tvHow, tvTurn, tvRight, tvLeft, tvbye, tvGood,tvGo,tvFinish,tvTime;
    int helloTrial, howTrial, turnTrial, rightTrial, leftTrial, doTrial, goodTrial,gobck,byebye;
    private float DEFAULT_SPEED = 0.75f; //is actually 0.01 * 100% //
    private double mySpeed;
    int defaultPercentage;


    private BroadcastReceiver BT_BroadcastReceiver = new BroadcastReceiver() {

        ArrayList<BluetoothDevice> potentialFaceDevices = new ArrayList<>();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BT_BroadcastReceiver");
            String action = intent.getAction();

            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_ON:
                            Log.d(TAG, "Bluetooth turned on!");
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            Log.d(TAG, "Bluetooth turned off!");
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.d(TAG, "Bluetooth is turning on!");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.d(TAG, "Bluetooth is turning off!");
                            break;
                    }
                    break;

                case BluetoothDevice.ACTION_FOUND:
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    if (deviceName != null) {
                        deviceName = deviceName.toUpperCase();
                    }
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.d(TAG, "Device found: " + deviceName + "; " + deviceHardwareAddress);
                    if (deviceName == null || deviceName.contains(deviceNameToSearch)) {
                        Log.d(TAG, "Found a device with name " + deviceNameToSearch);
                        potentialFaceDevices.add(device);
                    }
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.d(TAG, "Discovery started");
                    progressBar.setVisibility(View.VISIBLE);
                    potentialFaceDevices.clear();
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d(TAG, "Discovery finished");
                    progressBar.setVisibility(View.GONE);
                    lookAtDevicesFound(potentialFaceDevices, false);
                    break;

                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.d(TAG, "Bluetooth device connected");
                    Toast.makeText(context, "Bluetooth device connected", LENGTH_SHORT).show();
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.d(TAG, "Bluetooth device disconnected");
                    Toast.makeText(context, "Bluetooth device disconnected", LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public static void writeToBluetooth(String classifier, String instruction, String modifier) {

        String fullMessage = classifier + SEPARATOR + instruction + SEPARATOR + modifier + TERMINATOR;
        Log.d(TAG, fullMessage);
        if (myBluetoothService != null) {
            myBluetoothService.write(fullMessage);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SEPARATOR = "_";
        TERMINATOR = "#";
        myName = "ROBOCHOTU_REMOTE";

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        setUpBluetooth();
        turnaround = (Button) findViewById(R.id.btnturn);
        backward = (Button) findViewById(R.id.btnLeft);
        forward = (Button) findViewById(R.id.btnRight);
        hello = (Button) findViewById(R.id.btnHello);
        btnbye = (Button) findViewById(R.id.btnBye);
        goodJob = (Button) findViewById(R.id.btngjob);
        howAre = (Button) findViewById(R.id.btnHow);
        goBack = (Button)findViewById(R.id.btngo);
        finish=(Button)findViewById(R.id.btnFinish);
        time_act=(Button)findViewById(R.id.act_time);
        tvTurn = (TextView) findViewById(R.id.turnAround);
        tvbye = (TextView) findViewById(R.id.byeTrial);
        tvLeft = (TextView) findViewById(R.id.tLeft);
        tvRight = (TextView) findViewById(R.id.tRight);
        tvGood = (TextView) findViewById(R.id.goodJob);
        tvGo =(TextView)findViewById(R.id.goTrial);
        tvFinish=(TextView)findViewById(R.id.finishTrial);
        tvTime=(TextView)findViewById(R.id.time);
        reset =(RadioButton)findViewById(R.id.resetButton);
        stopMtion=(RadioButton)findViewById(R.id.stopButton);

        //students name
        student1= (Button) findViewById(R.id.khizar);
        student2= (Button) findViewById(R.id.Usman);
        student3= (Button) findViewById(R.id.Adyaan);
        student4= (Button) findViewById(R.id.Armaan);
        student5= (Button) findViewById(R.id.Essa);
        student6= (Button) findViewById(R.id.Arham);
        hello.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G", "Hello", valueOf(mySpeed));

            }
        });

        turnaround.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G", "Do this", valueOf(mySpeed));
                writeToBluetooth(LEFT_MOVEMENT_CLASSIFIER, trunDistance, MODIFIER);
            turnTrial ++;
            tvTurn.setText(String.valueOf(turnTrial));
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToBluetooth("G","Do this",valueOf(mySpeed));
                writeToBluetooth(FORWARD_MOVEMENT_CLASSIFIER,distance,MODIFIER);
                rightTrial++;
                tvRight.setText(String.valueOf(rightTrial));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        writeToBluetooth("G","Stop",valueOf(mySpeed));
                    }
                },5000);
                //writeToBluetooth("G","Stop",valueOf(mySpeed));
            }
        });
        /*forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G", "Do this", valueOf(mySpeed));
                Log.i("777","Front Do this");
                writeToBluetooth(FORWARD_MOVEMENT_CLASSIFIER, distance, MODIFIER);
                rightTrial++;
                tvRight.setText(String.valueOf(rightTrial));
                writeToBluetooth("G","stop",valueOf(mySpeed));
                *//*handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("777","Front Stop");
                        writeToBluetooth("G","Stop",valueOf(mySpeed));
                        writeToBluetooth(BACKWARD_MOVEMENT_CLASSIFIER+"G",distance+"DO this",MODIFIER);
                        Log.i("777","Back Stop");
                        writeToBluetooth("G","Stop",valueOf(mySpeed));
                    }
                },5000);
*//*

            }
        });*/
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G", "DO This", valueOf(mySpeed));
                writeToBluetooth(BACKWARD_MOVEMENT_CLASSIFIER, distance, MODIFIER);
                leftTrial++;
                tvLeft.setText(String.valueOf(leftTrial));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       // Toast.makeText(MainActivity.this,"working",LENGTH_SHORT).show();
                        writeToBluetooth("G", "Stop", valueOf(mySpeed));
                    }
                },5000);

            }
        });
        btnbye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G", "Bye Bye", valueOf(mySpeed));
                doTrial++;
                tvbye.setText(String.valueOf(doTrial));

            }
        });
        goodJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G", "Good Job", valueOf(mySpeed));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        writeToBluetooth(EMOTION_CLASSIFIER,"HAPPY"+VERBALIZE_EMOTION,TAIL_STRING);
                    }
                },500);// writeToBluetooth();
                goodTrial++;
                tvGood.setText(String.valueOf(goodTrial));
            }
        });
        howAre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G", "How Are You", valueOf(mySpeed));
            }
        });
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G","Go Back",valueOf(mySpeed));
                gobck++;
                tvGo.setText(String.valueOf(gobck));
            }
        });
        stopMtion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth(STOP_MOVEMENT_CLASSIFIER,"","");
                writeToBluetooth("G", "Stop", valueOf(mySpeed));
            }
        });
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G","Finish",valueOf(mySpeed));
                byebye++;
                tvFinish.setText(valueOf(byebye));
            }
        });
        time_act.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToBluetooth("G","Activity Time",valueOf(DEFAULT_SPEED));
            }
        });
        student1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G", "Khizar Do This", valueOf(DEFAULT_SPEED));
            }
        });

        student2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G", "Usmaan Do This", valueOf(mySpeed));
            }
        });

        student3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G", "Adyaan Do This", valueOf(mySpeed));
            }
        });

        student4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G", "Armaan Do This", valueOf(mySpeed));
            }
        });

        student5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G", "eesa Do This", valueOf(mySpeed));
            }
        });

        student6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToBluetooth("G","Arhaam Do This", valueOf(mySpeed));
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leftTrial=0;
                rightTrial=0;
                gobck=0;
                turnTrial=0;
                goodTrial=0;
                doTrial=0;
                byebye=0;
                tvLeft.setText(String.valueOf(leftTrial));
                tvRight.setText(String.valueOf(rightTrial));
                tvGo.setText(String.valueOf(gobck));
                tvbye.setText(String.valueOf(doTrial));
                tvGood.setText(String.valueOf(goodTrial));
                tvTurn.setText(String.valueOf(turnTrial));
                tvFinish.setText(String.valueOf(byebye));
            }
        });
    }

    private void setUpBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d(TAG, "This device does not support Bluetooth");
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToBluetoothDevice();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Log.d(TAG, "Bluetooth enabled");
            connectToBluetoothDevice();
        }
        if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "Unable to start Bluetooth");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connectToBluetoothDevice() {

        Log.d(TAG, bluetoothAdapter.getName());
        deviceOldName = bluetoothAdapter.getName();
        bluetoothAdapter.setName(myName);

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(BT_BroadcastReceiver, intentFilter);

        deviceNameToSearch = "ROBOCHOTU_FACE";

        lookAtPairedDevices(deviceNameToSearch);
    }

    private void lookAtDevicesFound(final ArrayList<BluetoothDevice> devicesToDisplay, boolean deviceListIsFromPairedDevices) {
        if (devicesToDisplay.isEmpty()) {
            Log.d(TAG, "No devices with name " + deviceNameToSearch + " found.");
            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No " + ((deviceListIsFromPairedDevices) ? "paired" : "nearby") + " devices found");
            if ((deviceListIsFromPairedDevices)) {
                builder.setMessage("Would you like to look at nearby devices?");
            } else {
                builder.setMessage("Would you like to continue to look?");
            }
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    checkPermissionsAndLocateDevices();
                }
            });
            builder.setNegativeButton("No", null);
            builder.show();
        } else {
            final ArrayList<String> listOfNames = new ArrayList<>();
            final ArrayList<String> listOfMACAddresses = new ArrayList<>();
            for (int i = 0; i < devicesToDisplay.size(); i++) {

                if (devicesToDisplay.get(i).getName() == null) {
                    listOfNames.add(getString(R.string.NULL_NAME_ERROR));
                } else {
                    listOfNames.add(devicesToDisplay.get(i).getName());
                }

                listOfMACAddresses.add(devicesToDisplay.get(i).getAddress());
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if ((deviceListIsFromPairedDevices)) {
                builder.setTitle("Paired devices found. Tap to connect to one.");
            } else {
                builder.setTitle("Devices found. Tap to connect.");
            }

            builder.setItems(listOfNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int deviceSelected) {
                    Log.d(TAG, deviceSelected + " was clicked. It has name " + listOfNames.get(deviceSelected) + " and MAC Address " + listOfMACAddresses.get(deviceSelected));
                    ConnectThread connectThread = new ConnectThread(devicesToDisplay.get(deviceSelected));
                    connectThread.start();
                }
            });

            builder.setPositiveButton(((deviceListIsFromPairedDevices) ? "Look for devices nearby" : "Look again"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    checkPermissionsAndLocateDevices();
                }
            });
            builder.setNegativeButton(((deviceListIsFromPairedDevices) ? "Exit" : "Stop looking"), null);
            builder.show();
        }
    }

    private void lookAtPairedDevices(String nameToSearch) {

        Log.d(TAG, "Currently paired devices:");
        ArrayList<BluetoothDevice> pairedDevicesWithName = new ArrayList<>();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (!pairedDevices.isEmpty()) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, deviceName + "; " + deviceHardwareAddress);

                if (deviceName.contains(nameToSearch)) {
                    pairedDevicesWithName.add(device);
                }
            }
        }

        if (!pairedDevicesWithName.isEmpty()) {
            Log.d(TAG, "Found some paired devices with the name you inserted:");
            for (int i = 0; i < pairedDevicesWithName.size(); i++) {
                String deviceName = pairedDevicesWithName.get(i).getName();
                String deviceHardwareAddress = pairedDevicesWithName.get(i).getAddress();
                Log.d(TAG, deviceName + "; " + deviceHardwareAddress);
            }
        }

        lookAtDevicesFound(pairedDevicesWithName, true);
    }

    private void checkPermissionsAndLocateDevices() {
        Log.d(TAG, "Going to start looking for devices");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Looks like we don't have permissions");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.d(TAG, "Permission request was previously rejected, asking for permission again with an explanation of why it is needed.");
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Permissions previously rejected");
                alertDialog.setMessage("We need access to certain permissions to find the Robochotu face device. Please click ACCEPT on the dialog that appears after this.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION_PERMISSION);
                            }
                        });
                alertDialog.show();
            } else {
                Log.d(TAG, "Asking for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION_PERMISSION);
            }
        } else {
            Log.d(TAG, "We have permissions");
            beginDiscovery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_COARSE_LOCATION_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission was granted, yay!");
                    beginDiscovery();
                } else {
                    Log.d(TAG, "permission denied, boo!");
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void beginDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "Already discovering. Cancelling.");
            Log.d(TAG, "Cancel discovery: " + bluetoothAdapter.cancelDiscovery());
        }
        Log.d(TAG, "Start discovery: " + bluetoothAdapter.startDiscovery());

        Toast.makeText(this, "Looking for devices nearby", LENGTH_SHORT).show();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void endDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void beginDiscoveryAgain() {
        if (restartDiscoveryBoolean) {
            beginDiscovery();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, bluetoothAdapter.getName());
        bluetoothAdapter.setName(deviceOldName);

        if (bluetoothAdapter.isDiscovering()) {
            restartDiscoveryBoolean = true;
            bluetoothAdapter.cancelDiscovery();
        } else {
            restartDiscoveryBoolean = false;
        }
    }

    @Override
    protected void onPause() {
        endDiscovery();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        beginDiscoveryAgain();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(BT_BroadcastReceiver);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            Log.d(TAG, "Creating a thread for trying to connect to server devices");
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                UUID MY_UUID = UUID.fromString(getString(R.string.UUID));
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d(TAG, "Trying to create a Bluetooth Client Socket");
            } catch (IOException e) {
                Log.d(TAG, "Socket's create() method failed");
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {

            Log.d(TAG, "Cancel discovery because it otherwise slows down the connection");
            Log.d(TAG, "cancel discovery: " + bluetoothAdapter.cancelDiscovery());

            try {
                Log.d(TAG, "Attempting connection...");
                // Connect to the remote_launcher device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.d(TAG, "Unable to connect; closing the socket and returning");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Unable to connect. The device is out of range.", LENGTH_SHORT).show();
                    }
                });

                try {
                    mmSocket.close();
                    Log.d(TAG, "Socket's close() method successful");
                } catch (IOException closeException) {
                    Log.d(TAG, "Socket's close() method failed");
                }
                return;
            }

            Log.d(TAG, "Connection attempt successful!");
            manageMyConnectedSocket = new ManageMyConnectedSocket(mmSocket);
            manageMyConnectedSocket.start();
            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            Log.d(TAG, "Closing the connect socket and causing the thread to finish.");
            try {
                mmSocket.close();
                Log.d(TAG, "Closed the client socket");
            } catch (IOException e) {
                Log.d(TAG, "Could not close the client socket");
            }
        }
    }

    private class ManageMyConnectedSocket extends Thread {

        private BluetoothSocket mmSocket;

        public ManageMyConnectedSocket(BluetoothSocket socket) {
            mmSocket = socket;

            final int MESSAGE_READ = 0;
            final int MESSAGE_WRITE = 1;
            final int MESSAGE_TOAST = 2;

            Handler handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    switch (msg.what) {
                        case MESSAGE_READ:
                            byte[] readBuffer = (byte[]) msg.obj;
                            // construct a string from the valid bytes in the buffer
                            String readMessage = new String(readBuffer, 0, msg.arg1);
                            Log.d(TAG, "MESSAGE_READ");
                            Log.d(TAG, readMessage);
//                            Toast.makeText(MainActivity.this, readMessage, Toast.LENGTH_SHORT).show();
                            break;
                        case MESSAGE_WRITE:
                            byte[] writeBuffer = (byte[]) msg.obj;
                            String writeMessage = new String(writeBuffer);
                            Log.d(TAG, "MESSAGE_WRITE");
                            Log.d(TAG, writeMessage);
                            break;
                        case MESSAGE_TOAST:
                            Log.d(TAG, "MESSAGE_TOAST");
                            break;
                    }
                    super.handleMessage(msg);
                }
            };

            myBluetoothService = new MyBluetoothService(mmSocket, handler);
        }
    }

}
