package it.uniba.dib.sms222321;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;


public class ActivityNotifications extends AppCompatActivity{

    Button notifyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notifyBtn = findViewById(R.id.notificationButton);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel("My notification", "My notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }




        notifyBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // il codice per le notifiche va qui;

                NotificationCompat.Builder builder = new NotificationCompat.Builder(ActivityNotifications.this,"My notification");
                builder.setContentTitle("My Title");
                builder.setContentText("Semplice notifica");
                builder.setSmallIcon(R.drawable.doggo);
                builder.setAutoCancel(true);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(ActivityNotifications.this);
                managerCompat.notify(1,builder.build());



            }
        });


    }



}









