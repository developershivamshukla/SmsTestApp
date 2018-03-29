package com.example.hp.smstestapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.hp.smstestapp.Database.ComplaintContract;

import static com.example.hp.smstestapp.Util.splitMsg;

public class MainActivity extends AppCompatActivity {
    private final String DELIVERED = "SMS_DELIVERED";
    SmsReciever smsReciever;
    Button sendButton;
    EditText editText;
    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;

    SmsDeliveryReport deliveryReportReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendButton = (Button) findViewById(R.id.send_message_btn);
        editText = (EditText) findViewById(R.id.msg_text);
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 444);
        }
        mPhoneStatelistener = new MyPhoneStateListener(this);
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        deliveryReportReceiver = new SmsDeliveryReport();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().equals("")) {

                } else {

                    if (mPhoneStatelistener.getStrength()) {
                        //TODO Deliver Msg Right Now Logic
                        //Do not add database entry it will discard  some cases like for example
                        //If Signal Strength is their but user balance is null then msg will not send and this will conflict our logic

                        PendingIntent deliveredPI = PendingIntent.getBroadcast(MainActivity.this, 0,
                                new Intent(DELIVERED), 0);
                        //Use this intent while sending sms

                      /*  SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);*/

                        Log.e("sms", "This should be deliver right now");
                    } else {
                        // Database Entry
                        Log.e("sms", "THis should be in Offline mode");
                        ContentValues values = new ContentValues();
                        values.put(ComplaintContract.SmsDelivery.COLUMN_MSG_BODY, editText.getText().toString());
                        values.put(ComplaintContract.SmsDelivery.COLUMN_DELIVERY_REPORT, "not_sent");
                        getContentResolver().insert(ComplaintContract.SmsDelivery.CONTENT_URI, values);
                        SharedPreferences.Editor sharedPreferences = getSharedPreferences("msg_delivery_report",0).edit();
                        sharedPreferences.putBoolean("all_sent",false);
                    }


                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(deliveryReportReceiver, new IntentFilter(DELIVERED));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 444:
                smsReciever = new SmsReciever();
                new DatabaseTask().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class DatabaseTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

            if (cursor.moveToFirst()) { // must check the result to prevent exception
                do {
                    String[] msgs = {cursor.getString(cursor.getColumnIndex("address")), cursor.getString(cursor.getColumnIndex("body"))};
                    if (msgs[0].equals("MD-IRCTCi")
                            || msgs[0].equals("LM-IRCTCi")
                            || msgs[0].equals("DT-IRCTCi")
                            || msgs[0].equals("DM-IRCTCi")
                            || msgs[0].equals("911")) {
                        String[] messageData = splitMsg(msgs[1]);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(ComplaintContract.PnrLog.COLUMN_PNR, messageData[0]);
                        contentValues.put(ComplaintContract.PnrLog.COLUMN_NAME, messageData[1]);
                        contentValues.put(ComplaintContract.PnrLog.COLUMN_TRAIN_NO, messageData[2]);
                        contentValues.put(ComplaintContract.PnrLog.COLUMN_SEAT_DETAILS, messageData[3]);
                        contentValues.put(ComplaintContract.PnrLog.COLUMN_DOJ, messageData[4]);
                        contentValues.put(ComplaintContract.PnrLog.COLUMN_DEPT_TIME, messageData[5]);

                        getContentResolver().insert(ComplaintContract.PnrLog.CONTENT_URI, contentValues);
                    }
                } while (cursor.moveToNext());
            } else {
                // empty box, no SMS
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Cursor cursor = getContentResolver().query(ComplaintContract.PnrLog.CONTENT_URI, null, null, null, null);
            cursor.moveToFirst();
            Log.i("PNR", cursor.getString(cursor.getColumnIndex("pnr")));
        }
    }
}
