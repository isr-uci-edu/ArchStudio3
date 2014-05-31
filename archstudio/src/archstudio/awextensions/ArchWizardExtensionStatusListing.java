package archstudio.awextensions;

public interface ArchWizardExtensionStatusListing {
	/**
	 * Indicates that the extension component is active and busy.
	 */
	public static final int STATUS_ACTIVE = 1234;
	
	/**
	 * Indicates the extension component is inactive.
	 */
	public static final int STATUS_INACTIVE = 1236;
	
	/**
	 * Indicates the extension component is unavailable.
	 */
	public static final int STATUS_UNAVAILABLE = 1237;
}