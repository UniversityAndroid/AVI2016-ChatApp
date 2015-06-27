package sss1415.di.uniba.it.avi2016chatapp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by katia on 25/06/2015.
 */
public class NotificationReceiver extends BroadcastReceiver
{
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        Bundle extras = intent.getExtras();

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty())
        {

            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {

                // emette una notifica sul dispositivo
                sendNotification(context,"Welcome to AVI 2016 Chat App!");

            }
        }
    }

    private void sendNotification(Context ctx,String msg)
    {
        mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        // scelta suoneria per notifica
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.gcm_img)
                        .setContentTitle("AVI2016 ChatApp: Notification")
                        .setContentText(msg)
                        .setSound(sound);

        // effettua la notifica
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}