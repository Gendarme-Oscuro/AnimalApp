package it.uniba.dib.sms222321;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class FragmentProfile extends Fragment {

    ImageView imageView;
    TextView p_name, p_surname, p_age;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageView = view.findViewById(R.id.profile_pic_fvp);
        p_name = view.findViewById(R.id.profile_name_fvp);
        p_surname = view.findViewById(R.id.profile_surname_fvp);
        p_age = view.findViewById(R.id.profile_age_fvp);

        ImageButton editProfile = view.findViewById(R.id.ib_edit_fvp);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CreateProfile.class));
            }
        });

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

                        }
                    }
                });

        return view;
    }
}