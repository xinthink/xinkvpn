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
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KeyStore extends AbstractWrapper {

    public static final String UNLOCK_ACTION = "android.credentials.UNLOCK";

    private static final int NO_ERROR = 1;

    /*
    private static final int LOCKED = 2;
    private static final int UNINITIALIZED = 3;
    private static final int SYSTEM_ERROR = 4;
    private static final int PROTOCOL_ERROR = 5;
    private static final int PERMISSION_DENIED = 6;
    private static final int KEY_NOT_FOUND = 7;
    private static final int VALUE_CORRUPTED = 8;
    private static final int UNDEFINED_ACTION = 9;
    private static final int WRONG_PASSWORD = 10;
    */

    public KeyStore(final Context ctx) {
        super(ctx, "android.security.KeyStore", new StubInstanceCreator() {
            @Override
            protected Object newStubInstance(final Class<?> stubClass, final Context ctx) throws Exception {
                Method method = stubClass.getMethod("getInstance");
                return method.invoke(null);
            }
        });
    }

    public boolean put(final String key, final String value) {
        return this.<Boolean> invokeStubMethod("put", key, value);
    }

    public boolean contains(final VpnProfile p) {
        String key = L2tpIpsecPskProfile.KEY_PREFIX_IPSEC_PSK + p.getId();
        return this.<Boolean> invokeStubMethod("contains", key);
    }

    public boolean delete(final String key) {
        return this.<Boolean> invokeStubMethod("delete", key);
    }

    public void unlock(final Activity ctx) {
        try {
            Intent intent = new Intent(UNLOCK_ACTION);
            ctx.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("xink", "unlock credentials failed", e);
        }
    }

    public boolean isUnlocked() {
        int err = this.<Integer> invokeStubMethod("test");
        Log.d("xink", "KeyStore.test result is: " + err);
        return err == NO_ERROR;
    }

    /**
     * Check whether the keystore is hacked by xink.
     */
    public boolean isHacked() {
        try {
            Method m = getStubClass().getDeclaredMethod("execute", int.class, byte[][].class);
            m.setAccessible(true);
            m.invoke(getStub(), 'x', new byte[0][]);
        } catch (Throwable e) {
            throw new AppException("verify failed", e);
        }

        int err = invokeStubMethod("getLastError");
        Log.d("xink", "cmd 'x' result=" + err);
        return err == NO_ERROR;
    }
}
