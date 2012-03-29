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
import xink.vpn.wrapper.L2tpIpsecPskProfile;
import xink.vpn.wrapper.VpnProfile;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
        txtKey.setLines(1);
        txtKey.setImeOptions(EditorInfo.IME_ACTION_NEXT);
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
        L2tpIpsecPskProfile p = getProfile();
        p.setPresharedKey(txtKey.getText().toString().trim());

        super.doPopulateProfile();
    }

    @Override
    protected void doBindToViews() {
        L2tpIpsecPskProfile p = getProfile();
        txtKey.setText(p.getPresharedKey());

        super.doBindToViews();
    }
}
