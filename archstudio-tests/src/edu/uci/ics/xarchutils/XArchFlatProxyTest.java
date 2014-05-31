package edu.uci.ics.xarchutils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import junit.framework.Assert;
import junit.framework.TestCase;
import edu.uci.isr.xarch.IXArch;
import edu.uci.isr.xarch.IXArchImplementation;
import edu.uci.isr.xarch.XArchTypeMetadata;
import edu.uci.isr.xarch.XArchUtils;
import edu.uci.isr.xarch.boolguard.IBooleanGuard;
import edu.uci.isr.xarch.boolguard.IBoolguardContext;
import edu.uci.isr.xarch.implementation.IImplementationContext;
import edu.uci.isr.xarch.implementation.IVariantComponentTypeImpl;
import edu.uci.isr.xarch.instance.IDescription;
import edu.uci.isr.xarch.instance.IInstanceContext;
import edu.uci.isr.xarch.javaimplementation.IJavaimplementationContext;
import edu.uci.isr.xarch.menage.IMenageContext;
import edu.uci.isr.xarch.menage.IOptionalComponentPosition;
import edu.uci.isr.xarch.options.IGuard;
import edu.uci.isr.xarch.options.IOptional;
import edu.uci.isr.xarch.options.IOptionalComponent;
import edu.uci.isr.xarch.options.IOptionsContext;
import edu.uci.isr.xarch.types.IArchStructure;
import edu.uci.isr.xarch.types.IArchTypes;
import edu.uci.isr.xarch.types.IComponent;
import edu.uci.isr.xarch.types.IComponentType;
import edu.uci.isr.xarch.types.ITypesContext;
import edu.uci.isr.xarch.variants.IVariantComponentType;
import edu.uci.isr.xarch.variants.IVariantsContext;

public class XArchFlatProxyTest extends TestCase {

	IXArchImplementation xArchImplementation;
	IXArch xArch;
	
	IInstanceContext instance;
	ITypesContext types;
	IImplementationContext implementation;
	IVariantsContext variants;
	IJavaimplementationContext javaimplementation;
	
	IArchStructure archStructure;
	IArchTypes archTypes;
	
	protected void setUp() {
		
		boolean testProxy = true;
		
		if(testProxy)
			xArchImplementation =
				XArchFlatProxyUtils.getXArchImplementation(new XArchFlatImpl());
		else
			xArchImplementation =
				XArchUtils.getDefaultXArchImplementation();
		
		xArch = xArchImplementation.createXArch();
		
		instance = (IInstanceContext) xArchImplementation.createContext(xArch, "instance");
		types = (ITypesContext) xArchImplementation.createContext(xArch, "types");
		implementation = (IImplementationContext) xArchImplementation.createContext(xArch, "implementation");
		variants = (IVariantsContext) xArchImplementation.createContext(xArch, "variants");
		javaimplementation = (IJavaimplementationContext) xArchImplementation.createContext(xArch, "javaimplementation");
		
		archStructure = types.createArchStructureElement();
		archTypes = types.createArchTypesElement();
		
		xArch.addObject(archStructure);
		xArch.addObject(archTypes);
	}
	
	public void tearDown() {
		xArchImplementation = null;
		xArch = null;

		instance = null;
		types = null;
		implementation = null;
		variants = null;
		javaimplementation = null;
		
		archStructure = null;
		archTypes = null;
	}

	public void testNulls(){
		IComponent c1 = (IComponent)XArchFlatProxyUtils.proxy(XArchFlatProxyUtils.getXArch(xArch), (ObjRef)null);
		ObjRef c1Ref = XArchFlatProxyUtils.getObjRef(c1);
		Assert.assertNull(c1);
		Assert.assertNull(c1Ref);
	}
	
	public void testCreate(){
		IComponent c1 = types.createComponent();
		c1.setId("c1");
		
		if(xArch instanceof XArchFlatProxyInterface){
			Assert.assertEquals(c1, XArchFlatProxyUtils.getByID(xArch, "c1"));
		}
	}
	
	public void testAddHasRemoveClear(){
		xArch.clearObjects();
		Assert.assertTrue(equals(xArch.getAllObjects(), Arrays.asList(new Object[]{})));
		
		xArch.addObjects(Arrays.asList(new Object[]{archStructure, archTypes}));
		Assert.assertTrue(xArch.hasObject(archStructure));
		Assert.assertTrue(xArch.hasObject(archTypes));
		Assert.assertTrue(xArch.hasAllObjects(Arrays.asList(new Object[]{archStructure, archTypes})));
		Assert.assertTrue(xArch.hasAllObjects(Arrays.asList(new Object[]{archStructure})));
		Assert.assertTrue(xArch.hasAllObjects(Arrays.asList(new Object[]{archTypes})));
		Assert.assertTrue(equals(xArch.getAllObjects(), Arrays.asList(new Object[]{archStructure, archTypes})));
		
		xArch.removeObjects(Arrays.asList(new Object[]{archStructure, archTypes}));
		Assert.assertTrue(!xArch.hasObject(archStructure));
		Assert.assertTrue(!xArch.hasObject(archTypes));
		Assert.assertTrue(!xArch.hasAllObjects(Arrays.asList(new Object[]{archStructure, archTypes})));
		Assert.assertTrue(!xArch.hasAllObjects(Arrays.asList(new Object[]{archStructure})));
		Assert.assertTrue(!xArch.hasAllObjects(Arrays.asList(new Object[]{archTypes})));
		Assert.assertTrue(equals(xArch.getAllObjects(), Arrays.asList(new Object[]{})));
		
		xArch.addObjects(Arrays.asList(new Object[]{archStructure, archTypes}));
		Assert.assertTrue(equals(xArch.getAllObjects(), Arrays.asList(new Object[]{archStructure, archTypes})));
		xArch.clearObjects();
		Assert.assertTrue(equals(xArch.getAllObjects(), Arrays.asList(new Object[]{})));
		
		xArch.addObject(archStructure);
		Assert.assertTrue(xArch.hasObject(archStructure));
		Assert.assertTrue(!xArch.hasObject(archTypes));
		Assert.assertTrue(!xArch.hasAllObjects(Arrays.asList(new Object[]{archStructure, archTypes})));
		Assert.assertTrue(xArch.hasAllObjects(Arrays.asList(new Object[]{archStructure})));
		Assert.assertTrue(!xArch.hasAllObjects(Arrays.asList(new Object[]{archTypes})));
		Assert.assertTrue(equals(xArch.getAllObjects(), Arrays.asList(new Object[]{archStructure})));
		
		xArch.addObject(archTypes);
		Assert.assertTrue(xArch.hasObject(archStructure));
		Assert.assertTrue(xArch.hasObject(archTypes));
		Assert.assertTrue(xArch.hasAllObjects(Arrays.asList(new Object[]{archStructure, archTypes})));
		Assert.assertTrue(xArch.hasAllObjects(Arrays.asList(new Object[]{archStructure})));
		Assert.assertTrue(xArch.hasAllObjects(Arrays.asList(new Object[]{archTypes})));
		Assert.assertTrue(equals(xArch.getAllObjects(), Arrays.asList(new Object[]{archStructure, archTypes})));
		
		xArch.removeObject(archStructure);
		Assert.assertTrue(!xArch.hasObject(archStructure));
		Assert.assertTrue(xArch.hasObject(archTypes));
		Assert.assertTrue(!xArch.hasAllObjects(Arrays.asList(new Object[]{archStructure, archTypes})));
		Assert.assertTrue(!xArch.hasAllObjects(Arrays.asList(new Object[]{archStructure})));
		Assert.assertTrue(xArch.hasAllObjects(Arrays.asList(new Object[]{archTypes})));
		Assert.assertTrue(equals(xArch.getAllObjects(), Arrays.asList(new Object[]{archTypes})));
		
		xArch.removeObject(archTypes);
		Assert.assertTrue(!xArch.hasObject(archStructure));
		Assert.assertTrue(!xArch.hasObject(archTypes));
		Assert.assertTrue(!xArch.hasAllObjects(Arrays.asList(new Object[]{archStructure, archTypes})));
		Assert.assertTrue(!xArch.hasAllObjects(Arrays.asList(new Object[]{archStructure})));
		Assert.assertTrue(!xArch.hasAllObjects(Arrays.asList(new Object[]{archTypes})));
		Assert.assertTrue(equals(xArch.getAllObjects(), Arrays.asList(new Object[]{})));
	}
	
	public void testObjRefEquality(){
		if(xArch instanceof XArchFlatProxyInterface){
			XArchFlatInterface xArchFlat = XArchFlatProxyUtils.getXArch(types);
			ObjRef typesRef = XArchFlatProxyUtils.getObjRef(types);
			
			ObjRef cRef = xArchFlat.create(typesRef, "Component");
			ObjRef dRef = xArchFlat.create(typesRef, "Description");
			xArchFlat.set(dRef, "Value", "desc");
			xArchFlat.set(cRef, "Description", dRef);
			ObjRef dRef2 = (ObjRef)xArchFlat.get(cRef, "Description");
			Assert.assertEquals(dRef, dRef2);
		}
		
		IComponent c = types.createComponent();
		IDescription d = types.createDescription();
		d.setValue("Description");
		c.setDescription(d);
		IDescription d2 = c.getDescription();
		Assert.assertEquals(d, d2);		
	}
	
	public void testGetAll(){
		IComponent c1 = types.createComponent();
		c1.setId("c1");
		archStructure.addComponent(c1);
		Assert.assertTrue(equals(archStructure.getAllComponents(), Arrays.asList(new Object[]{c1})));
		
		IComponent c2 = types.createComponent();
		c2.setId("c2");
		archStructure.addComponent(c2);
		Assert.assertTrue(equals(archStructure.getAllComponents(), Arrays.asList(new Object[]{c1, c2})));
		
		IComponent c3 = types.createComponent();
		c3.setId("c3");
		archStructure.addComponent(c3);
		Assert.assertTrue(equals(archStructure.getAllComponents(), Arrays.asList(new Object[]{c1, c2, c3})));
	}
	
	public void testHasHasAll(){
		
		boolean t = true;
		boolean f = false;
		
		IComponent c1 = types.createComponent();
		c1.setId("c1");
		IDescription d1 = types.createDescription();
		d1.setValue("c1d");
		c1.setDescription(d1);
		
		IComponent c2 = types.createComponent();
		c2.setId("c2");
		IDescription d2 = types.createDescription();
		d2.setValue("c2d");
		c2.setDescription(d2);
		
		IComponent c3 = types.createComponent();
		c3.setId("c3");
		IDescription d3 = types.createDescription();
		d3.setValue("c3d");
		c3.setDescription(d3);
		
		Assert.assertTrue(equals(archStructure.hasComponents(Arrays.asList(new Object[]{c1, c2, c3})), new boolean[]{f, f, f}));
		Assert.assertTrue(!archStructure.hasAllComponents(Arrays.asList(new Object[]{c1, c2, c3})));
		
		archStructure.addComponent(c1);
		Assert.assertTrue(equals(archStructure.hasComponents(Arrays.asList(new Object[]{c1, c2, c3})), new boolean[]{t, f, f}));
		Assert.assertTrue(equals(archStructure.hasComponents(Arrays.asList(new Object[]{c2, c3, c1})), new boolean[]{f, f, t}));
		Assert.assertTrue(equals(archStructure.hasComponents(Arrays.asList(new Object[]{c3, c1, c2})), new boolean[]{f, t, f}));
		Assert.assertTrue(!archStructure.hasAllComponents(Arrays.asList(new Object[]{c1, c2, c3})));
		
		archStructure.addComponent(c2);
		Assert.assertTrue(equals(archStructure.hasComponents(Arrays.asList(new Object[]{c1, c2, c3})), new boolean[]{t, t, f}));
		Assert.assertTrue(!archStructure.hasAllComponents(Arrays.asList(new Object[]{c1, c2, c3})));
		
		archStructure.addComponent(c3);
		Assert.assertTrue(equals(archStructure.hasComponents(Arrays.asList(new Object[]{c1, c2, c3})), new boolean[]{t, t, t}));
		Assert.assertTrue(archStructure.hasAllComponents(Arrays.asList(new Object[]{c1, c2, c3})));
	}
	
	public void testAddAll(){
		IComponent c1 = types.createComponent();
		c1.setId("c1");
		archStructure.clearComponents();
		Assert.assertTrue(equals(archStructure.getAllComponents(), Arrays.asList(new Object[]{})));
		archStructure.addComponents(Arrays.asList(new Object[]{c1}));
		Assert.assertTrue(equals(archStructure.getAllComponents(), Arrays.asList(new Object[]{c1})));
		
		IComponent c2 = types.createComponent();
		c2.setId("c2");
		archStructure.clearComponents();
		Assert.assertTrue(equals(archStructure.getAllComponents(), Arrays.asList(new Object[]{})));
		archStructure.addComponents(Arrays.asList(new Object[]{c1, c2}));
		Assert.assertTrue(equals(archStructure.getAllComponents(), Arrays.asList(new Object[]{c1, c2})));
		
		IComponent c3 = types.createComponent();
		c3.setId("c3");
		archStructure.clearComponents();
		Assert.assertTrue(equals(archStructure.getAllComponents(), Arrays.asList(new Object[]{})));
		archStructure.addComponents(Arrays.asList(new Object[]{c1, c2, c3}));
		Assert.assertTrue(equals(archStructure.getAllComponents(), Arrays.asList(new Object[]{c1, c2, c3})));
	}
	
	public void testPromoteTo(){
		IComponentType ct1 = types.createComponentType();
		IVariantComponentType vct1 = variants.promoteToVariantComponentType(ct1);
		IVariantComponentTypeImpl vcti1 = implementation.promoteToVariantComponentTypeImpl(vct1);
		
		Assert.assertEquals(ct1, vct1);
		Assert.assertEquals(ct1, vcti1);
	}
	
	public void testRecontextualize(){
		IDescription d = types.createDescription();
		IDescription d2 = instance.recontextualizeDescription(d);
		Assert.assertEquals(d, d2);
	}
	
	public void testEquivalentEquals(){
		IComponent c1 = types.createComponent();
		IDescription d1 = types.createDescription();
		d1.setValue("Test Component 1");
		c1.setId("c1");
		c1.setDescription(d1);
		
		IComponent c2 = types.createComponent();
		IDescription d2 = types.createDescription();
		d2.setValue("Test Component 2");
		c2.setId("c2");
		c2.setDescription(d2);
		
		Assert.assertTrue(!c1.isEquivalent(c2));
		Assert.assertTrue(!c1.isEqual(c2));
		d2.setValue("Test Component 1");
		Assert.assertTrue(c1.isEquivalent(c2));
		Assert.assertTrue(!c1.isEqual(c2));
		c2.setId("c1");
		Assert.assertTrue(c1.isEquivalent(c2));
		Assert.assertTrue(c1.isEqual(c2));
		d2.setValue("Test Component 2");
		Assert.assertTrue(!c1.isEquivalent(c2));
		Assert.assertTrue(c1.isEqual(c2));
	}
	
	public void testTypeMetadata(){
		IComponent c1 = types.createComponent();
		XArchTypeMetadata cmd = c1.getTypeMetadata();
		Assert.assertNotNull(cmd);
		Assert.assertEquals(cmd, IComponent.TYPE_METADATA);

		IDescription d1 = types.createDescription();
		XArchTypeMetadata dmd = d1.getTypeMetadata();
		Assert.assertNotNull(dmd);
		Assert.assertEquals(dmd, IDescription.TYPE_METADATA);
	}
	
	public void testInstanceMetadata(){
		IDescription d1 = types.createDescription();
		Assert.assertEquals("types", d1.getInstanceMetadata().getCurrentContext());
		d1 = instance.recontextualizeDescription(d1);
		Assert.assertEquals("instance", d1.getInstanceMetadata().getCurrentContext());
		IDescription d2 = instance.createDescription();
		Assert.assertEquals("instance", d2.getInstanceMetadata().getCurrentContext());
	}
	
	public void testGetParent(){
		IComponent c1 = types.createComponent();
		IDescription d1 = types.createDescription();
		c1.setDescription(d1);
		Assert.assertEquals(XArchFlatProxyUtils.getObjRef(c1), XArchFlatProxyUtils.getObjRef(XArchFlatProxyUtils.getParent(d1)));
	}
	
	public void testPromoteToOddity(){
		IOptionsContext options = (IOptionsContext)xArchImplementation.createContext(xArch, "options");
		IMenageContext menage = (IMenageContext)xArchImplementation.createContext(xArch, "menage");
		IBoolguardContext boolguard = (IBoolguardContext)xArchImplementation.createContext(xArch, "boolguard");

		IComponent c1 = types.createComponent();
		archStructure.addComponent(c1);
		IOptionalComponent oc1 = options.promoteToOptionalComponent(c1);
		IOptional op1 = options.createOptional();
		oc1.setOptional(op1);
		IGuard g1 = options.createGuard();
		op1.setGuard(g1);
		IBooleanGuard bg1 = boolguard.promoteToBooleanGuard(g1);
	}
	
	public void testPromoteToGuts(){
		final XArchFlatImpl xArch = new XArchFlatImpl();
		IXArchImplementation xArchImpl = XArchUtils.getDefaultXArchImplementation();
		ObjRef xArchRef = xArch.createXArch("");
		ObjRef typesContextRef = xArch.createContext(xArchRef, "types");
		ObjRef variantsContextRef = xArch.createContext(xArchRef, "variants");
		ObjRef ctRef = xArch.create(typesContextRef, "ComponentType");
		final Vector resultClass = new Vector();
		xArch.addXArchFlatListener(new XArchFlatListener() {
			public void handleXArchFlatEvent(XArchFlatEvent evt) {
				try{
					String className = xArch.getType(evt.getSource());
					Class c = Class.forName(className);
					synchronized(resultClass){
						resultClass.add(c);
						resultClass.notifyAll();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		synchronized(resultClass){
			ObjRef vctRef = xArch.promoteTo(variantsContextRef, "VariantComponentType", ctRef);
			while(resultClass.size() == 0){
				try{
					resultClass.wait();
				}catch(Exception e){
				}
			}
			Class c = (Class)resultClass.get(0);
			String className = c.getName();
			if (!IVariantComponentType.class.isAssignableFrom(c))
				throw new RuntimeException("Class is reported as: "+className);
		}
	}
	
	public void testCreateVSPromote(){
		IComponentType ct;
		IDescription d;
		
		ct = types.createComponentType();
		ct.setId("id1");
		d = types.createDescription();
		d.setValue("id1 description");
		ct.setDescription(d);
		archTypes.addComponentType(ct);
		
		ct = types.createComponentType();
		ct = variants.promoteToVariantComponentType(ct);
		ct.setId("id2");
		d = types.createDescription();
		d.setValue("id1 description");
		ct.setDescription(d);
		archTypes.addComponentType(ct);
		
		ct = variants.createVariantComponentType();
		ct.setId("id3");
		d = types.createDescription();
		d.setValue("id1 description");
		ct.setDescription(d);
		archTypes.addComponentType(ct);
		
		d = types.createDescription();
		d.setValue("types description");
		archStructure.setDescription(d);
		archStructure.setId("idarch");
		
		try{
			System.err.println(xArchImplementation.serialize(xArch, null));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	private static boolean equals(Collection c1, Collection c2){
		return c1.containsAll(c2) && c2.containsAll(c1);
	}
	
	private static boolean equals(Collection c1, boolean[] b2){
		boolean[] b1 = new boolean[c1.size()];
		int count = 0;
		for (Iterator i = c1.iterator(); i.hasNext();)
			b1[count++]= ((Boolean)i.next()).booleanValue();
		
		return equals(b1, b2);
	}
	
	private static boolean equals(boolean[] b1, boolean[] b2){

		if(b1.length != b2.length)
			return false;
		for(int i=0; i<b1.length; i++)
			if(b1[i] != b2[i])
				return false;
			
		return true;
	}
}