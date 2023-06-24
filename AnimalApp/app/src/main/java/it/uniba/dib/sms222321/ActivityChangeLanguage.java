package it.uniba.dib.sms222321;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Locale;

public class ActivityChangeLanguage extends AppCompatActivity {

    Button italiano;
    Button inglese;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_change_language);
        italiano = findViewById(R.id.btnitaliano);
        inglese = findViewById(R.id.btninglese);



        italiano.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ActivityChangeLanguage.this);
                builder.setTitle(R.string.cambio_lingua);
                builder.setMessage(R.string.impostare_italiano_come_lingua);
                builder.setPositiveButton(R.string.conferma, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        setLanguage("it");
                        Toast.makeText(ActivityChangeLanguage.this, R.string.cambio_avvenuto_con_successo, Toast.LENGTH_SHORT).show();
                        redirectActivity(ActivityChangeLanguage.this, ActivitySettings.class);

                    }
                });
                builder.setNegativeButton(R.string.annulla, null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        inglese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ActivityChangeLanguage.this);
                builder.setTitle(R.string.cambio_lingua1);
                builder.setMessage(R.string.impostare_inglese_come_lingua);
                builder.setPositiveButton(R.string.conferma1, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        setLanguage("en");
                        Toast.makeText(ActivityChangeLanguage.this, R.string.change_made_successfully, Toast.LENGTH_SHORT).show();
                        redirectActivity(ActivityChangeLanguage.this, ActivitySettings.class);

                    }
                });
                builder.setNegativeButton("Annulla", null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });



    }


    public static void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
    // se l'utente va indietro con l'apposito tasto di android verrà riportato all'activity precedente
    @Override
    public void onBackPressed() {
        redirectActivity(ActivityChangeLanguage.this, ActivitySettings.class);
    }

    public void setLanguage(String languageCode) {

        Resources resources  = this.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(languageCode);
        locale.setDefault(locale);
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

    }

}
