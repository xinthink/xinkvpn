/*
 * Copyright 2011 yingxinwu.g@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xink.vpn.editor;

import xink.vpn.R;
import xink.vpn.wrapper.L2tpProfile;
import xink.vpn.wrapper.VpnProfile;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
        chkSecretEnabled.setText(getString(R.string.l2tp_secret_enabled)); //$NON-NLS-1$
        content.addView(chkSecretEnabled);

        final TextView lblSecret = new TextView(this);
        lblSecret.setText(getString(R.string.l2tp_secret)); //$NON-NLS-1$
        content.addView(lblSecret);

        txtSecret = new EditText(this);
        txtSecret.setLines(1);
        txtSecret.setImeOptions(EditorInfo.IME_ACTION_NEXT);
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
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        super.onRestoreInstanceState(savedInstanceState);

        chkSecretEnabled.setChecked(savedInstanceState.getBoolean("secretEnabled")); //$NON-NLS-1$
        txtSecret.setText(savedInstanceState.getCharSequence("secret")); //$NON-NLS-1$
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("secretEnabled", chkSecretEnabled.isChecked()); //$NON-NLS-1$
        outState.putCharSequence("secret", txtSecret.getText()); //$NON-NLS-1$
    }

    @Override
    protected VpnProfile createProfile() {
        return new L2tpProfile(getApplicationContext());
    }

    @Override
    protected void doPopulateProfile() {
        L2tpProfile p = getProfile();
        boolean secretEnabled = chkSecretEnabled.isChecked();
        p.setSecretEnabled(secretEnabled);
        p.setSecretString(secretEnabled ? txtSecret.getText().toString().trim() : "");
    }

    @Override
    protected void doBindToViews() {
        L2tpProfile p = getProfile();
        chkSecretEnabled.setChecked(p.isSecretEnabled());

        if (p.isSecretEnabled()) {
            txtSecret.setText(p.getSecretString());
        }
    }

}
