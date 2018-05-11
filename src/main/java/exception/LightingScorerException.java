package exception;

public class LightingScorerException extends RuntimeException
{
    public LightingScorerException(String message)
    {
        super(message);
    }

    public LightingScorerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
