package xink.vpn;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class VpnAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "xink";

    @Override
    public void onEnabled(final Context context) {
        super.onEnabled(context);

        Log.d(TAG, "VpnAppWidgetProvider enabled");
        context.startService(new Intent(context, VpnConnectorService.class));
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        Log.d(TAG, "VpnAppWidgetProvider onUpdate");
        context.startService(new Intent(context, VpnConnectorService.class));

        // for (int widgetId : appWidgetIds) {
        // // // Create an Intent to launch ExampleActivity
        // Intent intent = new Intent(Constants.ACT_TOGGLE_VPN_CONN);
        // PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        //
        // // Get the layout for the App Widget and attach an on-click listener to the button
        // RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.vpn_widget_disconnected);
        // views.setOnClickPendingIntent(R.id.btnToggleVpnStatus, pendingIntent);
        // // views.setTextViewText(R.id.btnToggleVpnStatus, "hello");
        //
        // // Tell the AppWidgetManager to perform an update on the current App Widget
        // appWidgetManager.updateAppWidget(widgetId, views);
        // }
    }

    @Override
    public void onDisabled(final Context context) {
        Log.d(TAG, "VpnAppWidgetProvider onDisabled");
        context.stopService(new Intent(context, VpnConnectorService.class));
        super.onDisabled(context);
    }
}
