package it.uniba.dib.sms222321;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


public class ActivityEditPassword extends AppCompatActivity {

    Button modificaPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        modificaPassword = findViewById(R.id.btnModificaPassword);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        final String userEmail = user.getEmail();



        modificaPassword.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ActivityEditPassword.this);
                builder.setTitle("MODIFICARE PASSWORD?");
                builder.setMessage("SEI SICURO DI VOLER MODIFICARE LA PASSWORD?");
                builder.setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Azioni da eseguire se l'utente conferma l'eliminazione
                        auth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
                                    Toast.makeText(ActivityEditPassword.this, "Check your email", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    // Disabilita la sincronizzazione automatica dei dati, elimina eventuali scritture in sospeso,
                                    // chiude la connessione con il database e la riapre.
                                    FirebaseDatabase.getInstance().getReference().keepSynced(false);
                                    FirebaseDatabase.getInstance().purgeOutstandingWrites();
                                    FirebaseDatabase.getInstance().goOffline();
                                    FirebaseDatabase.getInstance().goOnline();

                                    FirebaseAuth.getInstance().signOut(); // Effettua il logout dall'account Firebase

                                    // Cancella le informazioni dell'utente dal database locale
                                    SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.clear();
                                    editor.apply();

                                    // Rimuove tutti i dati dalla cache
                                    getApplicationContext().getCacheDir().delete();

                                    redirectActivity((Activity) v.getContext(), MainActivity.class);

                                } else {
                                    Toast.makeText(ActivityEditPassword.this, "Unable to send email", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
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

    @Override
    public void onBackPressed() {
        redirectActivity(ActivityEditPassword.this, ActivityPrivacy.class);
    }
}


