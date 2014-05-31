package archstudio.comp.archon;

import org.python.util.*; 
import org.python.core.*; 

import edu.uci.ics.nativeutils.SystemUtils;

import java.io.*;
import java.util.*;

public class ArchonInterpreter{

	protected String id;
	protected Map availableServices;
	
	protected InteractiveConsole interpreter;
	protected BufferedReader stdoutReader;
	protected BufferedReader stderrReader;
	
	/*
	public static void main(String[] args){
		ArchonInterpreter ai = new ArchonInterpreter("foo");
		ai.addArchonOutputListener(new ConsoleArchonOutputListener());
		ai.exec("import zlib");
		ai.exec("x = 5");
		ai.exec("print x");
		ai.exec("print 500");
	}
	*/
	
	public ArchonInterpreter(String id, Map availableServices){
		this.id = id;
		this.availableServices = availableServices;

		interpreter = new InteractiveConsole();
		
		try{
			PipedReader stdoutPipedReader = new PipedReader();
			PipedReader stderrPipedReader = new PipedReader();
			stdoutReader = new BufferedReader(stdoutPipedReader);
			stderrReader = new BufferedReader(stderrPipedReader);
			interpreter.setOut(new PipedWriter(stdoutPipedReader));
			interpreter.setErr(new PipedWriter(stderrPipedReader));
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}

		initLibs();
		
		for(Iterator it = availableServices.keySet().iterator(); it.hasNext(); ){
			String name = (String)it.next();
			Object value = availableServices.get(name);
			interpreter.set(name, value);
		}
		
		//OutputHandlerThread oht = new OutputHandlerThread();
		//oht.start();
	}
	
	protected void initLibs(){
		exec("import sys");
		String[] classpathEntries = SystemUtils.getClassPathEntries(true);
		for(int i = 0; i < classpathEntries.length; i++){
			String syspathEntry = escapeBackslashes(classpathEntries[i])/* + "/src/pylibs"*/;
			String cmd = "sys.path.append('" + syspathEntry + "')";
			//System.out.println("execcing: " + cmd);
			exec(cmd);
		}
	}
	
	public String getID(){
		return id;
	}
	
	public static String escapeBackslashes(String s){
		StringBuffer sb = new StringBuffer(s.length() + 10);
		int len = s.length();
		for(int i = 0; i < len; i++){
			char ch = s.charAt(i);
			if(ch == '\\'){
				sb.append("\\\\");
			}
			else{
				sb.append(ch);
			}
		}
		return sb.toString();
	}
	
	public static String normalizeCode(String code){
		int beginIndex = 0;
		int endIndex = code.length();
		while(code.charAt(beginIndex) == '\n'){
			beginIndex++;
		}
		while(code.charAt(endIndex - 1) == '\n'){
			endIndex--;
		}
		return code.substring(beginIndex, endIndex);
	}
	
	boolean more = false;

	public boolean exec(String code){
		try{
			String prompt = more ? "..." : ">>>";
			fireArchonEchoLine(prompt + " " + code + "\n");
			more = interpreter.push(code);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		handleOutput();
		fireArchonInputReady(more);
		return more;
	}
	
	char[] buf = new char[8192];
	
	protected void handleOutput(){
		try{
			while(stdoutReader.ready()){
				int chars = stdoutReader.read(buf);
				
				if(chars == -1){
					return;
				}
				if(chars > 0){
					String output = new String(buf, 0, chars);
					fireArchonLine(output);
					fireArchonStdoutLine(output);
				}
			}
			while(stderrReader.ready()){
				int chars = stderrReader.read(buf);
				if(chars == -1){
					return;
				}
				if(chars > 0){
					String output = new String(buf, 0, chars);
					fireArchonLine(output);
					fireArchonStderrLine(output);
				}
			}
		}
		catch(IOException ioe){
			System.err.println("This shouldn't happen: " + ioe.toString());
		}
	}
	
	protected Vector archonInputReadyListeners = new Vector();
	
	public void addArchonInputReadyListener(ArchonInputReadyListener l){
		archonInputReadyListeners.addElement(l);
	}
	
	public void removeArchonInputReadyListener(ArchonInputReadyListener l){
		archonInputReadyListeners.removeElement(l);
	}
	
	protected void fireArchonInputReady(boolean continuing){
		synchronized(archonInputReadyListeners){
			int len = archonInputReadyListeners.size();
			for(int i = 0; i < len; i++){
				ArchonInputReadyListener l = (ArchonInputReadyListener)archonInputReadyListeners.elementAt(i);
				l.archonInputReady(id, continuing);
			}
		}
	}
	
	protected Vector archonOutputListeners = new Vector();
	
	public void addArchonOutputListener(ArchonOutputListener l){
		archonOutputListeners.addElement(l);
	}
	
	public void removeArchonOutputListener(ArchonOutputListener l){
		archonOutputListeners.removeElement(l);
	}
	
	protected void fireArchonEchoLine(String line){
		synchronized(archonOutputListeners){
			int len = archonOutputListeners.size();
			for(int i = 0; i < len; i++){
				ArchonOutputListener l = (ArchonOutputListener)archonOutputListeners.elementAt(i);
				l.archonEchoLine(id, line);
			}
		}
	}
	
	protected void fireArchonStdoutLine(String line){
		synchronized(archonOutputListeners){
			int len = archonOutputListeners.size();
			for(int i = 0; i < len; i++){
				ArchonOutputListener l = (ArchonOutputListener)archonOutputListeners.elementAt(i);
				l.archonStdoutLine(id, line);
			}
		}
	}
	
	protected void fireArchonStderrLine(String line){
		synchronized(archonOutputListeners){
			int len = archonOutputListeners.size();
			for(int i = 0; i < len; i++){
				ArchonOutputListener l = (ArchonOutputListener)archonOutputListeners.elementAt(i);
				l.archonStderrLine(id, line);
			}
		}
	}

	protected void fireArchonLine(String line){
		synchronized(archonOutputListeners){
			int len = archonOutputListeners.size();
			for(int i = 0; i < len; i++){
				ArchonOutputListener l = (ArchonOutputListener)archonOutputListeners.elementAt(i);
				l.archonLine(id, line);
			}
		}
	}

}
