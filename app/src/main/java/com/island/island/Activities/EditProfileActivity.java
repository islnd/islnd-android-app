package com.island.island.Activities;

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

    public void saveProfile(View view)
    {
        // TODO: handle image stuff for saving
        profile.setAboutMe(aboutMe.getText().toString());
        IslandDB.updateProfile(profile);

        Snackbar.make(view, getString(R.string.profile_saved), Snackbar.LENGTH_SHORT).show();
    }
}
