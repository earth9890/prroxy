package com.example.prroxy;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class wifiDirectBrodcastReciever extends BroadcastReceiver{

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;

    public wifiDirectBrodcastReciever(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            //CHECK TO SEE IF WIFI IS ENABLED AND NOTIFY APPROPRIATE ACTIVITY

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            if (manager != null) {

                if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this.activity,
                            new String[] {ACCESS_FINE_LOCATION},
                            1);
                }
                manager.requestPeers(channel, activity.peerListListener);
            }
            //CALL Wifip2pmanager requestPeers() to get a list of current peers
        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {

            if(manager!=null)
            {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected())
                {
                    manager.requestConnectionInfo(channel,activity.connectionInfoListener);
                }
                else
                {
                    activity.connectionStatus.setText(" Not Connected");
                }
            }
            //Respond to new connection or disconnection
        }
    }


}