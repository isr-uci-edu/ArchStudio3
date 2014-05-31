package edu.uci.ics.bna;

//Interface for Things that slowly fade out after creation; usually
//by painting filled areas in Alpha Blending mode in increasingly
//transparent steps, as a quick indicator to the user.

public interface IEphemeral extends Thing{

	public static final String EPHEMERAL_TRANSPARENCY_PROPERTY_NAME = "ephemeralTransparency";
	
	public void setEphemeralTransparency(float transparency);
	public float getEphemeralTransparency();
}
