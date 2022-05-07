package com.example.prroxy;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CHANGE_WIFI_STATE;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.InetAddresses;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    TextView connectionStatus, messageTextView;
    Button aSwitch, discoverButton;
    ImageButton sendButton;
    EditText typing;
    ListView listView;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int PERMISSION_REQUEST_CODE = 200;


    WifiP2pManager manager;
    public WifiP2pManager.Channel channel;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;
    Socket socket;
    ServerClass serverClass;
    ClientClass clientClass;
    boolean isHost;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeWork();
        exqListener();
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
            {
                connectionStatus.setText(" Host ");
                isHost = true;
                serverClass = new ServerClass();
                serverClass.start();
            }
            else if(wifiP2pInfo.groupFormed)
            {
                connectionStatus.setText(" Client ");
                isHost = false;
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };

    private void initializeWork() {
        connectionStatus = findViewById(R.id.connection_status);
        messageTextView = findViewById(R.id.messageTextView);

        aSwitch = findViewById(R.id.switch1);
        discoverButton = findViewById(R.id.buttonDiscover);
        listView = findViewById(R.id.listView);
        typing = findViewById(R.id.editTextTypeMsg);
        sendButton = findViewById(R.id.sendButton);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new wifiDirectBrodcastReciever(manager, channel, this);


        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);




    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            if (!wifiP2pDeviceList.equals(peers)) {
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());

                deviceNameArray = new String[wifiP2pDeviceList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];

                int index = 0;
                for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);

                if (peers.size() == 0) {
                    try {
                        connectionStatus.setText("No Device Found");
                    }catch (Exception e)
                    {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }

                    return;
                }


            }
        }
    };


//    public boolean checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(this,
//                ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    ACCESS_FINE_LOCATION)) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//                new AlertDialog.Builder(this)
//                        .setTitle(R.string.title_location_permission)
//                        .setMessage(R.string.text_location_permission)
//                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                //Prompt the user once explanation has been shown
//                                ActivityCompat.requestPermissions(MainActivity.this,
//                                        new String[]{ACCESS_FINE_LOCATION},
//                                        MY_PERMISSIONS_REQUEST_LOCATION);
//                            }
//                        })
//                        .create()
//                        .show();
//
//
//            } else {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(this,
//                        new String[]{ACCESS_FINE_LOCATION},
//                        MY_PERMISSIONS_REQUEST_LOCATION);
//            }
//            return false;
//        } else {
//            return true;
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                        showMessageOKCancel("You need to allow access to both the permissions",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(new String[]{ACCESS_FINE_LOCATION},
                                                    1);
                                        }
                                    }
                                });
                        return;
                    }
                }
        }
    }

    private void showMessageOKCancel(String s, DialogInterface.OnClickListener onClickListener) {
    }


    private void exqListener() {
        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(intent, 1);

            }
        });

        discoverButton.setOnClickListener(view -> {

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{ACCESS_FINE_LOCATION}, 1);
                return;
            }
            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    try {
                        connectionStatus.setText("No Device Found");
                    }catch (Exception e)
                    {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }


                @Override
                public void onFailure(int i) {
                    try {
                        connectionStatus.setText("No Device Found");
                    }catch (Exception e)
                    {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            });


        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{ACCESS_FINE_LOCATION}, 1);
                    return;
                }
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText(" Connected  :" + device.deviceAddress);

                    }

                    @Override
                    public void onFailure(int i) {

                        connectionStatus.setText(" Not Connected ");

                    }
                });
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                String msg= typing.getText().toString();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if(msg !=null && isHost)
                        {
                            serverClass.write(msg.getBytes());

                        }else if(msg != null && !isHost)
                        {
                            clientClass.write(msg.getBytes());
                        }
                    }
                });
            }
        });




    }

    @Override
    protected void onResume () {
        super.onResume();
        registerReceiver(receiver, intentFilter);

    }

    public  class ServerClass extends  Thread
    {
        ServerSocket serverSocket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void run()
        {

          try {
              serverSocket = new ServerSocket(8888);
              socket =serverSocket.accept();
              inputStream =socket.getInputStream();
              outputStream = socket.getOutputStream();
          }catch (IOException e)
          {
              e.printStackTrace();
          }
          ExecutorService executorService = Executors.newSingleThreadExecutor();
          Handler handler = new Handler(Looper.getMainLooper());

          executorService.execute(new Runnable() {
              @Override
              public void run() {
                  byte[] buffer = new byte[1024];
                  int bytes;


                  while(socket!=null)
                  {
                      try {
                          bytes = inputStream.read(buffer);
                      } catch (IOException e)
                      {
                          e.printStackTrace();
                      }
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                      Handler handler = new Handler(Looper.getMainLooper());

                      executor.execute(new Runnable() {
                          @Override
                          public void run() {
                              byte[] buffer = new byte[1024];
                              int bytes;

                              while(socket!=null)
                              {
                                  try {
                                      bytes = inputStream.read(buffer);
                                      if(bytes>0)
                                      {
                                          int finalBytes = bytes;
                                          handler.post(new Runnable() {
                                              @Override
                                              public void run() {
                                                  String tempMSG = new String(buffer,0, finalBytes);
                                                  messageTextView.setText(tempMSG);
                                              }
                                          });
                                      }
                                  }
                                  catch(IOException e)
                                  {
                                      e.printStackTrace();
                                  }
                              }
                          }
                      });
                  }
              }
          });
        }
    }
    @Override
    protected void onPause () {
        super.onPause();
        unregisterReceiver(receiver);

    }

    public class ClientClass extends Thread
    {
        String hostAdd;
        private InputStream inputStream;
        private OutputStream outputStream;

        public ClientClass(InetAddress hostAddress)
        {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();


        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch(IOException e)
            {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(new Runnable() {
                @Override
                public void run() {

                    byte [] buffer = new  byte[1024];
                    int bytes;

                     while(socket !=null)
                    {
                        try {
                            bytes = inputStream.read(buffer);
                            if(bytes>0)
                            {
                                int finalBytes = bytes;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String tempMSG = new String(buffer,0,finalBytes);
                                        messageTextView.setText(tempMSG);
                                    }
                                });
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }

                }
            });
            {

            }
        }
    }
}