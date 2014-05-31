package archstudio;

import edu.uci.isr.xarch.*;
import edu.uci.isr.xarch.instance.*;
import edu.uci.isr.xarch.types.*;
import edu.uci.isr.xarch.implementation.*;
import edu.uci.isr.xarch.variants.*;
import edu.uci.isr.xarch.javaimplementation.*;

public class DistClientDescription{

	private static IInstanceContext instance;
	private static ITypesContext types;
	private static IImplementationContext implementation;
	private static IVariantsContext variants;
	private static IJavaimplementationContext javaimplementation;
	
	public static void main(String[] args){
		System.out.println(getXml());
	}
	
	public static String getXml(){
		IXArchImplementation xArchImplementation = XArchUtils.getDefaultXArchImplementation();
		IXArch xArch = xArchImplementation.createXArch();
		instance = (IInstanceContext)xArchImplementation.createContext(xArch, "instance");
		types = (ITypesContext)xArchImplementation.createContext(xArch, "types");
		implementation = (IImplementationContext)xArchImplementation.createContext(xArch, "implementation");
		variants = (IVariantsContext)xArchImplementation.createContext(xArch, "variants");
		javaimplementation = (IJavaimplementationContext)xArchImplementation.createContext(xArch, "javaimplementation");
		
		IArchStructure archStructure = types.createArchStructureElement();
		archStructure.setId("ArchStudio_3_DistClient");
		archStructure.setDescription(createDescription("ArchStudio 3.0 Distributed Client Architecture"));
		
		IArchTypes archTypes = types.createArchTypesElement();
		
		xArch.addObject(archStructure);
		xArch.addObject(archTypes);
		
		IInterfaceType c2TopType = types.createInterfaceType();
		IInterfaceTypeImpl c2TopTypeImpl = implementation.promoteToInterfaceTypeImpl(c2TopType); 
		c2TopTypeImpl.setId("C2TopType");
		c2TopTypeImpl.setDescription(createDescription("C2 Top Interface"));
		IJavaImplementation javaImplementation = javaimplementation.createJavaImplementation();
		IJavaClassFile javaClassFile = javaimplementation.createJavaClassFile();
		IJavaClassName jcn = javaimplementation.createJavaClassName();
		jcn.setValue("c2.fw.SimpleInterface");
		javaClassFile.setJavaClassName(jcn);
		javaImplementation.setMainClass(javaClassFile);
		c2TopTypeImpl.addImplementation(javaImplementation);
		archTypes.addInterfaceType(c2TopTypeImpl);
		
		IInterfaceType c2BottomType = types.createInterfaceType();
		IInterfaceTypeImpl c2BottomTypeImpl = implementation.promoteToInterfaceTypeImpl(c2BottomType); 
		c2BottomTypeImpl.setId("C2BottomType");
		c2BottomTypeImpl.setDescription(createDescription("C2 Bottom Interface"));
		javaImplementation = javaimplementation.createJavaImplementation();
		javaClassFile = javaimplementation.createJavaClassFile();
		jcn = javaimplementation.createJavaClassName();
		jcn.setValue("c2.fw.SimpleInterface");
		javaClassFile.setJavaClassName(jcn);
		javaImplementation.setMainClass(javaClassFile);
		c2BottomTypeImpl.addImplementation(javaImplementation);
		archTypes.addInterfaceType(c2BottomTypeImpl);
		
		IInterfaceType localType = types.createInterfaceType();
		IInterfaceTypeImpl localTypeImpl = implementation.promoteToInterfaceTypeImpl(localType); 
		c2BottomTypeImpl.setId("LocalType");
		c2BottomTypeImpl.setDescription(createDescription("Local Interface Type"));
		javaImplementation = javaimplementation.createJavaImplementation();
		javaClassFile = javaimplementation.createJavaClassFile();
		jcn = javaimplementation.createJavaClassName();
		jcn.setValue("c2.fw.SimpleInterface");
		javaClassFile.setJavaClassName(jcn);
		javaImplementation.setMainClass(javaClassFile);
		localTypeImpl.addImplementation(javaImplementation);
		archTypes.addInterfaceType(localTypeImpl);
		
		//archTypes.addComponentType(
		//	createComponentType("xArchADT_type", "xArchADT Type", "archstudio.comp.xarchadt.XArchADTC2Component")
		//);
		archTypes.addComponentType(
			createComponentType("DistHostMonitor_type", "Distributed Host Monitor Type", "archstudio.comp.disthostmon.DistributedHostMonitorC2Component")
		);
		archTypes.addConnectorType(
			//createConnectorType("BusConnector_type", "Bus Connector Type", "c2.legacy.conn.BusConnector")
			createConnectorType("BusConnector_type", "Bus Connector Type", "c2.legacy.conn.BusConnector")
		);
		archTypes.addConnectorType(
			//createConnectorType("BusConnector_type", "Bus Connector Type", "c2.legacy.conn.BusConnector")
			createFredConnectorType("FredConnector_type", "Fred Connector Type", "c2.conn.fred.FredConnector")
		);
		
		//archStructure.addComponent(
		//	createComponent("xArchADT", "xArchADT", "xArchADT_type")
		//);
		archStructure.addComponent(
			createComponent("DistHostMonitor", "Distributed Host Monitor", "DistHostMonitor_type")
		);
		archStructure.addConnector(
			createConnector("DistClientBus", "Distributed Client Bus", "BusConnector_type")
		);
		archStructure.addConnector(
			createFredConnector("DistClientFredConnector", "Distributed Client Fred Connector")
		);

		archStructure.addLink(createLink("DistClientBus", "DistHostMonitor"));
		
		archStructure.addLink(createLink("DistClientBus", "IFACE_TOP", "DistClientFredConnector", "IFACE_LOCAL"));
		
		try{
			return xArchImplementation.serialize(xArch, null);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	private static IDescription createDescription(String value){
		IDescription desc = types.createDescription();
		desc.setValue(value);
		return desc;
	}

	private static ILink createLink(String topId, String bottomId){
		ILink link = types.createLink();
		link.setId(topId + "_to_" + bottomId);
		link.setDescription(createDescription(topId + " to " + bottomId + " Link"));
		
		IPoint point1 = types.createPoint();
		IXMLLink anchor1 = instance.createXMLLink();
		anchor1.setType("simple");
		anchor1.setHref("#" + topId + ".IFACE_BOTTOM");
		point1.setAnchorOnInterface(anchor1);
		
		IPoint point2 = types.createPoint();
		IXMLLink anchor2 = instance.createXMLLink();
		anchor2.setType("simple");
		anchor2.setHref("#" + bottomId + ".IFACE_TOP");
		point2.setAnchorOnInterface(anchor2);
		
		link.addPoint(point1);
		link.addPoint(point2);
		return link;
	}
	
	private static ILink createLink(String topId, String topIfaceId, String bottomId, String bottomIfaceId){
		ILink link = types.createLink();
		link.setId(topId + "_to_" + bottomId);
		link.setDescription(createDescription(topId + " to " + bottomId + " Link"));
		
		IPoint point1 = types.createPoint();
		IXMLLink anchor1 = instance.createXMLLink();
		anchor1.setType("simple");
		anchor1.setHref("#" + topId + "." + topIfaceId);
		point1.setAnchorOnInterface(anchor1);
		
		IPoint point2 = types.createPoint();
		IXMLLink anchor2 = instance.createXMLLink();
		anchor2.setType("simple");
		anchor2.setHref("#" + bottomId + "." + bottomIfaceId);
		point2.setAnchorOnInterface(anchor2);
		
		link.addPoint(point1);
		link.addPoint(point2);
		return link;
	}
	
	private static IComponent createComponent(String id, String description, String typeId){
		IComponent c = types.createComponent();
		
		c.setId(id);
		IDescription desc = types.createDescription();
		desc.setValue(description);
		c.setDescription(desc);
		
		IInterface topIface = types.createInterface();
		topIface.setId(id + ".IFACE_TOP");
		topIface.setDescription(createDescription(description + " Top Interface"));
		IDirection topDirection = types.createDirection();
		topDirection.setValue("inout");
		topIface.setDirection(topDirection);
	
		IXMLLink topIfaceType = types.createXMLLink();
		topIfaceType.setType("simple");
		topIfaceType.setHref("#C2TopType");
		
		topIface.setType(topIfaceType);
		
		IInterface bottomIface = types.createInterface();
		bottomIface.setId(id + ".IFACE_BOTTOM");
		bottomIface.setDescription(createDescription(description + " Bottom Interface"));
		IDirection bottomDirection = types.createDirection();
		bottomDirection.setValue("inout");
		bottomIface.setDirection(bottomDirection);
	
		IXMLLink bottomIfaceType = types.createXMLLink();
		bottomIfaceType.setType("simple");
		bottomIfaceType.setHref("#C2BottomType");
		
		bottomIface.setType(bottomIfaceType);
		
		c.addInterface(topIface);
		c.addInterface(bottomIface);

		IXMLLink cType = types.createXMLLink();
		cType .setType("simple");
		cType .setHref("#" + typeId);
		c.setType(cType);
		return c;
	}
	
	private static IConnector createFredConnector(String id, String description){
		IConnector c = types.createConnector();
		
		c.setId(id);
		IDescription desc = types.createDescription();
		desc.setValue(description);
		c.setDescription(desc);
		
		IInterface localIface = types.createInterface();
		localIface.setId(id + ".IFACE_LOCAL");
		localIface.setDescription(createDescription(description + " Local Interface"));
		IDirection localDirection = types.createDirection();
		localDirection.setValue("inout");
		localIface.setDirection(localDirection);
	
		IXMLLink localIfaceType = types.createXMLLink();
		localIfaceType.setType("simple");
		localIfaceType.setHref("#LocalType");
		
		localIface.setType(localIfaceType);
		
		c.addInterface(localIface);

		IXMLLink cType = types.createXMLLink();
		cType.setType("simple");
		cType.setHref("#FredConnector_type");
		c.setType(cType);
		return c;
	}
	
	private static IConnector createConnector(String id, String description, String typeId){
		IConnector c = types.createConnector();
		
		c.setId(id);
		IDescription desc = types.createDescription();
		desc.setValue(description);
		c.setDescription(desc);
		
		IInterface topIface = types.createInterface();
		topIface.setId(id + ".IFACE_TOP");
		topIface.setDescription(createDescription(description + " Top Interface"));
		IDirection topDirection = types.createDirection();
		topDirection.setValue("inout");
		topIface.setDirection(topDirection);
	
		IXMLLink topIfaceType = types.createXMLLink();
		topIfaceType.setType("simple");
		topIfaceType.setHref("#C2TopType");
		
		topIface.setType(topIfaceType);
		
		IInterface bottomIface = types.createInterface();
		bottomIface.setId(id + ".IFACE_BOTTOM");
		bottomIface.setDescription(createDescription(description + " Bottom Interface"));
		IDirection bottomDirection = types.createDirection();
		bottomDirection.setValue("inout");
		bottomIface.setDirection(bottomDirection);
	
		IXMLLink bottomIfaceType = types.createXMLLink();
		bottomIfaceType.setType("simple");
		bottomIfaceType.setHref("#C2BottomType");
		
		bottomIface.setType(bottomIfaceType);
		
		c.addInterface(topIface);
		c.addInterface(bottomIface);

		IXMLLink cType = types.createXMLLink();
		cType.setType("simple");
		cType.setHref("#" + typeId);
		c.setType(cType);
		return c;
	}
	
	private static IComponentType createComponentType(String id, String description, String javaClassName){
		IComponentType ct0 = types.createComponentType();
		IVariantComponentType ct1 = variants.promoteToVariantComponentType(ct0);
		IVariantComponentTypeImpl ct = implementation.promoteToVariantComponentTypeImpl(ct1);
		
		ct.setId(id);
		IDescription desc = types.createDescription();
		desc.setValue(description);
		ct.setDescription(desc);
		
		ISignature topSig = types.createSignature();
		IDirection topDirection = types.createDirection();
		topDirection.setValue("inout");
		topSig.setId(id + "_topSig");
		topSig.setDirection(topDirection);
				
		IXMLLink topSigType = types.createXMLLink();
		topSigType.setType("simple");
		topSigType.setHref("#C2TopType");
		
		topSig.setType(topSigType);
		
		ISignature botSig = types.createSignature();
		IDirection botDirection = types.createDirection();
		botDirection.setValue("inout");
		botSig.setId(id + "_bottomSig");
		botSig.setDirection(botDirection);
		
		IXMLLink botSigType = types.createXMLLink();
		botSigType.setType("simple");
		botSigType.setHref("#C2BottomType");
		botSig.setType(botSigType);
		
		ct.addSignature(topSig);
		ct.addSignature(botSig);
		
		IJavaImplementation javaImplementation = javaimplementation.createJavaImplementation();
		IJavaClassFile javaClassFile = javaimplementation.createJavaClassFile();
		IJavaClassName jcn = javaimplementation.createJavaClassName();
		jcn.setValue(javaClassName);
		javaClassFile.setJavaClassName(jcn);
		javaImplementation.setMainClass(javaClassFile);
		
		ct.addImplementation(javaImplementation);
		
		return ct;
	}

	private static IConnectorType createConnectorType(String id, String description, String javaClassName){
		IConnectorType ct0 = types.createConnectorType();
		IVariantConnectorType ct1 = variants.promoteToVariantConnectorType(ct0);
		IVariantConnectorTypeImpl ct = implementation.promoteToVariantConnectorTypeImpl(ct1);
		
		ct.setId(id);
		IDescription desc = types.createDescription();
		desc.setValue(description);
		ct.setDescription(desc);
		
		ISignature topSig = types.createSignature();
		IDirection topDirection = types.createDirection();
		topDirection.setValue("inout");
		topSig.setId(id + "_topSig");
		topSig.setDirection(topDirection);
		
		IXMLLink topSigType = types.createXMLLink();
		topSigType.setType("simple");
		topSigType.setHref("#C2TopType");
		
		topSig.setType(topSigType);
		
		ISignature botSig = types.createSignature();
		IDirection botDirection = types.createDirection();
		botDirection.setValue("inout");
		botSig.setId(id + "_bottomSig");
		botSig.setDirection(botDirection);
		
		IXMLLink botSigType = types.createXMLLink();
		botSigType.setType("simple");
		botSigType.setHref("#C2BottomType");
		botSig.setType(botSigType);
		
		ct.addSignature(topSig);
		ct.addSignature(botSig);
		
		IJavaImplementation javaImplementation = javaimplementation.createJavaImplementation();
		IJavaClassFile javaClassFile = javaimplementation.createJavaClassFile();
		IJavaClassName jcn = javaimplementation.createJavaClassName();
		jcn.setValue(javaClassName);
		javaClassFile.setJavaClassName(jcn);
		javaImplementation.setMainClass(javaClassFile);
		
		ct.addImplementation(javaImplementation);
		
		return ct;
	}

	private static IConnectorType createFredConnectorType(String id, String description, String javaClassName){
		IConnectorType ct0 = types.createConnectorType();
		IVariantConnectorType ct1 = variants.promoteToVariantConnectorType(ct0);
		IVariantConnectorTypeImpl ct = implementation.promoteToVariantConnectorTypeImpl(ct1);
		
		ct.setId(id);
		IDescription desc = types.createDescription();
		desc.setValue(description);
		ct.setDescription(desc);
		
		ISignature localSig = types.createSignature();
		IDirection localDirection = types.createDirection();
		localDirection.setValue("inout");
		localSig.setId(id + "_localSig");
		localSig.setDirection(localDirection);
		
		IXMLLink localSigType = types.createXMLLink();
		localSigType.setType("simple");
		localSigType.setHref("#LocalType");
		
		localSig.setType(localSigType);
		
		ct.addSignature(localSig);
		
		IJavaImplementation javaImplementation = javaimplementation.createJavaImplementation();
		IJavaClassFile javaClassFile = javaimplementation.createJavaClassFile();
		IJavaClassName jcn = javaimplementation.createJavaClassName();
		jcn.setValue(javaClassName);
		javaClassFile.setJavaClassName(jcn);
		javaImplementation.setMainClass(javaClassFile);
		
		ct.addImplementation(javaImplementation);
		
		return ct;
	}
	
}