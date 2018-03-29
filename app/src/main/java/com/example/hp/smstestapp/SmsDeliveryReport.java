package com.example.hp.smstestapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by HP on 29-03-2018.
 */

public class SmsDeliveryReport extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                //TODO Update Sms table with sent
                Toast.makeText(context, "Delivered",
                        Toast.LENGTH_SHORT).show();
                break;
            case Activity.RESULT_CANCELED:
                Toast.makeText(context, "not_delivered",
                        Toast.LENGTH_SHORT).show();
                break;
        }

    }
}
