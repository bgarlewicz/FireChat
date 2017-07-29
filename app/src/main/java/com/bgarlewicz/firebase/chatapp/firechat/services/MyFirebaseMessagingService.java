package com.bgarlewicz.firebase.chatapp.firechat.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.bgarlewicz.firebase.chatapp.firechat.MessageActivity;
import com.bgarlewicz.firebase.chatapp.firechat.R;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        String body = data.get(getString(R.string.message_body));
        String title = data.get(getString(R.string.message_title));
        String author = data.get(getString(R.string.message_author));
        String senderUid = data.get(getString(R.string.sender_uid));

        Intent intent = new Intent(this, MessageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(getString(R.string.user_uid_extra), senderUid);
        intent.putExtra(getString(R.string.user_name_extra), author);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setContentText(body)
                .setContentTitle(title+author)
                .setSmallIcon(R.drawable.ic_mail_white_24dp)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0, builder.build());

    }
}
