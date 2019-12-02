package com.jriz.homelock;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ConnectedThread extends Thread{
    private Handler bluetoothIn;
    private int handlerState;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private boolean importantIndicator;

    public ConnectedThread(BluetoothSocket socket, Handler newBluetoothIn, int newHandlerState){
        importantIndicator =true;
        handlerState=newHandlerState;
        bluetoothIn=newBluetoothIn;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try{
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run()
    {
        byte[] buffer = new byte[256];
        int bytes;
        while (importantIndicator) {
            try {
                bytes = mmInStream.read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }
    public boolean write(String input)
    {
        try {
            mmOutStream.write(input.getBytes());
            return true;
        }
        catch (IOException e)
        {
            return false;
            //si no es posible enviar datos se cierra la conexión
            //Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
            //finish();
        }
    }
}
