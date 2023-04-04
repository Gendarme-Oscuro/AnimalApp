package it.uniba.dib.sms222321;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import javax.annotation.Nullable;

public class Welcome extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        TextView textView = findViewById(R.id.prof);
        textView.setOnClickListener(this);

        TextView textView2 = findViewById(R.id.show_profile);
        textView2.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.prof:
                Intent intent = new Intent(Welcome.this, CreateProfile.class);
                startActivity(intent);
                break;

            case R.id.show_profile:
                Intent intent2 = new Intent(Welcome.this, ViewProfile.class);
                startActivity(intent2);
                break;
        }

    }
}