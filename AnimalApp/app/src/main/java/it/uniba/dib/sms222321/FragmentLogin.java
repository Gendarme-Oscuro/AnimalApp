package it.uniba.dib.sms222321;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;
import android.text.TextUtils;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.Objects;

public class FragmentLogin extends Fragment {

    private FirebaseAuth auth;
    private EditText loginEmail, loginPassword;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        auth = FirebaseAuth.getInstance();
        loginEmail = view.findViewById(R.id.login_email);
        loginPassword = view.findViewById(R.id.login_password);
        Button loginButton = view.findViewById(R.id.login_button);
        TextView forgotPassword = view.findViewById(R.id.forgot_password);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString();
                String pass = loginPassword.getText().toString();

                /*
                Controllo per verificare che la password e la mail siano corrispondenti a quelle dell'account
                 */

                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if(!pass.isEmpty()) {
                        auth.signInWithEmailAndPassword(email, pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        Toast.makeText(getContext(), R.string.login_eseguito_con_successo, Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getContext(), Welcome.class));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), R.string.login_fallito, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        loginPassword.setError(getString(R.string.la_password_non_pu_essere_vuota));
                    }
                } else if (email.isEmpty()){
                    loginEmail.setError(getString(R.string.l_email_non_pu_essere_nulla));
                } else {
                    loginEmail.setError(getString(R.string.perfavore_inserisca_una_email_valida));
                }
            }
        });


        /*
        Il metodo serve a modificare la password all'interno della sezione login qualora l'utente
        non ricordasse la password attuale. In questo caso verrà richiesta una mail su cui l'utente
        dovrà inserire la nuova password. Sono presenti dei controlli nel caso in cui la mail inserita
        non fosse valida
         */

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot, null);
                EditText emailBox = dialogView.findViewById(R.id.emailBox);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                //cliccando sull'apposito tasto reset
                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String userEmail = emailBox.getText().toString();

                        // viene richiesto l'indirizzo email su cui ricevere il messaggio per il cambio password
                        if (TextUtils.isEmpty(userEmail) && !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){

                            Toast.makeText(requireContext(), R.string.enter_your_registered_e_mail_id, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        auth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // caso in cui la mail è valida
                                if (task.isSuccessful()){
                                    Toast.makeText(requireContext(), R.string.check_your_email, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                // caso in cui la mail non è valida
                                } else {
                                    Toast.makeText(requireContext(), R.string.unable_to_send_email, Toast.LENGTH_SHORT).show();
                                }

                            }
                        });


                    }
                });

                // premendo sull'apposito tasto cancel torneremo indietro
                dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();

                    }
                });

                if ( dialog.getWindow() != null) {
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable());
                }
                dialog.show();
            }
        });


        return view;

    }
}