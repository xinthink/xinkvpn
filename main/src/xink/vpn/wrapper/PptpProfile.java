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

import java.lang.reflect.Method;

import xink.vpn.AppException;
import android.content.Context;

public class PptpProfile extends VpnProfile {

    public PptpProfile(final Context ctx) {
        super(ctx, "android.net.vpn.PptpProfile");
    }

    @Override
    public VpnType getType() {
        return VpnType.PPTP;
    }

    /**
     * Enables/disables the encryption for PPTP tunnel.
     */
    public void setEncryptionEnabled(final boolean enabled) {
        try {
            Method m = getStubClass().getMethod("setEncryptionEnabled", boolean.class);
            m.invoke(getStub(), enabled);
        } catch (Throwable e) {
            throw new AppException("setEncryptionEnabled failed", e);
        }
    }

    public boolean isEncryptionEnabled() {
        return this.<Boolean>invokeStubMethod("isEncryptionEnabled");
    }

    /*
     * (non-Javadoc)
     * 
     * @see xink.vpn.wrapper.VpnProfile#dulicateToConnect()
     */
    @Override
    public PptpProfile dulicateToConnect() {
        PptpProfile p = (PptpProfile) super.dulicateToConnect();
        p.setEncryptionEnabled(isEncryptionEnabled());
        return p;
    }

}
