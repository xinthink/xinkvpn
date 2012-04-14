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
import android.view.View;
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

        //Log.i(TAG, "VpnConnectorService created");
        context = getApplicationContext();
        actor = new VpnActor(context);

        updateViews(null);
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
        //Log.i(TAG, "VpnConnectorService destroyed");

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
        // int err = intent.getIntExtra(BROADCAST_ERROR_CODE, VPN_ERROR_NO_ERROR);
        //Log.d(TAG, profileName + " stateChanged: " + state + "->" + newState + ", errCode=" + err);

        updateViews(profileName, newState);
    }

    private void updateViews(final String profileName, final VpnState newState) {
        if (!profileName.equals(Utils.getActvieProfileName(context)))
            //Log.d(TAG, "updateViews, ignores non-active profile event for " + profileName);
            return;

        state = newState;
        updateViews(newState);
    }

    private void updateViews(final VpnState state) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.vpn_widget);
        views.setViewVisibility(R.id.propStateTransition, getConnProgVisibility(state));
        views.setTextColor(R.id.txtState, getStateTextColor(state));
        views.setInt(R.id.txtState, "setBackgroundResource", getIndicator(state));

        installIntent(views);
        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        gm.updateAppWidget(THIS_APPWIDGET, views);
    }


    private int getConnProgVisibility(final VpnState state) {
        return (state != null && state.isTransitive()) ? View.VISIBLE : View.GONE;
    }

    private int getStateTextColor(final VpnState state) {
        int color = state == VpnState.CONNECTED ? R.color.vpn_widget_text_color_on : R.color.vpn_widget_text_color_off;
        return getResources().getColor(color);
    }

    private int getIndicator(final VpnState state) {
        return state == VpnState.CONNECTED ? R.drawable.vpn_on : R.drawable.vpn_off;
    }

    private void installIntent(final RemoteViews views) {
        Intent intent = new Intent(context, ToggleVpn.class);
        intent.putExtra(Constants.KEY_VPN_STATE, state);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.frmToggleVpnStatue, pendingIntent);
    }
}
