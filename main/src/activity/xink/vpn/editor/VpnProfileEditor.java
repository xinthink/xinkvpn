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

import xink.vpn.AppException;
import xink.vpn.Constants;
import xink.vpn.R;
import xink.vpn.VpnProfileRepository;
import xink.vpn.wrapper.InvalidProfileException;
import xink.vpn.wrapper.KeyStore;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnState;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public abstract class VpnProfileEditor extends Activity {
    private static final int DESC_FONT_SIZE = 12;
    private static final int GRAY = 0xFF999999;
    private EditAction editAction;
    private VpnProfile profile;
    private EditText txtVpnName;
    private EditText txtServer;
    private EditText txtDnsSuffices;
    private EditText txtUserName;
    private EditText txtPassword;
    private VpnProfileRepository repository;
    private KeyStore keyStore;
    private Runnable resumeAction;
    private Object[] errMsgArgs; // prompt error message

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vpn_profile_editor);

        repository = VpnProfileRepository.getInstance(getApplicationContext());
        keyStore = new KeyStore(getApplicationContext());

        LinearLayout contentView = new LinearLayout(this);
        contentView.setOrientation(LinearLayout.VERTICAL);
        initWidgets(contentView);

        ScrollView containerView = (ScrollView) findViewById(R.id.editorScrollView);
        containerView.addView(contentView);

        Intent intent = getIntent();
        init(intent);
    }

    private void initWidgets(final ViewGroup content) {
        TextView lblVpnName = new TextView(this);
        lblVpnName.setText(getString(R.string.vpnname));
        content.addView(lblVpnName);

        txtVpnName = new EditText(this);
        txtVpnName.setLines(1);
        txtVpnName.setImeOptions(EditorInfo.IME_ACTION_DONE);
        content.addView(txtVpnName);

        TextView lblServer = new TextView(this);
        lblServer.setText(getString(R.string.server));
        content.addView(lblServer);

        txtServer = new EditText(this);
        txtServer.setLines(1);
        txtServer.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        content.addView(txtServer);

        initSpecificWidgets(content);

        TextView lblDnsSuffices = new TextView(this);
        lblDnsSuffices.setText(getString(R.string.dns_suffices));
        content.addView(lblDnsSuffices);

        TextView lblDnsSufficesDesc = new TextView(this);
        lblDnsSufficesDesc.setText(getString(R.string.comma_sep));
        lblDnsSufficesDesc.setTextColor(GRAY);
        lblDnsSufficesDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DESC_FONT_SIZE);
        content.addView(lblDnsSufficesDesc);

        txtDnsSuffices = new EditText(this);
        txtDnsSuffices.setLines(1);
        txtDnsSuffices.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        content.addView(txtDnsSuffices);

        TextView lblUserName = new TextView(this);
        lblUserName.setText(getString(R.string.username));
        content.addView(lblUserName);

        txtUserName = new EditText(this);
        txtUserName.setLines(1);
        txtUserName.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        content.addView(txtUserName);

        TextView lblPassword = new TextView(this);
        lblPassword.setText(getString(R.string.password));
        content.addView(lblPassword);

        txtPassword = new EditText(this);
        txtPassword.setLines(1);
        txtPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtPassword.setTransformationMethod(new PasswordTransformationMethod());
        content.addView(txtPassword);

        initButtons();
    }

    private void initButtons() {
        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                onSave();
            }
        });

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                onCancel();
            }
        });
    }

    private void init(final Intent intent) {
        editAction = EditAction.valueOf(intent.getAction());

        switch (editAction) {
        case CREATE:
            profile = createProfile();
            break;
        case EDIT:
            String name = (String) intent.getExtras().get(Constants.KEY_VPN_PROFILE_NAME);
            profile = repository.getProfileByName(name);
            initViewBinding();
            break;
        default:
            throw new AppException("failed to init VpnProfileEditor, unknown editAction: " + editAction); //$NON-NLS-1$
        }

        setTitle(profile.getType().getNameRid());
    }

    private void initViewBinding() {
        txtVpnName.setText(profile.getName());
        txtServer.setText(profile.getServerName());
        txtDnsSuffices.setText(profile.getDomainSuffices());
        txtUserName.setText(profile.getUsername());
        txtPassword.setText(profile.getPassword());
        doBindToViews();
    }

    protected abstract void doBindToViews();

    @Override
    protected Dialog onCreateDialog(final int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true).setMessage(""); //$NON-NLS-1$
        return builder.create();
    }

    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog) {
        Object[] args = errMsgArgs;
        errMsgArgs = null;
        ((AlertDialog) dialog).setMessage(getString(id, args));
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState == null)
            return;

        txtVpnName.setText(savedInstanceState.getCharSequence("name")); //$NON-NLS-1$
        txtServer.setText(savedInstanceState.getCharSequence("server")); //$NON-NLS-1$
        txtDnsSuffices.setText(savedInstanceState.getCharSequence("dns")); //$NON-NLS-1$
        txtUserName.setText(savedInstanceState.getCharSequence("user")); //$NON-NLS-1$
        txtPassword.setText(savedInstanceState.getCharSequence("password")); //$NON-NLS-1$
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putCharSequence("name", txtVpnName.getText()); //$NON-NLS-1$
        outState.putCharSequence("server", txtServer.getText()); //$NON-NLS-1$
        outState.putCharSequence("dns", txtDnsSuffices.getText()); //$NON-NLS-1$
        outState.putCharSequence("user", txtUserName.getText()); //$NON-NLS-1$
        outState.putCharSequence("password", txtPassword.getText()); //$NON-NLS-1$
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("xink", "VpnProfileEditor.onResume, check and run resume action");
        if (resumeAction != null) {
            Runnable action = resumeAction;
            resumeAction = null;
            runOnUiThread(action);
        }
    }

    protected abstract void doPopulateProfile();

    private void onSave() {
        try {
            populateProfile();
            saveProfile();
        } catch (InvalidProfileException e) {
            promptInvalidProfile(e);
        }
    }

    private void populateProfile() {
        String name = txtVpnName.getText().toString().trim();
        profile.setName(name);
        profile.setServerName(txtServer.getText().toString().trim());
        profile.setDomainSuffices(txtDnsSuffices.getText().toString().trim());
        profile.setUsername(txtUserName.getText().toString().trim());
        profile.setPassword(txtPassword.getText().toString().trim());
        profile.setState(VpnState.IDLE);
        doPopulateProfile();

        repository.checkProfile(profile);
    }

    private void saveProfile() {
        if (unlockKeyStoreIfNeeded()) {
            if (editAction == EditAction.CREATE) {
                repository.addVpnProfile(profile);
            } else {
                profile.postUpdate();
            }

            prepareResult();
            finish();
        }
    }

    private boolean unlockKeyStoreIfNeeded() {
        if (!profile.needKeyStoreToSave() || keyStore.isUnlocked())
            return true;

        Log.i("xink", "keystore is locked, unlock it now and redo saving later.");
        resumeAction = new Runnable() {
            @Override
            public void run() {
                // redo this after unlock activity return
                saveProfile();
            }
        };

        keyStore.unlock(this);
        return false;
    }

    private void prepareResult() {
        Intent intent = new Intent();
        intent.putExtra(Constants.KEY_VPN_PROFILE_NAME, profile.getName());
        setResult(RESULT_OK, intent);
    }

    private void promptInvalidProfile(final InvalidProfileException e) {
        errMsgArgs = e.getMessageArgs();
        showDialog(e.getMessageCode());
    }

    private void onCancel() {
        finish();
    }

    protected EditAction getEditAction() {
        return editAction;
    }

    protected abstract void initSpecificWidgets(final ViewGroup content);

    protected abstract VpnProfile createProfile();

    @SuppressWarnings("unchecked")
    protected <T extends VpnProfile> T getProfile() {
        return (T) profile;
    }

}
