package io.islnd.android.islnd.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.integration.android.IntentIntegrator;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.RepeatSyncService;
import io.islnd.android.islnd.app.activities.VerticalCaptureActivity;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.Encoder;
import io.islnd.android.islnd.messaging.PublicIdentity;
import io.islnd.android.islnd.messaging.MessageLayer;

public class ShowQrFragment extends Fragment {

    private static final String TAG = ShowQrFragment.class.getSimpleName();
    Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();

        View v = inflater.inflate(R.layout.content_show_qr, container, false);
        setHasOptionsMenu(true);

        ImageView qrImageView = (ImageView) v.findViewById(R.id.qr_image_view);
        PublicIdentity myPublicIdentity = MessageLayer.createNewPublicIdentity(mContext);
        String encodedIdentity = new Encoder().encodeToString(myPublicIdentity.toByteArray());
        Log.v(TAG, "encoded identity string has length " + encodedIdentity.length());
        Util.buildQrCode(qrImageView, encodedIdentity);

        //--Start find friend service because someone may snap our QR code
        Intent repeatSyncServiceIntent = new Intent(mContext, RepeatSyncService.class);
        mContext.startService(repeatSyncServiceIntent);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(R.string.app_name);
    }
}
