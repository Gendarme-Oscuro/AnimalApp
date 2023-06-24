package it.uniba.dib.sms222321;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

/*
 * Classe adibita alla creazione della risposta
 */
public class ActivityAnswer extends AppCompatActivity {

    String uid, key, request;
    EditText editText;
    Button button;
    AnswerMember member;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userid = user.getUid();
    DocumentReference AllRequests, userDocument;
    CollectionReference collectionReference;
    String name, surname, company_name, url, time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);

        member = new AnswerMember(); // Crea un'istanza per la risposta
        editText = findViewById(R.id.editRisposta);
        button = findViewById(R.id.btnInviaRisposta);

        /*
         * Ottenimento dei parametri passati da ActivityReply
         */
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            uid = bundle.getString("uid");
            key = bundle.getString("key");
            request = bundle.getString("request");
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }

        /*
         * Salviamo la richiesta e prima di tornare all'activity precedente
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
                onBackPressed();
            }
        });
    }

    /*
     * Metodo adibito al salvataggio della risposta che verrà salvata
     * nell'apposita collezione Answer all'interno della richiesta presente in AllRequests
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

            collectionReference = database.collection("AllRequests");
            AllRequests = collectionReference.document(key);
            AllRequests.collection("Answer").add(member);


            Toast.makeText(this, R.string.inviato, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, R.string.per_favore_inserisci_una_risposta, Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Ottenimento delle informazini di chi crea la risposta
     */
    @Override
    protected void onStart() {
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
                            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, R.string.error_retrieving_data, Toast.LENGTH_SHORT).show();
        }
    }
}