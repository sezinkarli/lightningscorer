package domain;


import java.io.Serializable;

public class ModelSummary implements Serializable
{
    private static final long serialVersionUID = -1809182620110068649L;

    private String summary;
    private String inputFields;
    private String outputFields;

    public ModelSummary()
    {
    }

    public ModelSummary(String summary)
    {
        this.summary = summary;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getInputFields()
    {
        return inputFields;
    }

    public void setInputFields(String inputFields)
    {
        this.inputFields = inputFields;
    }

    public String getOutputFields()
    {
        return outputFields;
    }

    public void setOutputFields(String outputFields)
    {
        this.outputFields = outputFields;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("ModelSummary{");
        sb.append("summary='").append(summary).append('\'');
        sb.append(", inputFields='").append(inputFields).append('\'');
        sb.append(", outputFields='").append(outputFields).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
