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

import xink.vpn.wrapper.KeyStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

public class HackKeyStore {

    private static final String PREF_IGNORE_HACK = "xink.vpn.pref.ignoreHack";

    private static final String TAG = "xink.HackKeyStore";

    private SharedPreferences pref;

    private Activity activity;

    private KeyStore keyStore;

    public HackKeyStore(final Activity activity) {
        super();
        this.activity = activity;
        pref = activity.getPreferences(Activity.MODE_PRIVATE);
        keyStore = new KeyStore(activity);
    }

    public void check(final boolean force) {
        boolean ignoreHack = pref.getBoolean(PREF_IGNORE_HACK, false);

        if (!force && ignoreHack) {
            return;
        }

        boolean hacked = keyStore.isHacked();
        Log.d(TAG, String.format("check keytore, hacked=%1$s, ignore=%2$s", hacked, ignoreHack));

        if (hacked && force) {
            Toast.makeText(activity, R.string.hacked, Toast.LENGTH_SHORT).show();
        }

        if (!hacked) {
            promoptHack(ignoreHack);
        }
    }

    private void promoptHack(final boolean ignoreHack) {
        AlertDialog.Builder builder;

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.hack, (ViewGroup) activity.findViewById(R.id.hackRoot));

        builder = new AlertDialog.Builder(activity);
        builder.setView(layout).setTitle(activity.getString(R.string.hack_keystore)).setIcon(android.R.drawable.ic_dialog_info);
        builder.setCancelable(true);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });

        final CheckBox chkIgnore = (CheckBox) layout.findViewById(R.id.chkIgnoreHack);
        chkIgnore.setChecked(ignoreHack);

        AlertDialog dlg = builder.create();
        dlg.setOwnerActivity(activity);
        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                savePref(chkIgnore.isChecked());
            }
        });
        dlg.show();
    }

    private void savePref(final boolean isChecked) {
        Log.d(TAG, PREF_IGNORE_HACK + "->" + isChecked);

        Editor editor = pref.edit();
        editor.putBoolean(PREF_IGNORE_HACK, isChecked);
        editor.commit();
    }

}
