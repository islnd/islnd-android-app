package io.islnd.android.islnd.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import io.islnd.android.islnd.app.R;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // TODO: Consider scaling the image...
        Intent intent = getIntent();
        Uri imageUri = Uri.parse(intent.getStringExtra(IMAGE_VIEW_URI));
        ImageView imageView = (ImageView) findViewById(R.id.image_view);
        imageView.setImageURI(imageUri);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
