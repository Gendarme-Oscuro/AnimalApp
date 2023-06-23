package it.uniba.dib.sms222321;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.uniba.dib.sms222321.databinding.ActivityDexProfileBinding;
import it.uniba.dib.sms222321.databinding.ActivityWelcomeBinding;

public class DexProfile extends AppCompatActivity {

    private TextView etName, etAge, etTypeAnimal;
    private ImageView imgProfile;
    private Button delete_profile;
    private String animalId;
    private DrawerLayout drawerLayout;
    private Animal pet;
    private FirebaseFirestore db;
    private All_User_Member member;

    ActivityDexProfileBinding binding;

    ImageView menu;
    LinearLayout about, logout, settings, animalDex, richieste, share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDexProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();



        etName = findViewById(R.id.animal_name);
        etAge = findViewById(R.id.animal_age);
        etTypeAnimal = findViewById(R.id.animal_type);
        imgProfile = findViewById(R.id.profile_pic);
        delete_profile = findViewById(R.id.delete_profile);

        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);

        about = findViewById(R.id.about);
        logout = findViewById(R.id.logout);
        settings = findViewById(R.id.settings);
        animalDex = findViewById(R.id.animaldex);
        richieste = findViewById(R.id.richieste);
        share = findViewById(R.id.share);

        pet = new Animal();
        member = new All_User_Member();


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            animalId = extras.getString("animalId");
            fetchAnimalData();
        }

        FirebaseUser user_temp = FirebaseAuth.getInstance().getCurrentUser();
        String currentId_temp = user_temp.getUid();
        DocumentReference reference;
        reference = db.collection("user").document(currentId_temp);

        reference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.getResult().exists()){

                            //Mostriamo diversi menu' in base al tipo di utente

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


        delete_profile.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(DexProfile.this);
            builder.setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this animal?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String currentUserId = user.getUid();

                            db.collection("user")
                                    .document(currentUserId)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {

                                                    member = document.toObject(All_User_Member.class);

                                                    List<String> pokedexList = (List<String>) document.get("pokedex");
                                                    if (pokedexList == null) {
                                                        pokedexList = new ArrayList<>();
                                                    }


                                                    pokedexList.remove(animalId);



                                                    db.collection("user")
                                                            .document(currentUserId)
                                                            .update("pokedex", pokedexList)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(DexProfile.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                                                                        finish();
                                                                    } else {
                                                                        Toast.makeText(DexProfile.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                }
                                            } else {
                                                Toast.makeText(DexProfile.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
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
                redirectActivity((Activity) v.getContext(), ActivityShare.class);
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


    private void fetchAnimalData() {
        DocumentReference animalRef = db.collection("animals").document(animalId);
        animalRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        etName.setText(document.getString("name"));
                        etAge.setText(document.getString("age"));
                        etTypeAnimal.setText(document.getString("animalType"));
                        String imageUrl = document.getString("url");
                        Picasso.get().load(imageUrl).into(imgProfile);

                        pet = document.toObject(Animal.class);

                    }
                } else {
                    Toast.makeText(DexProfile.this, "Failed to retrieve animal data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}