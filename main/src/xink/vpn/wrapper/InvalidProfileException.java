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
