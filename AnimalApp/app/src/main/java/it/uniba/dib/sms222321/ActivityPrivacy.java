package it.uniba.dib.sms222321;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ActivityPrivacy extends AppCompatActivity {

    Button changePassword;
    Button eliminateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        changePassword = findViewById(R.id.btnChangePassword);
        eliminateAccount = findViewById(R.id.btnEliminateAccount);
        // porta l'utente alla sezione relativa alla modifica della password dell'account
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(ActivityPrivacy.this, ActivityEditPassword.class);

            }
        });
        // porta l'utente alla sezione relativa alla cancellazione dell'account
        eliminateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(ActivityPrivacy.this, ActivityDeleteAccount.class);
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
        redirectActivity(ActivityPrivacy.this, ActivitySettings.class);
    }
}