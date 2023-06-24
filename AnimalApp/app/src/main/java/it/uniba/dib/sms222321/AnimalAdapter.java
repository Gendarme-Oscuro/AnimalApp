package it.uniba.dib.sms222321;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/*
adapter used to manage the actual view of owned animal  interface for each profile
 */
public class AnimalAdapter extends ArrayAdapter<Animal> {
    private Context context;

    private FirebaseFirestore db;


    /*
    method used to retrieve the right AnimalList in FragmentHome
     */
    public AnimalAdapter(Context context, List<Animal> animals) {
        super(context, 0, animals);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        db = FirebaseFirestore.getInstance();
        final String[] animalId = new String[1];

        View itemView = convertView;

        // Controlla se l'elemento dell'elenco è nullo, nel qual caso crea una nuova vista
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_animal, parent, false);
        }

        Button profileButton = itemView.findViewById(R.id.profileAnimal);
        ImageView imageView = itemView.findViewById(R.id.imageViewAnimal);

        Animal animal = getItem(position);

        if (animal != null) {
            String imageUrl = animal.getUrl();

            // Carica l'immagine dell'animale nell'ImageView utilizzando Picasso
            Picasso.get()
                    .load(imageUrl)
                    .into(imageView);
        }

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Avvia l'attività AnimalProfile
                if (animal != null) {
                    String name = animal.getName();
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    // Ottieni l'ID dell'animale
                    getAnimalId(userId, name, new AnimalIdCallback() {
                        @Override
                        public void onAnimalId(String animalId) {
                            if (animalId != null) {
                                Intent intent = new Intent(context, AnimalProfile.class);
                                intent.putExtra("animalId", animalId);
                                context.startActivity(intent);
                            } else {
                                Toast.makeText(context, R.string.failed_to_retrieve_animal_id, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        return itemView;
    }

    // Metodo per ottenere l'ID dell'animale
    private void getAnimalId(String userId, String name, AnimalIdCallback callback) {
        db.collection("animals")
                .whereEqualTo("owner", userId)
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String animalId = document.getId();
                            callback.onAnimalId(animalId);
                        } else {
                            callback.onAnimalId(null); // Nessun documento corrispondente trovato
                        }
                    } else {
                        callback.onAnimalId(null); // Si è verificato un errore durante la query a Firestore
                    }
                });
    }

    // Interfaccia di callback per l'ID dell'animale
    interface AnimalIdCallback {
        void onAnimalId(String animalId);
    }
}
