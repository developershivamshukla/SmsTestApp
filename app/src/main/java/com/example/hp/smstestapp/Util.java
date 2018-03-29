package com.example.hp.smstestapp;

import android.app.job.JobInfo;
import android.content.Context;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;


public class Util {

    public static String[] splitMsg(String msg) {
        String pnr = null;
        String name = null;
        String train_no = null;
        String seat_details = null;
        String doj = null;
        String time = null;
        String checkcorrectmsg;
        if (msg != null) {
            String[] splitData = msg.split(",");
            checkcorrectmsg = splitData[0];
            if (checkcorrectmsg.charAt(0) == 'P' && checkcorrectmsg.charAt(1) == 'N' && checkcorrectmsg
                    .charAt(2) == 'R' && checkcorrectmsg.charAt(3) == ':') {
                pnr = splitData[0].replace("PNR:", "");
                if (pnr.length() != 10 || !pnr.matches("[0-9]+"))
                    pnr = "not found";
            } else {
                pnr = "not found";

            }

            name = splitData[6].split("\\+")[0];
            train_no = splitData[1].replace("TRAIN:", "");
            seat_details = splitData[7];
            doj = splitData[2].replace("DOJ:", "");
            time = splitData[3].replace("TIME:", "");
        }
        return new String[]{pnr, name, train_no, seat_details, doj, time};
    }

    public static void startJob(Context context) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Job job = dispatcher.newJobBuilder()
                //persist the task across boots
                .setLifetime(Lifetime.FOREVER)
                //.setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                //call this service when the criteria are met.
                .setService(SmsJobSchedule.class)
                //unique id of the task
                .setTag("SMS_SENT_MESSAGE_TAG")
                //don't overwrite an existing job with the same tag
                .setReplaceCurrent(false)
                // We are mentioning that the job is periodic.
                .setRecurring(true)
                // Run between 30 - 60 seconds from now.
                .setTrigger(Trigger.executionWindow(0, 60))
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                //.setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                //Run this job only when the network is available.
                .build();

        dispatcher.mustSchedule(job);
    }

    public static void stopJob(Context context){
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        //Cancel all the jobs for this package
        dispatcher.cancelAll();
        // Cancel the job for this tag
        dispatcher.cancel("SMS_SENT_MESSAGE_TAG");
    }
}
