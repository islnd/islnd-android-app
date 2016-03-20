package io.islnd.android.islnd.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.islnd.android.islnd.messaging.CommentUpdate;

public class VersionedContentBuilder {
    private static final String TAG = VersionedContentBuilder.class.getSimpleName();

    public static CommentUpdate buildComment(
            Context context,
            String postAuthorPseudonym,
            String commentAuthorPseudonym,
            String postId,
            String content) {
        int newVersion = getNewVersionAndUpdate(
                context,
                context.getString(R.string.comment_version_key));
        return CommentUpdate.buildComment(
                postAuthorPseudonym,
                commentAuthorPseudonym,
                postId,
                String.valueOf(newVersion),
                content);
    }

    private static int getNewVersionAndUpdate(Context context, String versionKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int lastVersion = preferences.getInt(
                versionKey,
                0);
        int newVersion = lastVersion + 1;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(versionKey, newVersion);
        editor.commit();
        return newVersion;
    }
}
