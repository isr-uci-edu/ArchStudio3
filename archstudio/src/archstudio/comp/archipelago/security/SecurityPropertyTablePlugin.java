package archstudio.comp.archipelago.security;

import archstudio.comp.archipelago.AbstractPropertyTablePlugin;
import archstudio.comp.archipelago.types.BrickTypeThing;
import archstudio.comp.booleannotation.IBooleanNotation;
import c2.fw.secure.ISubject;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.floatingtable.TableData;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;

public class SecurityPropertyTablePlugin extends AbstractPropertyTablePlugin{

	protected IBooleanNotation bni;

	public SecurityPropertyTablePlugin(XArchFlatInterface xarch, IBooleanNotation bni){
		super(xarch);
		this.bni = bni;
	}
	
	public void addProperties(Thing t, ObjRef thingRef, TableData td){
		if(t instanceof BrickTypeThing){
			try{
				ObjRef	securityRef = (ObjRef)xarch.get(thingRef, "Security");
				if (securityRef != null) {
					td.addRow(tableUtils.createSubheadRow("Security"));

			    	ObjRef		subjectRef = (ObjRef)xarch.get(securityRef, ISubject.SUBJECT);
			    	String		subjectName = "[No Subject assigned]";
			    	if (subjectRef != null) {
			    		subjectName = (String)xarch.get(subjectRef, "value");
			    	}
					td.addRow(tableUtils.createBodyRow("Subject", subjectName));
			    	
			    	ObjRef		principalsRef = (ObjRef)xarch.get(securityRef, "Principals");
			    	String[]	principals = new String[]{};
			    	if (principalsRef != null) {
			    		ObjRef[]	principalRefs = (ObjRef[])xarch.getAll(principalsRef, "Principal");
			    		principals = new String[principalRefs.length];
			    		for (int i = 0; i<principalRefs.length; i++) {
			    			principals[i] =  (String)xarch.get(principalRefs[i], "value");
			    		}
			    	}
		    		td.addRows(tableUtils.createMultiValueBodyRows("Principals", principals));

			    	ObjRef		privilegesRef = (ObjRef)xarch.get(securityRef, "Privileges");
			    	String[]	privileges = new String[]{};
			    	if (privilegesRef != null) {
			    		ObjRef[]	privilegeRefs = (ObjRef[])xarch.getAll(privilegesRef, "Privilege");
			    		privileges = new String[privilegeRefs.length];
			    		for (int i = 0; i<privilegeRefs.length; i++) {
			    			privileges[i] = (String)xarch.get(privilegeRefs[i], "value");
			    		}
			    	}
		    		td.addRows(tableUtils.createMultiValueBodyRows("Privileges", privileges));

			    	ObjRef		policyRef = (ObjRef)xarch.get(securityRef, "PolicySet");
			    	String		policy = "";
			    	if (policyRef != null) {
			    		policy = (String)xarch.get(policyRef, "Policy");
			    	}
					td.addRow(tableUtils.createBodyRow("Policy", policy));
				}
			}
			catch(Exception e){
				return;
			}
		}
	}
}
