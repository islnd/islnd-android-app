package io.islnd.android.islnd.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.List;

import io.islnd.android.islnd.app.EventPushService;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.messaging.event.Event;
import io.islnd.android.islnd.messaging.event.EventListBuilder;
import io.islnd.android.islnd.messaging.event.EventProcessor;

public class NewPostActivity extends AppCompatActivity
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
            List<Event> postEvents = new EventListBuilder(this)
                    .makePost(postText)
                    .build();
            for (Event event : postEvents) {
                EventProcessor.process(this, event);

                Intent pushEventService = new Intent(this, EventPushService.class);
                pushEventService.putExtra(EventPushService.EVENT_EXTRA, event);
                startService(pushEventService);
            }

            finish();
        }
    }
}
