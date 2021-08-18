package uniregistrar.local.extensions.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.local.LocalUniRegistrar;
import uniregistrar.local.extensions.Extension;
import uniregistrar.local.extensions.Extension.AbstractExtension;
import uniregistrar.local.extensions.ExtensionStatus;
import uniregistrar.request.CreateRequest;
import uniregistrar.state.CreateState;

public class DummyExtension extends AbstractExtension implements Extension {

	private static Logger log = LoggerFactory.getLogger(DummyExtension.class);

	@Override
	public ExtensionStatus afterCreate(String method, CreateRequest createRequest, CreateState createState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

		if (log.isDebugEnabled()) log.debug("Dummy extension called!");
		return ExtensionStatus.DEFAULT;
	}
}
