package com.example.hp.smstestapp;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hp.smstestapp.Database.ComplaintContract;
import com.example.hp.smstestapp.Database.ComplaintDbHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.provider.Telephony.Sms.Intents.getMessagesFromIntent;
import static com.example.hp.smstestapp.Util.splitMsg;


public class SmsReciever extends BroadcastReceiver {
    Context context;
    String[] getSplitStrings = null;
    ContentValues contentValues;

    public void onReceive(Context context, Intent intent) {
        this.context = context;

        String checkPnr = "no";

        SmsMessage[] msgs = getMessagesFromIntent(intent);
        if (msgs[0].getOriginatingAddress().equalsIgnoreCase("MD-IRCTCi")
                || msgs[0].getOriginatingAddress().equalsIgnoreCase("LM-IRCTCi")
                || msgs[0].getOriginatingAddress().equalsIgnoreCase("DT-IRCTCi")
                || msgs[0].getOriginatingAddress().equalsIgnoreCase("DM-IRCTCi")
                || msgs[0].getOriginatingAddress().equalsIgnoreCase("911")) {
            getSplitStrings = splitMsg(msgs[0].getMessageBody());
            contentValues = new ContentValues();
            contentValues.put(ComplaintContract.PnrLog.COLUMN_PNR, getSplitStrings[0]);
            contentValues.put(ComplaintContract.PnrLog.COLUMN_NAME, getSplitStrings[1]);
            contentValues.put(ComplaintContract.PnrLog.COLUMN_TRAIN_NO, getSplitStrings[2]);
            contentValues.put(ComplaintContract.PnrLog.COLUMN_SEAT_DETAILS, getSplitStrings[3]);
            contentValues.put(ComplaintContract.PnrLog.COLUMN_DOJ, getSplitStrings[4]);
            contentValues.put(ComplaintContract.PnrLog.COLUMN_DEPT_TIME, getSplitStrings[5]);

            if (!isNetworkAvailable()) {

                contentValues.put(ComplaintContract.PnrLog.COLUMN_STATUS, checkPnr);
                context.getContentResolver().insert(ComplaintContract.PnrLog.CONTENT_URI, contentValues);
            } else {
                getjsondata();//TODO Check Pnr and enter details in database
            }

        }


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void getjsondata() {
        //progressDialog.show();
        StringRequest st = new StringRequest(Request.Method.GET, "https://api.railwayapi.com/v2/pnr-status/pnr/" + getSplitStrings[0] + "/apikey/2cv4xjncln/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                get_response(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue q = Volley.newRequestQueue(context);
        q.add(st);
    }

    public  void get_response(String response_obj) {
        try {
            JSONObject obj = new JSONObject(response_obj);

            String res = obj.getString("response_code");
            String c_prep = obj.getString("chart_prepared");
            if (res.equals("200")) {
                contentValues.put(ComplaintContract.PnrLog.COLUMN_STATUS, "yes");
                context.getContentResolver().insert(ComplaintContract.PnrLog.CONTENT_URI, contentValues);
                JSONArray arr = obj.getJSONArray("passengers");
                JSONObject pos = arr.getJSONObject(0);

                if (c_prep.equals("true")) {
                    String mydata = "Status=" + pos.getString("current_status");
                } else {
                    String mydata = "Status=" + pos.getString("booking_status");
                }
            }
        } catch (Exception r) {
            //Toast.makeText(this, "Error=" + r, Toast.LENGTH_SHORT).show();
        }
    }
}
