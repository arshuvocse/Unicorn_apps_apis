package com.creatrix.unicorn.Activity.Notice;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import com.creatrix.unicorn.Manager.Zoom;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.databinding.ActivityImageFullScreenBinding;

public class ImageFullScreenActivity extends AppCompatActivity {
ActivityImageFullScreenBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityImageFullScreenBinding.inflate(getLayoutInflater());
        //setContentView(R.layout.activity_image_full_screen);
        setContentView(binding.getRoot());
        new Zoom(this);

        byte[] decodedString = Base64.decode(getIntent().getStringExtra("ImgFull"), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        binding.fullimg.setImageBitmap(decodedByte);
        //Zoom.
    }
}