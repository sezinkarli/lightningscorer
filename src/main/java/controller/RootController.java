package controller;

import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.http.Resp;

@Controller
public class RootController extends ParentController
{
    private final static String WELCOME_MESSAGE = "I have come here to chew bubblegum and kick ass...";

    @GET("/")
    public void getWelcomeMessage(Resp resp)
    {
        resp.json(toResponse(WELCOME_MESSAGE));
    }

}
