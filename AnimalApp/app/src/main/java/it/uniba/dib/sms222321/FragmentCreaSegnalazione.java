package it.uniba.dib.sms222321;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

public class FragmentCreaSegnalazione extends Fragment implements PhotosAdapter.OnImageClickListener {

    private static final int PICK_IMAGES_REQUEST_CODE = 1;

    EditText editText;
    Button button, btnSelezionaImmagini, btnCancellaImmagini, btnLocation;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SegnalazioneMember member;
    String url, name, surname, company_name, uid, latitude, longitude;
    ArrayList<String> photoUrls;
    RecyclerView photosRecyclerView;
    FirebaseStorage storage;
    StorageReference storageRef;
    PhotosAdapter photosAdapter;

    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private boolean isReadPermissionGranted = false;
    private boolean isFineLocationPermissionGranted = false;
    private boolean isCoarseLocationPermissionGranted = false;


    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private ActivityResultLauncher<Intent> pickImagesLauncher;

    List<Uri> selectedImages; // Lista che serve a salvare le immagini selezionate

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_crea_segnalazione, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        /*
         * Controlliamo i permessi in lettura dei file per poter accedere alle immagini
         * dal file manager
         */
        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if (result.get(Manifest.permission.READ_EXTERNAL_STORAGE) != null) {
                    isReadPermissionGranted = Boolean.TRUE.equals(result.get(Manifest.permission.READ_EXTERNAL_STORAGE));
                    if (isReadPermissionGranted) {
                        selectImagesFromGallery();
                    }
                }
                if (result.get(Manifest.permission.ACCESS_FINE_LOCATION) != null) {
                    isFineLocationPermissionGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    if (isFineLocationPermissionGranted) {
                        isCoarseLocationPermissionGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                        if (isCoarseLocationPermissionGranted) {
                            getLastLocation();
                        }
                    }
                }
            }
        });

        pickImagesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                handleImageSelectionResult(result.getData());
            }
        });

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentId = user.getUid();
        editText = rootView.findViewById(R.id.editDomandaSegnalazione);
        button = rootView.findViewById(R.id.btnInviaSegnalazione);
        btnSelezionaImmagini = rootView.findViewById(R.id.btnSelezionaImmaginiSegnalazione);
        btnCancellaImmagini = rootView.findViewById(R.id.deleteAllImagesSegnalazione);
        btnLocation = rootView.findViewById(R.id.btnLocation);
        photosRecyclerView = rootView.findViewById(R.id.photosRecyclerViewSegnalazioni);

        /*
         * Usiamo due collezioni per poter creare le segnalazioni in AllSegnalazioni
         * e le segnalazioni del singolo utente compariranno in una collection
         * interna all'utente
         */
        CollectionReference allSegnalazioni = db.collection("AllSegnalazioni");
        DocumentReference utente = db.collection("user").document(currentId);

        member = new SegnalazioneMember();   // Inizializza un nuovo oggetto SegnalazioneMember
        photoUrls = new ArrayList<>();       // Inizializza la lista delle URL delle immagini
        selectedImages = new ArrayList<>();  // Inizializza la lista delle immagini selezionate

        btnSelezionaImmagini.setOnClickListener(v -> {
            if (isReadPermissionGranted) {
                selectImagesFromGallery();
            } else {
                requestPermission();
            }
        });

        btnCancellaImmagini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoUrls.clear();
                selectedImages.clear();
                photosAdapter.notifyDataSetChanged();
                btnCancellaImmagini.setVisibility(View.GONE);
            }
        });

        btnLocation.setOnClickListener(vu -> {
            getLastLocation();
        });


        photosAdapter = new PhotosAdapter(requireContext(), photoUrls); // Inizializza l'adapter delle immagini
        photosAdapter.setOnImageClickListener(this);

        photosRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        photosRecyclerView.setAdapter(photosAdapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String question = editText.getText().toString();

                //Salviamo data e ora
                Calendar cdate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                final String savedate = currentDate.format(cdate.getTime());

                Calendar ctime = Calendar.getInstance();
                SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss");
                final String savetime = currenttime.format(ctime.getTime());

                String time = savedate + ":" + savetime;

                utente.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Ottieni il nome dell'utente dal documento
                            uid = currentId;                      // Recupera l'UID dell'utente
                            name = document.getString("name");
                            surname = document.getString("surname");
                            company_name = document.getString("company name");
                            url = document.getString("url");     // Recupera l'URL dell'immagine profilo dell'utente

                            // Aggiungi le informazioni dell'utente all'oggetto SegnalazioneMember
                            if (question != null && !question.isEmpty()) {
                                member.setUrl(url);
                                member.setName(name);
                                member.setSurname(surname);
                                member.setCompany_name(company_name);
                                member.setId(uid);
                                member.setTime(time);
                                member.setSegnalazione(question);
                                member.setPhotoUrls(photoUrls);

                                // Aggiungi la segnalazione all'utente corrente nel Firestore
                                utente.collection("Segnalazioni").add(member)
                                        .addOnSuccessListener(documentReference -> {
                                            String id_key = documentReference.getId();
                                            member.setKey(id_key);
                                            documentReference.update("key", id_key);

                                            // Utilizza le coordinate per le tue operazioni
                                            saveLocation(id_key);

                                            // Update the photoUrls field in Firestore
                                            documentReference.update("photoUrls", photoUrls).addOnCompleteListener(aVoid -> {
                                                // Crea una nuova segnalazione nel Firestore
                                                allSegnalazioni.document(id_key).set(member)
                                                        .addOnSuccessListener(bVoid -> {

                                                            /*
                                                             * Controlliamo se sono state selezionate delle immagini,
                                                             * in caso contrario impostiamo un attesa di mezzo secondo per permettere alla segnalazione
                                                             * di essere caricata e visualizzata correttamente in FragmentSegnalazioni
                                                             */
                                                            if(photoUrls.isEmpty()) {
                                                                try {
                                                                    Thread.sleep(500);
                                                                } catch (InterruptedException e) {
                                                                    throw new RuntimeException(e);
                                                                }
                                                            }
                                                            Toast.makeText(requireContext(), "Segnalazione inviata", Toast.LENGTH_SHORT).show();
                                                            requireActivity().getSupportFragmentManager().popBackStack();

                                                        }).addOnFailureListener(e -> Toast.makeText(requireActivity(), "Errore nell'invio della segnalazione", Toast.LENGTH_SHORT).show());
                                            });

                                        }).addOnFailureListener(e -> Toast.makeText(requireActivity(), "Errore nell'invio della segnalazione", Toast.LENGTH_SHORT).show());
                            }

                        }
                    }
                });

                if (question.trim().isEmpty()) {
                    Toast.makeText(requireActivity(), "Inserisci una domanda", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        return rootView;
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    // Hai ottenuto la posizione attuale
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    member.setLatitude(String.valueOf(latitude));
                    member.setLongitude(String.valueOf(longitude));
                    Toast.makeText(getContext(), "Coordinate ottenute correttamente", Toast.LENGTH_SHORT).show();

                } else {
                    // La posizione non è disponibile
                    Toast.makeText(requireContext(), "Posizione non disponibile", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                // Errore durante l'ottenimento della posizione
                Toast.makeText(requireContext(), "Errore nell'ottenimento della posizione", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void requestPermission() {
        mPermissionResultLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
        mPermissionResultLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        mPermissionResultLauncher.launch(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    private void selectImagesFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        pickImagesLauncher.launch(intent);
    }

    public void saveLocation(String idKey) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentId = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference utente = db.collection("user").document(currentId);
        CollectionReference allSegnalazioni = db.collection("AllSegnalazioni");

        utente.update("latitude", member.getLatitude(), "longitude", member.getLongitude())
                .addOnSuccessListener(aVoid -> {
                    // Le coordinate sono state salvate con successo
                })
                .addOnFailureListener(bVoid -> {
                    // Si è verificato un errore durante il salvataggio delle coordinate
                });

        allSegnalazioni.document(idKey).update("latitude", member.getLatitude(), "longitude", member.getLongitude())
                .addOnSuccessListener(b -> {
                })
                .addOnFailureListener(e -> {
                    // Si è verificato un errore durante il salvataggio delle coordinate
                });
    }


    private void handleImageSelectionResult(Intent data) {
        if (data.getClipData() != null) {
            // Multiple images selected
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                selectedImages.add(imageUri);
            }
        } else if (data.getData() != null) {
            // Single image selected
            Uri imageUri = data.getData();
            selectedImages.add(imageUri);
        }

        // Save the selected images to storage
        saveImagesToStorage(selectedImages);
        photoUrls.clear();
    }

    private void saveImagesToStorage(List<Uri> images) {
        for (Uri imageUri : images) {
            String imageName = System.currentTimeMillis() + ".jpg"; // Genera un nome univoco per l'immagine
            StorageReference imageRef = storageRef.child("segnalazioni_images/" + imageName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Ottieni l'URL dell'immagine caricata
                        Task<Uri> downloadUrlTask = imageRef.getDownloadUrl();
                        downloadUrlTask.addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            // Verifica se l'URL dell'immagine è già presente nella lista photoUrls
                            if (!photoUrls.contains(imageUrl)) {
                                photoUrls.add(imageUrl);
                                photosAdapter.notifyDataSetChanged();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Gestisci eventuali errori durante il caricamento dell'immagine
                        Toast.makeText(requireContext(), "Errore nel caricamento delle immagini", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public void handleOnBackPressed() {
        //Il metodo popBackStack() del FragmentManager permette di ritornare al fragment precedente
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    /**
     * Al click sull'immagine passiamo imageUrl come parametro nell'intent per poter
     * essere utilizzata nell'activity corrispondente
     * @param imageUrl
     */
    @Override
    public void onImageClick(String imageUrl) {
        Intent intent = new Intent(getContext(), ImmagineIngranditaActivity.class);
        intent.putExtra("image_url", imageUrl);
        startActivity(intent);
    }
}
