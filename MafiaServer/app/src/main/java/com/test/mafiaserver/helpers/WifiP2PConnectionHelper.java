package com.test.mafiaserver.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.Collection;

import static android.os.Looper.getMainLooper;

public class WifiP2PConnectionHelper {

    private static final String TAG = WifiP2PConnectionHelper.class.getSimpleName();

    private final Context mContext;
    private final WifiP2pManager mP2pManager;
    private WifiP2pManager.Channel mChannel;
    private final IntentFilter mIntentFilter = new IntentFilter();
    private BroadcastReceiver mBroadcastReceiver;

    private String groupPassword = null;
    private String groupName = null;

    public WifiP2PConnectionHelper(final Context context) {
        mContext = context;
        mP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
    }

    public void initializeP2pManager() {
        Log.d(TAG, "initializing p2p group");

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        mChannel = mP2pManager.initialize(mContext, getMainLooper(), null);

        mBroadcastReceiver = new P2pConnectionBroadcastReceiver(mP2pManager, mChannel, this);
        mContext.registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    public void constructSoftAp() {
        Log.d(TAG, "creating softAP");
        mP2pManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Started the group");
                mP2pManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                    /**
                     *
                     * {@inheritDoc}
                     */
                    @Override
                    public void onGroupInfoAvailable(final WifiP2pGroup wifiP2pGroup) {
                        if (wifiP2pGroup != null) {
                            Log.d(TAG, "SoftAP with SSID " + wifiP2pGroup.getNetworkName()
                                    + " has been created!");
                        } else {
                            Log.d(TAG, "wifiP2pGroup is null");
                        }
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Failed creating P2P group " + reason);
            }
        });
    }

    public String getPassphrase() {
        return groupPassword;
    }

    public String getGroupName() {
        return groupName;
    }

    private class P2pConnectionBroadcastReceiver extends BroadcastReceiver {

        private final WifiP2PConnectionHelper mWifiP2pConnectionHelper;
        private final WifiP2pManager mWifiP2pManager;
        private final WifiP2pManager.Channel mChannel;

        private final String TAG = P2pConnectionBroadcastReceiver.class.getName();

        public P2pConnectionBroadcastReceiver(final WifiP2pManager manager,
                                              final WifiP2pManager.Channel channel,
                                              final WifiP2PConnectionHelper p2PConnectionHelper) {
            mWifiP2pManager = manager;
            mChannel = channel;
            mWifiP2pConnectionHelper = p2PConnectionHelper;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive()");
            String action = intent.getAction();
            switch (action) {
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    Log.i(TAG, "P2P state changed. State : "
                            + ((state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) ? "Enabled" : "Disabled"));
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        mWifiP2pConnectionHelper.constructSoftAp();
                    }
                    break;

                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                    if (mWifiP2pManager == null) {
                        return;
                    }
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                    if (networkInfo == null) {
                        Log.e(TAG, "Network info is null");
                    } else if (networkInfo.isConnected()) {
                        Log.d(TAG, "Connected");
                        mWifiP2pManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {

                            private Collection<WifiP2pDevice> mConnectedDevices;

                            @Override
                            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                                Log.d(TAG, "onGroupInfoAvailable");
                                groupPassword = wifiP2pGroup.getPassphrase();
                                groupName = wifiP2pGroup.getNetworkName();
                                int numberOfConnectedDevices;
                                mConnectedDevices = wifiP2pGroup.getClientList();
                                numberOfConnectedDevices = mConnectedDevices.size();
                                Log.d(TAG, "Number of connected devices: " + numberOfConnectedDevices);
                                if (numberOfConnectedDevices > 0) {
                                    Log.i(TAG, "new Device connected!!");
                                    for (WifiP2pDevice device :
                                            mConnectedDevices) {
                                        Log.i(TAG, "device name: " + device.deviceName);
                                    }
                                }
                            }
                        });
                    }
                    break;
                default:
                    Log.e(TAG, "Unknown action");
            }
        }
    }
}
