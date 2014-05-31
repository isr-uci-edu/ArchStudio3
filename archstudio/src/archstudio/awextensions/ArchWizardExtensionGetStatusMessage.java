package archstudio.awextensions;

import c2.fw.*;

public class ArchWizardExtensionGetStatusMessage extends NamedPropertyMessage {
	// Class variables
	public static final String MESSAGE_NAME = "ArchWizardExtensionGetStatusMessage";
	public static final String EXTENSION_IDS = "extensionIDs";
	
	// Class constructor
	public ArchWizardExtensionGetStatusMessage() {
		super(MESSAGE_NAME);
	}
	
	// Class constructor
	public ArchWizardExtensionGetStatusMessage(Identifier extensionID) {
		super(MESSAGE_NAME);
		super.addParameter(EXTENSION_IDS, new Identifier[]{extensionID});
	}
	
	// Class constructor
	public ArchWizardExtensionGetStatusMessage(Identifier[] extensionIDs) {
		super(MESSAGE_NAME);
		super.addParameter(EXTENSION_IDS, extensionIDs);
	}
	
	public Identifier[] getExtensionIDs() {
		return (Identifier[])getParameter(EXTENSION_IDS);
	}
	
	protected ArchWizardExtensionGetStatusMessage(ArchWizardExtensionGetStatusMessage copyMe) {
		super(copyMe);
	}
	
	public Message duplicate() {
		return new ArchWizardExtensionGetStatusMessage(this);
	}
}