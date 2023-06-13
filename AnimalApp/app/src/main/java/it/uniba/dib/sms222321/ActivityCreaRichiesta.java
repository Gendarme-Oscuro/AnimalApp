package it.uniba.dib.sms222321;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ActivityCreaRichiesta extends AppCompatActivity implements PhotosAdapter.OnImageClickListener {

    private static final int PICK_IMAGES_REQUEST_CODE = 1;

    EditText editText;
    Button button, btnSelezionaImmagini, deleteAllImages;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RequestMember member;
    String url, name, surname, company_name, uid;
    ArrayList<String> photoUrls;
    RecyclerView photosRecyclerView;
    FirebaseStorage storage;
    StorageReference storageRef;
    PhotosAdapter photosAdapter;

    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private boolean isReadPermissionGranted = false;

    List<Uri> selectedImages; // Aggiunto: per salvare le immagini selezionate

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_richiesta);

        /*
         * Controlliamo i permessi in lettura dei file per poter accedere alle imamgini
         * dal file manager
         */
        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if(result.get(Manifest.permission.READ_EXTERNAL_STORAGE) != null){
                    isReadPermissionGranted = Boolean.TRUE.equals(result.get(Manifest.permission.READ_EXTERNAL_STORAGE));
                    if(isReadPermissionGranted){
                        selectImagesFromGallery();
                    }
                }
            }
        });

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentId = user.getUid();
        editText = findViewById(R.id.editDomanda);
        button = findViewById(R.id.btnInvia);
        btnSelezionaImmagini = findViewById(R.id.btnSelezionaImmagini);
        deleteAllImages = findViewById(R.id.deleteAllImages);
        photosRecyclerView = findViewById(R.id.photosRecyclerView);

        /*
         * Usiamo due collezioni per poter creare le richieste in AllRequests
         * e le richieste del singolo utente compariranno in un collection
         * interna all'utente
         */
        CollectionReference allRequests = db.collection("AllRequests");
        DocumentReference utente = db.collection("user").document(currentId);

        member = new RequestMember();       // Imizializza un istanza per le richieste
        photoUrls = new ArrayList<>();      // Inizializza l'array delle immagini da mostrare a schermo e caricare su firebase
        selectedImages = new ArrayList<>(); // Inizializza l'elenco delle immagini selezionate

        btnSelezionaImmagini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReadPermissionGranted == false){
                    requestPermission();
                }else {
                    selectImagesFromGallery();
                }
            }
        });

        /*
         * Passiamo le foto selezionate dall'utente per poterle mostrare con
         * PhotosAdapter, permettendo anche un click su di esso.
         * Impostiamo il layout orizzontale per lo scorrimento delle immagini
         * con la recyclerView
         */
        photosAdapter = new PhotosAdapter(ActivityCreaRichiesta.this, photoUrls);
        photosAdapter.setOnImageClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ActivityCreaRichiesta.this, LinearLayoutManager.HORIZONTAL, false);
        photosRecyclerView.setLayoutManager(layoutManager);
        photosRecyclerView.setAdapter(photosAdapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * Salviamo data e ora
                 */
                String request = editText.getText().toString();

                Calendar cdate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                final String savedate = currentDate.format(cdate.getTime());

                Calendar ctime = Calendar.getInstance();
                SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss");
                final String savetime = currenttime.format(ctime.getTime());

                String time = savedate + ":" + savetime;
                photoUrls.clear();

                utente.get()
                        .addOnCompleteListener((task -> {
                            if (task.isSuccessful() && task.getResult() != null) {

                                url = task.getResult().getString("url");
                                name = task.getResult().getString("name");
                                surname = task.getResult().getString("surname");
                                company_name = task.getResult().getString("company name");
                                uid = task.getResult().getId();

                                if (request != null) {
                                    member.setUrl(url);
                                    member.setName(name);
                                    member.setSurname(surname);
                                    member.setCompany_name(company_name);
                                    member.setTime(time);
                                    member.setId(uid);
                                    member.setRequest(request);
                                    member.setPhotoUrls(photoUrls);

                                    /*
                                     * Aggiungiamo la richiesta al singolo utente e impostiamo la key autogenerata
                                     * e con questa aggiungiamo come chiave dell'istanza in AllRequests
                                     */
                                    utente.collection("Requests").add(member)
                                            .addOnSuccessListener(documentReference -> {
                                                String id_key = documentReference.getId();
                                                member.setKey(id_key);
                                                documentReference.update("key", id_key);
                                                allRequests.document(id_key).set(member)
                                                        .addOnSuccessListener(aVoid -> {
                                                            uploadImagesToDatabase(id_key); // Carica le immagini nel database
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(ActivityCreaRichiesta.this, "Errore", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(ActivityCreaRichiesta.this, "Errore", Toast.LENGTH_SHORT).show();
                                                finish();
                                            });
                                } else {
                                    Toast.makeText(ActivityCreaRichiesta.this, "Per favore, inserisci una richiesta", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ActivityCreaRichiesta.this, "Errore", Toast.LENGTH_SHORT).show();
                            }
                            /*
                             * Controlliamo se sono state selezionate delle immagini,
                             * in caso contrario impostiamo un attesa di mezzo secondo per permettere alla richiesta
                             * di essere caricata e visualizzata correttamente in ActivityRichieste
                             */
                            if(photoUrls.isEmpty()) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                Toast.makeText(ActivityCreaRichiesta.this, "Richiesta inviata", Toast.LENGTH_SHORT).show();
                                redirectActivity(ActivityCreaRichiesta.this, ActivityRichieste.class);
                            }
                        }));
            }
        });

        /*
         * Tramite la pressione del cestino, permettiamo di cancellare tutte le immagini
         * che sono state selezionate in precedenza
         */
        deleteAllImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoUrls.clear();
                selectedImages.clear();
                photosAdapter.notifyDataSetChanged();
                deleteAllImages.setVisibility(View.GONE);
            }
        });
    }

    private void requestPermission() {
        mPermissionResultLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
    }

    /*
     * Permettiamo la selezione multipla delle immagini dal file manager
     */
    private void selectImagesFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGES_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImages.add(imageUri); // Salva l'URI dell'immagine selezionata
                    photoUrls.add(imageUri.toString());
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                selectedImages.add(imageUri); // Salva l'URI dell'immagine selezionata
                photoUrls.add(imageUri.toString());
            }
            deleteAllImages.setVisibility(View.VISIBLE);
            photosAdapter.notifyDataSetChanged();
        }
    }

    private void uploadImagesToDatabase(String requestId) {
        for (Uri imageUri : selectedImages) {
            StorageReference fileRef = storageRef.child("requests_images/" + System.currentTimeMillis() + ".jpg");

            final RequestMember imageMember = new RequestMember(); // Crea una nuova istanza di RequestMember per ogni immagine
            imageMember.setKey(requestId); // Imposta la chiave generata come ID della richiesta

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            photoUrls.add(imageUrl);
                            imageMember.setPhotoUrls(photoUrls); // Aggiorna l'elenco di URL delle immagini nell'istanza di RequestMember

                            // Aggiorna il documento nel database con l'istanza separata di RequestMember
                            updateRequestWithImage(requestId, imageMember);
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(ActivityCreaRichiesta.this, "Errore nel caricamento dell'immagine", Toast.LENGTH_SHORT).show());
        }
    }

    /*
     * Aggiunge le immagini al database nel campo photoUrls
     * @param requestId
     * @param imageMember
     */
    private void updateRequestWithImage(String requestId, RequestMember imageMember) {
        DocumentReference requestRef = db.collection("AllRequests").document(requestId);
        requestRef.update("photoUrls", imageMember.getPhotoUrls())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ActivityCreaRichiesta.this, "Richiesta inviata", Toast.LENGTH_SHORT).show();
                    redirectActivity(ActivityCreaRichiesta.this, ActivityRichieste.class);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ActivityCreaRichiesta.this, "Errore nell'aggiornamento della richiesta", Toast.LENGTH_SHORT).show();
                    redirectActivity(ActivityCreaRichiesta.this, ActivityRichieste.class);
                });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = user.getUid();

        DocumentReference userRequestsRef = db.collection("user").document(currentUserId).collection("Requests").document(requestId);
        userRequestsRef.update("photoUrls", imageMember.getPhotoUrls())
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ActivityCreaRichiesta.this, "Errore nell'aggiornamento della richiesta", Toast.LENGTH_SHORT).show();
                    redirectActivity(ActivityCreaRichiesta.this, ActivityRichieste.class);
                });
    }

    public static void redirectActivity(Activity activity, Class secondActivity) {
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public void onBackPressed() {
        redirectActivity(ActivityCreaRichiesta.this, ActivityRichieste.class);
    }

    /*
     * Al click sull'immagine passiamo imageUrl come parametro nell'intent per poter
     * essere utilizzata nell'activity corrispondente
     * @param imageUrl
     */
    @Override
    public void onImageClick(String imageUrl) {
        Intent intent = new Intent(this, ImmagineIngranditaActivity.class);
        intent.putExtra("image_url", imageUrl);
        startActivity(intent);
    }
}
