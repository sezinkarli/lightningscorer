package service.impl;

import com.google.common.base.Strings;
import domain.ModelInputFields;
import org.jpmml.evaluator.Evaluator;
import org.pmw.tinylog.Logger;
import org.rapidoid.annotation.Service;
import org.rapidoid.io.Upload;

@Service
public class ValidatorService
{
    public void validateModelId(String modelId)
    {
        if (Strings.isNullOrEmpty(modelId))
        {
            Logger.error("Model id is not valid. It is null or empty: [{}]", modelId);
            throw new IllegalArgumentException("Model id is empty");
        }
    }

    public void validateUploadFile(String modelId, Upload upload)
    {
        if (upload == null)
        {
            Logger.error("No file uploaded for model id [{}]", modelId);
            throw new IllegalArgumentException("Nothing is uploaded");
        }
    }

    public void validateEvaluator(Evaluator evaluator, String modelId)
    {
        if (evaluator == null)
        {
            Logger.error("Model with given id does not have an evaluator: [{}]", modelId);
            throw new IllegalArgumentException("Model with given id does not have an evaluator");
        }
    }

    public void validateModelInputFields(String modelId, ModelInputFields inputFields)
    {
        if (inputFields == null || inputFields.getFields() == null || inputFields.getFields().isEmpty())
        {
            Logger.error("Model input fields are null or empty for model id [{}]", modelId);
            throw new IllegalArgumentException("Model input fields are null or empty");
        }
    }
}
