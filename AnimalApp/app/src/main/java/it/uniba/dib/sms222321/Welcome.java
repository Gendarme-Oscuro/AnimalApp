package it.uniba.dib.sms222321;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;

import it.uniba.dib.sms222321.databinding.ActivityWelcomeBinding;


public class Welcome extends AppCompatActivity implements View.OnClickListener{

    Boolean DoublePressToExit = false;
    Toast toast;
    ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new FragmentHome());
        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.home:
                    replaceFragment(new FragmentHome());
                    break;

                case R.id.pokedex:
                    replaceFragment(new FragmentPokedex());
                    break;

                case R.id.settings:
                    replaceFragment(new FragmentSettings());
            }
            return true;
        });

        ImageButton showProfile = findViewById(R.id.show_profile);
        showProfile.setOnClickListener(this);

    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            /* case R.id.prof:
                Intent intent = new Intent(Welcome.this, CreateProfile.class);
                startActivity(intent);
                break; */

            case R.id.show_profile:
                Intent intent2 = new Intent(Welcome.this, ViewProfile.class);
                startActivity(intent2);
                break;
        }

    }

    @Override
    public void onBackPressed() {
        if (DoublePressToExit){
            finishAffinity();
            toast.cancel();
        } else {
            DoublePressToExit = true;
            toast = Toast.makeText(this, "Premi di nuovo per chiudere l'app", Toast.LENGTH_SHORT);
            toast.show();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    DoublePressToExit = false;
                }
            }, 1500);
        }
    }

}