package com.island.island.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.island.island.Adapters.PostAdapter;
import com.island.island.Database.CommentDatabase;
import com.island.island.Database.FriendDatabase;
import com.island.island.Database.PostDatabase;
import com.island.island.DeletePostFragment;
import com.island.island.Models.CommentKey;
import com.island.island.Models.CommentViewModel;
import com.island.island.Models.Post;
import com.island.island.Models.Comment;
import com.island.island.Models.PostKey;
import com.island.island.Models.RawPost;
import com.island.island.PostCollection;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;
import com.island.island.Utils.Utils;

import org.island.messaging.MessageLayer;
import org.island.messaging.CommentCollection;
import org.island.messaging.server.CommentQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeedActivity extends Fragment implements DeletePostFragment.NoticeDeletePostListener {
    private final static String TAG = FeedActivity.class.getSimpleName();

    private static final int NEW_POST_RESULT = 1;
    public static final int DELETE_POST_RESULT = 2;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Post> mArrayOfPosts;
    private Set<PostKey> mPostMap;
    private SwipeRefreshLayout mRefreshLayout;
    private Map<PostKey, Long> mPostKeyToTimestamp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.content_feed, container, false);


        // Feed posts setup
        mArrayOfPosts = new ArrayList<>();
        mPostMap = new HashSet<>();
        mPostKeyToTimestamp = new HashMap<>();
        mRecyclerView = (RecyclerView) v.findViewById(R.id.feed_recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PostAdapter(getContext(), mArrayOfPosts);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        // Populate feed
        getPostsFromDatabase();
        new GetPostsFromServerTask().execute();

        // Swipe to refresh
        mRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_to_refresh_layout);

        mRefreshLayout.setOnRefreshListener(() -> {
            new GetPostsFromServerTask().execute();
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_POST_RESULT) {
            if (data != null) {
                Post post = (Post) data.getSerializableExtra(Post.POST_EXTRA);
                mArrayOfPosts.add(0, post);
                mAdapter.notifyDataSetChanged();
                mPostMap.add(post.getKey());
                mPostKeyToTimestamp.put(post.getKey(), post.getTimestamp());
            }
        } else if (requestCode == DELETE_POST_RESULT) {
            if (data != null) {
                PostKey postKey = (PostKey) data.getSerializableExtra(PostKey.POST_KEY_EXTRA);
                removePostFromFeed(postKey);
            }
        }
    }

    private void getPostsFromDatabase() {
        List<RawPost> localPosts = PostDatabase.getInstance(getContext()).getAll();
        FriendDatabase friendDatabase = FriendDatabase.getInstance(getContext());
        CommentDatabase commentDatabase = CommentDatabase.getInstance(getContext());
        boolean listChanged = false;
        for (RawPost p : localPosts) {
            if (addPostToFeed(friendDatabase, commentDatabase, p)) {
                listChanged = true;
            }
        }

        if (listChanged) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private boolean addPostToFeed(FriendDatabase friendDatabase, CommentDatabase commentDatabase, RawPost p) {
        int userId = p.getUserId();
        String postAuthor = userId == 0
                ? Utils.getUser(getContext())
                : friendDatabase.getUsername(userId);

        final Post post = new Post(
                postAuthor,
                userId,
                p.getPostId(),
                p.getTimestamp(),
                p.getContent(),
                getCommentsForPost(commentDatabase, p)
        );

        if (mPostMap.contains(post.getKey())) {
            return false;
        }

        mPostMap.add(post.getKey());
        int insertionPoint = getIndexToInsertPost(p.getTimestamp());
        mArrayOfPosts.add(insertionPoint, post);
        mPostKeyToTimestamp.put(post.getKey(), post.getTimestamp());

        return true;
    }

    private List<CommentViewModel> getCommentsForPost(CommentDatabase commentDatabase, RawPost post) {
        List<Comment> comments = commentDatabase.getComments(post.getUserId(), post.getPostId());
        return Utils.buildCommentViewModels(getContext(), comments);
    }

    public void startNewPostActivity(View view) {
        Intent newPostIntent = new Intent(getContext(), NewPostActivity.class);
        startActivityForResult(newPostIntent, NEW_POST_RESULT);
    }

    @Override
    public void onDeletePostDialogPositiveClick(DialogFragment dialogFragment) {
        Bundle args = dialogFragment.getArguments();
        String postId = args.getString(DeletePostFragment.POST_ID_BUNDLE_KEY);
        int postUserId = args.getInt(DeletePostFragment.USER_ID_BUNDLE_KEY);
        final PostKey postKey = new PostKey(postUserId, postId);
        removePostFromFeed(postKey);
    }

    private void removePostFromFeed(PostKey postKey) {
        int index = findPost(postKey);
        if (index != -1) {
            mArrayOfPosts.remove(index);
            mAdapter.notifyDataSetChanged();
            mPostMap.remove(postKey);
        }
    }

    private class GetCommentsFromServerTask extends AsyncTask<Void, Void, CommentCollection> {
        private final String TAG = GetCommentsFromServerTask.class.getSimpleName();

        @Override
        protected CommentCollection doInBackground(Void... params) {
            List<CommentQuery> commentQueries = new ArrayList<>();
            FriendDatabase friendDatabase = FriendDatabase.getInstance(getContext());

            for (Post post : mArrayOfPosts) {
                String postAuthorPseudonym = friendDatabase.getPseudonym(post.getUserId());
                commentQueries.add(new CommentQuery(postAuthorPseudonym, post.getPostId()));
            }

            return MessageLayer.getCommentCollection(getContext(), commentQueries);
        }

        @Override
        protected void onPostExecute(CommentCollection commentCollection) {
            boolean anyPostUpdated = false;

            Map<PostKey, List<Comment>> postKeyToComments = commentCollection.getCommentsGroupedByPostKey();
            for (PostKey postKey : postKeyToComments.keySet()) {
                int index = getPostIndex(postKey);

                List<CommentViewModel> commentViewModels = Utils.buildCommentViewModels(
                        getContext(),
                        postKeyToComments.get(postKey));
                if (mArrayOfPosts.get(index).addComments(commentViewModels)) {
                    anyPostUpdated = true;
                }
            }

            Map<PostKey, List<CommentKey>> postKeyToDeletions = commentCollection.getDeletions();
            for (PostKey postKey : postKeyToDeletions.keySet()) {
                List<CommentKey> deletions = postKeyToDeletions.get(postKey);
                int index = getPostIndex(postKey);

                if (mArrayOfPosts.get(index).deleteComments(deletions)) {
                    anyPostUpdated = true;
                }
            }

            if (anyPostUpdated) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class GetPostsFromServerTask extends AsyncTask<Void, Void, PostCollection> {
        private final String TAG = GetPostsFromServerTask.class.getSimpleName();

        @Override
        protected PostCollection doInBackground(Void... params) {
            return MessageLayer.getPosts(getContext());
        }

        @Override
        protected void onPostExecute(PostCollection postCollection) {
            if (postCollection != null) {
                boolean adapterChanged = addPostsToFeed(postCollection);
                if (adapterChanged) {
                    mAdapter.notifyDataSetChanged();
                }
            }

            mRefreshLayout.setRefreshing(false);
            Utils.printAvailableMemory(getContext(), TAG);

            new GetCommentsFromServerTask().execute();
        }

        private boolean addPostsToFeed(PostCollection postCollection) {
            boolean postsModified = false;
            for (Post post : postCollection.getPosts()) {
                Log.v(TAG, String.format("%s %s", post.getContent(), post.getKey()));
                if (!mPostMap.contains(post.getKey())) {
                    mPostMap.add(post.getKey());
                    int insertionPoint = getIndexToInsertPost(post.getTimestamp());
                    mArrayOfPosts.add(insertionPoint, post);
                    mPostKeyToTimestamp.put(post.getKey(), post.getTimestamp());
                    postsModified = true;
                }
            }

            for (PostKey postKey : postCollection.getDeletedKeys()) {
                int index = findPost(postKey);
                if (index != -1) {
                    mArrayOfPosts.remove(index);
                    postsModified = true;
                    Log.d(TAG, "removing post at index " + index);
                }
                else {
                    Log.d(TAG, "tried to delete post not in feed");
                }
            }

            return postsModified;
        }
    }

    private int findPost(PostKey postKey) {
        for (int i = 0; i < mArrayOfPosts.size(); i++) {
            if (mArrayOfPosts.get(i).getKey().equals(postKey)) {
                return i;
            }
        }

        return -1;
    }

    //--This method finds the index to insert a post or find the
    //  first index of a post with a given timestamp
    private int getIndexToInsertPost(long postTimestamp) {
        //--TODO binary search
        int insertionPoint = 0;
        while (insertionPoint < mArrayOfPosts.size()
                && postTimestamp < mArrayOfPosts.get(insertionPoint).getTimestamp()) {
            Log.v(TAG, "" + insertionPoint);
            insertionPoint++;
        }

        return insertionPoint;
    }

    private int getPostIndex(PostKey postKey) {
        int index = getIndexToInsertPost(getTimestamp(postKey));
        while (!mArrayOfPosts.get(index).getKey().equals(postKey)) {
            index++;
        }
        return index;
    }

    private long getTimestamp(PostKey postKey) {
        return mPostKeyToTimestamp.get(postKey);
    }
}
