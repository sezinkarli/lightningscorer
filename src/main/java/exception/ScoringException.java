package exception;

public class ScoringException extends LightingScorerException
{
    private static final long serialVersionUID = -5206681147934496591L;

    public ScoringException(String message)
    {
        super(message);
    }

    public ScoringException(String message, Throwable cause)
    {
        super(message, cause);
    }
}