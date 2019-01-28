package service.impl;

import com.google.common.base.Strings;
import domain.DetailedModelContent;
import domain.ModelInputFields;
import domain.ModelSummary;
import domain.ScoringResult;
import exception.AdditionalParametersException;
import exception.EvaluatorCreationException;
import exception.ScoringException;
import exception.SummaryException;
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

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ModelService
{
    private ModelHolderService modelHolderService;

    @Inject
    public ModelService(ModelHolderService modelHolderService)
    {
        this.modelHolderService = modelHolderService;
    }

    public void deploy(String modelId, Upload upload, Map<String, String> additionalParameters)
    {
        validateModelId(modelId);
        validateUploadFile(modelId, upload);

        DetailedModelContent content = new DetailedModelContent();
        content.setFilename(upload.filename());
        content.setAdditionalParameters(Optional.ofNullable(additionalParameters));

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

        modelHolderService.put(modelId, content);

        Logger.info("Model uploaded with model id: [{}]", modelId);
    }

    public ModelSummary getSummary(String modelId, boolean isExtended)
    {
        validateModelId(modelId);

        try
        {
            DetailedModelContent detailedModelContent = modelHolderService.get(modelId);
            Evaluator evaluator = detailedModelContent.getEvaluator();

            validateEvaluator(evaluator, modelId);

            ModelSummary modelSummary = new ModelSummary(evaluator.getSummary());

            if (isExtended)
            {
                addExtendedModelInfo(modelSummary, evaluator);
            }

            Logger.info("Model summary is prepared for model id: [{}]. Result is [{}]", modelId, modelSummary);

            return modelSummary;
        } catch (Exception e)
        {
            Logger.error(e, "Exception during retrieval of summary for model id: [{}]", modelId);
            throw new SummaryException("Exception during retrieval of summary", e);
        }
    }

    public ScoringResult score(String modelId, ModelInputFields inputFields)
    {
        validateModelId(modelId);
        validateModelInputFields(modelId, inputFields);

        try
        {
            DetailedModelContent detailedModelContent = modelHolderService.get(modelId);
            Evaluator evaluator = detailedModelContent.getEvaluator();

            validateEvaluator(evaluator, modelId);

            ScoringResult scoringResult = new ScoringResult( score(evaluator, inputFields) );
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

        modelHolderService.remove(modelId);
    }

    public void undeployAll()
    {
        modelHolderService.clear();
        Logger.info("All models removed");
    }

    public List<String> getAllModelIds()
    {
        return modelHolderService.getAllModelIds();
    }

    public Map<String, Map<String, String>> getAllAdditionalParameters()
    {
        return modelHolderService.getAllAdditionalParameters();
    }

    public Map<String, String> getAdditionalParameter(String modelId)
    {
        validateModelId(modelId);

        DetailedModelContent modelContent = modelHolderService.get(modelId);

        Optional<Map<String, String>> additionalParameters;
        try
        {
            additionalParameters = modelContent.getAdditionalParameters();
        } catch (Exception e)
        {
            Logger.error(e, "Exception during retrieval of additional parameters");
            throw new AdditionalParametersException("Exception during retrieval of additional parameters", e);
        }

        Map<String, String> result = additionalParameters.orElse(Collections.emptyMap());

        Logger.info("Additional parameters are fetched for model id: [{}]. Result is [{}]", modelId, result);

        return result;
    }

    private Map<FieldName, FieldValue> prepareEvaluationArgs(Evaluator evaluator, ModelInputFields inputFields)
    {
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();

        List<InputField> evaluatorFields = evaluator.getActiveFields();

        for (InputField evaluatorField : evaluatorFields)
        {
            FieldName evaluatorFieldName = evaluatorField.getName();
            String evaluatorFieldNameValue = evaluatorFieldName.getValue();

            Object inputValue = inputFields.getFields().get(evaluatorFieldNameValue);

            if (inputValue == null)
            {
                Logger.warn("Model value not found for the following field [{}]", evaluatorFieldNameValue);
            }

            arguments.put(evaluatorFieldName, evaluatorField.prepare(inputValue));
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

    private void validateUploadFile(String modelId, Upload upload)
    {
        if (upload == null)
        {
            Logger.error("No file uploaded for model id [{}]", modelId);
            throw new IllegalArgumentException("Nothing is uploaded");
        }
    }

    private void validateEvaluator(Evaluator evaluator, String modelId)
    {
        if (evaluator == null)
        {
            Logger.error("Model with given id does not have an evaluator: [{}]", modelId);
            throw new IllegalArgumentException("Model with given id does not have an evaluator");
        }
    }

    private void validateModelInputFields(String modelId, ModelInputFields inputFields)
    {
        if (inputFields == null || inputFields.getFields() == null || inputFields.getFields().isEmpty())
        {
            Logger.error("Model input fields are null or empty for model id [{}]", modelId);
            throw new IllegalArgumentException("Model input fields are null or empty");
        }
    }

    private void addExtendedModelInfo(ModelSummary modelSummary, Evaluator evaluator)
    {
        modelSummary.setInputFields(evaluator.getInputFields() != null ? evaluator.getInputFields().toString() : null);
        modelSummary.setOutputFields(evaluator.getTargetFields() != null ? evaluator.getTargetFields().toString() : null);
    }
}