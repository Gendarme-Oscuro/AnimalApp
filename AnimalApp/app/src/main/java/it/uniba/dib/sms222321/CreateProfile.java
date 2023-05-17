package it.uniba.dib.sms222321;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CreateProfile extends AppCompatActivity {

    TextView textUserType;
    EditText p_name, p_surname, p_company_name, p_age;
    Spinner p_user_type;
    Button button;
    ProgressBar progressBar;
    ImageView imageView;
    Uri imageUri;
    UploadTask uploadTask;
    StorageReference storageReference;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    All_User_Member member;
    String currentUserId;

    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private boolean isReadPermissionGranted = false;

    final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        imageUri = data.getData();
                        imageView.setImageURI(imageUri);
                    } else {
                        Toast.makeText(CreateProfile.this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if(result.get(Manifest.permission.READ_EXTERNAL_STORAGE) != null){
                    isReadPermissionGranted = Boolean.TRUE.equals(result.get(Manifest.permission.READ_EXTERNAL_STORAGE));
                    if(isReadPermissionGranted){
                        launchActivity();
                    }
                }
            }
        });

        member = new All_User_Member();
        imageView = findViewById(R.id.profile_pic);
        p_name = findViewById(R.id.profile_name);
        p_surname = findViewById(R.id.profile_surname);
        p_company_name = findViewById(R.id.profile_company_name);
        p_age = findViewById(R.id.profile_age);
        p_user_type = findViewById(R.id.user_types);
        textUserType = findViewById(R.id.text_select_user_type);

        button = findViewById(R.id.save_profile);
        progressBar = findViewById(R.id.cp_progressbar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user.getUid();
        documentReference = db.collection("user").document(currentUserId);
        storageReference = FirebaseStorage.getInstance().getReference("Profile image");
        databaseReference = database.getReference("All Users");

        p_user_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedUserType = parent.getItemAtPosition(position).toString();

                if (selectedUserType.equals("Ente")) {
                    // Nascondere i campi "p_name", "p_surname" e "p_age"
                    p_name.setVisibility(View.GONE);
                    p_surname.setVisibility(View.GONE);
                    p_age.setVisibility(View.GONE);
                    p_company_name.setVisibility(View.VISIBLE);
                } else {
                    // Mostrare i campi "p_name", "p_surname" e "p_age"
                    p_name.setVisibility(View.VISIBLE);
                    p_surname.setVisibility(View.VISIBLE);
                    p_age.setVisibility(View.VISIBLE);
                    p_company_name.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Non fare nulla se non viene selezionato alcun elemento
            }
        });

        if ((p_name != null && p_surname != null && p_age != null) || p_company_name != null){
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

            documentReference = firebaseFirestore.collection("user").document(currentUserId);

            documentReference.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if(task.getResult().exists()){

                                p_user_type.setVisibility(View.GONE);
                                textUserType.setVisibility(View.GONE);

                                String nameResult = task.getResult().getString("name");
                                String surnameResult = task.getResult().getString("surname");
                                String company_nameResult = task.getResult().getString("company name");
                                String ageResult = task.getResult().getString("age");
                                String url = task.getResult().getString("url");

                                Picasso.get().load(url).into(imageView);
                                p_name.setText(nameResult);
                                p_surname.setText(surnameResult);
                                p_company_name.setText(company_nameResult);
                                p_age.setText(ageResult);

                            }
                        }
                    });
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isReadPermissionGranted == false){
                    requestPermission();
                }else {
                    launchActivity();
                }
            }
        });

    }

    private void launchActivity(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(intent);
    }

    private String getFileExt (Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType((contentResolver.getType(uri)));

    }
    private void uploadData() {
        String name = p_name.getText().toString();
        String surname = p_surname.getText().toString();
        String company_name = p_company_name.getText().toString();
        String age = p_age.getText().toString();
        String userType = p_user_type.getSelectedItem().toString();


        if (imageUri != null){

            if ((!TextUtils.isEmpty(name) || !TextUtils.isEmpty(surname) || !TextUtils.isEmpty(age)) || !TextUtils.isEmpty(company_name)) {

                progressBar.setVisibility(View.VISIBLE);
                final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getFileExt(imageUri));
                uploadTask = reference.putFile(imageUri);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }
                        return reference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();

                            Map<String, String> profile = new HashMap<>();
                            profile.put("name", name);
                            profile.put("surname", surname);
                            profile.put("company name", company_name);
                            profile.put("age", age);
                            profile.put("userType", userType);
                            profile.put("url", downloadUri.toString());

                            member.setName(name);
                            member.setSurname(surname);
                            member.setCompany_name(company_name);
                            member.setAge(age);
                            member.setUserType(userType);
                            member.setUrl(downloadUri.toString());
                            member.setUid(currentUserId);

                            databaseReference.child(currentUserId).setValue(member);
                            documentReference.set(profile)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            progressBar.setVisibility(View.INVISIBLE);
                                            Toast.makeText(CreateProfile.this, "Profile Created", Toast.LENGTH_SHORT).show();

                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                    Intent intent = new Intent(CreateProfile.this, Welcome.class);
                                                    startActivity(intent);

                                                }
                                            }, 1000);
                                        }
                                    });

                        }

                    }
                });

            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "please choose a profile picture before saving", Toast.LENGTH_SHORT).show();
        }


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