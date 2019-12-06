package com.jriz.homelock;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class NotConnectedView extends AppCompatActivity {

    Button disconnect, tryToConnect;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private StringBuilder DataStringIN;
    private ConnectedThread myConnectionBT;
    private UUID BTMODULEUUID;
    private int handlerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_connected_view);
        handlerState = 0;
        DataStringIN = new StringBuilder();
        BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        disconnect = findViewById(R.id.btnDisconnectDevice);
        tryToConnect = findViewById(R.id.btnTryToConnect);
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NotConnectedView.this);
                builder.setTitle("Insert Your PIN");
                final EditText input = new EditText(NotConnectedView.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);
                builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String oldPassword = input.getText().toString();
                        if (isTheCorrectOldPIN(oldPassword)) {
                            if (btSocket != null) {
                                try {
                                    btSocket.close();
                                    deleteMAC();
                                    Intent i = new Intent(NotConnectedView.this, ConnectionView.class);
                                    startActivity(i);
                                } catch (IOException e) {
                                    Toast.makeText(getBaseContext(), "Error closing", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(NotConnectedView.this, "Wrong PIN", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
        tryToConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onResume();
            }
        });
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver2, BTIntent);
    }
    private void deleteMAC() {
        SQLiteDatabase myDB =
                openOrCreateDatabase("my.db", MODE_PRIVATE, null);
        myDB.execSQL(
                "DELETE FROM user WHERE id>0"
        );
        myDB.close();
        Intent intent = new Intent(this, ConnectionView.class);
        startActivity(intent);
    }

    private boolean isTheCorrectOldPIN(String oldPassword) {
        SQLiteDatabase myDB =
                openOrCreateDatabase("my.db", MODE_PRIVATE, null);
        Cursor myCursor =
                myDB.rawQuery("select pin from user", null);
        boolean exist = false;
        while (myCursor.moveToNext()) {
            if (myCursor.getString(0).equals(oldPassword)) {
                exist = true;
            }
        }
        myCursor.close();
        myDB.close();
        return exist;
    }

    private void sendMessage(String msg) {
        myConnectionBT.write(msg + "#");
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String address = intent.getStringExtra(ConnectionView.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        try {
            btSocket = createBluetoothSocket(device);
            btSocket.connect();
            if(connect()){
                btSocket.close();
                Intent i= new Intent(this, PasswordsView.class);
                i.putExtra(ConnectionView.EXTRA_DEVICE_ADDRESS,address);
                i.putExtra("rol","show");
                startActivity(i);
            }btSocket.close();
        } catch (IOException e) {
            Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show();
            /*Intent i = new Intent(this, NotConnectedView.class);
            i.putExtra(ConnectionView.EXTRA_DEVICE_ADDRESS,address);
            startActivity(i);*/
        }
    }

    @SuppressLint("HandlerLeak")
    private boolean connect() {
        try {
            myConnectionBT = new ConnectedThread(
                    btSocket,
                    new Handler() {
                        public void handleMessage(android.os.Message msg) {
                            if (msg.what == handlerState) {
                                String readMessage = (String) msg.obj;
                                DataStringIN.append(readMessage);
                                int endOfLineIndex = DataStringIN.indexOf("#");
                                if (endOfLineIndex > 0) {
                                    String dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                                    DataStringIN.delete(0, DataStringIN.length());
                                    Toast.makeText(NotConnectedView.this, dataInPrint, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(NotConnectedView.this, "ALGO PASA", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    , handlerState);
            myConnectionBT.start();
            return true;
        }catch(Exception e){
            return  false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            btSocket.close();
        } catch (IOException e2) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {/*Nothing*/}

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        //*Enabling discoverability*//*
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
//                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        Toast.makeText(context, "CONECTADO", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };
}
