package com.jriz.homelock;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;

public class PasswordsView extends AppCompatActivity {

    public BluetoothAdapter mBluetoothAdapter;
    Button btnCloseConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwords_view);
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, BTIntent);
        btnCloseConnection=findViewById(R.id.btnCloseConnection);
        btnCloseConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteMAC();
            }
        });
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
    @Override
    public void onBackPressed() {/*Nothing*/}

    public void deleteMAC() {
        SQLiteDatabase myDB =
                openOrCreateDatabase("my.db", MODE_PRIVATE, null);
        myDB.execSQL(
                    "DELETE FROM user WHERE id>0"
        );
        myDB.close();
        Intent intent=new Intent(this,ConnectionView.class);
        startActivity(intent);
    }
    private void bluetoothDisabled() {
        Intent intent = new Intent(this, EnableView.class);
        startActivity(intent);
    }
}
