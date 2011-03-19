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
    }

    @Override
    public void onDisabled(final Context context) {
        Log.d(TAG, "VpnAppWidgetProvider onDisabled");
        context.stopService(new Intent(context, VpnConnectorService.class));
        super.onDisabled(context);
    }
}
