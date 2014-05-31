package archstudio.awextensions;

import c2.fw.*;

public class ArchWizardExtensionSetStatusMessage extends NamedPropertyMessage {
	// Class variables
	public static final String MESSAGE_NAME = "ArchWizardExtensionSetStatusMessage";
	public static final String EXTENSION_IDS = "extensionIDs";
	public static final String NEW_STATUS = "newStatus";
	
	// Class constructor
	public ArchWizardExtensionSetStatusMessage(Identifier extensionID, int newStatus) {
		super(MESSAGE_NAME);
		super.addParameter(EXTENSION_IDS, new Identifier[]{extensionID});
		super.addParameter(NEW_STATUS, newStatus);
	}
	
	// Class constructor
	public ArchWizardExtensionSetStatusMessage(Identifier[] extensionIDs, int newStatus) {
		super(MESSAGE_NAME);
		super.addParameter(EXTENSION_IDS, extensionIDs);
		super.addParameter(NEW_STATUS, newStatus);
	}
	
	public Identifier[] getExtensionIDs() {
		return (Identifier[])getParameter(EXTENSION_IDS);
	}
	
	public int getNewStatus() {
		return getIntParameter(NEW_STATUS);
	}
	
	protected ArchWizardExtensionSetStatusMessage(ArchWizardExtensionSetStatusMessage copyMe) {
		super(copyMe);
	}
	
	public Message duplicate() {
		return new ArchWizardExtensionSetStatusMessage(this);
	}
}