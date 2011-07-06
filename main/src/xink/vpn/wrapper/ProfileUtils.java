package xink.vpn.wrapper;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public final class ProfileUtils {

    private static final String TAG = "xink.ProfileUtils";

    private static final String JSON_FIELD_PROFILES = "profiles";

    private static final String JSON_FIELD_ACTIVE_ID = "activeProfileId";

    public static void loadProfiles(final Context context, final ObjectInputStream is, final List<VpnProfile> profiles) throws Exception {
        Object obj = null;

        try {
            while (true) {
                VpnType type = (VpnType) is.readObject();
                obj = is.readObject();
                loadProfileObject(context, type, obj, is, profiles);
            }
        } catch (EOFException eof) {
            Log.i(TAG, "reach the end of profiles file");
        }
    }

    private static void loadProfileObject(final Context context, final VpnType type, final Object obj, final ObjectInputStream is,
            final List<VpnProfile> profiles)
            throws Exception {
        if (obj == null) {
            return;
        }

        VpnProfile p = VpnProfile.newInstance(type, context);
        if (p.isCompatible(obj)) {
            p.read(obj, is);
            profiles.add(p);
        } else {
            Log.e(TAG, "saved profile '" + obj + "' is NOT compatible with " + type);
        }
    }

    public static String toJson(final String activeId, final List<VpnProfile> profiles) throws JSONException {
        JSONObject repoJson = new JSONObject();
        repoJson.put(JSON_FIELD_ACTIVE_ID, activeId == null ? JSONObject.NULL : activeId);
        makeJsonString(profiles, repoJson);
        return repoJson.toString();
    }

    private static void makeJsonString(final List<VpnProfile> profiles, final JSONObject repoJson) throws JSONException {
        JSONArray profilesJson = new JSONArray();
        for (VpnProfile p : profiles) {
            JSONObject jo = makeJson(p);
            profilesJson.put(jo);
        }
        repoJson.put(JSON_FIELD_PROFILES, profilesJson);
    }

    private static JSONObject makeJson(final VpnProfile p) throws JSONException {
        JSONObject jo = new JSONObject();
        p.toJson(jo);
        return jo;
    }

    public static void fromJson(final Context context, final String repoJson, final StringBuilder activeId, final List<VpnProfile> profiles)
            throws JSONException {
        JSONObject repo = new JSONObject(repoJson);
        if (!repo.isNull(JSON_FIELD_ACTIVE_ID)) {
            activeId.append(repo.getString(JSON_FIELD_ACTIVE_ID));
        }

        parseProfiles(context, repo, profiles);
    }

    private static void parseProfiles(final Context context, final JSONObject repo, final List<VpnProfile> profiles) throws JSONException {
        JSONArray arr = repo.getJSONArray(JSON_FIELD_PROFILES);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject jo = arr.getJSONObject(i);
            profiles.add(parseProfile(context, jo));
        }
    }

    private static VpnProfile parseProfile(final Context context, final JSONObject jo) throws JSONException {
        VpnType type = VpnType.valueOf(jo.getString("type"));
        VpnProfile p = VpnProfile.newInstance(type, context);
        p.fromJson(jo);
        return p;
    }

    private ProfileUtils() {
    }
}
