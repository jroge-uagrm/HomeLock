package com.jriz.homelock;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

public class ConnectionView extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_view);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, BTIntent);
    }
    private void bluetoothDisabled(){
        Intent intent=new Intent(this,EnableView.class);
        startActivity(intent);
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
                        bluetoothDisabled();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
//                        Toast.makeText(context, "Disabling bluetooth...", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
//                        Toast.makeText(context, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
//                        Toast.makeText(context, "Enabling bluetooth...", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };
}
