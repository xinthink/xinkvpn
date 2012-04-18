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

package xink.vpn;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import xink.crypto.StreamCrypto;
import xink.vpn.stats.VpnConnectivityStats;
import xink.vpn.wrapper.InvalidProfileException;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnState;
import xink.vpn.wrapper.VpnType;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * Repository of VPN profiles
 *
 * @author ywu
 */
public final class VpnProfileRepository {

    private static final String TAG = "xink";

    private static final String FILE_PROFILES = "profiles";

    private static final String FILE_ACT_ID = "active_profile_id";

    private static VpnProfileRepository instance;

    private Context context;
    private String activeProfileId;
    private List<VpnProfile> profiles;

    private VpnState activeVpnState;
    private VpnConnectivityStats connStats;

    private VpnProfileRepository(final Context ctx) {
        this.context = ctx;
        profiles = new ArrayList<VpnProfile>();
        connStats = new VpnConnectivityStats(ctx);
    }

    /**
     * Retrieves the single instance of repository.
     *
     * @param ctx
     *            Context
     * @return singleton
     */
    public static VpnProfileRepository getInstance(final Context ctx) {
        if (instance == null)  {
            instance = new VpnProfileRepository(ctx);
            instance.load();

            StreamCrypto.init(ctx);
        }

        return instance;
    }


    /**
     * Get state of the active vpn.
     */
    public VpnState getActiveVpnState() {
        if (activeVpnState == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            String v = prefs.getString(context.getString(R.string.active_vpn_state_key),
                    context.getString(R.string.active_vpn_state_default));
            activeVpnState = VpnState.valueOf(v);
        }

        return activeVpnState;
    }

    /**
     * Update state of the active vpn.
     */
    public void setActiveVpnState(final VpnState state) {
        if (!state.isStable()) return;

        this.activeVpnState = state;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(context.getString(R.string.active_vpn_state_key), state.toString()).commit();
    }

    /**
     * Retrieves the connectivity stats instance
     */
    public VpnConnectivityStats getConnectivityStats() {
        return this.connStats;
    }

    public void save() {
        Log.d(TAG, "save, activeId=" + activeProfileId + ", profiles=" + profiles);

        try {
            saveActiveProfileId();
            saveProfiles();
        } catch (IOException e) {
            Log.e(TAG, "save profiles failed", e);
        }
    }

    private void saveActiveProfileId() throws IOException {
        ObjectOutputStream os = null;

        try {
            os = new ObjectOutputStream(openPrivateFileOutput(FILE_ACT_ID));
            os.writeObject(activeProfileId);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    private void saveProfiles() throws IOException {
        ObjectOutputStream os = null;

        try {
            os = new ObjectOutputStream(openPrivateFileOutput(FILE_PROFILES));
            for (VpnProfile p : profiles) {
                p.write(os);
            }
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    private FileOutputStream openPrivateFileOutput(final String fileName) throws FileNotFoundException {
        return context.openFileOutput(fileName, Context.MODE_PRIVATE);
    }

    private void load() {
        try {
            loadActiveProfileId();
            loadProfiles();

            Log.d(TAG, "loaded, activeId=" + activeProfileId + ", profiles=" + profiles);
        } catch (Exception e) {
            Log.e(TAG, "load profiles failed", e);
        }
    }

    private void loadActiveProfileId() throws IOException, ClassNotFoundException {
        ObjectInputStream is = null;

        try {
            is = new ObjectInputStream(context.openFileInput(FILE_ACT_ID));
            activeProfileId = (String) is.readObject();
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private void loadProfiles() throws Exception {
        ObjectInputStream is = null;

        try {
            is = new ObjectInputStream(context.openFileInput(FILE_PROFILES));
            loadProfilesFrom(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private void loadProfilesFrom(final ObjectInputStream is) throws Exception {
        Object obj = null;

        try {
            while (true) {
                VpnType type = (VpnType) is.readObject();
                obj = is.readObject();
                loadProfileObject(type, obj, is);
            }
        } catch (EOFException eof) {
            Log.d(TAG, "reach the end of profiles file");
        }
    }

    private void loadProfileObject(final VpnType type, final Object obj, final ObjectInputStream is) throws Exception {
        if (obj == null)
            return;

        VpnProfile p = VpnProfile.newInstance(type, context);
        if (p.isCompatible(obj)) {
            p.read(obj, is);
            profiles.add(p);
        } else {
            Log.e(TAG, "saved profile '" + obj + "' is NOT compatible with " + type);
        }
    }

    public void setActiveProfile(final VpnProfile profile) {
        Log.i(TAG, "active vpn set to: " + profile);
        activeProfileId = profile.getId();
    }

    public String getActiveProfileId() {
        return activeProfileId;
    }

    public VpnProfile getActiveProfile() {
        if (activeProfileId == null)
            return null;

        return getProfileById(activeProfileId);
    }

    private VpnProfile getProfileById(final String id) {
        for (VpnProfile p : profiles) {
            if (p.getId().equals(id))
                return p;
        }
        return null;
    }

    public VpnProfile getProfileByName(final String name) {
        for (VpnProfile p : profiles) {
            if (p.getName().equals(name))
                return p;
        }
        return null;
    }

    /**
     * @return a read-only view of the VpnProfile list.
     */
    public List<VpnProfile> getAllVpnProfiles() {
        return Collections.unmodifiableList(profiles);
    }

    public synchronized void addVpnProfile(final VpnProfile p) {
        p.postConstruct();
        profiles.add(p);
    }

    public void checkProfile(final VpnProfile newProfile) {
        String newName = newProfile.getName();

        if (TextUtils.isEmpty(newName))
            throw new InvalidProfileException("profile name is empty.", R.string.err_empty_name);

        for (VpnProfile p : profiles) {
            if (newProfile != p && newName.equals(p.getName()))
                throw new InvalidProfileException("duplicated profile name '" + newName + "'.", R.string.err_duplicated_profile_name, newName);
        }

        newProfile.validate();
    }

    public synchronized void deleteVpnProfile(final VpnProfile profile) {
        String id = profile.getId();
        boolean removed = profiles.remove(profile);
        Log.d(TAG, "delete vpn: " + profile + ", removed=" + removed);

        if (id.equals(activeProfileId)) {
            activeProfileId = null;
            Log.d(TAG, "deactivate vpn: " + profile);
        }
    }

    public void backup(final String path) {
        if (profiles.isEmpty()) {
            Log.i(TAG, "profile list is empty, will not export");
            return;
        }

        save();
        File dir = ensureDir(path);

        try {
            doBackup(dir, FILE_ACT_ID);
            doBackup(dir, FILE_PROFILES);
        } catch (Exception e) {
            throw new AppException("backup failed", e, R.string.err_exp_failed);
        }
    }

    private File ensureDir(final String path) {
        File dir = new File(path);
        Utils.ensureDir(dir);

        return dir;
    }

    private void doBackup(final File dir, final String name) throws Exception {
        InputStream is = context.openFileInput(name);
        OutputStream os = new FileOutputStream(new File(dir, name));
        StreamCrypto.encrypt(is, os);
    }

    public void restore(final String dir) {
        checkExternalData(dir);

        try {
            doRestore(dir, FILE_ACT_ID);
            doRestore(dir, FILE_PROFILES);

            clean();
            load();
        } catch (Exception e) {
            throw new AppException("restore failed", e, R.string.err_imp_failed);
        }
    }

    private void clean() {
        activeProfileId = null;
        profiles.clear();
    }

    private void doRestore(final String dir, final String name) throws Exception {
        InputStream is = new FileInputStream(new File(dir, name));
        OutputStream os = openPrivateFileOutput(name);
        StreamCrypto.decrypt(is, os);
    }

    /*
     * verify data files in external storage.
     */
    private void checkExternalData(final String path) {
        File id = new File(path, FILE_ACT_ID);
        File profiles = new File(path, FILE_PROFILES);

        if (!(verifyDataFile(id) && verifyDataFile(profiles)))
            throw new AppException("no valid data found in: " + path, R.string.err_imp_nodata);
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

        if (!verifyDataFile(id))
            return null;

        return new Date(id.lastModified());
    }
}
