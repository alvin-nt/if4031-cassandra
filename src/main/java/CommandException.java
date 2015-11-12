/**
 * Created by alvin on 11/12/15.
 */
public class CommandException extends RuntimeException {
    public CommandException(Exception e) {
        super(e);
    }

    public CommandException(String error) {
        super(error);
    }
}
