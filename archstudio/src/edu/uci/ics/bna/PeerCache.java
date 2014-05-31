package edu.uci.ics.bna;

import java.lang.reflect.*;
import java.util.*;

public class PeerCache {
	
	protected BNAComponent bnaComponent;
	protected Map peerMap;
	
	public PeerCache(BNAComponent bnaComponent){
		this.bnaComponent = bnaComponent;
		this.peerMap = Collections.synchronizedMap(new HashMap());
	}
	
	public ThingPeer createPeer(Thing th){
		try{
			Class peerClass = th.getPeerClass();
			Constructor constructor = peerClass.getConstructor(new Class[]{BNAComponent.class, Thing.class});
			ThingPeer peer = (ThingPeer)constructor.newInstance(new Object[]{bnaComponent, th});
			peerMap.put(th, peer);
			return peer;
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException("Could not instantiate peer.", ite);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException("Could not instantiate peer.", iae);
		}
		catch(InstantiationException ie){
			throw new RuntimeException("Could not instantiate peer.", ie);
		}
		catch(NoSuchMethodException nsme){
			throw new RuntimeException("Invalid peer class.", nsme);
		}
	}
	
	int cnt = 0;
	public ThingPeer getPeer(Thing th){
		ThingPeer peer = (ThingPeer)peerMap.get(th);
		if(peer != null){
			return peer;
		}
		else{
			return createPeer(th);
		}
	}	
	

}
