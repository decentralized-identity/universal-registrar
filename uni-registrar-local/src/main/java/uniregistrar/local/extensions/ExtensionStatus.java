package uniregistrar.local.extensions;

public class ExtensionStatus {

	public static final ExtensionStatus DEFAULT = new ExtensionStatus(false, false, false);

	public static final ExtensionStatus SKIP_EXTENSION_BEFORE = new ExtensionStatus(true, false, false);
	public static final ExtensionStatus SKIP_DRIVER = new ExtensionStatus(false, true, false);
	public static final ExtensionStatus SKIP_EXTENSIONS_AFTER = new ExtensionStatus(false, false, true);

	private boolean skipExtensionsBefore;
	private boolean skipDriver;
	private boolean skipExtensionsAfter;

	public ExtensionStatus(boolean skipExtensionsBefore, boolean skipDriver, boolean skipExtensionsAfter) {

		this.skipExtensionsBefore = skipExtensionsBefore;
		this.skipDriver = skipDriver;
		this.skipExtensionsAfter = skipExtensionsAfter;
	}

	public ExtensionStatus() {

		this(false, false, false);
	}

	public void or(ExtensionStatus extensionStatus) {

		if (extensionStatus == null) return;

		this.skipExtensionsBefore |= extensionStatus.skipExtensionsBefore;
		this.skipDriver |= extensionStatus.skipDriver;
		this.skipExtensionsAfter |= extensionStatus.skipExtensionsAfter;
	}

	public boolean skip(String extensionStage) {
		return switch (extensionStage) {
			case "beforeReadCreate", "beforeReadUpdate", "beforeReadDeactivate", "beforeReadExecute" -> false;
			case "beforeCreate", "beforeUpdate", "beforeDeactivate", "beforeExecute" -> this.skipExtensionsBefore;
			case "beforeCreateResource", "beforeUpdateResource", "beforeDeactivateResource" -> this.skipExtensionsBefore;
			case "afterCreate", "afterUpdate", "afterDeactivate", "afterExecute" -> this.skipExtensionsAfter;
			case "afterCreateResource", "afterUpdateResource", "afterDeactivateResource" -> this.skipExtensionsAfter;
			case "beforeWriteCreate", "beforeWriteUpdate", "beforeWriteDeactivate", "beforeWriteExecute" -> false;
			default -> throw new IllegalStateException("Unexpected extension stage: " + extensionStage);
		};
	}

	public boolean skipExtensionsBefore() {

		return this.skipExtensionsBefore;
	}

	public boolean skipDriver() {

		return this.skipDriver;
	}

	public boolean skipExtensionsAfter() {

		return this.skipExtensionsAfter;
	}
}
