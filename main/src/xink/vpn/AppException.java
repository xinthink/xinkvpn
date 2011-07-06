package xink.vpn;

public class AppException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int messageResourceId;

    private Object[] messageArgs;

    public AppException(final String detailMessage) {
        super(detailMessage);
    }

    public AppException(final String detailMessage, final int messageResourceId, final Object... messageArgs) {
        super(detailMessage);
        setMessageResourceId(messageResourceId);
        setMessageArgs(messageArgs);
    }

    public AppException(final String detailMessage, final Throwable throwable, final int messageResourceId, final Object... messageArgs) {
        super(detailMessage, throwable);
        setMessageResourceId(messageResourceId);
        setMessageArgs(messageArgs);
    }

    public AppException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }

    public void setMessageResourceId(final int messageResourceId) {
        this.messageResourceId = messageResourceId;
    }

    public int getMessageResourceId() {
        return messageResourceId;
    }

    public Object[] getMessageArgs() {
        return messageArgs;
    }

    public void setMessageArgs(final Object... messageArgs) {
        this.messageArgs = messageArgs;
    }

}
