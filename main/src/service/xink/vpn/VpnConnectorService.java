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
import xink.vpn.wrapper.VpnState;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class VpnConnectorService extends Service {

    private final class VpnStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();

            if (ACT_TOGGLE_VPN_CONN.equals(action)) {
                toggleVpnState(intent);
            } else if (ACTION_VPN_CONNECTIVITY.equals(action)) {
                onStateChanged(intent);
            } else {
                Log.w(TAG, "VpnStateReceiver ignores unknown intent:" + intent);
            }
        }

    }

    private static final String TAG = VpnConnectorService.class.getName();

    private static final ComponentName THIS_APPWIDGET = new ComponentName("xink.vpn", "xink.vpn.VpnAppWidgetProvider");

    private Context context;
    private VpnStateReceiver receiver;
    private VpnState state = VpnState.IDLE;
    private VpnActor actor;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "VpnConnectorService created");
        context = getApplicationContext();
        actor = new VpnActor(context);

        updateViews();
        registerReceivers();
        actor.checkStatus();
    }

    private void registerReceivers() {
        receiver = new VpnStateReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_VPN_CONNECTIVITY);
        filter.addAction(ACT_TOGGLE_VPN_CONN);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "VpnConnectorService destroyed");

        unregisterReceivers();
        super.onDestroy();
    }

    private void unregisterReceivers() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    public void toggleVpnState(final Intent intent) {
        switch (state) {
        case IDLE:
            connect();
            break;
        case CONNECTED:
            actor.disconnect();
            break;
        default:
            Log.i(TAG, "toggleVpnState intent not handled, currentState=" + state + ", intent=" + intent);
            break;
        }
    }

    private void connect() {
        try {
            actor.connect();
        } catch (NoActiveVpnException e) {
            Toast.makeText(this, R.string.err_no_active_vpn, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "connect failed, no active vpn");
        }
    }

    public void onStateChanged(final Intent intent) {
        String profileName = intent.getStringExtra(BROADCAST_PROFILE_NAME);
        VpnState newState = Utils.extractVpnState(intent);
        int err = intent.getIntExtra(BROADCAST_ERROR_CODE, VPN_ERROR_NO_ERROR);
        //Log.d(TAG, profileName + " stateChanged: " + state + "->" + newState + ", errCode=" + err);

        updateViews(profileName, newState);
    }

    private void updateViews(final String profileName, final VpnState newState) {
        if (!profileName.equals(Utils.getActvieProfileName(context))) {
            //Log.d(TAG, "updateViews, ignores non-active profile event for " + profileName);
            return;
        }

        state = newState;
        updateViews();
    }

    private void updateViews() {
        RemoteViews views = new RemoteViews(context.getPackageName(), getViewId());
        // Update specific list of appWidgetIds if given, otherwise default to all
        updateButtons(views);
        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        gm.updateAppWidget(THIS_APPWIDGET, views);
    }

    private int getViewId() {
        int view = 0;
        switch (state) {
        case CONNECTED:
            view = R.layout.vpn_widget_connected;
            break;
        case CONNECTING:
        case DISCONNECTING:
            view = R.layout.vpn_widget_transition;
            break;
        default:
            view = R.layout.vpn_widget_disconnected;
            break;
        }

        return view;
    }

    private void updateButtons(final RemoteViews views) {
        Intent intent = new Intent(context, ToggleVpn.class);
        intent.putExtra(Constants.KEY_VPN_STATE, state);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.frmToggleVpnStatue, pendingIntent);
    }
}
