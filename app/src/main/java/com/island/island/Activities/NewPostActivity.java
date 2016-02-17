package com.island.island.Activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.island.island.Database.IslandDB;
import com.island.island.R;

public class NewPostActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void submitNewPost(View view)
    {
        EditText postEditText = (EditText) findViewById(R.id.new_post_edit_text);
        String postText = postEditText.getText().toString();

        if(postText.equals(""))
        {
            Snackbar.make(view, getString(R.string.empty_string_post), Snackbar.LENGTH_LONG).show();
        }
        else
        {
            IslandDB.post(getApplicationContext(), postText);
            finish();
        }
    }
}
