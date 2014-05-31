package edu.uci.ics.ljm;

import java.io.IOException;

public class TestServerObject implements TestServerInterface{

	public static void main(String[] args){
		try{
			LJMDeployment.deploy("TestServer", new TestServerObject());
		}
		catch(LJMException e){
			e.printStackTrace();
			return;
		}
	}
	
	
	public void sayHello(){
		System.out.println("Hello, world!");
	}
	
	public int add(int a, int b){
		return a + b;
	}

	public void printString(String s){
		System.out.println(s);
	}
}

