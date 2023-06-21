package it.uniba.dib.sms222321;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
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

        //vengono definite le informazioni relativi al canale per le notifiche
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel("My notification", "My notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }




        notifyBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // definiamo il corpo della notifica

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

    // sposta l'utente da un'activity ad un'altra
    public static void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
    // se l'utente va indietro con l'apposito tasto di android verrà riportato all'activity precedente
    @Override
    public void onBackPressed() {
        redirectActivity(ActivityNotifications.this, ActivitySettings.class);
    }


}









