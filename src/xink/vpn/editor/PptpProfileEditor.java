package xink.vpn.editor;

import xink.vpn.wrapper.PptpProfile;
import xink.vpn.wrapper.VpnProfile;
import android.view.ViewGroup;
import android.widget.CheckBox;

public class PptpProfileEditor extends VpnProfileEditor {

    private CheckBox chkEncrypt;

    @Override
    protected void initSpecificWidgets(final ViewGroup content) {
        // ListView row = new ListView(this);
        // content.addView(row);
        //
        // CheckedTextView txtEncrypt = new CheckedTextView(this);
        // txtEncrypt.setText("Encryption Enabled");
        //
        // row.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        // row.addView(txtEncrypt);

        chkEncrypt = new CheckBox(this);
        chkEncrypt.setText("Encryption Enabled");
        content.addView(chkEncrypt);
    }

    @Override
    protected VpnProfile createProfile() {
        return new PptpProfile(getApplicationContext());
    }

    @Override
    protected void populate() {
        PptpProfile profile = getProfile();
        profile.setEncryptionEnabled(chkEncrypt.isChecked());
    }
}
