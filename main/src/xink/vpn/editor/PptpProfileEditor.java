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
import xink.vpn.wrapper.PptpProfile;
import xink.vpn.wrapper.VpnProfile;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;

public class PptpProfileEditor extends VpnProfileEditor {

    private CheckBox chkEncrypt;

    @Override
    protected void initSpecificWidgets(final ViewGroup content) {
        chkEncrypt = new CheckBox(this);
        chkEncrypt.setText(getString(R.string.encrypt_enabled)); //$NON-NLS-1$
        content.addView(chkEncrypt);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        super.onRestoreInstanceState(savedInstanceState);

        chkEncrypt.setChecked(savedInstanceState.getBoolean("encrypt")); //$NON-NLS-1$
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("encrypt", chkEncrypt.isChecked()); //$NON-NLS-1$
    }

    @Override
    protected VpnProfile createProfile() {
        return new PptpProfile(getApplicationContext());
    }

    @Override
    protected void doPopulateProfile() {
        PptpProfile profile = getProfile();
        profile.setEncryptionEnabled(chkEncrypt.isChecked());
    }

    @Override
    protected void doBindToViews() {
        PptpProfile profile = getProfile();
        chkEncrypt.setChecked(profile.isEncryptionEnabled());
    }
}
