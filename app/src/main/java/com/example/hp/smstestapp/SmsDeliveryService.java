package com.example.hp.smstestapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.example.hp.smstestapp.Database.ComplaintContract;

public class SmsDeliveryService extends Service {
    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Cursor cursor = getApplicationContext().getContentResolver().query(ComplaintContract.SmsDelivery.CONTENT_URI, null,
                ComplaintContract.SmsDelivery.COLUMN_DELIVERY_REPORT + "=not_sent", null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                //TODO Add logic for sms send
                mPhoneStatelistener = new MyPhoneStateListener(this);
                mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                String message = cursor.getString(cursor.getColumnIndex(ComplaintContract.SmsDelivery.COLUMN_MSG_BODY));
                cursor.moveToNext();
            }
        }
        return START_STICKY;
    }
}
