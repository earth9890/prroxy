package com.example.prroxy;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;

import androidx.core.app.ActivityCompat;

public class wifiDirectBrodcastReciever extends BroadcastReceiver {

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

            if (manager != null)
            {
                if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                manager.requestPeers(channel, activity.peerListListener);
            }
            //CALL WIFIP2PMANAGER.requesrPeers() to get a list of current peers
        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            //Respond to new connection or disconnection
        }
    }


}
