package it.uniba.dib.sms222321;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class FragmentAnswerSegnalazioni extends Fragment {

    String uid, key, segnalazione;
    EditText editText;
    Button button;
    AnswerMember member;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userid = user.getUid();
    DocumentReference AllSegnalazioni, userDocument;
    CollectionReference collectionReference;
    String name, surname, company_name, url, time;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_answer_segnalazioni, container, false);

        member = new AnswerMember(); // Crea un'istanza per la risposta
        editText = rootView.findViewById(R.id.editRispostaSegnalazione);
        button = rootView.findViewById(R.id.btnInviaRispostaSegnalazione);

        Bundle args = getArguments();
        if (args != null) {
            uid = args.getString("userId");
            key = args.getString("key");
            segnalazione = args.getString("segnalazione");
        }

        /*
         * Salviamo la segnalazione e prima di tornare al fragment precedente
         * impostiamo una pausa di mezzo secondo per dare il tempo
         * di essere caricata correttamente
         */
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAnswer();
                try {
                    Thread.sleep(500); // Pausa di mezzo secondo
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return rootView;
    }

    /*
     * Metodo adibito al salvataggio della risposta che verrà salvata
     * nell'apposita collezione Answer all'interno della segnalazione presente in AllSegnalazioni
     */
    void saveAnswer(){
        String answer = editText.getText().toString();
        if (answer != null) {
            Calendar cdate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String savedate = currentDate.format(cdate.getTime());

            Calendar ctime = Calendar.getInstance();
            SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss");
            final String savetime = currenttime.format(ctime.getTime());

            time = savedate + ":" + savetime;

            member.setAnswer(answer);
            member.setTime(time);
            member.setName(name);
            member.setSurname(surname);
            member.setCompany_name(company_name);
            member.setUrl(url);
            member.setUid(userid);

            collectionReference = database.collection("AllSegnalazioni");
            AllSegnalazioni = collectionReference.document(key);
            AllSegnalazioni.collection("Answer").add(member);

            Toast.makeText(requireContext(), "Inviato", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(requireContext(), "Per favore, inserisci una risposta", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Ottenimento delle informazini di chi crea la risposta
     */
    @Override
    public void onStart() {
        super.onStart();

        userDocument = database.collection("user").document(userid);

        if (userDocument != null) {
            userDocument.get()
                    .addOnCompleteListener(task -> {
                        if (task.getResult() != null && task.getResult().exists()) {

                            url = task.getResult().getString("url");
                            name = task.getResult().getString("name");
                            surname = task.getResult().getString("surname");
                            company_name = task.getResult().getString("company name");

                        } else {
                            Toast.makeText(requireContext(), "error", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(requireContext(), "Error retrieving data", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleOnBackPressed() {
        //Il metodo popBackStack() del FragmentManager permette di ritornare al fragment precedente
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    // Aggiungi un metodo newInstance con i parametri corretti
    public static FragmentAnswerSegnalazioni newInstance(String userId,  String key, String segnalazione) {
        FragmentAnswerSegnalazioni fragment = new FragmentAnswerSegnalazioni();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("key", key);
        args.putString("segnalazione", segnalazione);
        fragment.setArguments(args);
        return fragment;
    }
}