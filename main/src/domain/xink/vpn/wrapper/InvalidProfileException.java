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

package xink.vpn.wrapper;

import xink.vpn.AppException;

public class InvalidProfileException extends AppException {

    private static final long serialVersionUID = 1L;


    public InvalidProfileException(final String detailMessage, final int messageResourceId, final Object... messageArgs) {
        super(detailMessage, messageResourceId, messageArgs);
    }

    public InvalidProfileException(final String detailMessage, final Throwable throwable, final int messageResourceId, final Object... messageArgs) {
        super(detailMessage, throwable, messageResourceId, messageArgs);
    }
}
