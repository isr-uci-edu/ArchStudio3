// *****************************************************************************
// ** Class Name: TypeMatchingArchWizardExtensionC2Component                  **
// **                                                                         **
// ** Description: This ArchWizard extension ensures that the interfaces of   **
// **              Components, Connectors and elements extending them match   **
// **              the signatures of their indicated type.                    **
// **                                                                         **
// ** 2/25/2003 - John Georgas[jgeorgas@ics.uci.edu]                          **
// **             Initial development.                                        **
// ** 5/7/2003 - John Georgas[jgeorgas@ics.uci.edu]                           **
// **            Improved event filtering.                                    **
// **                                                                         **
// ** Copyright 2003, by the University of California, Irvine.                **
// ** ALL RIGHTS RESERVED.                                                    **
// *****************************************************************************

package archstudio.comp.awextensions.typematching;

// C2 imports
import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.*;

import archstudio.awextensions.*;

// xArch utilities imports
import edu.uci.isr.xarch.*;
import edu.uci.ics.xarchutils.*;

// Java imports
import java.util.*;

public class TypeMatchingArchWizardExtensionC2Component extends AbstractArchWizardExtension {
	
	// Class constructor
	public TypeMatchingArchWizardExtensionC2Component(Identifier id) {
		super(id);
	}
	
	protected String getDescription() {
		return "This ArchWizard extension ensures that the interfaces of Components, Connectors " +
		       "and elements extending them match the signatures of their indicated type.  It will be " +
		       "triggered when the type of one of these elements is changed.";
	}
	
	protected void handleStateChangeEvent(XArchFlatEvent evt) {
		if (isActive()) {
			if (evt.getIsAttached()) {
				if (evt.getEventType() == XArchFlatEvent.SET_EVENT) {
					if ((evt.getTargetName()).equals("href")) {
						ObjRef parentRef = xarch.getParent(evt.getSource());
						if (xarch.isInstanceOf(parentRef, "edu.uci.isr.xarch.types.ComponentImpl") ||
						    xarch.isInstanceOf(parentRef, "edu.uci.isr.xarch.types.ConnectorImpl")) {
						    	matchTypeStructure(parentRef, (String)(evt.getTarget()));
						}
					}
				}
			}
		}
	}
	
	private void matchTypeStructure(ObjRef to, String href) {
		ObjRef fromRef = xarch.resolveHref(xarch.getXArch(to), href);
		if (fromRef != null) {
			xarch.clear(to, "Interface");
			ObjRef typesContextRef = xarch.createContext(xarch.getXArch(to), "types");
			ObjRef instanceContextRef = xarch.createContext(xarch.getXArch(to), "instance");
			ObjRef[] sigs = xarch.getAll(fromRef, "Signature");
			for (int i = 0; i < sigs.length; i++) {	
				ObjRef intRef = xarch.create(typesContextRef, "Interface");
				ObjRef sigTypeRef = (ObjRef)(xarch.get(sigs[i], "Type"));
				if (sigTypeRef != null) {
					ObjRef intTypeRef = xarch.create(instanceContextRef, "XMLLink");
					xarch.set(intTypeRef, "Href", (String)(xarch.get(sigTypeRef, "Href")));
					xarch.set(intTypeRef, "Type", (String)(xarch.get(sigTypeRef, "Type")));
					xarch.set(intRef, "Type", intTypeRef);
				}
				ObjRef sigDirRef = (ObjRef)(xarch.get(sigs[i], "Direction"));
				if (sigDirRef != null) {
					ObjRef dirRef = xarch.create(instanceContextRef, "Direction");
					xarch.set(dirRef, "Value", (String)(xarch.get(sigDirRef, "Value")));
					xarch.set(intRef, "Direction", dirRef);
				}
				xarch.add(to, "Interface", intRef);
			}
		}
	}
}