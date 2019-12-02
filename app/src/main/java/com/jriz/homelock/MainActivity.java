package com.jriz.homelock;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private String newMac;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent;
        BluetoothAdapter bAdapter;
        bAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bAdapter == null) {
            return;
        }
        if (!bAdapter.isEnabled()) {
            intent = new Intent(this, EnableView.class);
        } else if (existAMACSaved()) {
            intent = new Intent(this, PasswordsView.class);
            intent.putExtra(ConnectionView.EXTRA_DEVICE_ADDRESS,newMac);
            intent.putExtra("rol","show");
        } else {
            intent = new Intent(this, ConnectionView.class);
        }
        startActivity(intent);
    }

    private boolean existAMACSaved() {
        SQLiteDatabase myDB =
                openOrCreateDatabase("my.db", MODE_PRIVATE, null);
        /*myDB.execSQL("DROP TABLE user");
        myDB.execSQL(
                "CREATE TABLE IF NOT EXISTS user (id INT,mac VARCHAR(200),pin VARCHAR(10),facial VARCHAR(200))"
        );*/
        Cursor myCursor =
                myDB.rawQuery("select mac from user", null);
        boolean exist = false;
        while (myCursor.moveToNext()) {
            newMac= myCursor.getString(0);
            exist = true;
        }
        myCursor.close();
        myDB.close();
        return exist;
    }
}
