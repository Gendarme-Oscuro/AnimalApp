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

    EditText etName, etAge, etAnimalType ;

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

    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private boolean isReadPermissionGranted = false;

    private Button btnCreateAnimal;

    Spinner spinnerAnimalType;
    String selectedAnimalType;



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
                        Toast.makeText(CreateAnimal.this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_animal);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Animal images");

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
                // Do nothing
            }
        });

        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if(result.get(Manifest.permission.READ_EXTERNAL_STORAGE) != null){
                isReadPermissionGranted = Boolean.TRUE.equals(result.get(Manifest.permission.READ_EXTERNAL_STORAGE));
                if(isReadPermissionGranted){
                    launchActivity();
                }
            }
        });


        btnCreateAnimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String name = etName.getText().toString();
                String age = etAge.getText().toString();
                selectedAnimalType = spinnerAnimalType.getSelectedItem().toString();





                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(age) && !TextUtils.isEmpty(selectedAnimalType)){
                    uploadData(selectedAnimalType);
                }else{
                    Toast.makeText(CreateAnimal.this, "Compilare tutti i campi", Toast.LENGTH_SHORT).show();

                }








            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isReadPermissionGranted == false){
                    requestPermission();
                }else {
                    launchActivity();
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

    public static void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private void requestPermission(){
        isReadPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        List<String> permessionRequest = new ArrayList<String>();

        if(!isReadPermissionGranted){
            permessionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permessionRequest.isEmpty()){
            mPermissionResultLauncher.launch(permessionRequest.toArray(new String[0]));
        }

    }

    private void saveAnimalToFirestore() {
        db.collection("animals")
                .add(pet)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CreateAnimal.this, "Animal created successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CreateAnimal.this, "Failed to create animal", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




    private void setUserToFirestore(String documentId) {
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
                                                    Toast.makeText(CreateAnimal.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    Toast.makeText(CreateAnimal.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(CreateAnimal.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void launchActivity(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(intent);
    }


    private void uploadData(String selectedAnimalType) {

        String name = etName.getText().toString();
        String age = etAge.getText().toString();
        String animalType = selectedAnimalType;




        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            currentUserId = user.getUid();


            documentReference = db.collection("user").document(currentUserId);

            documentReference.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.getResult().exists()) {


                                long numAnimalsResult = task.getResult().contains("numAnimals") ? task.getResult().getLong("numAnimals") : 0;
                                numAnimalsResult++;

                                final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getFileExt(imageUri));
                                uploadTask = reference.putFile(imageUri);

                                long finalNumAnimalsResult = numAnimalsResult;
                                Tasks.whenAllComplete(uploadTask)
                                        .addOnSuccessListener(taskSnapshots -> {
                                            if (uploadTask.isSuccessful()) {
                                                reference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                                    String imageUrl = downloadUri.toString();
                                                    pet.setUrl(imageUrl);

                                                    // Rest of the code related to pet creation and Firestore update
                                                    pet.setName(name);
                                                    pet.setAge(age);
                                                    pet.setAnimalType(animalType);
                                                    pet.setOwner(currentUserId);

                                                    member.setNumAnimals(finalNumAnimalsResult);
                                                    member.addPet(name);

                                                    setUserToFirestore(currentUserId);
                                                    saveAnimalToFirestore();

                                                }).addOnFailureListener(e -> {
                                                    // Handle download URL retrieval failure
                                                });
                                            } else {
                                                // Handle upload failure
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle upload failure
                                        });




                            }
                        }
                    });


        } else {
            Toast.makeText(CreateAnimal.this, "E' tutto buggato", Toast.LENGTH_SHORT).show();

        }

    }


    private String getFileExt (Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType((contentResolver.getType(uri)));

    }







}