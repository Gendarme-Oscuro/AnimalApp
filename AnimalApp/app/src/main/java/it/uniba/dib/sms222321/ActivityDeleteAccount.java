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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class ActivityDeleteAccount extends AppCompatActivity {

    Button deleteAccount;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);
        deleteAccount = findViewById(R.id.btnEliminaAccount);

        FirebaseUser user = mAuth.getCurrentUser();

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ActivityDeleteAccount.this);
                builder.setTitle("ELIMINAZIONE ACCOUNT");
                builder.setMessage("SEI SICURO DI VOLER CANCELLARE L'ACCOUNT?");
                builder.setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Azioni da eseguire se l'utente conferma l'eliminazione
                        if (user != null) {
                            // Eliminazione dei dati dell'utente da Firebase Firestore
                            String userId = user.getUid();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                            DocumentReference userRef = db.collection("user").document(userId);

                            userRef.get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful() && task.getResult().exists()) {
                                                String url = task.getResult().getString("url");
                                                firebaseStorage.getReferenceFromUrl(url).delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // L'immagine è stata eliminata con successo
                                                                // Ora puoi procedere all'eliminazione dell'utente e reindirizzarlo
                                                                userRef.delete()
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                // I dati dell'utente sono stati eliminati con successo
                                                                                user.delete()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    // L'utente è stato eliminato con successo
                                                                                                    Toast.makeText(ActivityDeleteAccount.this, "Account eliminato con successo", Toast.LENGTH_SHORT).show();
                                                                                                    redirectActivity(ActivityDeleteAccount.this, MainActivity.class);
                                                                                                } else {
                                                                                                    // Si è verificato un errore durante l'eliminazione dell'utente
                                                                                                    Toast.makeText(ActivityDeleteAccount.this, "Si è verificato un errore durante l'eliminazione dell'utente", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                // Si è verificato un errore durante l'eliminazione dei dati dell'utente
                                                                                Toast.makeText(ActivityDeleteAccount.this, "Si è verificato un errore durante l'eliminazione dei dati dell'utente", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // Si è verificato un errore durante l'eliminazione dell'immagine
                                                                Toast.makeText(ActivityDeleteAccount.this, "Si è verificato un errore durante l'eliminazione dell'immagine", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                // Il documento dell'utente non esiste o non contiene l'URL dell'immagine
                                                // Gestisci il caso in modo appropriato
                                                Toast.makeText(ActivityDeleteAccount.this, "Il documento dell'utente non esiste o non contiene l'URL dell'immagine", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            // L'utente non è autenticato
                            Toast.makeText(ActivityDeleteAccount.this, "L'utente non è autenticato", Toast.LENGTH_SHORT).show();
                        }
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
        redirectActivity(ActivityDeleteAccount.this, ActivityPrivacy.class);
    }
}