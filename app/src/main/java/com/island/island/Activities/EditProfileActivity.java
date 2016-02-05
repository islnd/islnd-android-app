package com.island.island.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.island.island.Containers.Profile;
import com.island.island.Database.IslandDB;
import com.island.island.R;

public class EditProfileActivity extends AppCompatActivity
{
    private Profile profile;
    private EditText aboutMe;

    private static final int SELECT_PROFILE_IMAGE = 1;
    private static final int SELECT_HEADER_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TODO: Load profile from database (local/or external)
        // Using a fake one for now
        profile = new Profile("David Thompson", "", "", "Pizza is the reason I wake up everyday.");

        TextView userName = (TextView) findViewById(R.id.profile_user_name);
        aboutMe = (EditText) findViewById(R.id.edit_profile_about_me);

        userName.setText(profile.getUserName());
        aboutMe.setText(profile.getAboutMe());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            if (requestCode == SELECT_PROFILE_IMAGE)
            {
                // TODO: set profile image
                Uri selectedImageUri = data.getData();
            }
            else if(requestCode == SELECT_HEADER_IMAGE)
            {
                // TODO: set header image
                Uri selectedImageUri = data.getData();
            }
        }
    }

    public void saveProfile(View view)
    {
        // TODO: handle image stuff for saving
        profile.setAboutMe(aboutMe.getText().toString());
        IslandDB.updateProfile(profile);

        Snackbar.make(view, getString(R.string.profile_saved), Snackbar.LENGTH_SHORT).show();
    }

    public void chooseProfileImage(View view)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                SELECT_PROFILE_IMAGE);
    }

    public void chooseHeaderImage(View view)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                SELECT_HEADER_IMAGE);
    }
}
