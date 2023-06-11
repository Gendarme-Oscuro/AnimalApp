package it.uniba.dib.sms222321;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ImmagineIngranditaActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_immagine_ingrandita);

        imageView = findViewById(R.id.immagineIngrandita);
        //Otteniamo il parametro passato con putExtra
        String imageUrl = getIntent().getStringExtra("image_url");
        //Carichiamo l'immagine a schermo intero
        Picasso.get().load(imageUrl).into(imageView);

    }
}