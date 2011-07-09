package xink.vpn;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Environment;

public class Utils {

    // private static final String TAG = "xink.Utils";

    public static void showErrMessage(final Activity ctx, final AppException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(true).setMessage(ctx.getString(e.getMessageCode(), e.getMessageArgs()));

        AlertDialog dlg = builder.create();
        dlg.setOwnerActivity(ctx);
        dlg.show();
    }

    public static void ensureDir(final File dir) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            throw new AppException("no writable storage found, state=" + state, R.string.err_no_writable_storage);
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!dir.exists()) {
            throw new AppException("failed to mkdir: " + dir, R.string.err_exp_write_storage_failed);
        }
    }

    private Utils() {

    }
}
