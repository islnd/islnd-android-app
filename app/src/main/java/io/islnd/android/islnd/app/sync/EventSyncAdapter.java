package io.islnd.android.islnd.app.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import javax.crypto.SecretKey;

import io.islnd.android.islnd.app.IslndAction;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.InvalidBlobException;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.crypto.EncryptedEvent;
import io.islnd.android.islnd.messaging.crypto.EncryptedMessage;
import io.islnd.android.islnd.messaging.crypto.InvalidSignatureException;
import io.islnd.android.islnd.messaging.event.Event;
import io.islnd.android.islnd.messaging.event.EventProcessor;
import io.islnd.android.islnd.messaging.event.EventType;
import io.islnd.android.islnd.messaging.message.Message;
import io.islnd.android.islnd.messaging.message.MessageProcessor;
import io.islnd.android.islnd.messaging.message.MessageType;
import io.islnd.android.islnd.messaging.message.ReceivedMessage;
import io.islnd.android.islnd.messaging.server.EventQuery;
import io.islnd.android.islnd.messaging.server.MessageQuery;

public class EventSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = EventSyncAdapter.class.getSimpleName();

    private Context mContext;
    private ContentResolver mContentResolver;

    public EventSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        init(context);
    }

    public EventSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.v(TAG, "onPerformSync");

        pushOutgoingMessages(provider);
        pushOutgoingEvents(provider);
        getIncomingMessages();
        getIncomingEvents();
        
        mContext.sendBroadcast(new Intent(IslndAction.EVENT_SYNC_COMPLETE));

        Log.v(TAG, "completed on perform sync");
    }

    private void getIncomingMessages() {
        Log.d(TAG, "getIncomingMessages");
        MessageQuery messageQuery = new MessageQuery(getMailboxes());
        Log.v(TAG, "message query " + messageQuery);
        List<EncryptedMessage> encryptedMessages = Rest.postMessageQuery(
                messageQuery,
                Util.getApiKey(mContext));

        if (encryptedMessages == null
                || encryptedMessages.size() == 0) {
            return;
        }

        Log.v(TAG, encryptedMessages.size() + " messages");
        PriorityQueue<Message> messageQueue = new PriorityQueue<>();
        for (EncryptedMessage encryptedMessage : encryptedMessages) {
            PublicKey authorPublicKey = null;
            try {
                authorPublicKey = DataUtils.getPublicKeyForUserOutbox(
                        mContext,
                        encryptedMessage.getMailbox());
            } catch (Exception e) {
                Log.v(TAG, "public key not found");
                //--This may fail if it is a new user
            }

            ReceivedMessage receivedMessage = null;
            try {
                receivedMessage = encryptedMessage.decryptMessageAndCheckSignature(
                        Util.getPrivateKey(mContext),
                        authorPublicKey);
            } catch (InvalidProtocolBufferException e) {
                Log.w(TAG, e.toString());
            } catch (InvalidBlobException e) {
                Log.w(TAG, e.toString());
            } catch (IllegalArgumentException e) {
                Log.w(TAG, e.toString());
            }


            if (receivedMessage == null) {
                continue;
            }

            //--All messages must have a valid signature, except identity messages, because
            //  those messages contain the user's public key. Since there is no previous knowledge
            //  of the public key, there is nothing to validate
            if (!receivedMessage.isSignatureValid()
                    && receivedMessage.getMessage().getType() != MessageType.PUBLIC_IDENTITY) {
                Log.d(TAG, String.format("message type %d signature invalid!",
                        receivedMessage.getMessage().getType()));
                continue;
            }

            messageQueue.add(receivedMessage.getMessage());
        }

        while (!messageQueue.isEmpty()) {
            MessageProcessor.process(mContext, messageQueue.poll());
        }
    }

    private void pushOutgoingMessages(ContentProviderClient provider) {
        Log.d(TAG, "pushOutgoingMessages");
        String[] projections = new String[] {
                IslndContract.OutgoingMessageEntry.COLUMN_MAILBOX,
                IslndContract.OutgoingMessageEntry.COLUMN_BLOB
        };
        try {
            Cursor cursor = provider.query(
                    IslndContract.OutgoingMessageEntry.CONTENT_URI,
                    projections,
                    null,
                    null,
                    null
            );
            Log.v(TAG, String.format("found %d outgoing messages", cursor.getCount()));
            if (cursor.moveToFirst()) {
                String apiKey = Util.getApiKey(mContext);

                do {
                    EncryptedMessage encryptedMessage = new EncryptedMessage(
                            cursor.getString(0),
                            cursor.getString(1)
                    );
                    Rest.postMessage(encryptedMessage, apiKey);
                } while (cursor.moveToNext());

                int records = mContentResolver.delete(
                        IslndContract.OutgoingMessageEntry.CONTENT_URI,
                        null,
                        null
                );
                Log.v(TAG, String.format("posted and deleted %d messages", records));
            }
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void getIncomingEvents() {
        Log.d(TAG, "getIncomingEvents");
        PriorityQueue<Event> comments = new PriorityQueue<>();
        boolean anyNewEventProcessed;
        do {
            Log.d(TAG, "get events loop");
            anyNewEventProcessed = false;
            List<EncryptedEvent> encryptedEvents = getEncryptedEvents();
            if (encryptedEvents == null) {
                mContext.sendBroadcast(new Intent(IslndAction.EVENT_SYNC_COMPLETE));
                Log.w(TAG, "event query returned null!");
                return;
            }

            Log.v(TAG, String.format("event service returned %d events", encryptedEvents.size()));
            PriorityQueue<Event> events = decryptEvents(encryptedEvents);

            //--Process events in order
            while (!events.isEmpty()) {
                Log.d(TAG, "process loop");
                final Event event = events.poll();
                switch (event.getType()) {
                    case EventType.NEW_COMMENT: {
                        //--Fall through
                    }
                    case EventType.DELETE_COMMENT: {
                        event.setUserId(DataUtils.getUserIdFromAlias(mContext, event.getAlias()));
                        comments.add(event);
                        break;
                    }
                    default: {
                        boolean newEventProcessed = EventProcessor.process(mContext, event);
                        if (newEventProcessed) {
                            anyNewEventProcessed = true;
                        }
                        break;
                    }
                }
            }
        } while (anyNewEventProcessed);

        //--Process all comments and delete comments
        while (!comments.isEmpty()) {
            EventProcessor.process(mContext, comments.poll());
        }
    }

    private void pushOutgoingEvents(ContentProviderClient provider) {
        Log.d(TAG, "pushOutgoingEvents");
        String[] projections = new String[] {
                IslndContract.OutgoingEventEntry.COLUMN_ALIAS,
                IslndContract.OutgoingEventEntry.COLUMN_BLOB
        };
        try {
            Cursor cursor = provider.query(
                    IslndContract.OutgoingEventEntry.CONTENT_URI,
                    projections,
                    null,
                    null,
                    null
            );
            Log.v(TAG, String.format("found %d outgoing events", cursor.getCount()));
            if (cursor.moveToFirst()) {
                String apiKey = Util.getApiKey(mContext);

                do {
                    EncryptedEvent encryptedEvent = new EncryptedEvent(
                            cursor.getString(1),
                            cursor.getString(0)
                    );
                    Rest.postEvent(encryptedEvent, apiKey);
                } while (cursor.moveToNext());

                int records = mContentResolver.delete(
                        IslndContract.OutgoingEventEntry.CONTENT_URI,
                        null,
                        null
                );
                Log.v(TAG, String.format("posted and deleted %d events", records));
            }
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteException: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();

        mContext.sendBroadcast(new Intent(IslndAction.EVENT_SYNC_COMPLETE));
        Log.d(TAG, "sync cancelled");
    }

    @NonNull
    private PriorityQueue<Event> decryptEvents(List<EncryptedEvent> encryptedEvents) {
        PriorityQueue<Event> events = new PriorityQueue<>();
        for (EncryptedEvent encryptedEvent : encryptedEvents) {
            String alias = encryptedEvent.getAlias();
            Log.v(TAG, "event alias " + alias);

            //--TODO need to handle multiple users having same alias
            int userId = DataUtils.getUserIdFromAlias(mContext, alias);

            SecretKey groupKey = DataUtils.getGroupKey(mContext, userId);
            PublicKey publicKey = DataUtils.getPublicKey(mContext, userId);
            try {
                final Event event = encryptedEvent.decryptAndVerify(groupKey, publicKey);
                if (event != null) {
                    events.add(event);
                }
            } catch (InvalidSignatureException e) {
                Log.w(TAG, e.toString());
            } catch (InvalidBlobException e) {
                Log.w(TAG, e.toString());
            } catch (InvalidProtocolBufferException e) {
                Log.w(TAG, e.toString());
            } catch (IllegalArgumentException e) {
                Log.w(TAG, e.toString());
            }
        }
        return events;
    }

    private List<EncryptedEvent> getEncryptedEvents() {
        String[] projection = {
                IslndContract.AliasEntry.COLUMN_ALIAS
        };

        String[] args = new String[] { Integer.toString(IslndContract.UserEntry.MY_USER_ID) };
        Cursor cursor = null;
        EventQuery eventQuery;
        try {
            cursor = mContentResolver.query(
                    IslndContract.AliasEntry.CONTENT_URI,
                    projection,
                    IslndContract.AliasEntry.COLUMN_USER_ID + " != ?",
                    args,
                    null);
            eventQuery = buildEventQuery(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return Rest.postEventQuery(
                eventQuery,
                Util.getApiKey(getContext()));
    }

    @NonNull
    private EventQuery buildEventQuery(Cursor cursor) {
        List<String> aliases = new ArrayList<String>();
        if (!cursor.moveToFirst()) {
            return new EventQuery(aliases);
        }

        do {
            final String alias = cursor.getString(cursor.getColumnIndex(IslndContract.AliasEntry.COLUMN_ALIAS));
            Log.v(TAG, "event query includes: " + alias);
            aliases.add(alias);
        } while (cursor.moveToNext());

        return new EventQuery(aliases);
    }

    @NonNull
    private List<String> getMailboxes() {
        List<String> mailboxes = DataUtils.getActiveUserOutboxes(getContext());
        mailboxes.addAll(DataUtils.getMessageTokenMailboxes(getContext()));
        return mailboxes;
    }
}
