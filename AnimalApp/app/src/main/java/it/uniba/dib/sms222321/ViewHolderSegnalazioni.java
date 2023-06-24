package it.uniba.dib.sms222321;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;


public class ViewHolderSegnalazioni extends RecyclerView.Adapter<ViewHolderSegnalazioni.MyViewHolder> {

    Context context;
    ArrayList<SegnalazioneMember> segnalazioneMemberArrayList;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage storage;
    private FragmentManager fragmentManager;
    String userId, segnalazione, key;
    ArrayList<String> photoUrlsForReply;

    public ViewHolderSegnalazioni(Context context, ArrayList<SegnalazioneMember> segnalazioneMemberArrayList, FragmentManager fragmentManager) {
        this.context = context;
        this.segnalazioneMemberArrayList = segnalazioneMemberArrayList;
        this.fragmentManager = fragmentManager;
    }



    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    private OnImageClickListener onImageClickListener;

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.onImageClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolderSegnalazioni.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.segnalazione_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderSegnalazioni.MyViewHolder holder, int position) {

        SegnalazioneMember segnalazioneMember = segnalazioneMemberArrayList.get(position);

        // Aggiorna la visualizzazione delle immagini solo se sono presenti
        if (segnalazioneMember.getPhotoUrls() != null && !segnalazioneMember.getPhotoUrls().isEmpty()) {
            holder.photosRecyclerView.setVisibility(View.VISIBLE);

            // Configura la RecyclerView orizzontale per visualizzare le immagini
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            holder.photosRecyclerView.setLayoutManager(layoutManager);

            // Crea un adapter per la RecyclerView delle immagini
            PhotosAdapter photosAdapter = new PhotosAdapter(context, segnalazioneMember.getPhotoUrls());
            holder.photosRecyclerView.setAdapter(photosAdapter);

            /*
             * Rendiamo cliccabile ogni foto presente nelle segnalazione
             */
            photosAdapter.setOnImageClickListener(new PhotosAdapter.OnImageClickListener() {
                @Override
                public void onImageClick(String imageUrl) {
                    if (onImageClickListener != null) {
                        onImageClickListener.onImageClick(imageUrl);
                    }
                }
            });

        } else {
            holder.photosRecyclerView.setVisibility(View.GONE);
        }

        DocumentReference userRef, reqRef;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentId = user.getUid();
        String reqkey = segnalazioneMemberArrayList.get(position).getKey(); //Il campo key è l'id della segnalazione
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        userRef = firebaseFirestore.collection("user").document(currentId).collection("Segnalazioni").document(reqkey);
        reqRef = firebaseFirestore.collection("AllSegnalazioni").document(reqkey);

        //If per far apparire il bottone di eliminazione solo sui post dell'utente corretto

        if (currentId.equals(segnalazioneMemberArrayList.get(position).getId())) {
            holder.deleteSegnalazione.setVisibility(View.VISIBLE);
        } else {
            holder.deleteSegnalazione.setVisibility(View.GONE);
        }

        //Popola i campi all'interno dell'app
        Picasso.get().load(segnalazioneMember.url).into(holder.imageView);
        holder.time_result.setText(segnalazioneMember.time);
        holder.name_result.setText(segnalazioneMember.name);
        holder.surname_result.setText(segnalazioneMember.surname);
        holder.company_name_result.setText(segnalazioneMember.company_name);
        holder.segnalazione_result.setText(segnalazioneMember.segnalazione);
        holder.latitude_result.setText(segnalazioneMember.latitude);
        holder.longitude_result.setText(segnalazioneMember.longitude);

        /*
        *   Controllo se sono presenti i dati di posizione nella segnalazione,
        *   in caso contrario i campi diventano gone
        */

        if (segnalazioneMember.latitude != null && segnalazioneMember.longitude != null) {
            holder.latitude_result.setVisibility(View.VISIBLE);
            holder.longitude_result.setVisibility(View.VISIBLE);
            holder.latitudeH.setVisibility(View.VISIBLE);
            holder.longitudeH.setVisibility(View.VISIBLE);

        } else {
            holder.latitude_result.setVisibility(View.GONE);
            holder.longitude_result.setVisibility(View.GONE);
            holder.latitudeH.setVisibility(View.GONE);
            holder.longitudeH.setVisibility(View.GONE);

        }

        userId = segnalazioneMemberArrayList.get(position).getId();
        segnalazione = segnalazioneMemberArrayList.get(position).getSegnalazione();
        key = segnalazioneMemberArrayList.get(position).getKey();
        photoUrlsForReply = segnalazioneMemberArrayList.get(position).getPhotoUrls();

        /*
         * Al tasto reply impostiamo un onClickListener e passeremo alcuni parametri essenziali
         * per il funzionamento di reply
         */
        holder.reply.setOnClickListener(v -> {
            SegnalazioneMember segnalazioneMember1 = segnalazioneMemberArrayList.get(position);
            FragmentReplySegnalazioni fragment = FragmentReplySegnalazioni.newInstance(
                    segnalazioneMember1.getId(),
                    segnalazioneMember1.getKey(),
                    segnalazioneMember1.getSegnalazione(),
                    segnalazioneMember1.getPhotoUrls()
            );


            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        /*
         * Al click del tasto deleteSegnalazione verrà prima aperto una finestra di allerta che chiederà se confermare o no
         *  l'eliminazione della segnalazione, dopo di che andrà a prendere tutti i dati necessari per eliminarla dal databse in tutte le
         * collezioni in cui appare; inoltre viene effettuato anche un ciclo per prendere gli url delle foto caricate ed eliminarle.
         */

        holder.deleteSegnalazione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.conferma_eliminazione);
                builder.setMessage(R.string.sei_sicuro_di_voler_eliminare_questa_segnalazione);
                builder.setPositiveButton(R.string.elimina, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Azioni da eseguire se l'utente conferma l'eliminazione
                        for (String url: photoUrlsForReply) {
                            //Foreach per prendere gli url delle foto ed eliminarle
                            storage.getReferenceFromUrl(url).delete();
                        }
                        reqRef.delete();
                        userRef.delete();
                        Toast.makeText(context, R.string.segnalazione_eliminata_con_successo, Toast.LENGTH_SHORT).show();

                        // Creare una nuova istanza del Fragment da avviare
                        FragmentSegnalazioni newFragment = new FragmentSegnalazioni();

                        // Eseguire la sostituzione del Fragment
                        fragmentManager.beginTransaction()
                                .replace(R.id.frame_layout, newFragment)
                                .addToBackStack(null)
                                .commit();
                    }

                });
                builder.setNegativeButton(R.string.annulla, null);

                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });


    }

    @Override
    public int getItemCount() {
        return segnalazioneMemberArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView time_result, name_result, surname_result, company_name_result, segnalazione_result, latitude_result, longitude_result, latitudeH, longitudeH;
        RecyclerView photosRecyclerView;
        Button reply, deleteSegnalazione;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_segnalazione_item);
            time_result = itemView.findViewById(R.id.time_segnalazione_item_tv);
            name_result = itemView.findViewById(R.id.name_segnalazione_item_tv);
            surname_result = itemView.findViewById(R.id.surname_segnalazione_item_tv);
            company_name_result = itemView.findViewById(R.id.company_name_segnalazione_item_tv);
            segnalazione_result = itemView.findViewById(R.id.segnalazione_item_tv);
            photosRecyclerView = itemView.findViewById(R.id.photosRecyclerViewSegnalazioni);
            reply = itemView.findViewById(R.id.reply_segnalazione_item);
            deleteSegnalazione = itemView.findViewById(R.id.delete_segnalazione);
            latitude_result = itemView.findViewById(R.id.latitude_tv);
            longitude_result = itemView.findViewById(R.id.longitude_tv);
            latitudeH = itemView.findViewById(R.id.latitudeHINT);
            longitudeH = itemView.findViewById(R.id.longitudeHINT);
        }
    }
}

