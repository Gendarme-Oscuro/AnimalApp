package it.uniba.dib.sms222321;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ViewHolderRichieste extends RecyclerView.Adapter<ViewHolderRichieste.MyViewHolder> {

    Context context;
    ArrayList<RequestMember> requestMemberArrayList;

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
        } else {
            holder.photosRecyclerView.setVisibility(View.GONE);
        }

        Picasso.get().load(requestMember.url).into(holder.imageView);
        holder.time_result.setText(requestMember.time);
        holder.name_result.setText(requestMember.name);
        holder.surname_result.setText(requestMember.surname);
        holder.company_name_result.setText(requestMember.company_name);
        holder.request_result.setText(requestMember.request);
    }

    @Override
    public int getItemCount() {
        return requestMemberArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView time_result, name_result, surname_result, company_name_result, request_result;
        RecyclerView photosRecyclerView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_request_item);
            time_result = itemView.findViewById(R.id.time_request_item_tv);
            name_result = itemView.findViewById(R.id.name_request_item_tv);
            surname_result = itemView.findViewById(R.id.surname_request_item_tv);
            company_name_result = itemView.findViewById(R.id.company_name_request_item_tv);
            request_result = itemView.findViewById(R.id.request_item_tv);
            photosRecyclerView = itemView.findViewById(R.id.photosRecyclerView);
        }
    }
}
