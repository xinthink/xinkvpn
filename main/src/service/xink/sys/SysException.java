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

package xink.sys;

import xink.vpn.AppException;

/**
 * System service exception
 * 
 * @author ywu
 * 
 */
public class SysException extends AppException {

    private static final long serialVersionUID = 1L;

    public SysException(final String detailMessage) {
        super(detailMessage);
    }

    public SysException(final String message, final int msgCode, final Object... msgArgs) {
        super(message, msgCode, msgArgs);
    }

    public SysException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SysException(final String message, final Throwable throwable, final int msgCode, final Object... msgArgs) {
        super(message, throwable, msgCode, msgArgs);
    }

}
