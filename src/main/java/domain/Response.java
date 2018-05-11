package domain;

import java.io.Serializable;

public class Response implements Serializable
{
    private static final long serialVersionUID = -1809182620110068649L;

    private Object data;
    private boolean success = true;

    public Response()
    {
    }

    public Response(Object data)
    {
        this.data = data;
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }
}
