package xink.vpn;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xink.vpn.wrapper.InvalidProfileException;
import xink.vpn.wrapper.ProfileUtils;
import xink.vpn.wrapper.VpnProfile;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class VpnProfileRepository {

    private static final String TAG = "xink";

    private static final String FILE_PROFILES = "profiles";

    private static final String FILE_ACT_ID = "active_profile_id";

    private static VpnProfileRepository instance;

    private Context context;
    private String activeProfileId;
    private List<VpnProfile> profiles;

    private VpnProfileRepository(final Context ctx) {
        this.context = ctx;
        profiles = new ArrayList<VpnProfile>();
    }

    public static VpnProfileRepository getInstance(final Context ctx) {
        if (instance == null)  {
            instance = new VpnProfileRepository(ctx);
            instance.load();
        }

        return instance;
    }

    public void save() {
        Log.d(TAG, "save, activeId=" + activeProfileId + ", profiles=" + profiles);

        try {
            saveActiveProfileId();
            saveProfiles();
        } catch (Throwable e) {
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
        } catch (Throwable e) {
            Log.e(TAG, "load profiles failed", e);
        }
    }

    private void loadActiveProfileId() throws Exception {
        ObjectInputStream is = null;

        try {
            is = new ObjectInputStream(context.openFileInput(FILE_ACT_ID));
            activeProfileId = (String) is.readObject();
        } catch (Exception e) {
            Log.w(TAG, "loadActiveProfileId failed", e);
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
            ProfileUtils.loadProfiles(context, is, profiles);
        } finally {
            if (is != null) {
                is.close();
            }
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
        if (activeProfileId == null) {
            return null;
        }

        return getProfileById(activeProfileId);
    }

    private VpnProfile getProfileById(final String id) {
        for (VpnProfile p : profiles) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public VpnProfile getProfileByName(final String name) {
        for (VpnProfile p : profiles) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    /**
     * @return a read-only view of the VpnProfile list.
     */
    public List<VpnProfile> getAllVpnProfiles() {
        return Collections.unmodifiableList(profiles);
    }

    public void addVpnProfile(final VpnProfile p) {
        p.postConstruct();
        profiles.add(p);
    }

    public void checkProfile(final VpnProfile newProfile) {
        String newName = newProfile.getName();

        if (TextUtils.isEmpty(newName)) {
            throw new InvalidProfileException("profile name is empty.", R.string.err_empty_name);
        }

        for (VpnProfile p : profiles) {
            if (newProfile != p && newName.equals(p.getName())) {
                throw new InvalidProfileException("duplicated profile name '" + newName + "'.", R.string.err_duplicated_profile_name, newName);
            }
        }

        newProfile.validate();
    }

    public void deleteVpnProfile(final VpnProfile profile) {
        String id = profile.getId();
        boolean removed = profiles.remove(profile);
        Log.d(TAG, "delete vpn: " + profile + ", removed=" + removed);

        if (id.equals(activeProfileId)) {
            activeProfileId = null;
            Log.d(TAG, "deactivate vpn: " + profile);
        }
    }
}
