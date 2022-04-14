package uniregistrar.request;

import java.util.Map;

public interface Request {

    String getJobId();
    void setJobId(String jobId);

    Map<String, Object> getOptions();
    void setOptions(Map<String, Object> options);

    Map<String, Object> getSecret();
    void setSecret(Map<String, Object> secret);
}
