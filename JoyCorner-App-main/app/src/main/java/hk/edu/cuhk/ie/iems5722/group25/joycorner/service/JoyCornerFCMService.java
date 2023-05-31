package hk.edu.cuhk.ie.iems5722.group25.joycorner.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.qmuiteam.qmui.util.QMUIDrawableHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Map;

import hk.edu.cuhk.ie.iems5722.group25.joycorner.MainApplication;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.activity.MainActivity;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.ActionType;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.constant.ContactType;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.entity.Friend;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ReceiveFriendConfirmEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ReceiveFriendRequestEvent;
import hk.edu.cuhk.ie.iems5722.group25.joycorner.event.ReceiveNewMessageEvent;

public class JoyCornerFCMService extends FirebaseMessagingService {
    private static final String TAG = "JoyCornerFCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        Map<String, String> dataPayload = remoteMessage.getData();
        if (dataPayload.size() > 0) {
            Log.d(TAG, "Message data payload: " + dataPayload);
            if (ContactType.ACTION.equals(dataPayload.get("msg_type"))) {
                switch (dataPayload.get("action_type")) {
                    case ActionType.APPLY_FRIEND:
                        applyFriendNotification(dataPayload);
                        break;
                    case ActionType.CONFIRM_FRIEND:
                        confirmFriendNotification(dataPayload.get("friend"));
                        break;
                }
            } else {
                newMsgNotification(dataPayload);
            }
        }
    }

    private void confirmFriendNotification(String friendJson) {
        final Gson gson = new Gson();
        Friend friend = gson.fromJson(friendJson, Friend.class);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) Calendar.getInstance().getTimeInMillis(), intent, PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, MainApplication.CHANNEL_ID)
                        .setSmallIcon(R.drawable.contact_mail_black)
                        .setLargeIcon(QMUIDrawableHelper.drawableToBitmap(getDrawable(R.drawable.happy)))
                        .setContentTitle("Friend request approved")
                        .setContentText(friend.getNickname())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.notify((int) Calendar.getInstance().getTimeInMillis(), notificationBuilder.build());
        }

        EventBus.getDefault().post(new ReceiveFriendConfirmEvent(friend));
    }

    private void applyFriendNotification(Map<String, String> dataPayload) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) Calendar.getInstance().getTimeInMillis(), intent, PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, MainApplication.CHANNEL_ID)
                        .setSmallIcon(R.drawable.contact_mail_black)
                        .setLargeIcon(QMUIDrawableHelper.drawableToBitmap(getDrawable(R.drawable.happy)))
                        .setContentTitle("New friend request")
                        .setContentText(dataPayload.get("nickname"))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.notify((int) Calendar.getInstance().getTimeInMillis(), notificationBuilder.build());
        }

        EventBus.getDefault().post(new ReceiveFriendRequestEvent(dataPayload.get("from_id")));
    }

    private void newMsgNotification(Map<String, String> dataPayload) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) Calendar.getInstance().getTimeInMillis(), intent, PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, MainApplication.CHANNEL_ID)
                        .setSmallIcon(R.drawable.message_black)
                        .setLargeIcon(QMUIDrawableHelper.drawableToBitmap(getDrawable(R.drawable.happy)))
                        .setContentTitle(dataPayload.get("nickname"))
                        .setContentText(dataPayload.get("display_msg"))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.notify((int) Calendar.getInstance().getTimeInMillis(), notificationBuilder.build());
        }

        EventBus.getDefault().post(new ReceiveNewMessageEvent(dataPayload));
    }
}
