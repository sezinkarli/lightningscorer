package domain;

import org.jpmml.evaluator.Evaluator;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public class DetailedModelContent implements Serializable
{
    private static final long serialVersionUID = 1896212006782157146L;

    private String filename;
    private Optional<Map<String, String>> additionalParameters;
    private Evaluator evaluator;

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public Optional<Map<String, String>> getAdditionalParameters()
    {
        return additionalParameters;
    }

    public void setAdditionalParameters(Optional<Map<String, String>> additionalParameters)
    {
        this.additionalParameters = additionalParameters;
    }

    public Evaluator getEvaluator()
    {
        return evaluator;
    }

    public void setEvaluator(Evaluator evaluator)
    {
        this.evaluator = evaluator;
    }
}
