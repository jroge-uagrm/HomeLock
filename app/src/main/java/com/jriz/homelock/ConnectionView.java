package com.jriz.homelock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

public class ConnectionView extends AppCompatActivity {

    private ListView IdList;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.device_adapter_view);//<-<- PARTE A MODIFICAR >->->
        IdList = findViewById(R.id.IdList);
        IdList.setAdapter(mPairedDevicesArrayAdapter);
        IdList.setOnItemClickListener(mDeviceClickListener);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            Intent i = new Intent(ConnectionView.this, PasswordsView.class);//<-<- PARTE A MODIFICAR >->->
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            i.putExtra("rol", "save");
            startActivity(i);
        }
    };
}