package com.jriz.homelock;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class EnableView extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    Button btnEnableBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable_view);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btnEnableBluetooth = findViewById(R.id.btnEnableBluetooth);
        btnEnableBluetooth.setText("Enable bluetooth");
        btnEnableBluetooth.setEnabled(true);
        btnEnableBluetooth.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBTIntent);
                IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mBroadcastReceiver1, BTIntent);
            }
        });
    }
    private void verifyMAC(){
        Intent intent;
        if (existAMACSaved()) {
            intent = new Intent(this, PasswordsView.class);
        } else {
            intent = new Intent(this, ConnectionView.class);
        }
        startActivity(intent);
    }

    private boolean existAMACSaved() {
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(
                            openFileInput("myMAC.txt")
                    )
            );
            String MACSaved = bufferedReader.readLine();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        /*Enabling/Disabling bluetooth*/
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
//                        Toast.makeText(context, "Bluetooth disabled", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
//                        Toast.makeText(context, "Disabling bluetooth...", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
//                        Toast.makeText(context, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
                        verifyMAC();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
//                        Toast.makeText(context, "Enabling bluetooth...", Toast.LENGTH_SHORT).show();
                        btnEnableBluetooth.setText("Enabling bluetooth...");
                        btnEnableBluetooth.setEnabled(false);
                        break;
                }
            }
        }
    };
    @Override
    public void onBackPressed() {
        //Nothing
    }

    /*private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        *//*Enabling discoverability*//*
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Toast.makeText(context, "mBroadcastReceiver2: Discoverability Enabled.", Toast.LENGTH_SHORT).show();
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Toast.makeText(context, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Toast.makeText(context, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Toast.makeText(context, "mBroadcastReceiver2: Connecting....", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        *//*Searching new bluetooth devices*//*
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };*/

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Toast.makeText(context, "Connected! :D", Toast.LENGTH_SHORT).show();
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
//                    Toast.makeText(context, "BroadcastReceiver: BOND_BONDING", Toast.LENGTH_SHORT).show();
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Toast.makeText(context, "Incorrect password! >:|", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
}
