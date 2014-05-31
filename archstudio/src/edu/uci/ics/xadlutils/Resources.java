package edu.uci.ics.xadlutils;

import javax.swing.Icon;

public class Resources {
	public static final Icon TYPES_ICON;
	public static final Icon STRUCTURE_ICON;
	public static final Icon COMPONENT_ICON;
	public static final Icon CONNECTOR_ICON;
	public static final Icon INTERFACE_ICON;
	public static final Icon COMPONENT_TYPE_ICON;
	public static final Icon CONNECTOR_TYPE_ICON;
	public static final Icon INTERFACE_TYPE_ICON;
	public static final Icon DOCUMENT_ICON;
	public static final Icon FOLDER_ICON;
	
	public static final Icon GO_ICON;
	public static final Icon EDIT_ICON;
	public static final Icon INFO_ICON_32;
	public static final Icon INFO_ICON_16;
	public static final Icon WARNING_ICON_32;
	public static final Icon WARNING_ICON_16;
	public static final Icon ERROR_ICON_32;
	public static final Icon ERROR_ICON_16;
	
	//Overlay icons that can be added to the above icons
	//using CompositeIcon
	public static final Icon XML_OVERLAY_ICON;
	public static final Icon CHECKMARK_OVERLAY_ICON;
	public static final Icon CHECKBOX_CHECKED_OVERLAY_ICON;
	public static final Icon CHECKBOX_UNCHECKED_OVERLAY_ICON;
	public static final Icon RED_X_OVERLAY_ICON;
	
	static{
		TYPES_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/types.gif");
		STRUCTURE_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/structure.gif");
		COMPONENT_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/component.gif");
		CONNECTOR_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/connector.gif");
		INTERFACE_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/interface.gif");
		COMPONENT_TYPE_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/component-type.gif");
		CONNECTOR_TYPE_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/connector-type.gif");
		INTERFACE_TYPE_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/interface-type.gif");
		DOCUMENT_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/doc.gif");
		FOLDER_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/folder.gif");

		GO_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/go.gif");
		EDIT_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/edit.gif");

		INFO_ICON_16 = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/info16.gif");
		INFO_ICON_32 = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/info32.gif");
		WARNING_ICON_16 = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/warning16.gif");
		WARNING_ICON_32 = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/warning32.gif");
		ERROR_ICON_16 = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/error16.gif");
		ERROR_ICON_32 = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/error32.gif");
		
		XML_OVERLAY_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/overlay-xml.gif");
		CHECKMARK_OVERLAY_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/overlay-checkmark.gif");
		CHECKBOX_CHECKED_OVERLAY_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/overlay-checkbox-checked.gif");
		CHECKBOX_UNCHECKED_OVERLAY_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/overlay-checkbox-unchecked.gif");
		RED_X_OVERLAY_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("edu/uci/ics/xadlutils/res/overlay-redx.gif");
	}

	private Resources(){}
}
