package xink.vpn.editor;

import xink.vpn.R;
import xink.vpn.wrapper.L2tpIpsecPskProfile;
import xink.vpn.wrapper.VpnProfile;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class L2tpIpsecPskProfileEditor extends L2tpProfileEditor {

    private EditText txtKey;

    @Override
    protected void initSpecificWidgets(final ViewGroup content) {
        TextView lblKey = new TextView(this);
        lblKey.setText(getString(R.string.psk)); //$NON-NLS-1$
        content.addView(lblKey);

        txtKey = new EditText(this);
        txtKey.setTransformationMethod(new PasswordTransformationMethod());
        content.addView(txtKey);

        super.initSpecificWidgets(content);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        super.onRestoreInstanceState(savedInstanceState);

        txtKey.setText(savedInstanceState.getCharSequence("psk")); //$NON-NLS-1$
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence("psk", txtKey.getText()); //$NON-NLS-1$
    }

    @Override
    protected VpnProfile createProfile() {
        return new L2tpIpsecPskProfile(getApplicationContext());
    }

    @Override
    protected void doPopulateProfile() {
        L2tpIpsecPskProfile profile = getProfile();
        profile.setPresharedKey(txtKey.getText().toString().trim());

        super.doPopulateProfile();
    }

    @Override
    protected void doBindToViews() {
        L2tpIpsecPskProfile profile = getProfile();
        txtKey.setText(profile.getPresharedKey());

        super.doBindToViews();
    }
}
