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

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;


public abstract class VpnProfile implements Cloneable {

    public String id;

    public String name;

    public String server; // vpn server IP address or host name

    public String username;

    public String password;

    public String domainSuffices;

    public VpnType type;

    public VpnState state = VpnState.IDLE;

    public static VpnProfile newInstance(final VpnType vpnType) {
        Class<? extends VpnProfile> profileClass = vpnType.getProfileClass();
        if (profileClass == null)
            throw new IllegalArgumentException("profile class is null for " + vpnType);

        try {
            VpnProfile p = profileClass.newInstance();
            p.type = vpnType;
            return p;
        } catch (Exception e) {
            throw new AppException("failed to create instance for " + vpnType, e);
        }
    }

    public void postConstruct() {
        id = UUID.randomUUID().toString();
    }

    public void postUpdate() {

    }

    public void validate() {

    }

    public void preConnect() {

    }

    @Override
    public String toString() {
        return id + "#" + name;
    }

    public boolean needKeyStoreToSave() {
        return false;
    }

    public boolean needKeyStoreToConnect() {
        return false;
    }

    @Override
    public VpnProfile clone() {
        VpnProfile c = null;
        try {
            c = (VpnProfile) super.clone();
        } catch (CloneNotSupportedException e) {
            // nerver happen
        }
        return c;
    }

    public VpnProfile clone4Connect() {
        return clone();
    }

    /**
     * Instantiate a VpnProfile object from json
     */
    public static VpnProfile fromJson(final JSONObject jo) {
        VpnProfile p = null;

        try {
            String typeName = jo.getString("type");
            p = VpnProfile.newInstance(VpnType.valueOf(typeName));
            p.restoreFrom(jo);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return p;
    }

    /**
     * Encode the profile as json
     */
    public JSONObject toJson() {
        JSONObject jo = new JSONObject();

        try {
            saveTo(jo);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jo;
    }

    /**
     * Save instance state into the given json object
     */
    protected void saveTo(final JSONObject jo) throws JSONException {
        jo.put("type", type);
        jo.put("id", id);
        jo.put("name", name);
        jo.put("server", server);
        jo.put("username", username);
        jo.put("password", password);
        jo.putOpt("domain", domainSuffices);
    }

    /**
     * Restore instance state from the given json object
     */
    protected void restoreFrom(final JSONObject jo) throws JSONException {
        id = jo.getString("id");
        name = jo.getString("name");
        server = jo.getString("server");
        username = jo.getString("username");
        password = jo.getString("password");
        domainSuffices = jo.optString("domain");
    }
}
