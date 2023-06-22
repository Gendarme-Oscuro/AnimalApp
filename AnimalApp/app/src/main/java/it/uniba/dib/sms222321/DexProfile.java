package it.uniba.dib.sms222321;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class DexProfile extends AppCompatActivity {

    private TextView etName, etAge, etTypeAnimal, etTotal;
    private ImageView imgProfile;
    private ImageButton delete;
    private String animalId;
    Animal pet;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dex_profile);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.animal_name);
        etAge = findViewById(R.id.animal_age);
        etTypeAnimal = findViewById(R.id.animal_type);
        imgProfile = findViewById(R.id.profile_pic);

        pet = new Animal();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            animalId = extras.getString("animalId");
            fetchAnimalData();
        }





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
}