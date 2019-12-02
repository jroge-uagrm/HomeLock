package com.jriz.homelock;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.UUID;

public class PasswordsView extends AppCompatActivity {

    Button btnDisconnect, btnTryWithPin, btnChangePassword;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private StringBuilder DataStringIN;
    private ConnectedThread myConnectionBT;
    private UUID BTMODULEUUID;
    private int handlerState;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwords_view);
        handlerState = 0;
        DataStringIN = new StringBuilder();
        BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        btnDisconnect = findViewById(R.id.btnCloseConnection);
        btnTryWithPin = findViewById(R.id.btnTryWithPin);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PasswordsView.this);
                builder.setTitle("Insert Your PIN");
                final EditText input = new EditText(PasswordsView.this);
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
                                    Intent i = new Intent(PasswordsView.this, ConnectionView.class);
                                    startActivity(i);
                                } catch (IOException e) {
                                    Toast.makeText(getBaseContext(), "Error closing", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(PasswordsView.this, "Wrong PIN", Toast.LENGTH_SHORT).show();
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
        btnTryWithPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PasswordsView.this);
                builder.setTitle("Insert Your PIN");
                final EditText input = new EditText(PasswordsView.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);
                builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String oldPassword = input.getText().toString();
                        if (isTheCorrectOldPIN(oldPassword)) {
                            sendMessage("o");
                        } else {
                            Toast.makeText(PasswordsView.this, "Wrong PIN", Toast.LENGTH_SHORT).show();
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
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });
        Intent intent = getIntent();
        String newRol = intent.getStringExtra("rol");
        if (newRol.equals("save")) {
            saveMac();
        }
    }

    private void saveMac() {
        SQLiteDatabase myDB =
                openOrCreateDatabase("my.db", MODE_PRIVATE, null);
        myDB.execSQL(
                "CREATE TABLE IF NOT EXISTS user (id INT,mac VARCHAR(200),pin VARCHAR(10),facial VARCHAR(200))"
        );
        Intent intent = getIntent();
        String newMac = intent.getStringExtra(ConnectionView.EXTRA_DEVICE_ADDRESS);
        ContentValues row1 = new ContentValues();
        row1.put("id", 1);
        row1.put("mac", newMac);
        row1.put("pin", "0000");
        row1.put("facial", "empty");
        myDB.insert("user", null, row1);
        myDB.close();
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

    private void changePassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Insert Old PIN");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String oldPassword = input.getText().toString();
                if (isTheCorrectOldPIN(oldPassword)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PasswordsView.this);
                    builder.setTitle("Insert New PIN");
                    final EditText input = new EditText(PasswordsView.this);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    builder.setView(input);
                    builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String oldPIN = input.getText().toString();
                            saveNewPIN(oldPIN);
                            Toast.makeText(PasswordsView.this, "PIN Changed!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(PasswordsView.this, "Wrong PIN", Toast.LENGTH_SHORT).show();
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

    private void saveNewPIN(String newPin) {
        SQLiteDatabase myDB =
                openOrCreateDatabase("my.db", MODE_PRIVATE, null);
        myDB.execSQL(
                "UPDATE user SET pin='"+newPin+"' WHERE id=1"
        );
        myDB.close();
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
            connect();
        } catch (IOException e) {
            Intent i = new Intent(this, ConnectionView.class);
            startActivity(i);
        }
    }

    @SuppressLint("HandlerLeak")
    private void connect() {
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
                                Toast.makeText(PasswordsView.this, dataInPrint, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(PasswordsView.this, "ALGO PASA", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                , handlerState);
        myConnectionBT.start();
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
}
