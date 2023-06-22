package it.uniba.dib.sms222321;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PokedexAdapter extends ArrayAdapter<Animal> {

    private Context context;

    private FirebaseFirestore db;



    public PokedexAdapter(Context context, List<Animal> animals) {
        super(context, 0, animals);
        this.context = context;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        db = FirebaseFirestore.getInstance();

        final String[] animalId = new String[1];


        View itemView = convertView;

        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.list_dex, parent, false);
        }

        Button profileButton = itemView.findViewById(R.id.profileAnimal);
        ImageView imageView = itemView.findViewById(R.id.imageDex);


        Animal animal = getItem(position);

        if (animal != null) {
            String imageUrl = animal.getUrl();

            Picasso.get()
                    .load(imageUrl)
                    .into(imageView);
        }

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the AnimalProfile activity
                if (animal != null) {
                    String name = animal.getName();
                    String user = animal.getOwner();


                    getAnimalId(user, name, new PokedexAdapter.AnimalIdCallback() {
                        @Override
                        public void onAnimalId(String animalId) {
                            if (animalId != null) {
                                Intent intent = new Intent(context, DexProfile.class);
                                intent.putExtra("animalId", animalId);
                                context.startActivity(intent);
                            } else {
                                Toast.makeText(context, "Failed to retrieve animal ID", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


        return itemView;
    }



    private void getAnimalId(String user, String name, PokedexAdapter.AnimalIdCallback callback) {
        db.collection("animals")
                .whereEqualTo("owner", user)
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
                            callback.onAnimalId(null); // No matching document found
                        }
                    } else {
                        callback.onAnimalId(null); // Error occurred while querying Firestore
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onAnimalId(null); // Error occurred while querying Firestore
                });
    }



    interface AnimalIdCallback {
        void onAnimalId(String animalId);
    }


}
