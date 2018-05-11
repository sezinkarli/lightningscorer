package controller;

import domain.Response;
import org.rapidoid.http.Req;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParentController
{
    public Response toResponse(Object object)
    {
        return new Response(object);
    }

    public Map<String, String> getRequestParametersExcept(Req req, List<String> exceptions)
    {
        return req.params().entrySet().stream()
                .filter(entry -> !exceptions.contains(entry.getKey()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }
}
