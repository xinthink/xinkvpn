package xink.vpn.editor;

import xink.vpn.Constants;
import xink.vpn.R;
import xink.vpn.VpnProfileRepository;
import xink.vpn.VpnSettings;
import xink.vpn.wrapper.InvalidProfileException;
import xink.vpn.wrapper.VpnProfile;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public abstract class VpnProfileEditor extends Activity {

    private EditAction editAction;
    private VpnProfile profile;
    private EditText txtVpnName;
    private EditText txtServer;
    private EditText txtDnsSuffices;
    private EditText txtUserName;
    private EditText txtPassword;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vpn_profile_editor);

        Intent intent = getIntent();
        editAction = EditAction.valueOf(intent.getAction());
        initProfile(intent);

        ScrollView scrollView = (ScrollView) findViewById(R.id.editorScrollView);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(content);
        initWidgets(content);
    }

    private void initProfile(final Intent intent) {
        if (editAction == EditAction.CREATE) {
            profile = createProfile();
        } else {
            profile = (VpnProfile) intent.getExtras().get(Constants.KEY_VPN_PROFILE);
        }

        setTitle(profile.getType().getNameRid());
    }

    private void initWidgets(final ViewGroup content) {
        TextView lblVpnName = new TextView(this);
        lblVpnName.setText("Name");
        content.addView(lblVpnName);

        txtVpnName = new EditText(this);
        content.addView(txtVpnName);

        TextView lblServer = new TextView(this);
        lblServer.setText("Server");
        content.addView(lblServer);

        txtServer = new EditText(this);
        content.addView(txtServer);

        initSpecificWidgets(content);

        TextView lblDnsSuffices = new TextView(this);
        lblDnsSuffices.setText("DNS Suffices");
        content.addView(lblDnsSuffices);

        TextView lblDnsSufficesDesc = new TextView(this);
        lblDnsSufficesDesc.setText("comma separated");
        lblDnsSufficesDesc.setTextColor(0xFF999999);
        lblDnsSufficesDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        content.addView(lblDnsSufficesDesc);

        txtDnsSuffices = new EditText(this);
        content.addView(txtDnsSuffices);

        TextView lblUserName = new TextView(this);
        lblUserName.setText("User Name");
        content.addView(lblUserName);

        txtUserName = new EditText(this);
        content.addView(txtUserName);

        TextView lblPassword = new TextView(this);
        lblPassword.setText("Password");
        content.addView(lblPassword);

        txtPassword = new EditText(this);
        txtPassword.setTransformationMethod(new PasswordTransformationMethod());
        content.addView(txtPassword);

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

    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog, final Bundle args) {
        Object[] msgArgs = (Object[]) args.getSerializable("messageArgs");
        ((AlertDialog) dialog).setMessage(getString(id, msgArgs));
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true).setMessage("xxx");
        return builder.create();
    }

    protected void onSave() {
        VpnProfileRepository actor = VpnProfileRepository.getInstance(getApplicationContext());

        try {
            String name = txtVpnName.getText().toString().trim();
            profile.setName(name);
            profile.setServerName(txtServer.getText().toString().trim());
            profile.setDomainSuffices(txtDnsSuffices.getText().toString().trim());
            profile.setUsername(txtUserName.getText().toString().trim());
            profile.setPassword(txtPassword.getText().toString().trim());
            populate();

            actor.addVpnProfile(profile);
            Intent intent = new Intent(this, VpnSettings.class);
            intent.putExtra(Constants.KEY_VPN_PROFILE, profile.getId());
            setResult(Constants.REQ_ADD_VPN, intent);
            finish();

        } catch (InvalidProfileException e) {
            Bundle args = new Bundle();
            args.putSerializable("messageArgs", e.getMessageArgs());
            showDialog(e.getMessageResourceId(), args);
        }
    }

    protected abstract void populate();

    protected void onCancel() {
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
