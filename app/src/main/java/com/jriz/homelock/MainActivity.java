package com.jriz.homelock;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent;
        BluetoothAdapter bAdapter;
        bAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bAdapter == null)
        {
            showLongMessage("Bluetooth not supported");
            return;
        }
        if(!bAdapter.isEnabled()) {
            intent = new Intent(this,EnableView.class);
        }else if (existAMACSaved()) {
            intent = new Intent(this, PasswordsView.class);
        } else {
            intent = new Intent(this, ConnectionView.class);
        }
        startActivity(intent);
    }

    //AUXILIARY FUNCTIONS
    private void saveNewMAC(String newMAC) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    openFileOutput("myMAC.txt", Context.MODE_APPEND)
            );
            outputStreamWriter.write(newMAC);
            outputStreamWriter.close();
            showLongMessage("New MAC saved!");
        } catch (Exception e) {
            showLongMessage("New MAC NOT saved");
        }
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
    private void showShortMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private void showLongMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
