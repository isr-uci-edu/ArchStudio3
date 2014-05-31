package archstudio.comp.criticmanager;

import java.util.*;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;
import c2.util.AdaptingQueue;

import archstudio.critics.*;

import edu.uci.ics.xarchutils.*;

public class CriticManagerC2Component extends AbstractC2DelegateBrick{

	//Maps critic IDs to CriticStatusMessages
	//This indicates the statuses that the critics themselves think they're in
	private Map criticStatuses;

	public CriticManagerC2Component(Identifier id){
		super(id);
		criticStatuses = Collections.synchronizedMap(new HashMap());
		addMessageProcessor(new CriticStatusMessageProcessor());
	}
	
	class CriticManagerLifecycleProcessor extends LifecycleAdapter{
		public void begin(){
			CriticGetStatusMessage cgsm = new CriticGetStatusMessage(new Identifier[]{});
			sendToAll(cgsm, topIface);
		}
	}
	
	class CriticMissingException extends Exception{
		private Identifier criticID;
		private Identifier missingDependencyID;
		
		public CriticMissingException(Identifier criticID, Identifier missingDependencyID){
			this.criticID = criticID;
			this.missingDependencyID = missingDependencyID;
		}
		
		public Identifier getCriticID(){
			return criticID;
		}
		
		public Identifier getMissingDependencyID(){
			return missingDependencyID;
		}
	}
	
	private Set getCriticDependenciesRec(Identifier critic) throws CriticMissingException{
		Set s = new HashSet();
		try{
			addCriticDependenciesRec(s, critic);
		}
		catch(CriticMissingException cme){
			CriticMissingException cme2 = new CriticMissingException(critic, cme.getMissingDependencyID());
			throw cme2;
		}
		return s;
	}
	
	private void addCriticDependenciesRec(Set s, Identifier critic) throws CriticMissingException{
		CriticStatusMessage csm = (CriticStatusMessage)criticStatuses.get(critic);
		if(csm == null){
			throw new CriticMissingException(null, critic);
		}

		//Don't hang on circular dependencies
		if(s.contains(critic)){
			return;
		}

		Identifier[] dependencies = csm.getDependencies();
		if(dependencies != null){
			for(int i = 0; i < dependencies.length; i++){
				addCriticDependenciesRec(s, dependencies[i]);
			}
		}
		s.add(critic);
	}
	
	private Set getReverseCriticDependenciesRec(Identifier criticID){
		Set reverseDependencies = new HashSet();
		
		for(Iterator it = criticStatuses.values().iterator(); it.hasNext(); ){
			CriticStatusMessage csm = (CriticStatusMessage)it.next();
		
			Identifier otherCriticID = csm.getCriticID();
			try{
				Set otherCriticDependencies = getCriticDependenciesRec(otherCriticID);
				if(otherCriticDependencies.contains(criticID)){
					reverseDependencies.add(otherCriticID);
				}
			}
			catch(CriticMissingException cme){
			}
		}
		return reverseDependencies;
	}
	
	
	private synchronized CriticStatusMessage getActualStatus(Identifier critic) throws CriticMissingException{
		CriticStatusMessage initialStatusMessage = (CriticStatusMessage)criticStatuses.get(critic);
		if(initialStatusMessage == null){
			throw new CriticMissingException(critic, null);
		}
		int initialStatus = initialStatusMessage.getStatus();
		
		if(initialStatus == CriticStatuses.STAT_AVAILABLE_ACTIVE){
			//If the critic thinks it's active, it may actually be in a state of waiting.
			//Check dependent critics...
			Set dependencies = getCriticDependenciesRec(critic);
			for(Iterator it = dependencies.iterator(); it.hasNext(); ){
				Identifier d = (Identifier)it.next();
				CriticStatusMessage dStatus = (CriticStatusMessage)criticStatuses.get(d);
				if(dStatus != null){
					if(dStatus.getStatus() == CriticStatuses.STAT_AVAILABLE_ACTIVE_BUSY){
						CriticStatusMessage newStatusMessage = new CriticStatusMessage(critic,
							initialStatusMessage.getDescription(), initialStatusMessage.getDependencies(),
							CriticStatuses.STAT_AVAILABLE_ACTIVE_WAITING, true);
						return newStatusMessage;
					}
				}
			}
			return getApprovedStatusMessage(initialStatusMessage);
		}
		else{
			//Otherwise, that stat takes precedence; return it.
			return getApprovedStatusMessage(initialStatusMessage);
		}
	}
	
	protected static CriticStatusMessage getApprovedStatusMessage(CriticStatusMessage csm){
		if(csm.getIsApproved()) return csm;
		return new CriticStatusMessage(csm.getCriticID(),
			csm.getDescription(), csm.getDependencies(), csm.getStatus(), true);
	}
	
	class CriticStatusMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof CriticStatusMessage){
				CriticStatusMessage csm = (CriticStatusMessage)m;
				int status = csm.getStatus();
				//System.out.println("Got critic status message: " + status);
				//If the critic has gone active or active-busy, then
				//that may affect the "waiting" status of other critics.
				if((status == CriticStatuses.STAT_AVAILABLE_ACTIVE) ||
					(status == CriticStatuses.STAT_AVAILABLE_ACTIVE_BUSY)){
					criticStatuses.put(csm.getCriticID(), csm);
					
					CriticStatusMessage actualCsm = csm;
					try{
						actualCsm = getActualStatus(csm.getCriticID());
					}
					catch(CriticMissingException cme){}
					
					//Check all the critics dependent on this one and report their status.
					Set reverseDependencies = getReverseCriticDependenciesRec(csm.getCriticID());
					for(Iterator it = reverseDependencies.iterator(); it.hasNext(); ){
						Identifier id = (Identifier)it.next();
						try{
							sendToAll(getActualStatus(id), bottomIface);
						}
						catch(CriticMissingException cme2){}
					}
					sendToAll(getApprovedStatusMessage(actualCsm), bottomIface);
				}
				else if(status == CriticStatuses.STAT_AVAILABLE_INACTIVE){
					criticStatuses.put(csm.getCriticID(), csm);
					sendToAll(getApprovedStatusMessage(csm), bottomIface);
				}
				else if(status == CriticStatuses.STAT_UNAVAILABLE){
					criticStatuses.remove(csm.getCriticID());
					sendToAll(getApprovedStatusMessage(csm), bottomIface);
				}
				//If the critic has gone inactive or unavailable,
				//Turn off all the critics that depend on that critic.

				if((status == CriticStatuses.STAT_AVAILABLE_INACTIVE) ||
				(status == CriticStatuses.STAT_UNAVAILABLE)){

					Set s = getReverseCriticDependenciesRec(csm.getCriticID());
					s.remove(csm.getCriticID());

					Identifier[] idsToMunge = new Identifier[s.size()];
					int i = 0;
					for(Iterator it = s.iterator(); it.hasNext(); ){
						idsToMunge[i++] = (Identifier)it.next();
					}
					CriticSetStatusMessage cssm2 = new CriticSetStatusMessage(idsToMunge, CriticStatuses.STAT_AVAILABLE_INACTIVE, true);
					sendToAll(cssm2, topIface);
				}
				
			}
			else if(m instanceof CriticGetStatusMessage){
				CriticGetStatusMessage cgsm = (CriticGetStatusMessage)m;
				Identifier[] ids = cgsm.getCriticIDs();
				if(ids == null){
					for(Iterator it = criticStatuses.values().iterator(); it.hasNext(); ){
						CriticStatusMessage csm = (CriticStatusMessage)it.next();
						try{
							csm = getActualStatus(csm.getCriticID());
							sendToAll(csm, bottomIface);
						}
						catch(CriticMissingException cme){
							sendToAll(getApprovedStatusMessage(csm), bottomIface);
						}
					}
				}
				else{
					for(int i = 0; i < ids.length; i++){
						//CriticStatusMessage csm = (CriticStatusMessage)criticStatuses.get(ids[i]);
						CriticStatusMessage csm = null;
						try{
							csm = getActualStatus(csm.getCriticID());
						}
						catch(CriticMissingException cme){}
						if(csm == null){
							csm = new CriticStatusMessage(ids[i], "No Description Available", new Identifier[]{}, CriticStatuses.STAT_UNAVAILABLE, true);
						}
						sendToAll(getApprovedStatusMessage(csm), bottomIface);
					}
				}
			}
			else if(m instanceof CriticSetStatusMessage){
				CriticSetStatusMessage cssm = (CriticSetStatusMessage)m;
				Identifier[] criticIDs = cssm.getCriticIDs();
				if(criticIDs == null){
					return;
				}
				HashSet criticsToMunge = new HashSet();
				int status = cssm.getNewStatus();
				
				if((status == CriticStatuses.STAT_AVAILABLE_ACTIVE) ||
					(status == CriticStatuses.STAT_AVAILABLE_ACTIVE_BUSY)){
					//We have to make sure to turn on all the critics that these critics depend on,
					//recursively
					for(int i = 0; i < criticIDs.length; i++){
						Set s = null;
						try{
							s = getCriticDependenciesRec(criticIDs[i]);
						}
						catch(CriticMissingException cme){
							//Can't turn on this critic at all--dependencies not met!
							CriticDependencyMissingMessage cdmm = new CriticDependencyMissingMessage(
								cme.getCriticID(), cme.getMissingDependencyID());
							sendToAll(cdmm, bottomIface);
							return;
						}
						for(Iterator it = s.iterator(); it.hasNext(); ){
							criticsToMunge.add(it.next());
						}
					}
					Identifier[] idsToMunge = new Identifier[criticsToMunge.size()];
					int i = 0;
					for(Iterator it = criticsToMunge.iterator(); it.hasNext(); ){
						idsToMunge[i++] = (Identifier)it.next();
					}
					CriticSetStatusMessage cssm2 = new CriticSetStatusMessage(idsToMunge, CriticStatuses.STAT_AVAILABLE_ACTIVE, true);
					//System.out.println("cssm2 ids = " + c2.util.ArrayUtils.arrayToString(idsToMunge));
					//System.out.println("Sending cssm2=" + cssm2);
					sendToAll(cssm2, topIface);
				}
				else if(status == CriticStatuses.STAT_AVAILABLE_INACTIVE){
					//We have to make sure to turn off all the critics that depend on this
					//critic
					for(int i = 0; i < criticIDs.length; i++){
						Set s = getReverseCriticDependenciesRec(criticIDs[i]);
						for(Iterator it = s.iterator(); it.hasNext(); ){
							criticsToMunge.add(it.next());
						}
					}
					Identifier[] idsToMunge = new Identifier[criticsToMunge.size()];
					int i = 0;
					for(Iterator it = criticsToMunge.iterator(); it.hasNext(); ){
						idsToMunge[i++] = (Identifier)it.next();
					}
					CriticSetStatusMessage cssm2 = new CriticSetStatusMessage(idsToMunge, CriticStatuses.STAT_AVAILABLE_INACTIVE, true);
					sendToAll(cssm2, topIface);
				}
			}
		}
	}
}
