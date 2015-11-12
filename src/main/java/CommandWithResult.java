import com.datastax.driver.core.ResultSet;

/**
 * Created by alvin on 11/12/15.
 */
public interface CommandWithResult extends Command {
    ResultSet getRawResult();

    String getResult();
}
