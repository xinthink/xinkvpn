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
import android.os.IBinder;

public class VpnService extends AbstractWrapper {

    public VpnService(final Context ctx) {
        super(ctx, "android.net.vpn.IVpnService$Stub", new StubInstanceCreator() {
            @Override
            protected Object newStubInstance(final Class<?> stubClass, final Context ctx) throws Exception {
                return null;
            }
        });
    }

    public boolean connect(final IBinder service, final VpnProfile profile) throws Exception {
        asInterface(service);

        Method m = getStubClass().getMethod("connect", profile.getGenericProfileClass(), String.class, String.class);
        return (Boolean) m.invoke(getStub(), profile.getStub(), profile.getUsername(), profile.getPassword());
    }

    public void disconnect(final IBinder service) throws Exception {
        asInterface(service);
        invokeStubMethod("disconnect");
    }

    public void checkStatus(final IBinder service, final VpnProfile p) throws Exception {
        asInterface(service);
        Method m = getStubClass().getMethod("checkStatus", p.getGenericProfileClass());
        m.invoke(getStub(), p.getStub());
    }

    private void asInterface(final IBinder service) throws Exception {
        Method method = getStubClass().getMethod("asInterface", IBinder.class);
        setStub(method.invoke(null, service));
    }
}
