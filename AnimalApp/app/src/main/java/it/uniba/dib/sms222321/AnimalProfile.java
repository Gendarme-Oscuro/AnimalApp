package it.uniba.dib.sms222321;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class AnimalProfile extends AppCompatActivity implements MyDialogFragment.DialogListener {

    private TextView etName, etAge, etTypeAnimal, etTotal;
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

    private ImageButton edit;
    Animal pet;

    int spesaTotale;

    String flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_profile);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.animal_name);
        etAge = findViewById(R.id.animal_age);
        etTypeAnimal = findViewById(R.id.animal_type);
        imgProfile = findViewById(R.id.profile_pic);
        etTotal = findViewById(R.id.spesa_totale);
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

        int vaccinationFlag = 1;
        int dewormingFlag = 2;
        int visitsFlag = 3;
        int ciboFlag = 4;
        int altroFlag = 5;

        int spesaTotale;

        pet = new Animal();




        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            animalId = extras.getString("animalId");
            fetchAnimalData();
        }

        String flag = pet.getName();



        findViewById(R.id.EditVaccinazioni).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddVaccinationDialog(vaccinationFlag);
            }
        });

        findViewById(R.id.EditSverminazioni).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddVaccinationDialog(dewormingFlag);
            }
        });

        findViewById(R.id.EditVisite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddVaccinationDialog(visitsFlag);
            }
        });

        findViewById(R.id.EditCibo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddVaccinationDialog(ciboFlag);
            }
        });

        findViewById(R.id.EditAltro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddVaccinationDialog(altroFlag);
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

                        // Populate rowDataList with vaccination data
                        rowVaccinationsList = pet.getVaccinations();
                        if(rowVaccinationsList != null){
                            populateTableRowsVaccinations(tableLayoutVaccinazioni, rowVaccinationsList);
                        }

                        rowDewormingList = pet.getDewormings();
                        if(rowDewormingList != null){
                            populateTableRowsVaccinations(tableLayoutSverminazioni, rowDewormingList);
                        }

                        rowVisitsList = pet.getVisits();
                        if(rowVisitsList != null){
                            populateTableRowsVaccinations(tableLayoutVisite, rowVisitsList);
                        }

                        rowCiboList = pet.getFood();
                        if(rowCiboList != null){
                            populateTableRowsVaccinations(tableLayoutCibo, rowCiboList);
                        }

                        rowAltroList = pet.getOther();
                        if(rowAltroList != null){
                            populateTableRowsVaccinations(tableLayoutAltro, rowAltroList);
                        }

                        spesaTotale = totaleSpesa(rowVaccinationsList, rowDewormingList,
                                rowVisitsList,
                                rowCiboList,
                                rowAltroList);

                        String spesa = String.valueOf(spesaTotale);
                        etTotal.setText(spesa);



                    }
                } else {
                    Toast.makeText(AnimalProfile.this, "Failed to retrieve animal data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showAddVaccinationDialog(int flag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        MyDialogFragment dialogFragment = MyDialogFragment.newInstance(flag);
        dialogFragment.show(fragmentManager, "Inserisci i dati dell'evento");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String descrizione, String data, String spesa, int flag) {
        if (descrizione.isEmpty() || data.isEmpty() || spesa.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AnimalProfile.this, "Animal updated successfully", Toast.LENGTH_SHORT).show();
                            fetchAnimalData();
                        } else {
                            Toast.makeText(AnimalProfile.this, "Failed to update animal", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AnimalProfile.this, "Animal updated successfully", Toast.LENGTH_SHORT).show();
                            fetchAnimalData();
                        } else {
                            Toast.makeText(AnimalProfile.this, "Failed to update animal", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AnimalProfile.this, "Animal updated successfully", Toast.LENGTH_SHORT).show();
                            fetchAnimalData();
                        } else {
                            Toast.makeText(AnimalProfile.this, "Failed to update animal", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AnimalProfile.this, "Animal updated successfully", Toast.LENGTH_SHORT).show();
                            fetchAnimalData();
                        } else {
                            Toast.makeText(AnimalProfile.this, "Failed to update animal", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AnimalProfile.this, "Animal updated successfully", Toast.LENGTH_SHORT).show();
                            fetchAnimalData();
                        } else {
                            Toast.makeText(AnimalProfile.this, "Failed to update animal", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void populateTableRowsVaccinations(TableLayout tableLayout,  List<SaluteTable> rowList) {
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


    public int totaleSpesa(List<SaluteTable> rowVaccinationsList, List<SaluteTable> rowDewormingList,
                           List<SaluteTable> rowVisitsList,
                           List<SaluteTable> rowCiboList,
                           List<SaluteTable> rowAltroList) {

        int totale = 0;

        if (rowVaccinationsList != null) {

            for (SaluteTable rowData : rowVaccinationsList) {

                int number = Integer.parseInt(rowData.getSpesa());
                totale = totale + number;
            }
        }

        if (rowDewormingList != null) {

            for (SaluteTable rowData : rowDewormingList) {

                int number = Integer.parseInt(rowData.getSpesa());
                totale = totale + number;
            }
        }

        if (rowVisitsList != null) {

            for (SaluteTable rowData : rowVisitsList) {

                int number = Integer.parseInt(rowData.getSpesa());
                totale = totale + number;
            }
        }

        if (rowCiboList != null) {

            for (SaluteTable rowData : rowCiboList) {

                int number = Integer.parseInt(rowData.getSpesa());
                totale = totale + number;
            }
        }

        if (rowAltroList != null) {

            for (SaluteTable rowData : rowAltroList) {

                int number = Integer.parseInt(rowData.getSpesa());
                totale = totale + number;
            }
        }

        return totale;

    }

}
