package it.uniba.dib.sms222321;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ViewProfile extends AppCompatActivity {

    ImageView imageView;
    TextView p_name, p_surname, p_age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        imageView = findViewById(R.id.profile_pic_fvp);
        p_name = findViewById(R.id.profile_name_fvp);
        p_surname = findViewById(R.id.profile_surname_fvp);
        p_age = findViewById(R.id.profile_age_fvp);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentId = user.getUid();
        DocumentReference reference;
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        reference = firebaseFirestore.collection("user").document(currentId);

        reference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.getResult().exists()){


                            String nameResult = task.getResult().getString("name");
                            String surnameResult = task.getResult().getString("surname");
                            String ageResult = task.getResult().getString("age");
                            String url = task.getResult().getString("url");

                            Picasso.get().load(url).into(imageView);
                            p_name.setText(nameResult);
                            p_surname.setText(surnameResult);
                            p_age.setText(ageResult);

                        }else {
                            Intent intent = new Intent(ViewProfile.this, Welcome.class);
                            startActivity(intent);
                        }
                    }
                });
    }

}