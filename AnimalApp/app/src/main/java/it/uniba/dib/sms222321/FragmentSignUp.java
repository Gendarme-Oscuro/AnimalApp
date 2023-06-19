package it.uniba.dib.sms222321;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.Objects;

public class FragmentSignUp extends Fragment {

    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword, signupConfirm;
    private final String emailPattern = "[a-zA-Z\\d._-]+@[a-z]+.+[a-z]+";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        auth = FirebaseAuth.getInstance();
        signupEmail = view.findViewById(R.id.signup_email);
        signupPassword = view.findViewById(R.id.signup_password);
        signupConfirm = view.findViewById(R.id.signup_confirm);
        Button signupButton = view.findViewById(R.id.signup_button);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = signupEmail.getText().toString().trim();
                String pass = signupPassword.getText().toString().trim();
                String conf = signupConfirm.getText().toString().trim();

                /*
                Controlli per assicurare che l'indirizzo mail sia un indirizzo valido e che la password
                rispetti i requisiti
                 */

                if (user.isEmpty()) {
                    signupEmail.setError("L'Email non può essere vuota");
                } else if (!user.matches(emailPattern)) {
                    signupEmail.setError("Inserisci un indirizzo email valido");
                } else if (pass.isEmpty()) {
                    signupPassword.setError("La password non può essere vuota");
                } else if (pass.length() < 6){
                    signupPassword.setError("La password deve contenere almeno 6 caratteri");
                } else if (conf.isEmpty()){
                    signupConfirm.setError("Il campo non può essere vuoto");
                } else if (!pass.equals(conf)){
                    signupConfirm.setError("Le password devono essere identiche");
                } else {
                    auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthUserCollisionException  existEmail) {
                                    Log.e("ERROR_EMAIL","onComplete: exist_email");
                                    Toast.makeText(view.getContext(), "Email gia' presa da un altro utente", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }else if (task.isComplete()) {
                                Toast.makeText(getContext(), "Registrazione avvenuta con successo", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getContext(), CreateProfile.class));
                            } else {
                                Toast.makeText(getContext(),"Registrazione Fallita" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });

        return view;
    }

}