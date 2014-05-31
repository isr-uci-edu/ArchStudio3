package archstudio.comp.archipelago;

import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.floatingtable.TableData;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;

public class ApplyThemeThemePropertyTablePlugin
	extends AbstractPropertyTablePlugin {

	public ApplyThemeThemePropertyTablePlugin(XArchFlatInterface xarch) {
		super(xarch);
	}

	public void addProperties(Thing t, ObjRef thingRef, TableData td){
		tableUtils.applyTheme(td);
		td.setBorder(1);
	}

}
