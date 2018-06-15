package exception;

public class SummaryException extends LightingScorerException
{
    private static final long serialVersionUID = -5206681147934496591L;

    public SummaryException(String message)
    {
        super(message);
    }

    public SummaryException(String message, Throwable cause)
    {
        super(message, cause);
    }
}