package exception;

public class ModelNotFoundException extends LightingScorerException
{
    private static final long serialVersionUID = 5123858921619908538L;

    public ModelNotFoundException(String message)
    {
        super(message);
    }

    public ModelNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}