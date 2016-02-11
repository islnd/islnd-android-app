package com.island.island.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.island.island.R;

public class ImageViewerActivity extends AppCompatActivity
{
    public static String IMAGE_VIEW_URI = "IMAGE_VIEW_URI";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Load image
        Intent intent = getIntent();
        Uri imageUri = Uri.parse(intent.getStringExtra(IMAGE_VIEW_URI));
        ImageView imageView = (ImageView) findViewById(R.id.image_view);
        imageView.setImageURI(imageUri);
    }
}
