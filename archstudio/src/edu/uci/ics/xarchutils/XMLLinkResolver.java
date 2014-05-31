/*
 * Copyright (c) 2000-2001 Regents of the University of California.
 * All rights reserved.
 *
 * This software was developed at the University of California, Irvine.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the University of California, Irvine.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package edu.uci.ics.xarchutils;

import edu.uci.isr.xarch.*;
import edu.uci.isr.xarch.instance.*;

import java.util.*;
import java.io.*;
import org.xml.sax.SAXException;


/**
 * XML Link resolver, provides methods for resolving XML Links across
 * multiple files.
 * 
 * @author Kari Nies
 */
public class XMLLinkResolver
{
    private IdTable idTable;

    /**
     * Create a new XML Link Resolver
     * @param idt IdTable that will be used to resolve all XML Links.
     */
    public XMLLinkResolver (IdTable idt) {
        idTable = idt;
    }

    /**
     * Resolves an XML Link by returning the XArch Element that it references.
     * @param xmlLink XML Link to be resolved.
     * @return the XArch Element that the given XML Link references.
     */
    public IXArchElement resolveXMLLink(IXMLLink xmlLink) 
        throws SAXException, IOException { 
        return resolveHref(xmlLink.getXArch(), xmlLink.getHref());
    }

    /**
     * Resolves an href by returning the XArch Element that it references.
     * @param href href string to be resolved. An href begins with an 
     * optional URL followed by #<id>, where the URL locates
     * an XML document and the <id> references an XArch Element
     * within that document.
     * @return the XArch Element that the given href references.
     */
    public IXArchElement resolveHref(String href) 
        throws SAXException, IOException {
        if (href == null) return null;
        String id;
        int i = href.indexOf('#');
        if (i == -1 || i == (href.length()-1)) {
            return null;
        } else {
            id = href.substring(i+1);
        }       
        if (!(i == 0)){
            // href is not local
            String url = href.substring(0,i);
            // load the xml doc if necessary
            IXArch xArch = idTable.parseFromURL(url);
        }
        return idTable.getEntity(id);       
    }	

    /**
     * Resolves an href by returning the XArch Element that it references.
     * @param href href string to be resolved. An href begins with an 
     * optional URL followed by #<id>, where the URL locates
     * an XML document and the <id> references an XArch Element
     * within that document.
	   * @param xArch The xArch that is the root of the tree that provides
	 	 * the context for the href.
		 * @param href the href to resolve.
     * @return the XArch Element that the given href references.
     */
    public IXArchElement resolveHref(IXArch xArch, String href) 
        throws SAXException, IOException {
        if (href == null) return null;
        String id;
        int i = href.indexOf('#');
        if (i == -1 || i == (href.length()-1)) {
            return null;
        } else {
            id = href.substring(i+1);
        }       
        if (!(i == 0)){
            // href is not local
            String url = href.substring(0,i);
            // load the xml doc if necessary
            IXArch remoteXArch = idTable.parseFromURL(url);
		        return idTable.getEntity(remoteXArch, id);
        }
				//System.out.println("idTable is : " + idTable);
        return idTable.getEntity(xArch, id);
    }	
}
