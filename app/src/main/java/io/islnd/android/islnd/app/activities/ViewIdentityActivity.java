package io.islnd.android.islnd.app.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.loader.LoaderId;
import io.islnd.android.islnd.app.util.Util;

public class ViewIdentityActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String USER_ID_EXTRA = "USER_ID";

    private int mUserId = -1;
    private TextView mPublicKeyTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_identity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mUserId = intent.getIntExtra(USER_ID_EXTRA, -1);

        mPublicKeyTextView = (TextView) findViewById(R.id.public_key_digest);

        getSupportLoaderManager().initLoader(LoaderId.VIEW_PUBLIC_KEY_ACTIVITY_LOADER_ID, new Bundle(), this);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                IslndContract.UserEntry.TABLE_NAME + "." + IslndContract.PostEntry._ID,
                IslndContract.UserEntry.COLUMN_PUBLIC_KEY_DIGEST
        };

        return new CursorLoader(
                this,
                IslndContract.UserEntry.buildUserWithUserId(mUserId),
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String[] partitions = Util.partition(data.getString(1), 4);
        String identity = Util.formatWithColons(partitions[0]) + "\n"
                + Util.formatWithColons(partitions[1]) + "\n"
                + Util.formatWithColons(partitions[2]) + "\n"
                + Util.formatWithColons(partitions[3]);

        mPublicKeyTextView.setText(identity);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
