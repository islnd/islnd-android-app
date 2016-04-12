package io.islnd.android.islnd.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import io.islnd.android.islnd.messaging.Identity;
import io.islnd.android.islnd.messaging.MessageLayer;

public class ShowQrFragment extends Fragment {

    Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();

        View v = inflater.inflate(R.layout.content_show_qr, container, false);
        setHasOptionsMenu(true);

        ImageView qrImageView = (ImageView) v.findViewById(R.id.qr_image_view);
        Identity myIdentity = MessageLayer.getMyIdentity(mContext);
        String encodedIdentity = new Encoder().encodeToString(myIdentity.toByteArray());
        Util.buildQrCode(qrImageView, encodedIdentity);

        //--Start find friend service because someone may snap our QR code
        Intent findFriendServiceIntent = new Intent(mContext, RepeatSyncService.class);
        mContext.startService(findFriendServiceIntent);

        Button getQrButton = (Button) v.findViewById(R.id.get_qr_button);
        getQrButton.setOnClickListener((View view) -> {
                    IntentIntegrator integrator = new IntentIntegrator(getActivity());
                    integrator.setCaptureActivity(VerticalCaptureActivity.class);
                    integrator.setOrientationLocked(false);
                    integrator.initiateScan();
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(R.string.app_name);
    }
}
