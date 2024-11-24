package uniregistrar.driver.util;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.openapi.model.*;
import uniregistrar.util.HttpBindingUtil;

import java.io.IOException;
import java.util.Map;

public class HttpBindingServerUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpBindingServerUtil.class);

    public static String toHttpBodyStreamState(Map<String, Object> stateMap) throws IOException {
        String jsonString = HttpBindingUtil.toHttpBodyMap(stateMap);
        if (log.isDebugEnabled()) log.debug("HTTP body for stream result: " + jsonString);
        return jsonString;
    }

    public static int httpStatusCodeForState(RegistrarState state) {
        if (state.getDidState() instanceof DidStateFailed didStateFailed) {
            if (RegistrationException.ERROR_NOTFOUND.equals(didStateFailed.getError()))
                return HttpStatus.SC_NOT_FOUND;
            else if (RegistrationException.ERROR_BADREQUEST.equals(didStateFailed.getError()))
                return HttpStatus.SC_BAD_REQUEST;
            else
                return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        } else if (state.getDidState() instanceof DidStateFinished) {
            if (state instanceof CreateState) {
                return HttpStatus.SC_CREATED;
            } else {
                return HttpStatus.SC_OK;
            }
        } else {
            return HttpStatus.SC_OK;
        }
    }

    public static int httpStatusCodeForState(RegistrarResourceState state) {
        if (state.getDidUrlState() instanceof DidUrlStateFailed didUrlStateFailed) {
            if (RegistrationException.ERROR_NOTFOUND.equals(didUrlStateFailed.getError()))
                return HttpStatus.SC_NOT_FOUND;
            else if (RegistrationException.ERROR_BADREQUEST.equals(didUrlStateFailed.getError()))
                return HttpStatus.SC_BAD_REQUEST;
            else
                return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        } else if (state.getDidUrlState() instanceof DidUrlStateFinished) {
            if (state instanceof CreateResourceState) {
                return HttpStatus.SC_CREATED;
            } else {
                return HttpStatus.SC_OK;
            }
        } else {
            return HttpStatus.SC_OK;
        }
    }
}
