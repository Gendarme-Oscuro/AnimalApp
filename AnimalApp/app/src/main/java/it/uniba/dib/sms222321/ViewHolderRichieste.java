package it.uniba.dib.sms222321;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ViewHolderRichieste extends RecyclerView.Adapter<ViewHolderRichieste.MyViewHolder> {

    Context context;
    ArrayList<RequestMember> requestMemberArrayList;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage storage;

    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    private OnImageClickListener onImageClickListener;

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.onImageClickListener = listener;
    }

    public ViewHolderRichieste(Context context, ArrayList<RequestMember> requestMemberArrayList) {
        this.context = context;
        this.requestMemberArrayList = requestMemberArrayList;
    }

    @NonNull
    @Override
    public ViewHolderRichieste.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.richieste_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderRichieste.MyViewHolder holder, int position) {

        RequestMember requestMember = requestMemberArrayList.get(position);

        // Aggiorna la visualizzazione delle immagini solo se sono presenti
        if (requestMember.getPhotoUrls() != null && !requestMember.getPhotoUrls().isEmpty()) {
            holder.photosRecyclerView.setVisibility(View.VISIBLE);

            // Configura la RecyclerView orizzontale per visualizzare le immagini
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            holder.photosRecyclerView.setLayoutManager(layoutManager);

            // Crea un adapter per la RecyclerView delle immagini
            PhotosAdapter photosAdapter = new PhotosAdapter(context, requestMember.getPhotoUrls());
            holder.photosRecyclerView.setAdapter(photosAdapter);

            /*
             * Rendiamo cliccabile ogni foto presente nelle richieste
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
        String reqkey = requestMemberArrayList.get(position).getKey(); //Il campo key è l'id della richiesta
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        userRef = firebaseFirestore.collection("user").document(currentId).collection("Requests").document(reqkey);
        reqRef = firebaseFirestore.collection("AllRequests").document(reqkey);

        //If per far apparire il bottone di eliminazione solo sui post dell'utente corretto

        if (currentId.equals(requestMemberArrayList.get(position).getId())) {
            holder.deleteRequest.setVisibility(View.VISIBLE);
        } else {
            holder.deleteRequest.setVisibility(View.GONE);
        }

        Picasso.get().load(requestMember.url).into(holder.imageView);
        holder.time_result.setText(requestMember.time);
        holder.name_result.setText(requestMember.name);
        holder.surname_result.setText(requestMember.surname);
        holder.company_name_result.setText(requestMember.company_name);
        holder.request_result.setText(requestMember.request);

        final String userId = requestMemberArrayList.get(position).getId();
        final String request = requestMemberArrayList.get(position).getRequest();
        final String key = requestMemberArrayList.get(position).getKey();
        final ArrayList<String> photoUrlsForReply = requestMemberArrayList.get(position).getPhotoUrls();

        /*
         * Al tasto reply impostiamo un onClickListener e passeremo alcuni parametri essenziali
         * per il funzionamento di reply
         */
        holder.reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent((Activity) v.getContext(), ActivityReply.class);
                intent.putExtra("uid", userId);
                intent.putExtra("request", request);
                intent.putExtra("key", key);
                intent.putExtra("photoUrls", photoUrlsForReply);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);
            }
        });

        /*
         * Al click del tasto deleteRequest verrà prima aperto una finestra di allerta che chiederà se confermare o no
         *  l'eliminazione della richiesta, dopo di che andrà a prendere tutti i dati necessari per eliminarla dal databse in tutte le
         * collezioni in cui appare; inoltre viene effettuato anche un ciclo per prendere gli url delle foto caricate ed eliminarle.
         */

        holder.deleteRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Conferma eliminazione");
                builder.setMessage("Sei sicuro di voler eliminare questa richiesta?");
                builder.setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Azioni da eseguire se l'utente conferma l'eliminazione
                        for (String url: photoUrlsForReply) {
                            //Foreach per prendere gli url delle foto ed eliminarle
                            storage.getReferenceFromUrl(url).delete();
                        }
                        reqRef.delete();
                        userRef.delete();
                        Toast.makeText(context, "Richiesta eliminata con successo", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Annulla", null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return requestMemberArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView time_result, name_result, surname_result, company_name_result, request_result;
        RecyclerView photosRecyclerView;
        Button reply, deleteRequest;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_request_item);
            time_result = itemView.findViewById(R.id.time_request_item_tv);
            name_result = itemView.findViewById(R.id.name_request_item_tv);
            surname_result = itemView.findViewById(R.id.surname_request_item_tv);
            company_name_result = itemView.findViewById(R.id.company_name_request_item_tv);
            request_result = itemView.findViewById(R.id.request_item_tv);
            photosRecyclerView = itemView.findViewById(R.id.photosRecyclerView);
            reply = itemView.findViewById(R.id.reply_request_item);
            deleteRequest = itemView.findViewById(R.id.delete_request);
        }
    }


}
