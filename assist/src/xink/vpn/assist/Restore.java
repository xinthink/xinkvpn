package xink.vpn.assist;

import static xink.vpn.Constants.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import xink.vpn.AppException;
import xink.vpn.assist.bak.RepositoryBackup;
import xink.vpn.common.AbstractDialogActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class Restore extends AbstractDialogActivity {

    private RepositoryBackup backup;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backup = new RepositoryBackup(getApplicationContext());

        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.info);
        setTitle(R.string.imp);

        try {
            String lastBak = checkLastBackupDate();
            ((TextView) findViewById(R.id.txtMsg)).setText(getString(R.string.i_imp, lastBak));
        } catch (AppException e) {
            Log.e(TAG, "no backup found", e);
            showErrMessage(e);
        }
    }

    private String checkLastBackupDate() {
        Date lastBak = backup.checkLastBackup(getBakDir());
        SimpleDateFormat f = new SimpleDateFormat(getString(R.string.date_format));

        return f.format(lastBak);
    }

    @Override
    protected void onPositiveButtonClink() {
        try {
            doRestore();
            finish();
        } catch (AppException e) {
            Log.e(TAG, "doRestore failed", e);
            showErrMessage(e);
        }
    }

    private void doRestore() {
        byte[] repoJson = backup.restore(getBakDir());
        Toast.makeText(this, R.string.i_imp_done, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent();
        intent.putExtra(KEY_REPOSITORY, repoJson);
        setResult(RESULT_OK, intent);
    }

    private String getBakDir() {
        return getString(R.string.exp_dir);
    }
}
