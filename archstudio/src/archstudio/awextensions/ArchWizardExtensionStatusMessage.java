package archstudio.awextensions;

import c2.fw.*;

public class ArchWizardExtensionStatusMessage extends NamedPropertyMessage {
	// Class variables
	public static final String MESSAGE_NAME = "ArchWizardExtensionStatusMessage";
	public static final String EXTENSION_ID = "extensionID";
	public static final String DESC = "description";
	public static final String STATUS = "status";
	
	// Class constructor
	public ArchWizardExtensionStatusMessage(Identifier extensionID, String description, int status) {
		super(MESSAGE_NAME);
		super.addParameter(EXTENSION_ID, extensionID);
		super.addParameter(DESC, description);
		super.addParameter(STATUS, status);
	}
	
	public Identifier getExtensionID() {
		return (Identifier)getParameter(EXTENSION_ID);
	}
	
	public String getDescription() {
		return (String)getParameter(DESC);
	}
	
	public int getStatus() {
		return getIntParameter(STATUS);
	}
	
	protected ArchWizardExtensionStatusMessage(ArchWizardExtensionStatusMessage copyMe) {
		super(copyMe);
	}
	
	public Message duplicate() {
		return new ArchWizardExtensionStatusMessage(this);
	}
}