package archstudio.critics;

public interface CriticStatuses{

	/** Means that the critic is welded in and active; that is, it's actively
	    analyzing architectures and looking for problems. */
	public static final int STAT_AVAILABLE_ACTIVE = 100;

	/** Means that the critic is welded in and active; that is, it's actively
	    analyzing architectures and looking for problems, and it's currently
	    busy looking for problems right now and will report findings shortly. */
	public static final int STAT_AVAILABLE_ACTIVE_BUSY = 125;
	
	/** Means that the critic is welded in and active; that is, it's actively
	    analyzing architectures and looking for problems, and it's currently
	    waiting for some other critic to become unbusy before it reports
	    its own status.  NOTE!!! This is managed internally by the
	    CriticManager and should not be emitted by any critic by itself! */
	public static final int STAT_AVAILABLE_ACTIVE_WAITING = 130;
	
	/** Means that the critic is welded in but inactive; that is, it's not
	    analyzing architectures looking for problems right now (but can
	    be activated if necessary. */
	public static final int STAT_AVAILABLE_INACTIVE = 150;

	/** Means that the critic is being unwelded and no longer available. */
	public static final int STAT_UNAVAILABLE = 200;
	
	

}
