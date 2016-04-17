package io.islnd.android.islnd.app;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.security.Key;
import java.security.PrivateKey;
import java.util.List;

import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.crypto.EncryptedEvent;
import io.islnd.android.islnd.messaging.event.Event;
import io.islnd.android.islnd.messaging.event.EventListBuilder;
import io.islnd.android.islnd.messaging.event.EventProcessor;

public class EventPublisher {

    private static final String TAG = EventPublisher.class.getSimpleName();

    private final Context mContext;
    private final EventListBuilder eventListBuilder;

    public EventPublisher(Context context) {
        this.mContext = context;
        this.eventListBuilder = new EventListBuilder(context);
    }
    
    public EventPublisher changeDisplayName(String displayName) {
        this.eventListBuilder.changeDisplayName(displayName);
        return this;
    }

    public EventPublisher makePost(String postText) {
        this.eventListBuilder.makePost(postText);
        return this;
    }

    public EventPublisher deletePost(String postId) {
        this.eventListBuilder.deletePost(postId);
        return this;
    }

    public EventPublisher makeComment(
            String postId,
            String postAuthorAlias,
            String commentText) {
        this.eventListBuilder.makeComment(
                postId,
                postAuthorAlias,
                commentText);
        return this;
    }

    public EventPublisher deleteComment(String commentId) {
        this.eventListBuilder.deleteComment(commentId);
        return this;
    }
    public EventPublisher changeAboutMe(String newAboutMeText) {
        this.eventListBuilder.changeAboutMe(newAboutMeText);
        return this;
    }

    public EventPublisher changeProfileImage(Uri profileImageUri) {
        this.eventListBuilder.changeProfileImage(profileImageUri);
        return this;
    }

    public EventPublisher changeHeaderImage(Uri headerImageUri) {
        this.eventListBuilder.changeHeaderImage(headerImageUri);
        return this;
    }

    public void publish() {
        final List<Event> events = this.eventListBuilder.build();
        ContentValues[] values = new ContentValues[events.size()];
        PrivateKey privateKey = Util.getPrivateKey(mContext);
        Key groupKey = Util.getGroupKey(mContext);
        for (int i = 0; i < events.size(); i++) {
            EventProcessor.process(mContext, events.get(i));
            EncryptedEvent encryptedEvent = new EncryptedEvent(events.get(i), privateKey, groupKey);
            values[i] = new ContentValues();
            values[i].put(IslndContract.OutgoingEventEntry.COLUMN_ALIAS, encryptedEvent.getAlias());
            values[i].put(IslndContract.OutgoingEventEntry.COLUMN_BLOB, encryptedEvent.getBlob());
        }

        mContext.getContentResolver().bulkInsert(
                IslndContract.OutgoingEventEntry.CONTENT_URI,
                values
        );

        Log.d(TAG, "requestSync");
        mContext.getContentResolver().requestSync(
                Util.getSyncAccount(mContext),
                IslndContract.CONTENT_AUTHORITY,
                new Bundle());
    }
}
