package service.impl;

import domain.DetailedModelContent;
import exception.AdditionalParametersException;
import exception.ModelNotFoundException;
import org.pmw.tinylog.Logger;
import org.rapidoid.annotation.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ModelHolderService
{
    private ConcurrentHashMap<String, DetailedModelContent> modelIdToDetailContentMap;

    @PostConstruct
    private void initialize()
    {
        modelIdToDetailContentMap = new ConcurrentHashMap<>();
    }

    public void put(String modelId, DetailedModelContent detailedModelContent)
    {
        DetailedModelContent previousModelContent = modelIdToDetailContentMap.put(modelId, detailedModelContent);

        if (previousModelContent != null)
        {
            Logger.info("Model id [{}] replaced with new model", modelId);
        }
    }

    public DetailedModelContent get(String modelId)
    {
        validateModelAvailability(modelId);

        DetailedModelContent detailedModelContent = modelIdToDetailContentMap.get(modelId);

        if (detailedModelContent == null)
        {
            Logger.error("Model with given id cant be found: [{}]", modelId);
            throw new IllegalArgumentException("Model with given id cant be found");
        }

        return detailedModelContent;
    }

    public void remove(String modelId)
    {
        validateModelAvailability(modelId);

        DetailedModelContent removedContent = modelIdToDetailContentMap.remove(modelId);

        if (removedContent == null)
        {
            Logger.warn("Could not remove model with model id: [{}]", modelId);
        }
        else
        {
            Logger.info("Model removed with model id: [{}]", modelId);
        }
    }


    public void clear()
    {
        modelIdToDetailContentMap.clear();
    }


    public List<String> getAllModelIds()
    {
        return Collections.list(modelIdToDetailContentMap.keys());
    }


    public Map<String, Map<String, String>> getAllAdditionalParameters()
    {
        try
        {
            return modelIdToDetailContentMap.entrySet().stream()
                    .filter(entry -> (entry.getValue() != null && entry.getValue().getAdditionalParameters().isPresent()))
                    .collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().getAdditionalParameters().get()));
        } catch (Exception e)
        {
            Logger.error(e, "Exception during preparation of additional parameters");
            throw new AdditionalParametersException("Exception during preparation of additional parameters", e);
        }
    }

    private void validateModelAvailability(String modelId)
    {
        if (!modelIdToDetailContentMap.containsKey(modelId))
        {
            Logger.error("Given model id is not uploaded: [{}]", modelId);
            throw new ModelNotFoundException("Given model id is not uploaded: " + modelId);
        }
    }
}
