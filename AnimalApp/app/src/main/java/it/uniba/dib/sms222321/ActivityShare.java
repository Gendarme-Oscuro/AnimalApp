package it.uniba.dib.sms222321;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import it.uniba.dib.sms222321.databinding.ActivityShareBinding;

public class ActivityShare extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout about, logout, settings, animalDex, richieste, share;
    ActivityShareBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);

        about = findViewById(R.id.about);
        logout = findViewById(R.id.logout);
        settings = findViewById(R.id.settings);
        animalDex = findViewById(R.id.animaldex);
        richieste = findViewById(R.id.richieste);
        share = findViewById(R.id.share);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentId = user.getUid();
        DocumentReference reference;
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        reference = firebaseFirestore.collection("user").document(currentId);

        /*
        Controllo per far apparire il pokedex e le richieste ai giusti utenti
         */

        reference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.getResult().exists()){


                            String userResult = task.getResult().getString("userType");

                            if (Objects.equals(userResult, "Veterinario") || Objects.equals(userResult, "Ente")) {
                                animalDex.setVisibility(View.GONE);
                            }

                            if (Objects.equals(userResult, "Veterinario")) {
                                richieste.setVisibility(View.GONE);
                            }

                        }
                    }
                });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity((Activity) v.getContext(), ActivityAbout.class);
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity((Activity) v.getContext(), ActivitySettings.class);
            }
        });
        animalDex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity((Activity) v.getContext(), ActivityAnimalDex.class);
            }
        });
        richieste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity((Activity) v.getContext(), ActivityRichieste.class);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Logout!", Toast.LENGTH_SHORT).show();

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
            }
        });
    }

    public static void openDrawer(DrawerLayout drawerLayout){
        drawerLayout.openDrawer(GravityCompat.START);
    }
    public static void closeDrawer(DrawerLayout drawerLayout){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
    public static void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayout);
    }
    @Override
    public void onBackPressed() {
        redirectActivity(ActivityShare.this, Welcome.class);
    }
}