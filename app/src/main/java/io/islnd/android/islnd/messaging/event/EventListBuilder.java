package io.islnd.android.islnd.messaging.event;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.ServerTime;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

public class EventListBuilder {

    private final Context mContext;
    private final List<Event> eventList;
    private final int mUserId;
    private int eventId;

    public EventListBuilder(Context context) {
        mContext = context;
        mUserId = Util.getUserId(mContext);
        this.eventList = new ArrayList<>();
        eventId = Util.getEventId(mContext);
    }

    public EventListBuilder changeDisplayName(String displayName) {
        this.eventList.add(new ChangeDisplayNameEvent(
                        getCurrentAlias(),
                        getNewEventId(),
                        displayName));
        return this;
    }

    public EventListBuilder makePost(String postText) {
        String postId = getNewContentIdAndUpdate(
                mContext,
                mContext.getString(R.string.post_id_key)
        );
        this.eventList.add(new NewPostEvent(
                        getCurrentAlias(),
                        getNewEventId(),
                        postId,
                        postText,
                        ServerTime.getCurrentTimeMillis(mContext)));

        return this;
    }

    public EventListBuilder deletePost(String postId) {
        this.eventList.add(new DeletePostEvent(
                        getCurrentAlias(),
                        getNewEventId(),
                        postId));

        return this;
    }

    public EventListBuilder makeComment(
            String postId,
            String postAuthorAlias,
            String commentText) {
        String commentId = getNewContentIdAndUpdate(
                mContext,
                mContext.getString(R.string.comment_id_key)
        );
        this.eventList.add(new NewCommentEvent(
                        getCurrentAlias(),
                        getNewEventId(),
                        postId,
                        postAuthorAlias,
                        commentId,
                        commentText,
                        ServerTime.getCurrentTimeMillis(mContext)));

        return this;
    }

    public EventListBuilder deleteComment(String commentId) {
        this.eventList.add(new DeleteCommentEvent(
                        getCurrentAlias(),
                        getNewEventId(),
                        commentId));

        return this;
    }
    public EventListBuilder changeAboutMe(String newAboutMeText) {
        this.eventList.add(new ChangeAboutMeEvent(
                        getCurrentAlias(),
                        getNewEventId(),
                        newAboutMeText));
        return this;
    }

    public EventListBuilder changeProfileImage(Uri profileImageUri) {
        this.eventList.add(new ChangeProfilePictureEvent(
                        getCurrentAlias(),
                        getNewEventId(),
                        ImageUtil.getByteArrayFromUri(mContext, profileImageUri)));
        return this;
    }

    public EventListBuilder changeHeaderImage(Uri headerImageUri) {
        this.eventList.add(new ChangeHeaderPictureEvent(
                        getCurrentAlias(),
                        getNewEventId(),
                        ImageUtil.getByteArrayFromUri(mContext, headerImageUri)));
        return this;
    }

    private EventListBuilder changeAlias() {
        this.eventList.add(new ChangeAliasEvent(
                        getCurrentAlias(),
                        getNewEventId(),
                        CryptoUtil.createAlias()
                ));
        return this;
    }

    private int getNewEventId() {
        ++this.eventId;
        return this.eventId;
    }

    public List<Event> build() {
        changeAlias();
        Util.setEventId(mContext, eventId);
        return this.eventList;
    }

    private String getCurrentAlias() {
        return DataUtils.getMostRecentAlias(mContext, mUserId);
    }

    private static String getNewContentIdAndUpdate(Context context, String versionKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int lastVersion = preferences.getInt(
                versionKey,
                0);
        int newVersion = lastVersion + 1;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(versionKey, newVersion);
        editor.commit();
        return Integer.toString(newVersion);
    }
}
