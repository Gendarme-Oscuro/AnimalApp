package it.uniba.dib.sms222321;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class FragmentProfile extends Fragment {

    ImageView imageView;
    TextView p_name, p_surname, p_company_name, p_age;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageView = view.findViewById(R.id.profile_pic_fvp);
        p_name = view.findViewById(R.id.profile_name_fvp);
        p_surname = view.findViewById(R.id.profile_surname_fvp);
        p_company_name = view.findViewById(R.id.profile_company_name_fvp);
        p_age = view.findViewById(R.id.profile_age_fvp);

        ImageButton editProfile = view.findViewById(R.id.ib_edit_fvp);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CreateProfile.class));
            }
        });

        //Otteniamo l'id dell'utente corrente e l'istanza di firebase
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

                            //In base al tipo di utente mostriamo nel profilo i campi company_name oppure name, surname ed age

                            String userResult = task.getResult().getString("userType");

                            if (Objects.equals(userResult, "Ente")) {
                                p_company_name.setVisibility(View.VISIBLE);
                                p_name.setVisibility(View.GONE);
                                p_surname.setVisibility(View.GONE);
                                p_age.setVisibility(View.GONE);
                            } else {
                                p_company_name.setVisibility(View.GONE);
                                p_name.setVisibility(View.VISIBLE);
                                p_surname.setVisibility(View.VISIBLE);
                                p_age.setVisibility(View.VISIBLE);
                            }

                            //Ottenimento dati dal database
                            String nameResult = task.getResult().getString("name");
                            String surnameResult = task.getResult().getString("surname");
                            String ageResult = task.getResult().getString("age");
                            String company_nameResult = task.getResult().getString("company name");
                            String url = task.getResult().getString("url");

                            //Popolazione nell'app dei vari campi
                            Picasso.get().load(url).into(imageView);
                            p_name.setText(nameResult);
                            p_surname.setText(surnameResult);
                            p_company_name.setText((company_nameResult));
                            p_age.setText(ageResult);

                        }
                    }
                });

        return view;
    }

    public void handleOnBackPressed() {
        //Il metodo popBackStack() del FragmentManager permette di ritornare al fragment precedente
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, new FragmentHome());
        fragmentTransaction.commit();

        // Aggiorna manualmente lo stato dell'icona selezionata nella bottom bar
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem homeMenuItem = menu.findItem(R.id.home);
        homeMenuItem.setChecked(true);
    }

}