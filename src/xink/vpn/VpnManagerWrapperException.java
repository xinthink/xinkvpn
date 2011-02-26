package xink.vpn;

public class VpnManagerWrapperException extends AppException {

    private static final long serialVersionUID = 1L;

    public VpnManagerWrapperException() {
    }

    public VpnManagerWrapperException(final String detailMessage) {
        super(detailMessage);
    }

    public VpnManagerWrapperException(final Throwable throwable) {
        super(throwable);
    }

    public VpnManagerWrapperException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }

}
