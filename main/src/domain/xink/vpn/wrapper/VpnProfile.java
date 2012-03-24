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

package xink.vpn.wrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import android.content.Context;

public abstract class VpnProfile extends AbstractWrapper {

    private String username;

    private String password;

    private VpnState state = VpnState.UNKNOWN;

    protected VpnProfile(final Context ctx, final String stubClass) {
        super(ctx, stubClass);
    }

    public static VpnProfile newInstance(final VpnType vpnType, final Context ctx) {
        Class<? extends VpnProfile> profileClass = vpnType.getProfileClass();
        if (profileClass == null) {
            throw new IllegalArgumentException("profile class is null for " + vpnType);
        }

        try {
            return profileClass.getConstructor(Context.class).newInstance(ctx);
        } catch (Exception e) {
            throw new WrapperException("failed to create instance for " + vpnType, e);
        }
    }

    public void postConstruct() {
        setId(UUID.randomUUID().toString());
    }

    public void postUpdate() {

    }

    public void validate() {

    }

    public void preConnect() {

    }

    /**
     * Sets an ID for this profile. The caller should make sure the uniqueness of the ID.
     */
    public void setId(final String id) {
        invokeStubMethod("setId", id);
    }

    public String getId() {
        return invokeStubMethod("getId");
    }

    /**
     * Sets a user-friendly name for this profile.
     */
    public void setName(final String name) {
        invokeStubMethod("setName", name);
    }

    public String getName() {
        return invokeStubMethod("getName");
    }

    /**
     * Sets the name of the VPN server. Used for DNS lookup.
     */
    public void setServerName(final String name) {
        invokeStubMethod("setServerName", name);
    }

    public String getServerName() {
        return invokeStubMethod("getServerName");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Sets the domain suffices for DNS resolution.
     *
     * @param entries
     *            a comma-separated(or space-separated) list of domain suffices
     */
    public void setDomainSuffices(final String entries) {
        invokeStubMethod("setDomainSuffices", entries);
    }

    public String getDomainSuffices() {
        return invokeStubMethod("getDomainSuffices");
    }

    /**
     * Sets the routing info for this VPN connection.
     *
     * @param entries
     *            a comma-separated(or space-separated) list of routes; each entry is in the format of
     *            "(network address)/(network mask)"
     */
    public void setRouteList(final String entries) {
        invokeStubMethod("setRouterList", entries);
    }

    public String getRouteList() {
        return invokeStubMethod("getRouteList");
    }

    public Class<?> getGenericProfileClass() {
        try {
            return loadClass("android.net.vpn.VpnProfile");
        } catch (ClassNotFoundException e) {
            throw new WrapperException("load class failed", e);
        }
    }

    public VpnState getState() {
        return state;
    }

    public void setState(final VpnState state) {
        this.state = state;
    }

    public abstract VpnType getType();

    public void write(final ObjectOutputStream os) throws IOException {
        os.writeObject(getType());
        os.writeObject(getStub());
        os.writeObject(username);
        os.writeObject(password);
    }

    public void read(final Object obj, final ObjectInputStream is) throws Exception {
        setStub(obj);
        username = (String) is.readObject();
        password = (String) is.readObject();
    }

    public boolean isCompatible(final Object obj) {
        return getStubClass().equals(obj.getClass());
    }

    @Override
    public String toString() {
        return getId() + "#" + getName();
    }

    public boolean needKeyStoreToSave() {
        return false;
    }

    public boolean needKeyStoreToConnect() {
        return false;
    }

    @Override
    public VpnProfile clone() {
        VpnProfile c = (VpnProfile) super.clone();
        c.setId(getId());
        c.setName(getName());
        c.setServerName(getServerName());
        c.setDomainSuffices(getDomainSuffices());
        return c;
    }

    public VpnProfile dulicateToConnect() {
        return clone();
    }
}
