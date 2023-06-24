package it.uniba.dib.sms222321;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ActivityAnimalDex extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout about, logout, settings, animalDex, richieste, share;

    private Button scanButton;

    private static final String TAG = "ActivityAnimalDex";
    private FirebaseFirestore db;

    private All_User_Member member;

    private List<Animal> pokedexList;

    private PokedexAdapter adapter;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final String TAGG = "QRCodeScanner";

    String id; // qrcode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_dex);

        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);

        about = findViewById(R.id.about);
        logout = findViewById(R.id.logout);
        settings = findViewById(R.id.settings);
        animalDex = findViewById(R.id.animaldex);
        richieste = findViewById(R.id.richieste);
        share = findViewById(R.id.share);

        db = FirebaseFirestore.getInstance();

        member = new All_User_Member();
        pokedexList = new ArrayList<>();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        scanButton = findViewById(R.id.scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
            }
        });

        adapter = new PokedexAdapter(ActivityAnimalDex.this, pokedexList);

        ListView listView = findViewById(R.id.listDex);
        listView.setAdapter(adapter);


        setupFirestoreListener();


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
                recreate();
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

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startQRCodeScanner();
        }
    }

    private void startQRCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a QR code");
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scannedValue = result.getContents();
                Log.d(TAGG, "Scanned value: " + scannedValue);
                // Process the scanned value as per your requirements
                handleScannedValue(scannedValue);
            } else {
                Toast.makeText(this, R.string.scan_canceled, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleScannedValue(String scannedValue) {
        // TODO: Perform actions with the scanned value

        id = scannedValue;
        final All_User_Member[] utente = {new All_User_Member()};


        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("user")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                utente[0] = document.toObject(All_User_Member.class);

                                utente[0].addPokedex(id);

                                db.collection("user")
                                        .document(userId)
                                        .update("pokedex", utente[0].getPokedex())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ActivityAnimalDex.this, R.string.user_updated_successfully, Toast.LENGTH_SHORT).show();

                                                } else {
                                                    Toast.makeText(ActivityAnimalDex.this, R.string.failed_to_update_user, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(ActivityAnimalDex.this, R.string.failed_to_retrieve_user_data, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        Toast.makeText(this, getString(R.string.scanned_value) + scannedValue, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRCodeScanner();
            } else {
                Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupFirestoreListener() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("user")
                .document(userId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            // Retrieve the All_User_Member object
                            All_User_Member user = snapshot.toObject(All_User_Member.class);

                            if (user != null) {
                                // Clear animalList before updating with new data
                                pokedexList.clear();

                                // Add the user's pets to the animalList
                                if (user.getPokedex() != null) {
                                    for (String petId : user.getPokedex()) {
                                        retrieveAnimal(petId); // Retrieve each animal using the updated method
                                    }
                                }
                                adapter.notifyDataSetChanged(); // Notify the adapter after updating the animalList
                            }
                        }
                    }
                });
    }

    private void retrieveAnimal(String petId) {
        getAnimal(petId, new AnimalCallback() {
            @Override
            public void onAnimalLoaded(Animal animal) {
                pokedexList.add(animal);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onAnimalNotFound() {
                // Handle the case when the animal was not found
            }

            @Override
            public void onAnimalLoadFailed(Exception e) {
                // Handle the failure to retrieve the animal
            }
        });
    }



    private void getAnimal(String petId, AnimalCallback callback) {
        db.collection("animals")
                .document(petId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Animal animal = document.toObject(Animal.class);
                            callback.onAnimalLoaded(animal); // Pass the animal object to the callback
                        } else {
                            callback.onAnimalNotFound(); // Notify the callback that the animal was not found
                        }
                    } else {
                        callback.onAnimalLoadFailed(task.getException()); // Notify the callback about the failure
                    }
                });
    }

    interface AnimalCallback {
        void onAnimalLoaded(Animal animal);
        void onAnimalNotFound();
        void onAnimalLoadFailed(Exception e);
    }





    public static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public static void redirectActivity(Activity activity, Class secondActivity) {
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
        redirectActivity(ActivityAnimalDex.this, Welcome.class);
    }
}
