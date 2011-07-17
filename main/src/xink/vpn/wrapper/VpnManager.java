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

import android.content.Context;
import android.content.ServiceConnection;

public class VpnManager extends AbstractWrapper {

    public static final String METHOD_BIND_VPN_SERVICE = "bindVpnService";
    public static final String METHOD_STOP_VPN_SERVICE = "stopVpnService";
    public static final String METHOD_START_VPN_SERVICE = "startVpnService";

	public VpnManager(final Context ctx) {
        super(ctx, "android.net.vpn.VpnManager", new StubInstanceCreator() {
            @Override
            protected Object newStubInstance(final Class<?> stubClass, final Context ctx) throws Exception {
                return stubClass.getConstructor(Context.class).newInstance(ctx);
            }
        });
    }

    /**
     * Starts the VPN service to establish VPN connection.
     */
    public void startVpnService() {
        invokeStubMethod(METHOD_START_VPN_SERVICE);
    }

    /**
     * Stops the VPN service.
     */
    public void stopVpnService() {
        invokeStubMethod(METHOD_STOP_VPN_SERVICE);
    }

    /**
     * Binds the specified ServiceConnection with the VPN service.
     */
    public boolean bindVpnService(final ServiceConnection c) {
        try {
            Method m = getStubClass().getMethod(METHOD_BIND_VPN_SERVICE, ServiceConnection.class);
            return (Boolean) m.invoke(getStub(), c);
        } catch (Throwable e) {
            throw new WrapperException("bindVpnService failed", e);
        }
    }
}
