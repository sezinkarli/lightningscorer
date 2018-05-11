package service.impl;

import com.google.common.base.Strings;
import domain.DetailedModelContent;
import domain.ModelInputFields;
import domain.ScoringResult;
import exception.EvaluatorCreationException;
import exception.ModelNotFoundException;
import exception.ScoringException;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Computable;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.evaluator.TargetField;
import org.jpmml.model.PMMLUtil;
import org.pmw.tinylog.Logger;
import org.rapidoid.annotation.Service;
import org.rapidoid.io.Upload;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ModelService
{
    private ConcurrentHashMap<String, DetailedModelContent> modelIdToDetailContentMap;

    @PostConstruct
    private void initialize()
    {
        modelIdToDetailContentMap = new ConcurrentHashMap<>();
    }

    public void deploy(String modelId, Upload upload, Map<String, String> additionalParameters)
    {
        validateModelId(modelId);

        validateUploadFile(modelId, upload);

        DetailedModelContent content = new DetailedModelContent();
        content.setFilename(upload.filename());

        if (additionalParameters != null && !additionalParameters.isEmpty())
        {
            content.setAdditionalParameters(additionalParameters);
        }

        try
        {
            PMML pmml = PMMLUtil.unmarshal(new ByteArrayInputStream(upload.content()));
            Evaluator evaluator = ModelEvaluatorFactory.newInstance().newModelEvaluator(pmml);
            evaluator.verify();

            content.setEvaluator(evaluator);

        } catch (Exception e)
        {
            Logger.error(e, "Exception during unmarshalling and verification of model id [{}]", modelId);
            throw new EvaluatorCreationException("Exception during unmarshalling and verification of model", e);
        }

        DetailedModelContent addedObj = modelIdToDetailContentMap.put(modelId, content);

        if (addedObj != null)
        {
            Logger.info("Model id [{}] replaced with new model", modelId);
        }

        Logger.info("Model uploaded with model id: [{}]", modelId);
    }

    public ScoringResult score(String modelId, ModelInputFields inputFields)
    {
        validateModelId(modelId);

        validateModelInputFields(modelId, inputFields);

        DetailedModelContent detailedModelContent = modelIdToDetailContentMap.get(modelId);

        if (detailedModelContent == null || detailedModelContent.getEvaluator() == null)
        {
            Logger.error("Model with given id cant be found: [{}]", modelId);
            throw new IllegalArgumentException("Model with given id cant be found");
        }

        try
        {
            Evaluator evaluator = detailedModelContent.getEvaluator();
            ScoringResult scoringResult = new ScoringResult(score(evaluator, inputFields));
            Logger.info("Model uploaded with model id: [{}]. Result is [{}]", modelId, scoringResult.getResult());

            return scoringResult;
        } catch (Exception e)
        {
            Logger.error(e, "Exception during preparation of input parameters or scoring of values for model id: [{}]", modelId);
            throw new ScoringException("Exception during preparation of input parameters or scoring of values", e);
        }
    }

    public void undeploy(String modelId)
    {
        validateModelId(modelId);

        validateModelAvailability(modelId);

        modelIdToDetailContentMap.remove(modelId);
        Logger.info("Model removed with model id: [{}]", modelId);
    }

    public List<String> getAllModelIds()
    {
       return Collections.list(modelIdToDetailContentMap.keys());
    }

    public Map<String, Map<String, String>> getAllAdditionalParameters()
    {
        return modelIdToDetailContentMap.entrySet().stream()
                .filter(entry -> (entry.getValue() != null && entry.getValue().getAdditionalParameters() != null))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().getAdditionalParameters()));
    }

    public Map<String, String> getAdditionalParameter(String modelId)
    {
        validateModelId(modelId);

        validateModelAvailability(modelId);

        DetailedModelContent modelContent = modelIdToDetailContentMap.get(modelId);

        if (modelContent == null)
        {
            Logger.error("Given model id does not contain any models: [{}]", modelId);
            throw new ModelNotFoundException("Given model id is empty: " + modelId);
        }

        Map<String, String> additionalParameters = modelContent.getAdditionalParameters();

        if (additionalParameters == null)
        {
            additionalParameters = new HashMap<>();
        }

        Logger.info("Additional parameters are fetched for model id: [{}]. Result is [{}]", modelId, additionalParameters);

        return additionalParameters;
    }

    private void validateModelInputFields(String modelId, ModelInputFields inputFields)
    {
        if (inputFields == null || inputFields.getFields() == null || inputFields.getFields().isEmpty())
        {
            Logger.error("Model input fields are null or empty for model id [{}]", modelId);
            throw new IllegalArgumentException("Model input fields are null or empty");
        }
    }

    private Map<FieldName, FieldValue> prepareEvaluationArgs(Evaluator evaluator, ModelInputFields inputFields)
    {
        Map<String, Object> inputFieldsAsMap = inputFields.getFields();

        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();

        List<InputField> evaluatorInputFields = evaluator.getActiveFields();

        for (InputField evaluatorInputField : evaluatorInputFields)
        {
            FieldName evaluatorInputFieldName = evaluatorInputField.getName();
            Object inputValue = inputFieldsAsMap.get(evaluatorInputFieldName.getValue());

            if (inputValue == null)
            {
                Logger.warn("Model value not found for the following field [{}]", evaluatorInputFieldName.getValue());
            }

            arguments.put(evaluatorInputFieldName, evaluatorInputField.prepare(inputValue));
        }
        return arguments;
    }

    private Map<String, Object> score(Evaluator evaluator, ModelInputFields inputFields)
    {
        Map<String, Object> result = new HashMap<>();

        Map<FieldName, ?> evaluationResultFromEvaluator = evaluator.evaluate(prepareEvaluationArgs(evaluator, inputFields));

        List<TargetField> targetFields = evaluator.getTargetFields();

        for (TargetField targetField : targetFields)
        {
            FieldName targetFieldName = targetField.getName();
            Object targetFieldValue = evaluationResultFromEvaluator.get(targetField.getName());

            if (targetFieldValue instanceof Computable)
            {
                targetFieldValue = ((Computable) targetFieldValue).getResult();
            }

            result.put(targetFieldName.getValue(), targetFieldValue);
        }
        return result;
    }

    private void validateModelId(String modelId)
    {
        if (Strings.isNullOrEmpty(modelId))
        {
            Logger.error("Model id is not valid. It is null or empty: [{}]", modelId);
            throw new IllegalArgumentException("Model id is empty");
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

    private void validateUploadFile(String modelId, Upload upload)
    {
        if (upload == null)
        {
            Logger.error("No file uploaded for model id [{}]", modelId);
            throw new IllegalArgumentException("Nothing is uploaded");
        }
    }
}