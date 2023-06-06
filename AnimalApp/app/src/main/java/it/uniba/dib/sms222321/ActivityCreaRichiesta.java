package it.uniba.dib.sms222321;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ActivityCreaRichiesta extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST_CODE = 1;

    EditText editText;
    Button button, btnSelezionaImmagini;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RequestMember member;
    String url, name, surname, company_name, uid;
    ArrayList<String> photoUrls;
    RecyclerView photosRecyclerView;
    FirebaseStorage storage;
    StorageReference storageRef;

    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private boolean isReadPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_richiesta);

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
        photosRecyclerView = findViewById(R.id.photosRecyclerView);

        CollectionReference allRequests = db.collection("AllRequests");
        DocumentReference utente = db.collection("user").document(currentId);

        member = new RequestMember();
        photoUrls = new ArrayList<>();

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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String request = editText.getText().toString();

                Calendar cdate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                final String savedate = currentDate.format(cdate.getTime());

                Calendar ctime = Calendar.getInstance();
                SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss");
                final String savetime = currenttime.format(ctime.getTime());

                String time = savedate + ":" + savetime;

                utente.get()
                        .addOnCompleteListener((task -> {
                            if (task.isSuccessful() && task.getResult() != null) {

                                url = task.getResult().getString("url");
                                name = task.getResult().getString("name");
                                surname = task.getResult().getString("surname");
                                company_name = task.getResult().getString("company name");
                                uid = task.getResult().getString("currentUserId");

                                if (request != null) {
                                    member.setUrl(url);
                                    member.setName(name);
                                    member.setSurname(surname);
                                    member.setCompany_name(company_name);
                                    member.setTime(time);
                                    member.setId(uid);
                                    member.setRequest(request);
                                    member.setPhotoUrls(photoUrls);

                                    utente.collection("Requests").add(member)
                                            .addOnSuccessListener(documentReference -> {
                                                String id = documentReference.getId();
                                                allRequests.document(id).set(member)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(ActivityCreaRichiesta.this, "Inviato", Toast.LENGTH_SHORT).show();
                                                            redirectActivity(ActivityCreaRichiesta.this, ActivityRichieste.class);
                                                        })
                                                        .addOnFailureListener(e -> Toast.makeText(ActivityCreaRichiesta.this, "Errore", Toast.LENGTH_SHORT).show());
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(ActivityCreaRichiesta.this, "Errore", Toast.LENGTH_SHORT).show());
                                } else {
                                    Toast.makeText(ActivityCreaRichiesta.this, "Per favore, inserisci una richiesta", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ActivityCreaRichiesta.this, "Errore", Toast.LENGTH_SHORT).show();
                            }
                        }));
            }
        });
    }

    private void selectImagesFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleziona immagini"), PICK_IMAGES_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    uploadImage(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                uploadImage(imageUri);
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        StorageReference fileRef = storageRef.child("requests_images/" + System.currentTimeMillis() + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        photoUrls.add(imageUrl);
                    });
                    Toast.makeText(ActivityCreaRichiesta.this, "Immagine caricata con successo", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(ActivityCreaRichiesta.this, "Errore nel caricamento dell'immagine", Toast.LENGTH_SHORT).show());
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

    private void requestPermission(){
        isReadPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        List<String> permessionRequest = new ArrayList<String>();

        if(!isReadPermissionGranted){
            permessionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permessionRequest.isEmpty()){
            mPermissionResultLauncher.launch(permessionRequest.toArray(new String[0]));
        }

    }

}
