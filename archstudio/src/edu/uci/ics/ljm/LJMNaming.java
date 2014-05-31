package edu.uci.ics.ljm;

public interface LJMNaming{
	public static final int DEFAULT_PORT = LJMNameServer.DEFAULT_PORT;

	public LJMEndpoint lookup(String objectName) throws LJMException;
	public void bind(String name, LJMEndpoint endpoint);
	public void unbind(String name);
}

