package xink.vpn;

public class AppException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int messageCode;

    private Object[] messageArgs;

    public AppException(final String detailMessage) {
        super(detailMessage);
    }

    public AppException(final String message, final int msgCode, final Object... msgArgs) {
        super(message);
        this.messageCode = msgCode;
        this.messageArgs = msgArgs;
    }

    public AppException(final String message, final Throwable throwable, final int msgCode, final Object... msgArgs) {
        super(message, throwable);
        this.messageCode = msgCode;
        this.messageArgs = msgArgs;
    }

    public AppException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public int getMessageCode() {
        return messageCode;
    }

    public Object[] getMessageArgs() {
        return messageArgs;
    }

}
