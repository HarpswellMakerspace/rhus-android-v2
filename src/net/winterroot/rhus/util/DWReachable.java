package net.winterroot.rhus.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class DWReachable {


	public static boolean reachable(String domain, int port) {

		boolean reachable = false;
		try{
		    SocketAddress sockaddr = new InetSocketAddress(domain, port);

		                                // Create an unbound socket
		                                Socket sock = new Socket();

		                                // This method will block no more than timeoutMs.
		                                // If the timeout occurs, SocketTimeoutException is thrown.
		                                int timeoutMs = 8000;   // 2 seconds
		                                sock.connect(sockaddr, timeoutMs);
		                                reachable=true;
		} catch(IOException e){
			e.printStackTrace();
			reachable = false;
		}
		return reachable;

	}
	
	public static void testReachability(String domain, int port) throws DWHostUnreachableException{
		
		if(! DWReachable.reachable(domain, port)){
			throw new DWHostUnreachableException(); 
		} 
	}
	
	
}
