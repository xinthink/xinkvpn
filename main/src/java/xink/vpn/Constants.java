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

public final class Constants {
    public static final int REQ_SELECT_VPN_TYPE = 1;

    public static final int REQ_ADD_VPN = 2;

    public static final int REQ_EDIT_VPN = 3;

    public static final String ACT_ADD_VPN = "xink.addVpnAction";

    public static final String ACT_VPN_SETTINGS = "android.net.vpn.SETTINGS";

    public static final String CAT_DEFAULT = "android.intent.category.DEFAULT";

    public static final String KEY_VPN_TYPE = "vpnType";

    public static final String KEY_VPN_PROFILE_ID = "vpnProfileId";

    public static final String KEY_VPN_PROFILE_NAME = "vpnProfileName";

    public static final String KEY_VPN_STATE = "activeVpnState";

    public static final int DLG_VPN_PROFILE_ALERT = 1;

    public static final int DLG_ABOUT = 2;

    public static final int DLG_BACKUP = 3;

    public static final int DLG_RESTORE = 4;

    public static final int DLG_HACK = 5;

    // Action for broadcasting a connectivity state.
    public static final String ACTION_VPN_CONNECTIVITY = "vpn.connectivity";
    /** Key to the profile name of a connectivity broadcast event. */
    public static final String BROADCAST_PROFILE_NAME = "profile_name";
    /** Key to the connectivity state of a connectivity broadcast event. */
    public static final String BROADCAST_CONNECTION_STATE = "connection_state";
    /** Key to the error code of a connectivity broadcast event. */
    public static final String BROADCAST_ERROR_CODE = "err";
    /** Error code to indicate an error from authentication. */
    public static final int VPN_ERROR_AUTH = 51;
    /** Error code to indicate the connection attempt failed. */
    public static final int VPN_ERROR_CONNECTION_FAILED = 101;
    /** Error code to indicate the server is not known. */
    public static final int VPN_ERROR_UNKNOWN_SERVER = 102;
    /** Error code to indicate an error from challenge response. */
    public static final int VPN_ERROR_CHALLENGE = 5;
    /** Error code to indicate an error of remote server hanging up. */
    public static final int VPN_ERROR_REMOTE_HUNG_UP = 7;
    /** Error code to indicate an error of remote PPP server hanging up. */
    public static final int VPN_ERROR_REMOTE_PPP_HUNG_UP = 48;
    /** Error code to indicate a PPP negotiation error. */
    public static final int VPN_ERROR_PPP_NEGOTIATION_FAILED = 42;
    /** Error code to indicate an error of losing connectivity. */
    public static final int VPN_ERROR_CONNECTION_LOST = 103;
    /** Largest error code used by VPN. */
    public static final int VPN_ERROR_LARGEST = 200;
    /** Error code to indicate a successful connection. */
    public static final int VPN_ERROR_NO_ERROR = 0;

    public static final String EXP_DIR_REGEX = "\\d{6}\\-\\d{6}";

    private Constants() {

    }
}
