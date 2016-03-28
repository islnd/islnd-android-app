package io.islnd.android.islnd.app;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.Identity;
import io.islnd.android.islnd.messaging.MessageLayer;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.message.Message;
import io.islnd.android.islnd.messaging.message.MessageType;
import io.islnd.android.islnd.messaging.message.ProfileMessage;
import io.islnd.android.islnd.messaging.server.MessageQuery;

public class FindNewFriendService extends Service {

    private static final String TAG = FindNewFriendService.class.getSimpleName();

    private Context mContext;
    private ContentResolver mContentResolver;

    private boolean mIdentityAdded;
    private boolean mProfileAdded;
    private String mMailbox;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");
        mContext = this;
        mContentResolver = getContentResolver();

        checkMailbox();

        return START_NOT_STICKY;
    }

    private void checkMailbox() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                int[] delay = {5000, 50000, 10000, 20000, 20000};

                for (int i = 0; i < delay.length; i++) {
                    List<String> mailboxes = getMailboxes();
                    MessageQuery messageQuery = new MessageQuery(mailboxes);

                    List<Message> messages = Rest.postMessageQuery(
                            messageQuery,
                            Util.getApiKey(mContext));

                    if (messages != null
                            && messages.size() > 0) {
                        Log.v(TAG, messages.size() + " messages");
                        PriorityQueue<Message> messageQueue = new PriorityQueue<>();
                        for (Message message : messages) {
                            messageQueue.add(message);
                        }

                        while (!messageQueue.isEmpty()) {
                            processMessage(messageQueue.poll());
                        }

                        if (mProfileAdded) {
                            removeMailboxFromQuerySet();

                            final String newMailbox = CryptoUtil.createAlias();
                            DataUtils.updateMyUserMailbox(mContext, newMailbox);
                            Util.setMyMailbox(mContext, newMailbox);
                            DataUtils.addMailboxToQuerySet(mContext, newMailbox);

                            Log.v(TAG, "my new mailbox is " + newMailbox);

                            break;
                        }
                    }

                    try {
                        Thread.sleep(delay[i]);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                stopSelf();
                return null;
            }
        }.execute();
    }

    private void removeMailboxFromQuerySet() {
        String selection = IslndContract.MailboxEntry.COLUMN_MAILBOX + " = ?";
        String[] args = new String[] {
                mMailbox
        };
        mContentResolver.delete(
                IslndContract.MailboxEntry.CONTENT_URI,
                selection,
                args
        );
    }

    private void processMessage(Message message) {
        Log.v(TAG, "message type " + message.getType());
        if (message.getType() == MessageType.IDENTITY) {
            Log.v(TAG, "process identity");
            Identity friendToAdd = Identity.fromProto(message.getBlob());
            Identity friendWithOurMailbox = new Identity(
                    friendToAdd.getDisplayName(),
                    friendToAdd.getAlias(),
                    friendToAdd.getMessageInbox(),
                    friendToAdd.getGroupKey(),
                    friendToAdd.getPublicKey()
            );

            MessageLayer.addFriendToDatabaseAndCreateDefaultProfile(
                    mContext,
                    friendWithOurMailbox,
                    Util.getMyInbox(mContext)
            );

            mIdentityAdded = true;

            Intent sendProfileIntent = new Intent(mContext, FriendAddBackService.class);
            sendProfileIntent.putExtra(FriendAddBackService.MAILBOX_EXTRA, friendToAdd.getMessageInbox());
            sendProfileIntent.putExtra(
                    FriendAddBackService.JOB_EXTRA,
                    FriendAddBackService.PROFILE_JOB);
            mContext.startService(sendProfileIntent);
        }
        else if (message.getType() == MessageType.PROFILE) {
            Log.v(TAG, "process profile");
            ProfileMessage profileMessage = ProfileMessage.fromProto(message.getBlob());
            Uri headerImageUri = ImageUtil.saveBitmapToInternalFromByteArray(
                    mContext,
                    profileMessage.getHeaderImageBytes());
            Uri profileImageUri = ImageUtil.saveBitmapToInternalFromByteArray(
                    mContext,
                    profileMessage.getProfileImageBytes());
            Profile profile = new Profile(
                    profileMessage.getAboutMe(),
                    profileImageUri,
                    headerImageUri
            );

            int userId = DataUtils.getUserIdWithMessageOutbox(mContext, message.getMailbox());
            DataUtils.insertProfile(mContext, profile, userId);
            Log.v(TAG, "adding profile for user " + userId);
            mProfileAdded = true;

            //--TODO this assumes all messages are from same mailbox
            //--TODO assumes all messages are from same user
            mMailbox = message.getMailbox();
        }
    }

    @NonNull
    private List<String> getMailboxes() {
        String[] projection = new String[] { IslndContract.MailboxEntry.COLUMN_MAILBOX };
        Cursor cursor = mContentResolver.query(
                IslndContract.MailboxEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
        List<String> mailboxes = new ArrayList<>();
        if (!cursor.moveToFirst()) {
            return mailboxes;
        }

        do {
            mailboxes.add(cursor.getString(0));
        } while (cursor.moveToNext());

        return mailboxes;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
