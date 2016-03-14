package io.islnd.android.islnd.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.integration.android.IntentIntegrator;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.activities.VerticalCaptureActivity;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.MessageLayer;

public class ShowQrFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_show_qr, container, false);
        setHasOptionsMenu(true);

        ImageView qrImageView = (ImageView) v.findViewById(R.id.qr_image_view);
        Util.buildQrCode(qrImageView, MessageLayer.getEncodedIdentityString(getContext()));

        Button getQrButton = (Button) v.findViewById(R.id.get_qr_button);
        getQrButton.setOnClickListener((View view) -> {
            IntentIntegrator integrator = new IntentIntegrator(getActivity());
            integrator.setCaptureActivity(VerticalCaptureActivity.class);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        });

        return v;
    }
}
