package xink.vpn;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class Utils {

    private static final String TAG = "xink.Utils";

    public static void showErrMessage(final Activity ctx, final AppException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(true).setMessage(ctx.getString(e.getMessageResourceId(), e.getMessageArgs()));

        AlertDialog dlg = builder.create();
        dlg.setOwnerActivity(ctx);
        dlg.show();
    }

    public static void copyAsset(final Context ctx, final String file, final File target) {
        File dir = target.getParentFile();
        ensureDir(dir);

        AssetManager assets = ctx.getAssets();

        try {
            cp(assets.open(file), new FileOutputStream(target));
        } catch (Throwable e) {
            throw new AppException("failed to copy " + file + " -> " + target, e, R.string.err_exp_write_storage_failed);
        }
    }

    private static void cp(final InputStream in, final OutputStream out) throws Exception {
        try {
            byte[] buf = new byte[256];
            int size;

            while ((size = in.read(buf)) > 0) {
                out.write(buf, 0, size);
            }
        } finally {
            close(in, out);
        }
    }

    private static void close(final InputStream in, final OutputStream out) throws IOException {
        if (in != null) {
            in.close();
        }

        if (out != null) {
            out.close();
        }
    }

    public static void ensureDir(final File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!dir.exists()) {
            throw new AppException("failed to mkdir: " + dir, R.string.err_exp_write_storage_failed);
        }
    }

    public static void sudo(final String cmd) {
        Process p = null;

        try {
            p = sudoImpl(cmd);
        } catch (AppException e) {
            throw e;
        } catch (Throwable e) {
            throw new AppException("failed to sudo:\n " + cmd, e, R.string.err_privcmd);
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    private static Process sudoImpl(final String cmd) throws Exception {
        Process p = new ProcessBuilder("su").redirectErrorStream(true).start();

        DataOutputStream os = new DataOutputStream(p.getOutputStream());
        os.writeBytes(cmd);

        // Close the terminal
        os.writeBytes("\nexit\n");
        os.flush();
        p.waitFor();

        int r = p.exitValue();
        String msg = readPrompt(p);
        Log.d(TAG, "sudo result=" + r + ", msg=" + msg);

        if (r != 0) {
            throw new AppException("failed to sudo:\n " + cmd, R.string.err_privcmd);
        }
        return p;
    }

    private static String readPrompt(final Process p) throws IOException {
        InputStream is = p.getInputStream();
        byte[] buf = new byte[128];
        int len;
        StringBuilder prompt = new StringBuilder();

        while ((len = is.read(buf)) > 0) {
            byte[] bytes = new byte[len];
            System.arraycopy(buf, 0, bytes, 0, len);
            prompt.append(new String(bytes));
        }
        return prompt.toString();
    }

    public static void reboot() {
        sudo("sync;sync;sync;reboot");
    }

    private Utils() {

    }
}
