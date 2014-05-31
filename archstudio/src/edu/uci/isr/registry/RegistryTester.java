package edu.uci.isr.registry;

public class RegistryTester{

	public static void main(String[] args){
		RegistryNode reg = RegistryUtils.loadOrCreateRegistry();
		RegistryNode software = new RegistryNode("Software");
		reg.putChild(software);
		RegistryEntry re = new RegistryEntry("ArchStudio");
		re.putValue("yes it sho is");
		software.putEntry(re);
		
		RegistryUtils.saveRegistry();
	}

}

