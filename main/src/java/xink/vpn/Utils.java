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

import static xink.vpn.Constants.*;

import java.io.File;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnState;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public final class Utils {

    private static final Logger LOG = LoggerFactory.getLogger("xink.Utils");

    public static void showErrMessage(final Activity ctx, final AppException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(true).setMessage(ctx.getString(e.getMessageCode(), e.getMessageArgs()));

        AlertDialog dlg = builder.create();
        dlg.setOwnerActivity(ctx);
        dlg.show();
    }

    /**
     * 显示一个短时间的Toast提示信息(duration为short).
     * 
     * @param ctx context
     * @param resId text resource id
     * @param formatArgs arguments for formatting the message
     */
    public static void showToast(final Context ctx, final int resId, final Object... formatArgs) {
        Toast.makeText(ctx, ctx.getString(resId, formatArgs), Toast.LENGTH_SHORT).show();
    }

    public static void ensureDir(final File dir) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state))
            throw new AppException("no writable storage found, state=" + state, R.string.err_no_writable_storage);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!dir.exists())
            throw new AppException("failed to mkdir: " + dir, R.string.err_exp_write_storage_failed);
    }

    public static String getActvieProfileName(final Context ctx) {
        VpnProfileRepository repository = VpnProfileRepository.getInstance(ctx);
        VpnProfile activeProfile = repository.getActiveProfile();
        return activeProfile != null ? activeProfile.getName() : null;
    }

    public static VpnState extractVpnState(final Intent intent) {
        Serializable obj = intent.getSerializableExtra(BROADCAST_CONNECTION_STATE);
        VpnState state = VpnState.IDLE;

        if (obj != null) {
            state = VpnState.valueOf(obj.toString());
        }
        return state;
    }

    public static boolean isInStableState(final VpnProfile p) {
        VpnState state = p.getState();
        return state == VpnState.CONNECTED || state == VpnState.IDLE;
    }

    //
    // *****************************************************************************************************
    //
    // Preferences Utilities
    //
    // *****************************************************************************************************
    //

    /**
     * Retrieves a boolean from default SharedPreferences
     * 
     * @param key resource defines the preference key
     * @param defaultValue resource defines the default value
     * @param ctx Context
     * @return boolean preference
     */
    public static boolean getPrefBool(final int key, final int defaultValue, final Context ctx) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

        String prefKey = ctx.getString(key);
        boolean prefDefault = ctx.getResources().getBoolean(defaultValue);

        return sp.getBoolean(prefKey, prefDefault);
    }

    /**
     * Retrieves an integer from default SharedPreferences
     * 
     * @param key resource defines the preference key
     * @param defaultValue resource defines the default value
     * @param ctx Context
     * @return int preference
     */
    public static int getPrefInt(final int key, final int defaultValue, final Context ctx) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

        String prefKey = ctx.getString(key);
        int value = ctx.getResources().getInteger(defaultValue);

        try {
            // 在xml中定义的数字型preference只能按字符串读取
            value = Integer.parseInt(sp.getString(prefKey, String.valueOf(value)));
        } catch (NumberFormatException e) {
            LOG.warn("invalid NumberFormat: int prefs '{}'", prefKey);
        }
        return value;
    }

    private Utils() {

    }
}
