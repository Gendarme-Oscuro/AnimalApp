package it.uniba.dib.sms222321;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/*
Classe per il funzionamento del ViewHolder per mostrare tutte le risposte dei post
 */


public class ViewHolderRisposte extends RecyclerView.Adapter<ViewHolderRisposte.MyViewHolder>{

    Context context;
    ArrayList<AnswerMember> answerMemberArrayList;

    public ViewHolderRisposte(Context context, ArrayList<AnswerMember> answerMemberArrayList) {
        this.context = context;
        this.answerMemberArrayList = answerMemberArrayList;
    }

    @NonNull
    @Override
    public ViewHolderRisposte.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.answer_layout, parent, false);
        return new MyViewHolder(v);
    }

    /*
    In questo metodo andiamo a prendere dalla classe di supporto AnswerMember le informazioni che ci servono
     */

    @Override
    public void onBindViewHolder(@NonNull ViewHolderRisposte.MyViewHolder holder, int position) {

        AnswerMember answerMember = answerMemberArrayList.get(position);

        Picasso.get().load(answerMember.url).into(holder.imageViewAnswer);
        holder.name_result.setText(answerMember.name);
        holder.surname_result.setText(answerMember.surname);
        holder.company_name_result.setText(answerMember.company_name);
        holder.time_result.setText(answerMember.time);
        holder.answer_result.setText(answerMember.answer);

    }

    @Override
    public int getItemCount() {
        return answerMemberArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewAnswer;
        TextView time_result, name_result, surname_result, company_name_result, answer_result;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAnswer = itemView.findViewById(R.id.imageViewAnswer);
            time_result = itemView.findViewById(R.id.time_answer_tv);
            name_result = itemView.findViewById(R.id.name_answer_tv);
            surname_result = itemView.findViewById(R.id.surname_answer_tv);
            company_name_result = itemView.findViewById(R.id.company_name_answer_tv);
            answer_result = itemView.findViewById(R.id.answer_tv);
        }
    }
}
