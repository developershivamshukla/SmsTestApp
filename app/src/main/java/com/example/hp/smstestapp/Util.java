package com.example.hp.smstestapp;

import android.util.Log;


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
            train_no = splitData[1].replace("TRAIN:","");
            seat_details = splitData[7];
            doj = splitData[2].replace("DOJ:","");
            time = splitData[3].replace("TIME:","");
        }
        return new String[]{pnr,name,train_no,seat_details,doj,time};
    }
}
