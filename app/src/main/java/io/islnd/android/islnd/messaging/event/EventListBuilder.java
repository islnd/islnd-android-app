package io.islnd.android.islnd.messaging.event;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.SoundPool;
import android.support.v7.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.ContentUtil;

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
                        ContentUtil.getContentTimestamp()));

        return this;
    }

    public EventListBuilder deletePost(int postUserId, String postId) {
        this.eventList.add(new DeletePostEvent(
                        getCurrentAlias(),
                        this.eventId + getNewEventId(),
                        postId));

        return this;
    }

    private int getNewEventId() {
        ++this.eventId;
        return this.eventId;
    }

    public List<Event> build() {
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
