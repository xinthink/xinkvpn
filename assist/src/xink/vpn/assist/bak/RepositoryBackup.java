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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xink.crypto.StreamCrypto;
import xink.vpn.AppException;
import xink.vpn.R;
import xink.vpn.wrapper.ProfileUtils;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnType;
import android.content.Context;
import android.util.Log;

public class RepositoryBackup {

    private static final String JSON_FIELD_PROFILES = "profiles";

    private static final String JSON_FIELD_ACTIVE_ID = "activeProfileId";

    private static final String TAG = "xink";

    private static final String FILE_PROFILES = "profiles";

    private static final String FILE_ACT_ID = "active_profile_id";

    private Context context;

    public void backup(final String repoJson, final String path) {
        File dir = ensureDir(path);

        StringBuilder activeId = new StringBuilder();
        List<VpnProfile> profiles = new ArrayList<VpnProfile>();

        try {
            parseProfiles(repoJson, activeId, profiles);

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

    private void parseProfiles(final String repoJson, final StringBuilder activeId, final List<VpnProfile> profiles) throws JSONException {
        JSONObject repo = new JSONObject(repoJson);
        if (!repo.isNull(JSON_FIELD_ACTIVE_ID)) {
            activeId.append(repo.getString(JSON_FIELD_ACTIVE_ID));
        }

        parseProfiles(repo, profiles);
    }

    private void parseProfiles(final JSONObject repo, final List<VpnProfile> profiles) throws JSONException {
        JSONArray arr = repo.getJSONArray(JSON_FIELD_PROFILES);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject jo = arr.getJSONObject(i);
            profiles.add(parseProfile(jo));
        }
    }

    private VpnProfile parseProfile(final JSONObject jo) throws JSONException {
        VpnType type = VpnType.valueOf(jo.getString("type"));
        VpnProfile p = VpnProfile.newInstance(type, context);
        p.fromJson(jo);
        return p;
    }

    protected void backupProfiles(final List<VpnProfile> profiles, final File dir) throws Exception {
        byte[] profileBytes = toBytes(profiles);
        doBackup(profileBytes, dir, FILE_PROFILES);
    }

    protected void backupActiveProfileId(final String activeProfileId, final File dir) throws IOException, Exception {
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
        StreamCrypto.encrypt(is, os);
    }

    public String restore(final String dir) {
        checkExternalData(dir);

        String result = null;

        try {
            String activeId = restoreActiveProfileId(dir);
            List<VpnProfile> profiles = restoreProfiles(dir);
            result = ProfileUtils.makeJsonString(activeId, profiles);
        } catch (Throwable e) {
            throw new AppException("restore failed", e, R.string.err_imp_failed);
        }

        return result;
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
        StreamCrypto.decrypt(is, os);
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
            return null;
        }

        return new Date(id.lastModified());
    }


}
