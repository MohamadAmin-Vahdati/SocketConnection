package com.mavp.socketconnection;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AppCompatEditText ET_IP, ET_PORT, ET_MESSAGE , ET_ANSWER;
    private AppCompatButton BTN_CONNECT, BTN_SEND;

    private String SERVER_IP, MESSAGE , ANSWER;
    private int SERVER_PORT;

    private Socket socket ;

    private PrintWriter out ;

    private boolean isClosed = true ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ET_IP = findViewById(R.id.ET_IP);
        ET_PORT = findViewById(R.id.ET_PORT);
        ET_MESSAGE = findViewById(R.id.ET_MESSAGE);
        ET_ANSWER = findViewById(R.id.ET_ANSWER);
        BTN_CONNECT = findViewById(R.id.BTN_CONNECT);
        BTN_SEND = findViewById(R.id.BTN_SEND);

        BTN_CONNECT.setOnClickListener(this);
        BTN_SEND.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.BTN_CONNECT:
                if (isClosed){

                    SERVER_IP = ET_IP.getText().toString().trim();
                    if (ET_PORT.getText().toString().trim().equals("")){
                        ET_PORT.setError("Enter Port Number");
                    }else {
                        SERVER_PORT = Integer.parseInt(ET_PORT.getText().toString().trim());
                    }

                    new Thread(new Connect()).start();

                }else{

                    try {
                        socket.close();
                        BTN_CONNECT.setText("CONNECT");
                        isClosed = socket.isClosed() ;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                break;

            case R.id.BTN_SEND:
                MESSAGE = ET_MESSAGE.getText().toString().trim();
                new Thread(new SendMessage()).start();
                break;
        }
    }

    class Connect implements Runnable {

        public void run() {
            try {

                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVER_PORT);
                isClosed = socket.isClosed() ;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connected !", Toast.LENGTH_SHORT).show();
                        BTN_CONNECT.setText("DISCONNECT");
                    }
                });

                out = new PrintWriter(socket.getOutputStream());
                new Thread(new ReceiveMessage()).start();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


    class SendMessage implements Runnable {

        public void run() {
            try {
                out.write(MESSAGE);
                out.flush();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


    class ReceiveMessage implements Runnable {

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                int charsRead ;
                char[] buffer = new char[1024];
                charsRead = in.read(buffer);
                ANSWER = new String(buffer).substring(0, charsRead);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ET_ANSWER.setText(ANSWER);
                    }
                });

                new Thread(new ReceiveMessage()).start();

            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

}
