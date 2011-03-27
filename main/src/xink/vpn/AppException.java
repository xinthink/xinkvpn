package xink.vpn;

public class AppException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AppException() {
    }

    public AppException(final String detailMessage) {
        super(detailMessage);
    }

    public AppException(final Throwable throwable) {
        super(throwable);
    }

    public AppException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }

}
