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

            //se l'utente clicca il tasto apposito per modificare la password verrà richiesta una conferma
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ActivityEditPassword.this);
                builder.setTitle(R.string.modificare_password);
                builder.setMessage(R.string.sei_sicuro_di_voler_modificare_la_password);
                builder.setPositiveButton(R.string.conferma3, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // se l'utente conferma di voler modificare la password
                        auth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // Se il processo va a buon fine
                                if (task.isSuccessful()){
                                    Toast.makeText(ActivityEditPassword.this, R.string.check_your_email1, Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(ActivityEditPassword.this, R.string.unable_to_send_email2, Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                });
                builder.setNegativeButton(R.string.annulla2, null);

                AlertDialog dialog = builder.create();
                dialog.show();
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
        redirectActivity(ActivityEditPassword.this, ActivityPrivacy.class);
    }
}


