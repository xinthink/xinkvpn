package xink.vpn;

import static xink.vpn.Constants.*;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class HackKeyStore extends Activity {

    private static final String TAG = "xink.HackKeyStore";

    private CheckBox ckbIgnore;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hack);
        ((TextView) findViewById(R.id.txtMsg)).setText(R.string.i_hack_keystore);

        ((Button) findViewById(R.id.btnOk)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                hackKeyStore();
            }
        });

        ((Button) findViewById(R.id.btnCancel)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                finish();
            }
        });
    }

    // public static Dialog createDialog(final Activity ctx) {
    // AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
    // builder.setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.hack_keystore).setMessage(R.string.i_hack_keystore);
    // builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    // @Override
    // public void onClick(final DialogInterface dialog, final int which) {
    // hackKeyStore(ctx);
    // }
    // }).setNegativeButton(android.R.string.cancel, null);
    //
    // AlertDialog dlg = builder.create();
    // dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
    // @Override
    // public void onDismiss(final DialogInterface dialog) {
    // ctx.removeDialog(DLG_HACK);
    // }
    // });
    // return dlg;
    // }

    private void hackKeyStore() {
        try {
            doHackKeyStore();
            showDialog(DLG_HACK);
        } catch (AppException e) {
            Log.e(TAG, "hack keystore failed", e);
            Utils.showErrMessage(this, e);
        }
    }

    private void doHackKeyStore() {
        File temp = new File("/sdcard/tmp/keystore");
        Utils.copyAsset(this, "keystore", temp);

        StringBuilder cmd = new StringBuilder();
        cmd.append("mount -o rw,remount /system\n");

        cmd.append("mv /system/bin/keystore /system/bin/keystore_orig\n");
        cmd.append("mv ").append(temp.getAbsolutePath()).append(" /system/bin/\n");
        cmd.append("chmod 755 /system/bin/keystore\n");

        // cmd.append("mount -o ro,remount /system\n");

        Utils.sudo(cmd.toString());
    }

    private Dialog createRebootConfirmDlg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.reboot).setMessage(R.string.i_reboot);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                reboot();
            }
        }).setNegativeButton(android.R.string.cancel, null);

        AlertDialog dlg = builder.create();
        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                removeDialog(DLG_HACK);
            }
        });
        return dlg;
    }

    private void reboot() {
        try {
            Utils.reboot();
        } catch (AppException e) {
            Log.e(TAG, "reboot failed", e);
            Utils.showErrMessage(this, e);
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        if (id == DLG_HACK) {
            return createRebootConfirmDlg();
        }
        return super.onCreateDialog(id);
    }
}
