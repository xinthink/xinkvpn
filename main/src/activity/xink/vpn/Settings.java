/*
 * Copyright 2011 yingxinwu.g@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package xink.vpn;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

/**
 * Modifing / storing user settings.
 */
public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private ListPreference periodList;
    private ListPreference connErrThresholdList;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref);
        periodList = (ListPreference) getPreferenceScreen().findPreference(KeepAlive.PREF_HEARTBEAT_PERIOD);
        connErrThresholdList = (ListPreference) getPreferenceScreen().findPreference(
                getString(R.string.pref_crash_conn_broken_threshold_key));
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateListPrefSum(periodList, R.string.keepalive_period_sum);
        updateListPrefSum(connErrThresholdList, R.string.pref_crash_conn_broken_threshold_sum);

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences
     * , java.lang.String)
     */
    @Override
    public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
        if (key.equals(KeepAlive.PREF_HEARTBEAT_PERIOD)) {
            updateListPrefSum(periodList, R.string.keepalive_period_sum);
        } else if (key.equals(getString(R.string.pref_crash_conn_broken_threshold_key))) {
            updateListPrefSum(connErrThresholdList, R.string.pref_crash_conn_broken_threshold_sum);
        }
    }

    private void updateListPrefSum(final ListPreference pref, final int sumRes) {
        pref.setSummary(getString(sumRes, pref.getEntry()));
    }
}
