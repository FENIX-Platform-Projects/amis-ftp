package org.fao.fenix.exception;


public class FenixException extends RuntimeException {
    private static final long serialVersionUID = -7101885636512184172L;

    public FenixException(Throwable e) {
        super(e);
    }

    public FenixException(String newMessage) {
        super(newMessage);
    }

    public FenixException(String msg, Throwable e) {
        super(msg, e);
    }
}
