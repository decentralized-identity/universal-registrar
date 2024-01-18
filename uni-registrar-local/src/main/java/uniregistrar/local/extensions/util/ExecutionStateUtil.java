package uniregistrar.local.extensions.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.local.extensions.Extension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExecutionStateUtil {

    private static final String EXTENSION_STAGES = "extensionStages";

    private static final Logger log = LoggerFactory.getLogger(ExecutionStateUtil.class);

    public static <E extends Extension> void addExtensionStage(Map<String, Object> executionState, Class<E> extensionClass, Extension extension) {

        String extensionStage = extensionClass.getAnnotation(Extension.ExtensionStage.class).value();
        String extensionName = extension.getClass().getSimpleName();
        if (log.isDebugEnabled()) log.debug("Add extension stage: " + extensionStage + " / " + extensionName);

        LinkedHashMap<String, List<String>> executionStateStages = (LinkedHashMap<String, List<String>>) executionState.computeIfAbsent(EXTENSION_STAGES, f -> new LinkedHashMap<String, List<String>>());
        List<String> executionStateStagesExtensions = executionStateStages.computeIfAbsent(extensionStage, f -> new ArrayList<>());
        executionStateStagesExtensions.add(extensionName);
    }

    public static <E extends Extension> boolean checkExtensionStage(Map<String, Object> executionState, Class<E> extensionClass, Extension extension) {

        String extensionStage = extensionClass.getAnnotation(Extension.ExtensionStage.class).value();
        String extensionName = extension.getClass().getSimpleName();
        if (log.isDebugEnabled()) log.debug("Check extension stage: " + extensionStage + " / " + extensionName);

        LinkedHashMap<String, List<String>> executionStateStages = (LinkedHashMap<String, List<String>>) executionState.get(EXTENSION_STAGES);
        if (executionStateStages == null) return false;

        List<String> executionStateStagesExtensions = executionStateStages.get(extensionStage);
        if (executionStateStagesExtensions == null) return false;

        return executionStateStagesExtensions.contains(extensionName);
    }
}
