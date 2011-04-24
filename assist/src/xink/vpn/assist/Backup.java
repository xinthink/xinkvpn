package xink.vpn.assist;

import static xink.vpn.Constants.*;
import xink.vpn.AppException;
import xink.vpn.assist.bak.RepositoryBackup;
import xink.vpn.common.AbstractDialogActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class Backup extends AbstractDialogActivity {

    private RepositoryBackup backup;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backup = new RepositoryBackup(getApplicationContext());

        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.info);
        setTitle(R.string.export);

        String msg = getString(R.string.i_exp, getString(R.string.exp_dir));
        ((TextView) findViewById(R.id.txtMsg)).setText(msg);
    }

    @Override
    protected void onPositiveButtonClink() {
        Intent data = getIntent();
        byte[] repoJson = data.getByteArrayExtra(KEY_REPOSITORY);
        if (repoJson == null) {
            Log.e(TAG, "repository json string not found from intent");
            finish();
        }

        try {
            doBackup(repoJson);
            finish();
        } catch (AppException e) {
            Log.e(TAG, "doBackup failed", e);
            showErrMessage(e);
        }
    }

    private void doBackup(final byte[] repoJson) {
        backup.backup(repoJson, getString(R.string.exp_dir));
        Toast.makeText(this, R.string.i_exp_done, Toast.LENGTH_SHORT).show();
    }
}
