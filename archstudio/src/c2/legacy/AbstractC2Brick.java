package c2.legacy;

import c2.fw.*;

/**
 * Base class that can be used to derive legacy C2-style components.
 * <B><FONT COLOR="#FF0000">NOTE!</FONT></B>: The preferred base class is now {@link c2.legacy.AbstractC2DelegateBrick}.
 * @author Eric M. Dashofy, <A HREF="mailto:edashofy@ics.uci.edu">edashofy@ics.uci.edu</A>
 */
public abstract class AbstractC2Brick extends c2.fw.AbstractBrick implements c2.fw.Brick{

	public static final Identifier TOP_INTERFACE_ID = new SimpleIdentifier("IFACE_TOP");
	public static final Identifier BOTTOM_INTERFACE_ID = new SimpleIdentifier("IFACE_BOTTOM");
	
	protected Interface topIface;
	protected Interface bottomIface;
	
	public AbstractC2Brick(Identifier id){
		super(id);
		topIface = new SimpleInterface(TOP_INTERFACE_ID, this);
		bottomIface = new SimpleInterface(BOTTOM_INTERFACE_ID, this);
	}
	
	public String getType(){
		return this.getClass().getName();
	}
	
	public Interface getInterface(Identifier id){
		if(id.equals(TOP_INTERFACE_ID)){
			return topIface;
		}
		else if(id.equals(BOTTOM_INTERFACE_ID)){
			return bottomIface;
		}
		else{
			return null;
		}
	}
	
	public Interface[] getAllInterfaces(){
		return new Interface[]{
			topIface,
			bottomIface
		};
	}
	
	public boolean hasInterface(Identifier id){
		return getInterface(id) != null;
	}
	
	public void sendNotification(Message m){
		if(m instanceof NamedPropertyMessage){
			m = Utils.tagAsC2Notification((NamedPropertyMessage)m);
		}
		sendToAll(m, bottomIface);
	}
	
	public void sendRequest(Message m){
		if(m instanceof NamedPropertyMessage){
			m = Utils.tagAsC2Request((NamedPropertyMessage)m);
		}
		sendToAll(m, topIface);
	}

}

