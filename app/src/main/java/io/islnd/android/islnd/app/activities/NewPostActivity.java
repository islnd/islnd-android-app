package io.islnd.android.islnd.app.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import io.islnd.android.islnd.app.EventPublisher;
import io.islnd.android.islnd.app.R;

public class NewPostActivity extends IslndActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    public void submitNewPost(View view)
    {
        EditText postEditText = (EditText) findViewById(R.id.new_post_edit_text);
        String postText = postEditText.getText().toString();

        if(postText.equals(""))
        {
            Snackbar.make(view, getString(R.string.empty_string_post), Snackbar.LENGTH_LONG).show();
        }
        else {
            new EventPublisher(this)
                    .makePost(postText)
                    .publish();

            finish();
        }
    }
}
