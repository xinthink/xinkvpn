package xink.vpn.common;

import xink.vpn.AppException;
import xink.vpn.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public abstract class AbstractDialogActivity extends Activity {

    protected final String TAG = "xink." + getClass().getSimpleName();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setTitle(android.R.string.dialog_alert_title);

        setContentView(R.layout.dialog);

        ((Button) findViewById(R.id.btnOk)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                onPositiveButtonClink();
            }
        });

        ((Button) findViewById(R.id.btnCancel)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
    }

    protected abstract void onPositiveButtonClink();

    protected void showErrMessage(final AppException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true).setMessage(getString(e.getMessageResourceId(), e.getMessageArgs()));

        AlertDialog dlg = builder.create();
        dlg.setOwnerActivity(this);
        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                AbstractDialogActivity.this.finish();
            }
        });
        dlg.show();
    }
}
