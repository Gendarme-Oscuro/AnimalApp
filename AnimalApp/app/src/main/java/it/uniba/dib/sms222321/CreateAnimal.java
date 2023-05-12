package it.uniba.dib.sms222321;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;


public class CreateAnimal extends AppCompatActivity {

    EditText etName, etAge, etAnimalType ;

    Animal pet;

    All_User_Member member;

    FirebaseFirestore db;
    String currentUserId;
    DocumentReference documentReference;




    private Button btnCreateAnimal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_animal);

        db = FirebaseFirestore.getInstance();

        pet = new Animal();
        member = new All_User_Member();
        etName = findViewById(R.id.et_name);
        etAge = findViewById(R.id.et_age);
        etAnimalType = findViewById(R.id.et_animal_type);
        btnCreateAnimal = findViewById(R.id.btn_create_animal);



        btnCreateAnimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // Retrieve data from EditText fields
                String name = etName.getText().toString();
                String age = etAge.getText().toString();
                String animalType = etAnimalType.getText().toString();

                pet.setName(name);
                pet.setAge(age);
                pet.setAnimalType(animalType);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {

                    currentUserId = user.getUid();
                    documentReference = db.collection("user").document(currentUserId);

                    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

                    documentReference = firebaseFirestore.collection("user").document(currentUserId);

                    documentReference.get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.getResult().exists()) {

                                        String nameResult = task.getResult().getString("name");
                                        String surnameResult = task.getResult().getString("surname");
                                        String result = nameResult.concat(" ").concat(surnameResult);
                                        long numAnimalsResult = task.getResult().contains("numAnimals") ? task.getResult().getLong("numAnimals") : 0;
                                        numAnimalsResult++;

                                        pet.setOwner(result);
                                        member.setNumAnimals(numAnimalsResult);
                                        member.addPet(name);



                                        setUserToFirestore(currentUserId);
                                        saveAnimalToFirestore();






                                    }
                                }
                            });




                } else {
                    Toast.makeText(CreateAnimal.this, "E' tutto buggato", Toast.LENGTH_SHORT).show();

                }


            }
        });
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







}