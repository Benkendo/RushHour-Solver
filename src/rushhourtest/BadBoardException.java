package rushhourtest;

public class BadBoardException extends RuntimeException {

    private static final long serialVersionUID = -1552327500999125270L;

    public BadBoardException(String message) {
        super(message);
    }

    public BadBoardException(Exception e) {
        super(e);
    }
}