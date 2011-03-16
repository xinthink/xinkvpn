package xink.vpn.editor;

import xink.vpn.wrapper.L2tpProfile;
import xink.vpn.wrapper.VpnProfile;
import android.text.method.PasswordTransformationMethod;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class L2tpProfileEditor extends VpnProfileEditor {

    private CheckBox chkSecretEnabled;
    private EditText txtSecret;

    @Override
    protected void initSpecificWidgets(final ViewGroup content) {
        chkSecretEnabled = new CheckBox(this);
        chkSecretEnabled.setText("L2TP Secret Enabled");
        content.addView(chkSecretEnabled);

        final TextView lblSecret = new TextView(this);
        lblSecret.setText("L2TP Secret String");
        content.addView(lblSecret);

        txtSecret = new EditText(this);
        txtSecret.setTransformationMethod(new PasswordTransformationMethod());
        content.addView(txtSecret);

        lblSecret.setEnabled(false);
        txtSecret.setEnabled(false);
        chkSecretEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                lblSecret.setEnabled(isChecked);
                txtSecret.setEnabled(isChecked);
            }
        });
    }

    @Override
    protected VpnProfile createProfile() {
        return new L2tpProfile(getApplicationContext());
    }

    @Override
    protected void doPopulateProfile() {
        L2tpProfile profile = getProfile();
        profile.setSecretEnabled(chkSecretEnabled.isChecked());

        if (profile.isSecretEnabled()) {
            profile.setSecretString(txtSecret.getText().toString().trim());
        }
    }

    @Override
    protected void doBindToViews() {
        L2tpProfile profile = getProfile();
        chkSecretEnabled.setChecked(profile.isSecretEnabled());

        if (profile.isSecretEnabled()) {
            txtSecret.setText(profile.getSecretString());
        }
    }

}
