package it.uniba.dib.sms222321;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


public class CreateAnimal extends AppCompatActivity {

    EditText etName, etAge, etAnimalType;

    // Variabili per il menu laterale
    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout about, logout, settings, animalDex, richieste, share;

    Animal pet;

    All_User_Member member;

    FirebaseFirestore db;
    String currentUserId;
    DocumentReference documentReference;

    StorageReference storageReference;
    ImageView imageView;
    Uri imageUri;
    UploadTask uploadTask;

    // Launcher per il risultato dell'attività
    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private boolean isReadPermissionGranted = false;

    private Button btnCreateAnimal;

    Spinner spinnerAnimalType;
    String selectedAnimalType;

    // Launcher per il risultato dell'attività
    final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        imageUri = data.getData();
                        imageView.setImageURI(imageUri);
                    } else {
                        Toast.makeText(CreateAnimal.this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_animal);

        // Inizializza l'istanza del database Firestore
        db = FirebaseFirestore.getInstance();

        // Ottieni un riferimento allo storage Firebase
        storageReference = FirebaseStorage.getInstance().getReference("Animal images");

        // Inizializza l'oggetto Animal
        pet = new Animal();
        member = new All_User_Member();

        etName = findViewById(R.id.et_name);
        etAge = findViewById(R.id.et_age);
        btnCreateAnimal = findViewById(R.id.btn_create_animal);
        imageView = findViewById(R.id.imageView);
        spinnerAnimalType = findViewById(R.id.spinner_animal_type);

        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);

        about = findViewById(R.id.about);
        logout = findViewById(R.id.logout);
        settings = findViewById(R.id.settings);
        animalDex = findViewById(R.id.animaldex);
        richieste = findViewById(R.id.richieste);
        share = findViewById(R.id.share);

        // Ottieni l'ID dell'utente corrente
        FirebaseUser user_temp = FirebaseAuth.getInstance().getCurrentUser();
        String currentId_temp = user_temp.getUid();

        // Ottieni il riferimento al documento dell'utente corrente nel database
        DocumentReference reference;
        reference = db.collection("user").document(currentId_temp);

        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {

                    // Mostriamo diversi menu in base al tipo di utente
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

        // Configura l'adattatore per lo spinner
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.animal_types, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAnimalType.setAdapter(spinnerAdapter);

        spinnerAnimalType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAnimalType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Non fare nulla
            }
        });

        // Launcher per il risultato delle autorizzazioni
        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (result.get(Manifest.permission.READ_EXTERNAL_STORAGE) != null) {
                isReadPermissionGranted = Boolean.TRUE.equals(result.get(Manifest.permission.READ_EXTERNAL_STORAGE));
                if (isReadPermissionGranted) {
                    launchActivity();
                }
            }
        });

        // Gestione del click sul pulsante di creazione dell'animale
        btnCreateAnimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String age = etAge.getText().toString().trim();
                selectedAnimalType = spinnerAnimalType.getSelectedItem().toString();

                if (TextUtils.isEmpty(name)) {
                    etName.setError("Inserisci un nome");
                    return;
                }

                if (TextUtils.isEmpty(age)) {
                    etAge.setError("Inserisci un'età");
                    return;
                }

                if (TextUtils.isEmpty(selectedAnimalType)) {
                    Toast.makeText(CreateAnimal.this, R.string.select_an_animal_type, Toast.LENGTH_SHORT).show();
                    return;
                }

                Drawable imageViewDrawable = imageView.getDrawable();
                Drawable assignedDrawable = ContextCompat.getDrawable(CreateAnimal.this, R.drawable.doggo); // Sostituisci con la tua immagine predefinita

                if (!areDrawablesEqual(imageViewDrawable, assignedDrawable)) {
                    // È stata caricata un'immagine
                    uploadData(selectedAnimalType);
                } else {
                    Toast.makeText(CreateAnimal.this, R.string.upload_an_image, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Gestione del click sull'immagine per selezionare l'immagine dell'animale
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReadPermissionGranted == false) {
                    requestPermission();
                } else {
                    launchActivity();
                }
            }
        });

        // Gestione del click sul pulsante del menu laterale
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });

        // Gestione del click sulle voci del menu laterale
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

    // Method to compare two drawables
    public boolean areDrawablesEqual(Drawable drawable1, Drawable drawable2) {
        // Controlla se i due drawables sono lo stesso oggetto
        if (drawable1 == drawable2) {
            return true;
        }

        // Controlla se uno dei due drawables è nullo
        if (drawable1 == null || drawable2 == null) {
            return false;
        }

        // Controlla se i due drawables sono istanze della stessa classe
        if (drawable1.getClass() != drawable2.getClass()) {
            return false;
        }

        // Se entrambi i drawables sono istanze di BitmapDrawable
        if (drawable1 instanceof BitmapDrawable && drawable2 instanceof BitmapDrawable) {
            Bitmap bitmap1 = ((BitmapDrawable) drawable1).getBitmap();
            Bitmap bitmap2 = ((BitmapDrawable) drawable2).getBitmap();
            // Confronta i bitmaps
            return bitmap1.equals(bitmap2);
        }

        // Se entrambi i drawables sono istanze di ColorDrawable
        if (drawable1 instanceof ColorDrawable && drawable2 instanceof ColorDrawable) {
            ColorDrawable colorDrawable1 = (ColorDrawable) drawable1;
            ColorDrawable colorDrawable2 = (ColorDrawable) drawable2;
            // Confronta i colori
            return colorDrawable1.getColor() == colorDrawable2.getColor();
        }

        // Per altri tipi di drawables, puoi aggiungere controlli aggiuntivi se necessario

        return false;
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        // Apre il drawer specificato
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void redirectActivity(Activity activity, Class secondActivity) {
        // Reindirizza l'attività corrente a un'altra attività specificata
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private void requestPermission() {
        // Verifica se il permesso di lettura è già stato concesso
        isReadPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        List<String> permissionRequest = new ArrayList<String>();

        // Se il permesso di lettura non è stato concesso, lo aggiunge alla lista di richieste
        if (!isReadPermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        // Se la lista di richieste non è vuota, avvia il processo di richiesta dei permessi
        if (!permissionRequest.isEmpty()) {
            mPermissionResultLauncher.launch(permissionRequest.toArray(new String[0]));
        }
    }

    private void saveAnimalToFirestore() {
        // Salva l'animale nel Firestore
        db.collection("animals")
                .add(pet)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CreateAnimal.this, R.string.animal_created_successfully, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CreateAnimal.this, R.string.failed_to_create_animal, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setUserToFirestore(String documentId) {
        // Imposta l'utente nel Firestore utilizzando l'ID del documento specificato
        db.collection("user")
                .document(documentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<String> petsList = (List<String>) document.get("pets");
                                if (petsList == null) {
                                    petsList = new ArrayList<>();
                                }
                                petsList.add(pet.getName());

                                db.collection("user")
                                        .document(documentId)
                                        .update("numAnimals", member.getNumAnimals(),
                                                "pets", petsList)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(CreateAnimal.this, R.string.user_updated_successfully1, Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    Toast.makeText(CreateAnimal.this, R.string.failed_to_update_user1, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(CreateAnimal.this, R.string.failed_to_retrieve_user_data1, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void launchActivity() {
        // Avvia un'attività per selezionare un'immagine
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(intent);
    }

    private void uploadData(String selectedAnimalType) {
        // Carica i dati nel Firestore utilizzando il tipo di animale selezionato
        String name = etName.getText().toString();
        String age = etAge.getText().toString();
        String animalType = selectedAnimalType;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            documentReference = db.collection("user").document(currentUserId);

            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.getResult().exists()) {
                        long numAnimalsResult = task.getResult().contains("numAnimals") ? task.getResult().getLong("numAnimals") : 0;
                        numAnimalsResult++;

                        final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getFileExt(imageUri));
                        uploadTask = reference.putFile(imageUri);

                        long finalNumAnimalsResult = numAnimalsResult;
                        Tasks.whenAllComplete(uploadTask).addOnSuccessListener(taskSnapshots -> {
                            if (uploadTask.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                    String imageUrl = downloadUri.toString();
                                    pet.setUrl(imageUrl);

                                    // Resto del codice relativo alla creazione dell'animale e all'aggiornamento del Firestore
                                    pet.setName(name);
                                    pet.setAge(age);
                                    pet.setAnimalType(animalType);
                                    pet.setOwner(currentUserId);

                                    member.setNumAnimals(finalNumAnimalsResult);
                                    member.addPet(name);

                                    setUserToFirestore(currentUserId);
                                    saveAnimalToFirestore();

                                }).addOnFailureListener(e -> {
                                    // Gestione dell'errore nel recupero dell'URL di download
                                });
                            } else {
                                // Gestione dell'errore nel caricamento
                            }
                        }).addOnFailureListener(e -> {
                            // Gestione dell'errore nel caricamento
                        });
                    }
                }
            });
        } else {
            Toast.makeText(CreateAnimal.this, R.string.e_tutto_buggato, Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExt(Uri uri) {
        // Ottiene l'estensione del file dal suo Uri
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}