package io.islnd.android.islnd.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import io.islnd.android.islnd.messaging.event.Event;
import io.islnd.android.islnd.messaging.event.EventListBuilder;
import io.islnd.android.islnd.messaging.event.EventProcessor;

public class EventPublisher {

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
        for (Event event : this.eventListBuilder.build()) {
            EventProcessor.process(mContext, event);

            Intent pushEventService = new Intent(mContext, EventPushService.class);
            pushEventService.putExtra(EventPushService.EVENT_EXTRA, event);
            mContext.startService(pushEventService);
        }
    }
}
