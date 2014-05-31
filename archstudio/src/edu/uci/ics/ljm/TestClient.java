package edu.uci.ics.ljm;

public class TestClient{

	public static void main(String[] args){
		if(args.length != 1){
			System.out.println("Usage is: java edu.uci.ics.TestClient [host]");
			return;
		}
		
		TestServerInterface tsi = (TestServerInterface)LJMProxyFactory.createProxy(args[0], "TestServer", new Class[]{TestServerInterface.class});
		tsi.sayHello();
		System.out.println(tsi.add(2, 2));
		long startTime = System.currentTimeMillis();
		for(int i = 0; i < 1000; i++){
			//System.out.println(tsi.add(i, i));
			tsi.printString( i + "  bottles of beer on the wall, " + i + " bottles of beer.");
			try{
				Thread.sleep(10000);
			}
			catch(InterruptedException e){}
		}
		long endTime = System.currentTimeMillis();
		
		System.out.println("1000 RPC's took " + (endTime - startTime) + " milliseconds.");
		System.out.println("That's " + ((endTime - startTime) / 1000) + " milliseconds per RPC.");
	}

}

