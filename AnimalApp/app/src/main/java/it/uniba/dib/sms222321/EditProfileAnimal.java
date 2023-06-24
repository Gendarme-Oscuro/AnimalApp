package it.uniba.dib.sms222321;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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

public class EditProfileAnimal extends AppCompatActivity {

    private TextView etName, etAge, etTypeAnimal;
    private EditText etBio;
    private ImageView imgProfile;
    private TableLayout tableLayoutVaccinazioni;
    private TableLayout tableLayoutSverminazioni;
    private TableLayout tableLayoutVisite;
    private TableLayout tableLayoutCibo;
    private TableLayout tableLayoutAltro;
    private List<SaluteTable> rowVaccinationsList;
    private List<SaluteTable> rowDewormingList;
    private List<SaluteTable> rowVisitsList;
    private List<SaluteTable> rowCiboList;
    private List<SaluteTable> rowAltroList;
    private FirebaseFirestore db;
    private String animalId;
    private Button finish;
    private Button delete_profile;

    Animal pet;
    All_User_Member member;

    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout about, logout, settings, animalDex, richieste, share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_animal);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.animal_name);
        etAge = findViewById(R.id.animal_age);
        etTypeAnimal = findViewById(R.id.animal_type);
        etBio = findViewById(R.id.bio);
        imgProfile = findViewById(R.id.profile_pic);
        finish = findViewById(R.id.finish_button);
        delete_profile = findViewById(R.id.delete_profile);
        tableLayoutVaccinazioni = findViewById(R.id.tableLayoutVaccinazioni);
        tableLayoutSverminazioni = findViewById(R.id.tableLayoutSverminazioni);
        tableLayoutVisite = findViewById(R.id.tableLayoutVisite);
        tableLayoutCibo = findViewById(R.id.tableLayoutCibo);
        tableLayoutAltro = findViewById(R.id.tableLayoutAltro);
        rowVaccinationsList = new ArrayList<>();
        rowDewormingList = new ArrayList<>();
        rowVisitsList = new ArrayList<>();
        rowCiboList = new ArrayList<>();
        rowAltroList = new ArrayList<>();


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

        finish.setOnClickListener(v -> {

            String temp = etBio.getText().toString();

            db.collection("animals")
                    .document(animalId)
                    .update("biografia", temp)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditProfileAnimal.this, R.string.animal_updated_successfully5, Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(EditProfileAnimal.this, R.string.failed_to_update_animal5, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


            finish();

        });

        delete_profile.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileAnimal.this);
            builder.setTitle(R.string.conferma_eliminazione)
                    .setMessage("Are you sure you want to delete this animal?")
                    .setPositiveButton(R.string.elimina, new DialogInterface.OnClickListener() {
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
                                                    List<String> petsList = (List<String>) document.get("pets");
                                                    if (petsList == null) {
                                                        petsList = new ArrayList<>();
                                                    }

                                                    member.setNumAnimals(member.numAnimals - 1);

                                                    petsList.remove(pet.getName());



                                                    db.collection("user")
                                                            .document(currentUserId)
                                                            .update("numAnimals", member.getNumAnimals(),
                                                                    "pets", petsList)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(EditProfileAnimal.this, R.string.user_updated_successfully3, Toast.LENGTH_SHORT).show();
                                                                        finish();
                                                                    } else {
                                                                        Toast.makeText(EditProfileAnimal.this, R.string.failed_to_update_user3, Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                }
                                            } else {
                                                Toast.makeText(EditProfileAnimal.this, R.string.failed_to_retrieve_user_data3, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });


                            deleteAnimal(animalId);
                            dialog.dismiss();

                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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

    public static void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private void deleteAnimal(String animalId) {
        db.collection("animals")
                .document(animalId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileAnimal.this, R.string.animale_eliminato_correttamente, Toast.LENGTH_SHORT).show();
                            // Optionally, navigate back to the previous activity or perform any other action after deletion.
                        } else {
                            Toast.makeText(EditProfileAnimal.this, R.string.fallimento_eliminazione_animale, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void fetchAnimalData() {

        int vaccinationFlag = 1;
        int dewormingFlag = 2;
        int visitsFlag = 3;
        int ciboFlag = 4;
        int altroFlag = 5;

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

                        String bio = document.getString("biografia");
                        if(bio != null){
                            etBio.setText(bio);
                        }

                        // Populate rowDataList with vaccination data
                        rowVaccinationsList = pet.getVaccinations();
                        if(rowVaccinationsList != null){
                            populateTableRows(tableLayoutVaccinazioni, rowVaccinationsList, vaccinationFlag);
                        }

                        rowDewormingList = pet.getDewormings();
                        if(rowDewormingList != null){
                            populateTableRows(tableLayoutSverminazioni, rowDewormingList, dewormingFlag);
                        }

                        rowVisitsList = pet.getVisits();
                        if(rowVisitsList != null){
                            populateTableRows(tableLayoutVisite, rowVisitsList, visitsFlag);
                        }

                        rowCiboList = pet.getFood();
                        if(rowCiboList != null){
                            populateTableRows(tableLayoutCibo, rowCiboList, ciboFlag);
                        }

                        rowAltroList = pet.getOther();
                        if(rowAltroList != null){
                            populateTableRows(tableLayoutAltro, rowAltroList, altroFlag);
                        }




                    }
                } else {
                    Toast.makeText(EditProfileAnimal.this, R.string.failed_to_retrieve_animal_data4, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ...

// ...

    private void populateTableRows(TableLayout tableLayout, List<SaluteTable> rowList, int flag) {
        tableLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        // Add default row
        TableRow defaultRow = (TableRow) inflater.inflate(R.layout.row_template, tableLayout, false);
        TextView defaultColumn1 = defaultRow.findViewById(R.id.column1);
        TextView defaultColumn2 = defaultRow.findViewById(R.id.column2);
        TextView defaultColumn3 = defaultRow.findViewById(R.id.column3);

        defaultColumn1.setText("Descrizione");
        defaultColumn2.setText("Data");
        defaultColumn3.setText("Spesa €");

        defaultRow.setBackground(ContextCompat.getDrawable(this, R.drawable.table_row_border));
        tableLayout.addView(defaultRow);

        List<SaluteTable> localRowList = new ArrayList<>(rowList); // Create a local copy of rowList

        for (final SaluteTable rowData : localRowList) {
            TableRow tableRow = (TableRow) inflater.inflate(R.layout.edit_row_template, tableLayout, false);
            TextView column1 = tableRow.findViewById(R.id.column1);
            TextView column2 = tableRow.findViewById(R.id.column2);
            TextView column3 = tableRow.findViewById(R.id.column3);
            Button deleteButton = tableRow.findViewById(R.id.delete_button);

            tableRow.setBackground(ContextCompat.getDrawable(this, R.drawable.table_row_border));

            column1.setText(rowData.getDescrizione());
            column2.setText(rowData.getData());
            column3.setText(rowData.getSpesa());

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeTableRow(tableLayout, tableRow, rowData, rowList, flag);
                }
            });

            tableLayout.addView(tableRow);
        }
    }

    private void removeTableRow(TableLayout tableLayout, TableRow row, SaluteTable rowData, List<SaluteTable> rowList, int flag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.conferma_eliminazione)
                .setMessage(R.string.sei_sicuro_di_voler_eliminare_questa_riga)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tableLayout.removeView(row);
                        rowList.remove(rowData);
                        saveListsAnimal(pet ,rowList, flag );
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    public void saveListsAnimal(Animal pet , List<SaluteTable> rowList, int flag ){

        if(flag == 1){

            pet.setVaccinations(rowList);

            db.collection("animals")
                    .document(animalId)
                    .update("vaccinations", pet.getVaccinations())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditProfileAnimal.this, R.string.animale_aggiornato_correttamente, Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(EditProfileAnimal.this, R.string.errore_aggiornamento_animale, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        if(flag == 2){

            pet.setDeworming(rowList);

            db.collection("animals")
                    .document(animalId)
                    .update("dewormings", pet.getDewormings())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditProfileAnimal.this, R.string.animale_aggiornato_correttamente, Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(EditProfileAnimal.this, R.string.errore_aggiornamento_animale, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        if(flag == 3){

            pet.setVisits(rowList);

            db.collection("animals")
                    .document(animalId)
                    .update("visits", pet.getVisits())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditProfileAnimal.this, R.string.animale_aggiornato_correttamente, Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(EditProfileAnimal.this, R.string.errore_aggiornamento_animale, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        if(flag == 4){

            pet.setFood(rowList);

            db.collection("animals")
                    .document(animalId)
                    .update("food", pet.getFood())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditProfileAnimal.this, R.string.animale_aggiornato_correttamente, Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(EditProfileAnimal.this, R.string.errore_aggiornamento_animale, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        if(flag == 5){

            pet.setOther(rowList);

            db.collection("animals")
                    .document(animalId)
                    .update("other", pet.getOther())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditProfileAnimal.this, R.string.animale_aggiornato_correttamente, Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(EditProfileAnimal.this, R.string.errore_aggiornamento_animale, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }




    }




}