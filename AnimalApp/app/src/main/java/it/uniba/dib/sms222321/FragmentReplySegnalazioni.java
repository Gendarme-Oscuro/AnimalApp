package it.uniba.dib.sms222321;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FragmentReplySegnalazioni extends Fragment implements ViewHolderSegnalazioni.OnImageClickListener{

    String uid, segnalazione, key, latitude, longitude;
    ArrayList<String> photoUrls;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference reference, reference2;
    TextView name_tv, surname_tv, company_name_tv, segnalazione_reply, reply_answer;
    RecyclerView photosRecyclerView, recyclerView;
    ViewHolderRisposteSegnalazione viewHolderRisposteSegnalazione;
    ArrayList<SegnalazioneAnswerMember> answerMemberArrayList;
    ImageView imageViewSegnalazione, imageViewUser;

    // Aggiungi il costruttore vuoto
    public FragmentReplySegnalazioni() {
        // Costruttore vuoto richiesto
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_reply_segnalazioni, container, false);

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        name_tv = rootView.findViewById(R.id.name_reply_segnalazione);
        surname_tv = rootView.findViewById(R.id.surname_reply_segnalazione);
        company_name_tv = rootView.findViewById(R.id.company_name_reply_segnalazione);
        segnalazione_reply = rootView.findViewById(R.id.segnalazione_reply);
        imageViewSegnalazione = rootView.findViewById(R.id.segnalazione_image_user);
        imageViewUser = rootView.findViewById(R.id.person_reply_user_segnalazione);
        reply_answer = rootView.findViewById(R.id.answer_segnalazione);

        //RecyclerView corrispondente alle immagini della segnalazione
        photosRecyclerView = rootView.findViewById(R.id.photosRecyclerViewSegnalazione);
        photosRecyclerView.setHasFixedSize(true);
        photosRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        //La recyclerView conterrà tutte le risposte
        recyclerView = rootView.findViewById(R.id.answer_recycler_view_segnalazione);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        answerMemberArrayList = new ArrayList<SegnalazioneAnswerMember>();
        viewHolderRisposteSegnalazione = new ViewHolderRisposteSegnalazione(requireContext(), answerMemberArrayList);

        recyclerView.setAdapter(viewHolderRisposteSegnalazione);

        Bundle args = getArguments();
        if (args != null) {
            uid = args.getString("userId");
            key = args.getString("key");
            segnalazione = args.getString("segnalazione");
            photoUrls = args.getStringArrayList("photoUrlsForReply");

            // Utilizza i dati come desideri nel Fragment di destinazione
            if (photoUrls != null && !photoUrls.isEmpty()) {
                photosRecyclerView.setVisibility(View.VISIBLE);

                // Configura la RecyclerView orizzontale per visualizzare le immagini
                LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
                photosRecyclerView.setLayoutManager(layoutManager);

                // Crea un adapter per la RecyclerView delle immagini
                PhotosAdapter photosAdapterForReply = new PhotosAdapter(requireContext(), photoUrls);
                photosAdapterForReply.setOnImageClickListener(this::onImageClick);
                photosRecyclerView.setAdapter(photosAdapterForReply);

            } else {
                photosRecyclerView.setVisibility(View.GONE);
            }

            EventChangeListener();

        } else {
            Toast.makeText(requireContext(), R.string.information_not_obtained, Toast.LENGTH_SHORT).show();
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = user.getUid();

        /*
         * Andiamo ad inserire la rispsota all'interno dell'utente che ha risposto
         */
        reference = uid != null ? db.collection("user").document(uid) : null;
        reference2 = db.collection("user").document(currentUserId);

        /*
         * Passiamo dei parametri aggiuntivi al fragment FragmentSegnalazioneAnswer alla pressione del tasto
         */
        /*
         * Al tasto reply impostiamo un onClickListener e passeremo alcuni parametri essenziali
         * per il funzionamento di reply
         */
        reply_answer.setOnClickListener(v -> {
            FragmentAnswerSegnalazioni fragment = FragmentAnswerSegnalazioni.newInstance(uid, key, segnalazione);

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        return rootView;
    }

    // Aggiungi un metodo newInstance con i parametri corretti
    public static FragmentReplySegnalazioni newInstance(String userId, String key, String segnalazione, ArrayList<String> photoUrlsForReply) {
        FragmentReplySegnalazioni fragment = new FragmentReplySegnalazioni();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("key", key);
        args.putString("segnalazione", segnalazione);
        args.putStringArrayList("photoUrlsForReply", photoUrlsForReply);
        fragment.setArguments(args);
        return fragment;
    }


    /*
     * Questo metodo personalizzato permette di scorrere con un foreach
     * gli elementi della lista di risposte e verranno mostrati a schermo
     */
    private void EventChangeListener() {
        db.collection("AllSegnalazioni").document(key).collection("Answer")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null){
                            Log.e("Firestore error", error.getMessage());
                            return;
                        }

                        for (DocumentChange dc : value.getDocumentChanges()){
                            if (dc.getType() == DocumentChange.Type.ADDED){
                                SegnalazioneAnswerMember answerMember = dc.getDocument().toObject(SegnalazioneAnswerMember.class);

                                answerMemberArrayList.add(answerMember);
                            }

                            viewHolderRisposteSegnalazione.notifyDataSetChanged();
                        }
                    }
                });
    }

    /*
     * Carica a schermo le informazioni dell'utente che ha creato la segbalazione
     * e degli utenti che hanno risposto
     */
    @Override
    public void onStart() {
        super.onStart();

        if (reference != null) {
            reference.get()
                    .addOnCompleteListener(task -> {
                        if (task.getResult() != null && task.getResult().exists()) {

                            String url = task.getResult().getString("url");
                            String name = task.getResult().getString("name");
                            String surname = task.getResult().getString("surname");
                            String company_name = task.getResult().getString("company name");
                            Picasso.get().load(url).into(imageViewSegnalazione);
                            name_tv.setText(name);
                            surname_tv.setText(surname);
                            company_name_tv.setText(company_name);
                            segnalazione_reply.setText(segnalazione);
                        } else {
                            Toast.makeText(requireContext(), R.string.error_retrieving_data, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(requireContext(), R.string.error_retrieving_data, Toast.LENGTH_SHORT).show();
        }

        reference2.get()
                .addOnCompleteListener(task -> {
                    if (task.getResult() != null && task.getResult().exists()) {
                        String url = task.getResult().getString("url");
                        Picasso.get().load(url).into(imageViewUser);
                    } else {
                        Toast.makeText(requireContext(), R.string.error_retrieving_data, Toast.LENGTH_SHORT).show();
                    }

                    viewHolderRisposteSegnalazione.notifyDataSetChanged();
                });

    }

    public void handleOnBackPressed() {
        //Il metodo popBackStack() del FragmentManager permette di ritornare al fragment precedente
        requireActivity().getSupportFragmentManager().popBackStack();
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