package xink.vpn;

public class NoActiveVpnException extends AppException {

    private static final long serialVersionUID = 1L;

    public NoActiveVpnException() {
        super();
    }

    public NoActiveVpnException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NoActiveVpnException(final String detailMessage) {
        super(detailMessage);
    }

    public NoActiveVpnException(final Throwable throwable) {
        super(throwable);
    }

}
