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
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Install / update app widgets, according to active vpn conn status.
 *
 * @author ywu
 *
 */
public class VpnAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "xink.AppWidget";

    private static final ComponentName THIS_APPWIDGET = new ComponentName("xink.vpn", "xink.vpn.VpnAppWidgetProvider");

    private Context context;

    @Override
    public void onEnabled(final Context context) {
        super.onEnabled(context);

        Log.d(TAG, "VpnAppWidgetProvider enabled");
        this.context = context;
        updateViews(getActiveVpnState(context));
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        Log.d(TAG, "VpnAppWidgetProvider onUpdate");
        this.context = context;
        updateViews(getActiveVpnState(context));
    }

    private VpnState getActiveVpnState(final Context context) {
        return VpnProfileRepository.getInstance(context).getActiveVpnState();
    }

    @Override
    public void onDisabled(final Context context) {
        Log.d(TAG, "VpnAppWidgetProvider onDisabled");
        this.context = context;
        super.onDisabled(context);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        this.context = context;

        // Log.d(TAG, "received " + intent);

        if (ACTION_VPN_CONNECTIVITY.equals(intent.getAction())) {
            onStateChanged(intent);
            return;
        }
        super.onReceive(context, intent);
    }

    private void onStateChanged(final Intent intent) {
        String profileName = intent.getStringExtra(BROADCAST_PROFILE_NAME);
        VpnState newState = Utils.extractVpnState(intent);

        if (!profileName.equals(Utils.getActvieProfileName(context)))
            // Log.d(TAG, "updateViews, ignores non-active profile event for " + profileName);
            return;

        Log.d(TAG, "update state of the active vpn");
        VpnProfileRepository.getInstance(context).setActiveVpnState(newState);
        updateViews(newState);
    }

    private void updateViews(final VpnState state) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.vpn_widget);
        views.setViewVisibility(R.id.propStateTransition, getConnProgVisibility(state));
        views.setTextColor(R.id.txtState, getStateTextColor(state));
        views.setInt(R.id.txtState, "setBackgroundResource", getIndicator(state));

        installIntent(views, state);
        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        gm.updateAppWidget(THIS_APPWIDGET, views);
    }

    private int getConnProgVisibility(final VpnState state) {
        return (state != null && state.isTransitive()) ? View.VISIBLE : View.GONE;
    }

    private int getStateTextColor(final VpnState state) {
        int color = state == VpnState.CONNECTED ? R.color.vpn_widget_text_color_on : R.color.vpn_widget_text_color_off;
        return context.getResources().getColor(color);
    }

    private int getIndicator(final VpnState state) {
        return state == VpnState.CONNECTED ? R.drawable.vpn_on : R.drawable.vpn_off;
    }

    private void installIntent(final RemoteViews views, final VpnState state) {
        Intent intent = new Intent(context, ToggleVpn.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.frmToggleVpnStatue, pendingIntent);
    }
}
