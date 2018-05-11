import exception.handler.LightingScorerExceptionHandler;
import org.rapidoid.setup.App;
import org.rapidoid.setup.My;

public class LightningScorer
{
    public static void main(String[] args)
    {
        App.bootstrap(args);
        My.error(Throwable.class).handler(new LightingScorerExceptionHandler());
    }
}
