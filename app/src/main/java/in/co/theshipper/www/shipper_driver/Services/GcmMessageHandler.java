package in.co.theshipper.www.shipper_driver.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import in.co.theshipper.www.shipper_driver.Activities.FullActivity;
import in.co.theshipper.www.shipper_driver.Helper;
import in.co.theshipper.www.shipper_driver.R;

public class GcmMessageHandler extends GcmListenerService {
    public static final int MESSAGE_NOTIFICATION_ID = 435345;
    public static final int PRIORITY_HIGH = 5;

    @Override
    public void onMessageReceived(String from, Bundle data) {

        createNotification(from,data);
        data.clear();

    }
    // Creates notification based on title and body received
    public void createNotification(String title, Bundle body) {

        Bundle bundle = new Bundle();
        String menu_fragment = Helper.getValueFromBundle(body, "menuFragment");
        bundle.putString("menuFragment",menu_fragment);
        bundle.putString("method", "push");
        String message =  Helper.getValueFromBundle(body, "message");
        String push_title =  Helper.getValueFromBundle(body, "title");

        if(menu_fragment.equals("NewBooking")){

            bundle.putString("pickup_point",Helper.getValueFromBundle(body,"pickup_point"));
            bundle.putString("dropoff_point",Helper.getValueFromBundle(body, "dropoff_point"));
            bundle.putString("crn_no", Helper.getValueFromBundle(body,"crn_no"));
            bundle.putString("booking_datetime", Helper.getValueFromBundle(body, "booking_datetime"));
        }
        body.clear();

        Intent i = new Intent(this, FullActivity.class);
        i.putExtras(Helper.CheckBundle(bundle));
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        Context context = getBaseContext();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)

                .setSmallIcon( R.drawable.vehicle_1).setContentTitle(push_title)
                .setContentText(message)
                .setContentIntent(pi)
                .setPriority(PRIORITY_HIGH) //private static final PRIORITY_HIGH = 5;
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);

        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(MESSAGE_NOTIFICATION_ID, mBuilder.build());

    }

}