package com.island.island;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.island.island.Activities.NewPostActivity;
import com.island.island.Models.Post;
import com.island.island.Models.Profile;
import com.island.island.Models.ProfileWithImageData;

import org.island.messaging.CommentUpdate;
import org.island.messaging.PostUpdate;

public class VersionedContentBuilder {
    private static final String TAG = VersionedContentBuilder.class.getSimpleName();

    public static ProfileWithImageData buildProfile(Context context, String username, String aboutMe,
                                       byte[] profileImageByteArray, byte[] headerImageByteArray) {
        int newVersion = getNewVersionAndUpdate(
                context,
                context.getString(R.string.previous_profile_version_key));
        return new ProfileWithImageData(username, aboutMe, profileImageByteArray,
                headerImageByteArray, newVersion);
    }

    public static PostUpdate buildPost(Context context, String content) {
        int newVersion = getNewVersionAndUpdate(
                context,
                context.getString(R.string.post_version_key));
        return PostUpdate.buildPost(content, String.valueOf(newVersion));
    }

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
