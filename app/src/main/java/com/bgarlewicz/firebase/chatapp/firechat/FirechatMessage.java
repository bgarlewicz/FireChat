package com.bgarlewicz.firebase.chatapp.firechat;

import android.content.Context;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FirechatMessage {

    private String text;
    private String sender;
    private String senderUid;
    private String receiver;
    private String receiverUid;
    private String photoUrl;
    private long timestamp;

    public FirechatMessage() {
    }


    public FirechatMessage(String text, String sender, String senderUid, String receiver, String receiverUid, String photoUrl, long timestamp) {
        this.text = text;
        this.sender = sender;
        this.senderUid = senderUid;
        this.receiver = receiver;
        this.receiverUid = receiverUid;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
    }

    public static String getMessageTime(Context context, long timestamp) {
        String date;
        SimpleDateFormat sDateFormat = new SimpleDateFormat("EEE, MMM d HH:mm", getCurrentLocale(context));
        SimpleDateFormat sTimeFormat = new SimpleDateFormat("HH:mm", getCurrentLocale(context));
        final long DAY_SEC = 24 * 3600;

        long currentTime = Calendar.getInstance().getTime().getTime();
        long timeDiff = getTimeDiffInSec(timestamp, currentTime);

        Date dateDate = new Date(timestamp);

        if (timeDiff < (DAY_SEC)) {
            date = sTimeFormat.format(dateDate);
        } else {
            date = sDateFormat.format(dateDate);
        }

        return date;
    }

    private static Locale getCurrentLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }

    public static long getTimeDiffInSec(long firstTimestamp, long secondTimestamp) {
        return (secondTimestamp - firstTimestamp) / 1000;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
