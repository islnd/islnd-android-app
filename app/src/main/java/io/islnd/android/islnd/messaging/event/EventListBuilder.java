package io.islnd.android.islnd.messaging.event;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.util.Util;

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
                        ++eventId,
                        displayName));
        return this;
    }

    public List<Event> build() {
        Util.setEventId(mContext, eventId);
        return this.eventList;
    }

    public String getCurrentAlias() {
        return DataUtils.getMostRecentAlias(mContext, mUserId);
    }
}
