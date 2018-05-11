package exception.handler;

import exception.LightingScorerException;
import org.rapidoid.http.Req;
import org.rapidoid.http.Resp;
import org.rapidoid.http.customize.ErrorHandler;
import org.rapidoid.u.U;

public class LightingScorerExceptionHandler implements ErrorHandler
{
    @Override
    public Object handleError(Req req, Resp resp, Throwable error) throws Exception
    {
        resp.result(null);

        if (error != null)
        {
            if (error instanceof LightingScorerException)
            {
                resp.code(400);
            }

            return U.map("data", null, "success", false,
                    "exceptionType", error.getClass().getSimpleName(), "exceptionMessage", error.getMessage());
        }

        return U.map("data", null, "success", false,
                "exceptionType", null, "exceptionMessage", null);
    }
}
