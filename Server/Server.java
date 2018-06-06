/*Student Name: Heaten Mistry
 *Student Number: 211 869 476
 *CSE account: cse13131
 *Assignment: 2
 *Course: EECS 3214*/

import java.net.ServerSocket;
import java.net.Socket;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;

import java.util.concurrent.Semaphore;

public class Server
{
	/*The Collection object, named clients, is a thread-safe collection that will hold clientInfo objects,
	 *where each clientInfo object will correspond to a unique connected user.
	 *
	 *The Semaphore object, named lock, will be used to ensure that only one thread, at a time, will
	 *be able to read/write to the clients Collection. This is to ensure that no race-conditions occur.*/
	public static List<ClientInfo> clients = Collections.synchronizedList(new ArrayList<ClientInfo>());
	public static Semaphore lock = new Semaphore(1);

	public static void main(String[] args)
	{
		/*If the Server program is not run with an argument, corresponding to a port number,
		 *then specify the proceeding error message below to the user and exit the program with an error.*/
		if(args.length != 1)
		{
			System.err.println("Error: Argument missing!\nRun as such: java Server <port number>");
			System.exit(1);
		}
		int portNumber = Integer.parseInt(args[0]);

		try
		{
			/*Creates a ServerSocket object.*/
			ServerSocket host = new ServerSocket(portNumber);

			boolean isActiveSocket = !(host.isClosed());

			/*Creates a Thread Object that will execute the code in the Runnable Object Room.*/
			Thread chatroom = new Thread(new Room("ChatRoom"), "ChatRoom");
			chatroom.start();
			
			


			while(!(host.isClosed()))//Can replace boolean expression with just 'true' for same added effect
			{
				/*The ServerSocket will wait for any incomming connection requests. Upon recieving one,
				 *the connection will then be referenced by a Socket Object.*/
				Socket newClient = host.accept();

				//System.out.println("DEBUGGING STATEMENT: Client Connected");

				/*The Socket Object, as well as the Input Stream Object (BufferedReader) and the 
				 *Output Stream Object, will then be used to create a ClientInfo Object. Upon its
				 *creation, a Semaphore lock (named lock) will be aquired so that the newly created ClientInfo
				 *Object can be placed in the clients Collection. Once done the lock will be released so that
				 *other threads can access/modify the clients Collection.*/
				BufferedReader tempin = new BufferedReader(new InputStreamReader(newClient.getInputStream()));
				PrintWriter tempout = new PrintWriter(newClient.getOutputStream(), true);

				lock.acquire();
				clients.add(new ClientInfo(newClient, tempin, tempout));
				lock.release();
			}

			/*If the while loop above ends execution, the Server program will close any connection stream that
			 *the ServerSocket Object provides.*/
			System.out.println("Connection Closed.");
			host.close();
		}
		catch(Exception e)
		{
			System.err.println("Exception Caught.");
			System.out.println(e);

			System.out.printf("%nError Message: %s%nStack Trace:", e.getMessage());
			e.printStackTrace();
		}
	}
}