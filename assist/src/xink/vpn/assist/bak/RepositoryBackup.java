package xink.vpn.assist.bak;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xink.crypto.Crypto;
import xink.vpn.AppException;
import xink.vpn.R;
import xink.vpn.wrapper.ProfileUtils;
import xink.vpn.wrapper.VpnProfile;
import android.content.Context;
import android.util.Log;

public class RepositoryBackup {

    private static final String TAG = "xink.RepoBak";

    private static final String FILE_PROFILES = "profiles";

    private static final String FILE_ACT_ID = "active_profile_id";

    private Context context;

    public RepositoryBackup(final Context context) {
        super();
        this.context = context;
    }

    public void backup(final byte[] code, final String path) {
        File dir = ensureDir(path);

        StringBuilder activeId = new StringBuilder();
        List<VpnProfile> profiles = new ArrayList<VpnProfile>();

        try {
            String repoJson = Crypto.decrypt(code);
            ProfileUtils.fromJson(context, repoJson, activeId, profiles);

            if (profiles.isEmpty()) {
                Log.i(TAG, "profile list is empty, will not export");
                return;
            }

            String id = activeId.length() > 0 ? activeId.toString() : null;
            backupActiveProfileId(id, dir);
            backupProfiles(profiles, dir);

        } catch (Throwable e) {
            throw new AppException("backup failed", e, R.string.err_exp_failed);
        }
    }

    private File ensureDir(final String path) {
        File dir = new File(path);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!dir.exists()) {
            throw new AppException("failed to mkdir: " + path, R.string.err_exp_write_storage_failed);
        }

        return dir;
    }

    private void backupProfiles(final List<VpnProfile> profiles, final File dir) throws Exception {
        byte[] profileBytes = toBytes(profiles);
        doBackup(profileBytes, dir, FILE_PROFILES);
    }

    private void backupActiveProfileId(final String activeProfileId, final File dir) throws IOException, Exception {
        byte[] activeIdBytes = toBytes(activeProfileId);
        doBackup(activeIdBytes, dir, FILE_ACT_ID);
    }

    private byte[] toBytes(final String string) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream os = null;

        try {
            os = new ObjectOutputStream(bytes);
            os.writeObject(string);
        } finally {
            if (os != null) {
                os.close();
            }
        }
        return bytes.toByteArray();
    }

    private byte[] toBytes(final List<VpnProfile> profiles) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream os = null;

        try {
            os = new ObjectOutputStream(bytes);
            for (VpnProfile p : profiles) {
                p.write(os);
            }
        } finally {
            if (os != null) {
                os.close();
            }
        }
        return bytes.toByteArray();
    }

    private void doBackup(final byte[] input, final File dir, final String name) throws Exception {
        InputStream is = new ByteArrayInputStream(input);
        OutputStream os = new FileOutputStream(new File(dir, name));
        Crypto.encrypt(is, os);
    }

    public byte[] restore(final String dir) {
        checkExternalData(dir);

        byte[] code = null;

        try {
            String activeId = restoreActiveProfileId(dir);
            List<VpnProfile> profiles = restoreProfiles(dir);
            code = Crypto.encrypt(ProfileUtils.toJson(activeId, profiles));
        } catch (Throwable e) {
            throw new AppException("restore failed", e, R.string.err_imp_failed);
        }

        return code;
    }

    private String restoreActiveProfileId(final String dir) throws Exception {
        byte[] bytes = doRestore(dir, FILE_ACT_ID);
        ObjectInputStream is = null;
        String id = null;

        try {
            is = new ObjectInputStream(new ByteArrayInputStream(bytes));
            id = (String) is.readObject();
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return id;
    }

    private List<VpnProfile> restoreProfiles(final String dir) throws Exception {
        List<VpnProfile> profiles = new ArrayList<VpnProfile>();

        byte[] bytes = doRestore(dir, FILE_PROFILES);
        ObjectInputStream is = null;

        try {
            is = new ObjectInputStream(new ByteArrayInputStream(bytes));
            ProfileUtils.loadProfiles(context, is, profiles);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return profiles;
    }

    private byte[] doRestore(final String dir, final String name) throws Exception {
        InputStream is = new FileInputStream(new File(dir, name));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Crypto.decrypt(is, os);
        return os.toByteArray();
    }

    /*
     * verify data files in external storage.
     */
    private void checkExternalData(final String path) {
        File id = new File(path, FILE_ACT_ID);
        File profiles = new File(path, FILE_PROFILES);

        if (!(verifyDataFile(id) && verifyDataFile(profiles))) {
            throw new AppException("no valid data found in: " + path, R.string.err_imp_nodata);
        }
    }

    private boolean verifyDataFile(final File file) {
        return file.exists() && file.isFile() && file.length() > 0;
    }

    /**
     * Check last backup time.
     *
     * @return timestamp of last backup, null for no backup.
     */
    public Date checkLastBackup(final String path) {
        File id = new File(path, FILE_ACT_ID);

        if (!verifyDataFile(id)) {
            throw new AppException("no valid data found in: " + path, R.string.err_imp_nodata);
        }

        return new Date(id.lastModified());
    }

}
