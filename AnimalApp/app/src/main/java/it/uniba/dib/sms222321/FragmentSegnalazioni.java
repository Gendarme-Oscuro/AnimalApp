package it.uniba.dib.sms222321;

import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import it.uniba.dib.sms222321.databinding.FragmentSegnalazioniBinding;
import android.os.Bundle;

public class FragmentSegnalazioni extends Fragment implements ViewHolderRichieste.OnImageClickListener {

    Button button, refresh;
    FragmentSegnalazioniBinding binding;
    RecyclerView recyclerView;
    ArrayList<SegnalazioneMember> segnalazioneMemberArrayList;
    ViewHolderSegnalazioni viewHolderSegnalazioni;
    FirebaseFirestore firebaseFirestore;
    CollectionReference segnalazioniCol;
    TextView p_name, p_surname, p_company_name;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSegnalazioniBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();


        button = binding.bottoneSegnalazione;
        refresh = binding.refreshSegnalazioni;

        p_name = rootView.findViewById(R.id.name_segnalazione_item_tv);
        p_surname = rootView.findViewById(R.id.surname_segnalazione_item_tv);
        p_company_name = rootView.findViewById(R.id.company_name_segnalazione_item_tv);

        //La recycler view conterrà tutte le segnalazioni
        recyclerView = binding.recyclerViewSegnalazioni;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext())); //Usiamo requireContext, uguale a getContext ma che lancia un eccezione in caso di errore

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentId = user.getUid();
        DocumentReference reference;
        firebaseFirestore = FirebaseFirestore.getInstance();
        segnalazioniCol = firebaseFirestore.collection("AllSegnalazioni");

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

        segnalazioneMemberArrayList = new ArrayList<SegnalazioneMember>();
        viewHolderSegnalazioni = new ViewHolderSegnalazioni(requireContext(), segnalazioneMemberArrayList, fragmentManager);
        viewHolderSegnalazioni.setOnImageClickListener(this::onImageClick);

        recyclerView.setAdapter(viewHolderSegnalazioni);

        EventChangeListener();


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ottiene il FragmentManager dall'Activity
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                // Crea una nuova istanza del Fragment da avviare
                FragmentCreaSegnalazione newFragment = new FragmentCreaSegnalazione();

                // Esegue la sostituzione del Fragment
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, newFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ottenere il FragmentManager dalla tua Activity
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                // Creare una nuova istanza del Fragment da ricreare
                FragmentSegnalazioni newFragment = new FragmentSegnalazioni();

                // Eseguire la sostituzione del Fragment
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, newFragment)
                        .commit();
            }
        });

        // Restituisci la radice del layout come vista del fragment
        return rootView;
    }

    /*
     * Questo metodo personalizzato permette di scorrere con un foreach
     * gli elementi della lista di segnalazioni e verranno mostrati a schermo
     */
    private void EventChangeListener() {
        firebaseFirestore.collection("AllSegnalazioni").orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null){
                            Log.e("Firestore error", error.getMessage());
                            return;
                        }

                        for (DocumentChange dc : value.getDocumentChanges()){
                            if (dc.getType() == DocumentChange.Type.ADDED){
                                SegnalazioneMember segnalazioneMember = dc.getDocument().toObject(SegnalazioneMember.class);

                                segnalazioneMemberArrayList.add(segnalazioneMember);
                            }

                            viewHolderSegnalazioni.notifyDataSetChanged();
                        }
                    }
                });
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

    /**
     * Al click sull'immagine passiamo imageUrl come parametro nell'intent per poter
     * essere utilizzata nell'activity corrispondente
     * @param imageUrl
     */
    @Override
    public void onImageClick(String imageUrl) {
        Intent intent = new Intent(requireContext(), ImmagineIngranditaActivity.class);
        intent.putExtra("image_url", imageUrl);
        startActivity(intent);
    }
}