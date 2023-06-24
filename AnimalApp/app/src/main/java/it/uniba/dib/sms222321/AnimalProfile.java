package it.uniba.dib.sms222321;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.view.LayoutInflater;
import android.view.View;


import android.os.Bundle;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import it.uniba.dib.sms222321.databinding.ActivityAnimalProfileBinding;


public class AnimalProfile extends AppCompatActivity implements MyDialogFragment.DialogListener {

    private TextView etName, etAge, etTypeAnimal, etTotal, etBio;

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

    private Button edit;
    private Animal pet;

    double spesaTotale;

    private String flag;
    private ImageView qrCodeImageView;

    private DrawerLayout drawerLayout;
    private ImageView menu;
    private LinearLayout about, logout, settings, animalDex, richieste, share;

    ActivityAnimalProfileBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnimalProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);

        about = findViewById(R.id.about);
        logout = findViewById(R.id.logout);
        settings = findViewById(R.id.settings);
        animalDex = findViewById(R.id.animaldex);
        richieste = findViewById(R.id.richieste);
        share = findViewById(R.id.share);


        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.animal_name);
        etAge = findViewById(R.id.animal_age);
        etTypeAnimal = findViewById(R.id.animal_type);
        imgProfile = findViewById(R.id.profile_pic);
        etTotal = findViewById(R.id.spesa_totale);
        etBio = findViewById(R.id.bio);
        edit = findViewById(R.id.EditProfile);
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

        qrCodeImageView = findViewById(R.id.qr_code_image_view);

        int vaccinationFlag = 1;
        int dewormingFlag = 2;
        int visitsFlag = 3;
        int ciboFlag = 4;
        int altroFlag = 5;

        double spesaTotale;

        pet = new Animal();




        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            animalId = extras.getString("animalId");
            fetchAnimalData();
        }

        String flag = pet.getName();

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


        findViewById(R.id.EditVaccinazioni).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyDialog(vaccinationFlag);
            }
        });

        findViewById(R.id.EditSverminazioni).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyDialog(dewormingFlag);
            }
        });

        findViewById(R.id.EditVisite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyDialog(visitsFlag);
            }
        });

        findViewById(R.id.EditCibo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyDialog(ciboFlag);
            }
        });

        findViewById(R.id.EditAltro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyDialog(altroFlag);
            }
        });

        edit.setOnClickListener(v -> {

            Intent intent = new Intent(AnimalProfile.this, EditProfileAnimal.class);
            intent.putExtra("animalId", animalId);
            startActivity(intent);

            if(flag != pet.getName()){
                finish();
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

    public static void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void openDrawer(DrawerLayout drawerLayout){
        drawerLayout.openDrawer(GravityCompat.START);
    }



    @Override
    protected void onResume() {
        super.onResume();

            fetchAnimalData();// Reload the data

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


                        String bio = document.getString("biografia");
                        if(bio != null){
                            etBio.setText(bio);
                        }

                        // Populate rowDataList with vaccination data
                        rowVaccinationsList = pet.getVaccinations();
                        if(rowVaccinationsList != null){
                            populateTableRows(tableLayoutVaccinazioni, rowVaccinationsList);
                        }

                        rowDewormingList = pet.getDewormings();
                        if(rowDewormingList != null){
                            populateTableRows(tableLayoutSverminazioni, rowDewormingList);
                        }

                        rowVisitsList = pet.getVisits();
                        if(rowVisitsList != null){
                            populateTableRows(tableLayoutVisite, rowVisitsList);
                        }

                        rowCiboList = pet.getFood();
                        if(rowCiboList != null){
                            populateTableRows(tableLayoutCibo, rowCiboList);
                        }

                        rowAltroList = pet.getOther();
                        if(rowAltroList != null){
                            populateTableRows(tableLayoutAltro, rowAltroList);
                        }

                        spesaTotale = totaleSpesa(rowVaccinationsList, rowDewormingList,
                                rowVisitsList,
                                rowCiboList,
                                rowAltroList);

                        String spesa = String.valueOf(spesaTotale);
                        spesa = spesa + ' ' + '€';
                        etTotal.setText(spesa);

                        QRCodeGenerator.generateQRCode(animalId, qrCodeImageView);



                    }
                } else {
                    Toast.makeText(AnimalProfile.this, R.string.failed_to_retrieve_animal_data, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showMyDialog(int flag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        MyDialogFragment dialogFragment = MyDialogFragment.newInstance(flag);
        dialogFragment.show(fragmentManager, String.valueOf(R.string.inserisci_i_dati_evento));
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String descrizione, String data, String spesa, int flag) {
        if (descrizione.isEmpty() || data.isEmpty() || spesa.isEmpty()) {
            Toast.makeText(this, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();
        } else {


            if(flag == 1){

                saveAnimalVaccination(descrizione, data, spesa);
                if(rowVaccinationsList == null){
                    rowVaccinationsList = new ArrayList<>();
                }
                rowVaccinationsList.add(new SaluteTable(descrizione, data, spesa));
                //populateTableRowsVaccinations();

            }

            if(flag == 2){

                saveAnimalDeworming(descrizione, data, spesa);
                if(rowDewormingList == null){
                    rowDewormingList = new ArrayList<>();
                }
                rowDewormingList.add(new SaluteTable(descrizione, data, spesa));

            }

            if(flag == 3){

                saveAnimalVisits(descrizione, data, spesa);
                if(rowVisitsList == null){
                    rowVisitsList = new ArrayList<>();
                }
                rowVisitsList.add(new SaluteTable(descrizione, data, spesa));

            }

            if(flag == 4){

                saveAnimalCibo(descrizione, data, spesa);
                if(rowCiboList == null){
                    rowCiboList = new ArrayList<>();
                }
                rowCiboList.add(new SaluteTable(descrizione, data, spesa));

            }

            if(flag == 5){

                saveAnimalAltro(descrizione, data, spesa);
                if(rowAltroList == null){
                    rowAltroList = new ArrayList<>();
                }
                rowAltroList.add(new SaluteTable(descrizione, data, spesa));

            }





            dialog.dismiss();
        }
    }



    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // Handle negative button click if needed
    }

    private void saveAnimalVaccination(String descrizione, String data, String spesa) {

        SaluteTable vaccination = new SaluteTable(descrizione, data, spesa);

        pet.addVaccination(vaccination);

        db.collection("animals")
                .document(animalId)
                .update("vaccinations", pet.getVaccinations())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AnimalProfile.this, R.string.animal_updated_successfully, Toast.LENGTH_SHORT).show();
                            fetchAnimalData();
                        } else {
                            Toast.makeText(AnimalProfile.this, R.string.failed_to_update_animal, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void saveAnimalDeworming(String descrizione, String data, String spesa) {

        SaluteTable deworming = new SaluteTable(descrizione, data, spesa);

        pet.addDeworming(deworming);

        db.collection("animals")
                .document(animalId)
                .update("dewormings", pet.getDewormings())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AnimalProfile.this, R.string.animal_updated_successfully1, Toast.LENGTH_SHORT).show();
                            fetchAnimalData();
                        } else {
                            Toast.makeText(AnimalProfile.this, R.string.failed_to_update_animal1, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void saveAnimalVisits(String descrizione, String data, String spesa) {

        SaluteTable visit = new SaluteTable(descrizione, data, spesa);

        pet.addVisit(visit);

        db.collection("animals")
                .document(animalId)
                .update("visits", pet.getVisits())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AnimalProfile.this, R.string.animal_updated_successfully2, Toast.LENGTH_SHORT).show();
                            fetchAnimalData();
                        } else {
                            Toast.makeText(AnimalProfile.this, R.string.failed_to_update_animal2, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void saveAnimalCibo(String descrizione, String data, String spesa) {

        SaluteTable cibo = new SaluteTable(descrizione, data, spesa);

        pet.addFood(cibo);

        db.collection("animals")
                .document(animalId)
                .update("food", pet.getFood())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AnimalProfile.this, R.string.animal_updated_successfully3, Toast.LENGTH_SHORT).show();
                            fetchAnimalData();
                        } else {
                            Toast.makeText(AnimalProfile.this, R.string.failed_to_update_animal3, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void saveAnimalAltro(String descrizione, String data, String spesa) {

        SaluteTable altro = new SaluteTable(descrizione, data, spesa);

        pet.addOther(altro);

        db.collection("animals")
                .document(animalId)
                .update("other", pet.getOther())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AnimalProfile.this, R.string.animal_updated_successfully4, Toast.LENGTH_SHORT).show();
                            fetchAnimalData();
                        } else {
                            Toast.makeText(AnimalProfile.this, R.string.failed_to_update_animal4, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void populateTableRows(TableLayout tableLayout,  List<SaluteTable> rowList) {
        tableLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);


        // Add default row
        TableRow defaultRow = (TableRow) inflater.inflate(R.layout.row_template, tableLayout, false);
        TextView defaultColumn1 = defaultRow.findViewById(R.id.column1);
        TextView defaultColumn2 = defaultRow.findViewById(R.id.column2);
        TextView defaultColumn3 = defaultRow.findViewById(R.id.column3);


        defaultColumn1.setText(R.string.descrizione);
        defaultColumn2.setText(R.string.data);
        defaultColumn3.setText(R.string.spesa);

        // Apply background and border to default row
        defaultRow.setBackground(ContextCompat.getDrawable(this, R.drawable.table_row_border));

        tableLayout.addView(defaultRow);

        for (SaluteTable rowData : rowList) {
            TableRow tableRow = (TableRow) inflater.inflate(R.layout.row_template, tableLayout, false);
            TextView column1 = tableRow.findViewById(R.id.column1);
            TextView column2 = tableRow.findViewById(R.id.column2);
            TextView column3 = tableRow.findViewById(R.id.column3);

            // Apply border to the table row
            tableRow.setBackground(ContextCompat.getDrawable(this, R.drawable.table_row_border));


            column1.setText(rowData.getDescrizione());
            column2.setText(rowData.getData());
            column3.setText(rowData.getSpesa());

            tableLayout.addView(tableRow);
        }
    }


    public double totaleSpesa(List<SaluteTable> rowVaccinationsList, List<SaluteTable> rowDewormingList,
                           List<SaluteTable> rowVisitsList,
                           List<SaluteTable> rowCiboList,
                           List<SaluteTable> rowAltroList) {

        double totale = 0;

        if (rowVaccinationsList != null) {

            for (SaluteTable rowData : rowVaccinationsList) {

                double number = Double.parseDouble(rowData.getSpesa().replace(",", "."));

                if(!isDotSeparator(number)){
                    number = Double.parseDouble(rowData.getSpesa().replace(",", "."));
                }
                totale = totale + number;
            }
        }

        if (rowDewormingList != null) {

            for (SaluteTable rowData : rowDewormingList) {

                double number = Double.parseDouble(rowData.getSpesa().replace(",", "."));

                if(!isDotSeparator(number)){
                    number = Double.parseDouble(rowData.getSpesa().replace(",", "."));
                }
                totale = totale + number;
            }
        }

        if (rowVisitsList != null) {

            for (SaluteTable rowData : rowVisitsList) {

                double number = Double.parseDouble(rowData.getSpesa().replace(",", "."));

                if(!isDotSeparator(number)){
                    number = Double.parseDouble(rowData.getSpesa().replace(",", "."));
                }
                totale = totale + number;
            }
        }

        if (rowCiboList != null) {

            for (SaluteTable rowData : rowCiboList) {

                double number = Double.parseDouble(rowData.getSpesa().replace(",", "."));

                if(!isDotSeparator(number)){
                    number = Double.parseDouble(rowData.getSpesa().replace(",", "."));
                }
                totale = totale + number;
            }
        }

        if (rowAltroList != null) {

            for (SaluteTable rowData : rowAltroList) {

                double number = Double.parseDouble(rowData.getSpesa().replace(",", "."));

                if(!isDotSeparator(number)){
                    number = Double.parseDouble(rowData.getSpesa().replace(",", "."));
                }

                totale = totale + number;
            }
        }

        return totale;

    }

    public static boolean isDotSeparator(double number) {
        String numberString = String.valueOf(number);
        return numberString.contains(".");
    }


}
