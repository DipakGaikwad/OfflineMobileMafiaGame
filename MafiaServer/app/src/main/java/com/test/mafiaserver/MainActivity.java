package com.test.mafiaserver;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.test.mafiaserver.helpers.WifiP2PConnectionHelper;
import com.test.mafiaserver.jetty.JettyServerService;

public class MainActivity extends AppCompatActivity {

    private WifiP2PConnectionHelper mP2PHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mP2PHelper = new WifiP2PConnectionHelper(this.getApplicationContext());
        mP2PHelper.initializeP2pManager();
        startService(new Intent(this.getApplicationContext(), JettyServerService.class));

        TextView softAPNameView = (TextView) findViewById(R.id.TextView1);
        TextView passphraseView = (TextView) findViewById(R.id.TextView2);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mP2PHelper.getGroupName() != null && !mP2PHelper.getGroupName().isEmpty() && mP2PHelper.getPassphrase() != null && !mP2PHelper.getPassphrase().isEmpty()) {
                    softAPNameView.setText(mP2PHelper.getGroupName());
                    passphraseView.setText(mP2PHelper.getPassphrase());
                } else {
                    new Handler().postDelayed(this, 1000);
                }
            }
        }, 1000);
    }
}
