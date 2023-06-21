package it.uniba.dib.sms222321;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class EditProfileAnimal extends AppCompatActivity {

    private TextView etName, etAge, etTypeAnimal;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_animal);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.animal_name);
        etAge = findViewById(R.id.animal_age);
        etTypeAnimal = findViewById(R.id.animal_type);
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





        pet = new Animal();

        member = new All_User_Member();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            animalId = extras.getString("animalId");
            fetchAnimalData();
        }

        finish.setOnClickListener(v -> {

            finish();

        });

        delete_profile.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileAnimal.this);
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
                                                                        Toast.makeText(EditProfileAnimal.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                                                                        finish();
                                                                    } else {
                                                                        Toast.makeText(EditProfileAnimal.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                }
                                            } else {
                                                Toast.makeText(EditProfileAnimal.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });


                            deleteAnimal(animalId);
                            dialog.dismiss();

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




    }

    private void deleteAnimal(String animalId) {
        db.collection("animals")
                .document(animalId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileAnimal.this, "Animal deleted successfully", Toast.LENGTH_SHORT).show();
                            // Optionally, navigate back to the previous activity or perform any other action after deletion.
                        } else {
                            Toast.makeText(EditProfileAnimal.this, "Failed to delete animal", Toast.LENGTH_SHORT).show();
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

                        // Populate rowDataList with vaccination data
                        rowVaccinationsList = pet.getVaccinations();
                        if(rowVaccinationsList != null){
                            populateTableRowsVaccinations(tableLayoutVaccinazioni, rowVaccinationsList, vaccinationFlag);
                        }

                        rowDewormingList = pet.getDewormings();
                        if(rowDewormingList != null){
                            populateTableRowsVaccinations(tableLayoutSverminazioni, rowDewormingList, dewormingFlag);
                        }

                        rowVisitsList = pet.getVisits();
                        if(rowVisitsList != null){
                            populateTableRowsVaccinations(tableLayoutVisite, rowVisitsList, visitsFlag);
                        }

                        rowCiboList = pet.getFood();
                        if(rowCiboList != null){
                            populateTableRowsVaccinations(tableLayoutCibo, rowCiboList, ciboFlag);
                        }

                        rowAltroList = pet.getOther();
                        if(rowAltroList != null){
                            populateTableRowsVaccinations(tableLayoutAltro, rowAltroList, altroFlag);
                        }




                    }
                } else {
                    Toast.makeText(EditProfileAnimal.this, "Failed to retrieve animal data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ...

// ...

    private void populateTableRowsVaccinations(TableLayout tableLayout, List<SaluteTable> rowList, int flag) {
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
        builder.setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this row?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tableLayout.removeView(row);
                        rowList.remove(rowData);
                        saveListsAnimal(pet ,rowList, flag );
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
                                Toast.makeText(EditProfileAnimal.this, "Animal updated successfully", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(EditProfileAnimal.this, "Failed to update animal", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(EditProfileAnimal.this, "Animal updated successfully", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(EditProfileAnimal.this, "Failed to update animal", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(EditProfileAnimal.this, "Animal updated successfully", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(EditProfileAnimal.this, "Failed to update animal", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(EditProfileAnimal.this, "Animal updated successfully", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(EditProfileAnimal.this, "Failed to update animal", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(EditProfileAnimal.this, "Animal updated successfully", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(EditProfileAnimal.this, "Failed to update animal", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }




    }




}