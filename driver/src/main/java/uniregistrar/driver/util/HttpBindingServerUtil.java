package uniregistrar.driver.util;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.state.CreateState;
import uniregistrar.state.SetStateFailed;
import uniregistrar.state.SetStateFinished;
import uniregistrar.state.State;

import java.io.IOException;

public class HttpBindingServerUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpBindingServerUtil.class);

    public static String toHttpBodyStreamState(State state) throws IOException {
        String jsonString = state.toString();
        if (log.isDebugEnabled()) log.debug("HTTP body for stream result: " + jsonString);
        return jsonString;
    }

    public static int httpStatusCodeForState(State state) {
        if (RegistrationException.ERROR_NOTFOUND.equals(SetStateFailed.getStateFailedError(state)))
            return HttpStatus.SC_NOT_FOUND;
        else if (RegistrationException.ERROR_BADREQUEST.equals(SetStateFailed.getStateFailedError(state)))
            return HttpStatus.SC_BAD_REQUEST;
        else if (SetStateFailed.isStateFailed(state))
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        else if (SetStateFinished.isStateFinished(state) && (state instanceof CreateState))
            return HttpStatus.SC_CREATED;
        else
            return HttpStatus.SC_OK;
    }
}
