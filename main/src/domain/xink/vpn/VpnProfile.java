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
            return profileClass.newInstance();
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
}
