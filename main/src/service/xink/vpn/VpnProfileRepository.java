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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xink.crypto.AesCrypto;
import xink.vpn.stats.VpnConnectivityStats;
import android.content.Context;
import android.text.TextUtils;

/**
 * Repository of VPN profiles
 *
 * @author ywu
 */
public final class VpnProfileRepository {

    private static final Logger LOG = LoggerFactory.getLogger("xink.vpnrepo");

    private static final String FILE_PROFILES = "profiles_store";

    private static VpnProfileRepository instance;

    private Context context;
    private Store store;

    private VpnProfile activeVpn;
    private VpnConnectivityStats connStats;

    private VpnProfileRepository(final Context ctx) {
        this.context = ctx;
    }

    /**
     * Retrieves the single instance of repository.
     *
     * @return singleton
     */
    public static VpnProfileRepository i() {
        if (instance == null)  {
            instance = new VpnProfileRepository(XinkVpnApp.i());
            instance.load();
        }

        return instance;
    }

    /**
     * Get state of the active vpn.
     */
    public VpnState getActiveVpnState() {
        if (activeVpn == null)
            return VpnState.IDLE;

        return activeVpn.state;

        // if (activeVpnState == null) {
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //
        // String v = prefs.getString(context.getString(R.string.active_vpn_state_key),
        // context.getString(R.string.active_vpn_state_default));
        // activeVpnState = VpnState.valueOf(v);
        // }
        //
        // return activeVpnState;
    }

    /**
     * Update state of the active vpn.
     */
    public void setActiveVpnState(final VpnState state) {
        if (!state.isStable() || activeVpn == null)
            return;

        activeVpn.state = state;

        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // prefs.edit().putString(context.getString(R.string.active_vpn_state_key), state.toString()).commit();
    }

    /**
     * Retrieves the connectivity stats instance
     */
    public VpnConnectivityStats getConnectivityStats() {
        return this.connStats;
    }

    public void save() {
        LOG.info("saving profile store");

        try {
            store.save();
        } catch (Exception e) {
            throw new AppException("failed to save profiles", e); // TODO prompt save failure
        }
    }

    private void load() {
        try {
            store = Store.load();
            activeVpn = store.getActiveVpn();
        } catch (Exception e) {
            LOG.error("load profiles failed", e);
        }
    }

    public void setActiveProfileId(final String id) {
        store.setActiveProfileId(id);
        activeVpn = store.getActiveVpn();
    }

    public String getActiveProfileId() {
        return store.activeProfileId;
    }

    public VpnProfile getActiveProfile() {
        return activeVpn;
    }

    public VpnProfile getProfileByName(final String name) {
        for (VpnProfile p : store.getProfileCollection()) {
            if (p.name.equals(name))
                return p;
        }
        return null;
    }

    /**
     * @return a read-only view of the VpnProfile list.
     */
    public List<VpnProfile> getAllVpnProfiles() {
        return Collections.unmodifiableList(new ArrayList<VpnProfile>(store.getProfileCollection()));
    }

    public synchronized void addVpnProfile(final VpnProfile p) {
        p.postConstruct();
        store.add(p);
    }

    public void checkProfile(final VpnProfile newProfile) {
        String newName = newProfile.name;

        if (TextUtils.isEmpty(newName))
            throw new InvalidProfileException("profile name is empty.", R.string.err_empty_name);

        for (VpnProfile p : store.getProfileCollection()) {
            if (newProfile != p && newName.equals(p.name))
                throw new InvalidProfileException("duplicated profile name '" + newName + "'.", R.string.err_duplicated_profile_name, newName);
        }

        newProfile.validate();
    }

    public synchronized void deleteVpnProfile(final VpnProfile profile) {
        store.remove(profile);
    }

    public void backup(final String path) {
        if (store.isEmpty()) {
            LOG.info("profile list is empty, will not export");
            return;
        }

        save();
        File dir = ensureDir(path);

        try {
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
        // AesCrypto.encrypt(is, os);
    }

    public void restore(final String dir) {
        checkExternalData(dir);

        try {
            doRestore(dir, FILE_PROFILES);

            clean();
            load();
        } catch (Exception e) {
            throw new AppException("restore failed", e, R.string.err_imp_failed);
        }
    }

    private void clean() {
        activeVpn = null;
        store = null;
    }

    private void doRestore(final String dir, final String name) throws Exception {
        // InputStream is = new FileInputStream(new File(dir, name));
        // OutputStream os = openPrivateFileOutput(name);
        // StreamCrypto.decrypt(is, os);
    }

    /*
     * verify data files in external storage.
     */
    private void checkExternalData(final String path) {
        File id = new File(path, FILE_PROFILES);
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
        File id = new File(path, FILE_PROFILES);

        if (!verifyDataFile(id))
            return null;

        return new Date(id.lastModified());
    }

    /**
     * Profiles storage
     */
    private static class Store {
        String activeProfileId;
        Map<String, VpnProfile> profiles = new LinkedHashMap<String, VpnProfile>();
        boolean isDirty;

        /**
         * load saved profiles from internal storage
         */
        static Store load() throws IOException, GeneralSecurityException, JSONException {
            Store i = null;

            InputStream is = null;
            try {
                is = XinkVpnApp.i().openFileInput(FILE_PROFILES);
                i = load(is);
            } catch (FileNotFoundException e) {
                LOG.info("store file not found, init an empty store");
                i = new Store();
            } finally {
                if (is != null) {
                    is.close();
                }
            }

            return i;
        }

        private static Store load(final InputStream is) throws IOException, GeneralSecurityException, JSONException {
            ByteArrayOutputStream result = new ByteArrayOutputStream();

            byte[] buf = new byte[64];
            int n;

            while ((n = is.read(buf)) != -1) {
                result.write(buf, 0, n);
            }

            String json = AesCrypto.decrypt(result.toByteArray());
            return fromJson(json);
        }

        private static Store fromJson(final String json) throws JSONException {
            Store s = new Store();

            JSONObject jo = new JSONObject(json);
            s.activeProfileId = jo.optString("activeProfileId");

            JSONArray arrProfiles = jo.getJSONArray("profiles");
            for (int i = 0; i < arrProfiles.length(); i++) {
                JSONObject jsonProfile = arrProfiles.getJSONObject(i);
                VpnProfile p = VpnProfile.fromJson(jsonProfile);
                s.profiles.put(p.id, p);
            }

            return s;
        }

        /**
         * save the whole strage to an internal file
         */
        void save() throws IOException, GeneralSecurityException, JSONException {
            if (!isDirty) return;

            OutputStream os = null;

            try {
                os = XinkVpnApp.i().openFileOutput(FILE_PROFILES, Context.MODE_PRIVATE);
                os.write(AesCrypto.encrypt(toJson()));
                isDirty = false;
            } finally {
                if (os != null) {
                    os.close();
                }
            }
        }

        private String toJson() throws JSONException {
            JSONObject o = new JSONObject();
            o.putOpt("activeProfileId", activeProfileId);

            JSONArray arrProfiles = new JSONArray();
            for (VpnProfile p : profiles.values()) {
                arrProfiles.put(p.toJson());
            }
            o.put("profiles", arrProfiles);

            return o.toString();
        }

        VpnProfile getActiveVpn() {
            if (TextUtils.isEmpty(activeProfileId) || profiles == null)
                return null;

            return profiles.get(activeProfileId);
        }

        Collection<VpnProfile> getProfileCollection() {
            return profiles.values();
        }

        void add(final VpnProfile p) {
            if (profiles.containsKey(p.id))
                return;

            profiles.put(p.id, p);
            isDirty = true;
        }

        void remove(final VpnProfile p) {
            if (profiles.remove(p.id) != null) {
                isDirty = true;
            }

            if (p.id.equals(activeProfileId)) {
                activeProfileId = null;
                isDirty = true;
            }
        }

        void setActiveProfileId(final String id) {
            Assert.notNull(id);
            if (!id.equals(activeProfileId)) {
                activeProfileId = id;
                isDirty = true;
            }
        }

        boolean isEmpty() {
            return profiles.isEmpty();
        }

    }
}
