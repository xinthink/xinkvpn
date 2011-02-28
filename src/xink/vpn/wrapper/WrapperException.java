package xink.vpn.wrapper;

import xink.vpn.AppException;

public class WrapperException extends AppException {

    private static final long serialVersionUID = 1L;

    public WrapperException() {
    }

    public WrapperException(final String detailMessage) {
        super(detailMessage);
    }

    public WrapperException(final Throwable throwable) {
        super(throwable);
    }

    public WrapperException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }

}
