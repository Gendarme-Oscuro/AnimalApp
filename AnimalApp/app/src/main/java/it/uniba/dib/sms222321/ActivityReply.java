package it.uniba.dib.sms222321;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class ActivityReply extends AppCompatActivity implements PhotosAdapter.OnImageClickListener{

    String uid, request, key;
    ArrayList<String> photoUrls;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference reference, reference2;
    TextView name_tv, surname_tv, company_name_tv, request_reply, reply_answer;
    RecyclerView photosRecyclerView, recyclerView;
    ViewHolderRisposte viewHolderRisposte;
    ArrayList<AnswerMember> answerMemberArrayList;
    ImageView imageViewRequest, imageViewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        name_tv = findViewById(R.id.name_reply);
        surname_tv = findViewById(R.id.surname_reply);
        company_name_tv = findViewById(R.id.company_name_reply);
        request_reply = findViewById(R.id.request_reply);
        imageViewRequest = findViewById(R.id.request_user);
        imageViewUser = findViewById(R.id.person_reply_user);
        reply_answer = findViewById(R.id.answer);

        //RecyclerView corrispondente alle immagini della richiesta
        photosRecyclerView = findViewById(R.id.photosRecyclerView);
        photosRecyclerView.setHasFixedSize(true);
        photosRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //La recyclerView conterrà tutte le risposte
        recyclerView = findViewById(R.id.answer_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        answerMemberArrayList = new ArrayList<AnswerMember>();
        viewHolderRisposte = new ViewHolderRisposte(ActivityReply.this, answerMemberArrayList);

        recyclerView.setAdapter(viewHolderRisposte);

        /*
         * Ottenimento dei parametri passati da ActivityRichieste
         * Permettiamo la visualizzazione delle immagini della richiesta
         */
        Bundle extra = getIntent().getExtras();
        if (extra != null){
            uid = extra.getString("uid");
            request = extra.getString("request");
            key = extra.getString("key");
            photoUrls = extra.getStringArrayList("photoUrls");

            if (photoUrls != null && !photoUrls.isEmpty()) {
                photosRecyclerView.setVisibility(View.VISIBLE);

                // Configura la RecyclerView orizzontale per visualizzare le immagini
                LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                photosRecyclerView.setLayoutManager(layoutManager);

                // Crea un adapter per la RecyclerView delle immagini
                PhotosAdapter photosAdapterForReply = new PhotosAdapter(this, photoUrls);
                photosAdapterForReply.setOnImageClickListener(this);
                photosRecyclerView.setAdapter(photosAdapterForReply);

            } else {
                photosRecyclerView.setVisibility(View.GONE);
            }

            EventChangeListener();

        } else {
            Toast.makeText(this, "Information not obtained", Toast.LENGTH_SHORT).show();
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = user.getUid();

        /*
         * Andiamo ad inserire la rispsota all'interno dell'utente che ha risposto
         */
        reference = uid != null ? db.collection("user").document(uid) : null;
        reference2 = db.collection("user").document(currentUserId);

        /*
         * Passiamo dei parametri aggiuntivi all'activity ActivityAnswer alla pressione del tasto
         */
        reply_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent((Activity) v.getContext(), ActivityAnswer.class);
                intent.putExtra("uid", uid);
                intent.putExtra("key", key);
                intent.putExtra("request", request);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);
            }
        });

    }


    /*
     * Questo metodo personalizzato permette di scorrere con un foreach
     * gli elementi della lista di risposte e verranno mostrati a schermo
     */
    private void EventChangeListener() {
        db.collection("AllRequests").document(key).collection("Answer")
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
                                AnswerMember answerMember = dc.getDocument().toObject(AnswerMember.class);

                                answerMemberArrayList.add(answerMember);
                            }

                            viewHolderRisposte.notifyDataSetChanged();
                        }
                    }
                });
    }

    /*
     * Carica a schermo le informazioni dell'utente che ha creato la richiesta
     * e degli utenti che hanno risposto
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (reference != null) {
            reference.get()
                    .addOnCompleteListener(task -> {
                        if (task.getResult() != null && task.getResult().exists()) {

                            String url = task.getResult().getString("url");
                            String name = task.getResult().getString("name");
                            String surname = task.getResult().getString("surname");
                            String company_name = task.getResult().getString("company name");
                            Picasso.get().load(url).into(imageViewRequest);
                            name_tv.setText(name);
                            surname_tv.setText(surname);
                            company_name_tv.setText(company_name);
                            request_reply.setText(request);
                        } else {
                            Toast.makeText(this, "Error retrieving data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Error retrieving data", Toast.LENGTH_SHORT).show();
        }

        reference2.get()
                .addOnCompleteListener(task -> {
                    if (task.getResult() != null && task.getResult().exists()) {
                        String url = task.getResult().getString("url");
                        Picasso.get().load(url).into(imageViewUser);
                    } else {
                        Toast.makeText(this, "Error retrieving data", Toast.LENGTH_SHORT).show();
                    }

                    viewHolderRisposte.notifyDataSetChanged();
                });

    }
    /*
     * Al click sull'immagine passiamo imageUrl come parametro nell'intent per poter
     * essere utilizzata nell'activity corrispondente
     * @param imageUrl
     */
    @Override
    public void onImageClick(String imageUrl) {
        Intent intent = new Intent(this, ImmagineIngranditaActivity.class);
        intent.putExtra("image_url", imageUrl);
        startActivity(intent);
    }
}