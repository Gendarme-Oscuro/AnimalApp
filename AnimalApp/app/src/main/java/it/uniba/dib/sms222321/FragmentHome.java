package it.uniba.dib.sms222321;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


import androidx.annotation.Nullable;

import com.google.firebase.firestore.EventListener;


public class FragmentHome extends Fragment {

    private static final String TAG = "HomeFragment";
    private Button addButton;
    private AnimalAdapter adapter;

    private DocumentReference documentReference;
    private FirebaseFirestore firestore;
    private FirebaseFirestore firestore2;
    private List<Animal> animalList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        addButton = view.findViewById(R.id.addAnimal);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateAnimal.class);
            startActivity(intent);
        });

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        firestore2 = FirebaseFirestore.getInstance();

        // Initialize animalList
        animalList = new ArrayList<>();

        // Initialize adapter
        adapter = new AnimalAdapter(getContext(), animalList);

        // Set adapter to ListView
        ListView listView = view.findViewById(R.id.listViewAnimals);
        listView.setAdapter(adapter);

        // Get the logged-in user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set up Firestore listener
        firestore.collection("user")
                .document(userId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            // Retrieve the All_User_Member object
                            All_User_Member user = snapshot.toObject(All_User_Member.class);

                            if (user != null) {
                                // Clear animalList before updating with new data
                                animalList.clear();

                                // Add the user's pets to the animalList
                                if (user.getPets() != null) {
                                    for (String petName : user.getPets()) {
                                        getImageUrl(userId, petName, imageUrl -> {
                                            Animal animal = new Animal();
                                            animal.setName(petName);
                                            animal.setUrl(imageUrl);
                                            animalList.add(animal);
                                            adapter.notifyDataSetChanged();
                                        });
                                    }
                                }
                            }
                        }
                    }
                });

        return view;
    }



    /*
    method used to get the imageUrl inside the listener after getting the reference on db
     */
    private void getImageUrl(String userId, String name, ImageUrlCallback callback) {
        firestore2.collection("animals")
                .whereEqualTo("owner", userId)
                .whereEqualTo("name", name)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String imageUrl = document.getString("url");

                                //calback on the url
                                callback.onImageUrlReceived(imageUrl);
                                return; // Stop after finding the first matching document
                            }
                        } else {
                            callback.onImageUrlReceived(null); // No document matches the specified userId and name
                        }
                    } else {
                        callback.onImageUrlReceived(null); // An error occurred while retrieving the documents
                    }
                });
    }

    /*
    method used to do the callback
     */
    interface ImageUrlCallback {
        void onImageUrlReceived(String imageUrl);
    }
}
