package controller;

import domain.ModelInputFields;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.DELETE;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.POST;
import org.rapidoid.http.Req;
import org.rapidoid.http.Resp;
import service.impl.ModelService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Map;

@Controller(value = "/model")
public class ModelController extends ParentController
{
    private static final String FORM_PARAMETER_NAME_FOR_FILENAME = "model";
    private static final String EXTENDED_PARAMETER_FOR_SUMMARY = "extended";

    private ModelService modelService;

    @Inject
    public ModelController(ModelService modelService)
    {
        this.modelService = modelService;
    }

    @POST(value = "/{modelId}")
    public void deploy(String modelId, Req req, Resp resp)
    {
        Map<String, String> requestParams = getRequestParametersExcept(req, Arrays.asList("modelId"));

        modelService.deploy(modelId, req.file(FORM_PARAMETER_NAME_FOR_FILENAME), requestParams);

        resp.json(toResponse(true));
    }

    @GET(value = "/{modelId}")
    public void getSummary(String modelId, Req req, Resp resp)
    {
        boolean isExtended = getRequestParamAsBoolean(req, EXTENDED_PARAMETER_FOR_SUMMARY);

        resp.json(toResponse(modelService.getSummary(modelId, isExtended)));
    }

    @DELETE(value = "/{modelId}")
    public void undeploy(String modelId, Resp resp)
    {
        modelService.undeploy(modelId);

        resp.json(toResponse(true));
    }

    @DELETE(value = "/")
    public void undeployAll(Resp resp)
    {
        modelService.undeployAll();

        resp.json(toResponse(true));
    }

    @POST(value = "/{modelId}/score")
    public void score(String modelId, ModelInputFields inputFields, Resp resp)
    {
        resp.json(toResponse(modelService.score(modelId, inputFields)));
    }

    @GET(value = "/ids")
    public void getAllModelIds(Resp resp)
    {
        resp.json(toResponse(modelService.getAllModelIds()));
    }

    @GET(value = "/additionals")
    public void getAllAdditionalParameters(Resp resp)
    {
        resp.json(toResponse(modelService.getAllAdditionalParameters()));
    }

    @GET(value = "/{modelId}/additional")
    public void getAdditionalParameter(String modelId, Resp resp)
    {
        resp.json(toResponse(modelService.getAdditionalParameter(modelId)));
    }
}
