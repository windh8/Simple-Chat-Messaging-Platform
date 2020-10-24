/*Student Name: Heaten Mistry
 *Assignment: 2
 *Course: EECS 3214*/

import java.net.ServerSocket;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.util.Scanner;

public class Client
{
	/*Scanner object created to allow for user input*/
	public static Scanner in = new Scanner(System.in);

	public static void main(String[] args)
	{
		/*When run by user, user must specify Server IP address so a connection could be made.
		 *If no argument is made when attempting to run program, error will occur*/
		if(args.length != 2)
		{
			System.err.println("Error: Run Program with following: java Client <Server IP> <Server PORT>");
			System.exit(1);
		}
		
		/*ServerIP variable will refer to the Serverès IP Address that the user specifies.
		 *ServerPort variable is predefined to a specific port number that the Server will be listen at*/
		String ServerIP = args[0];
		int ServerPort = Integer.parseInt(args[1]);

		try 
		{
			/*Creates and opens a connection to the Server Program*/
			Socket serverHost = new Socket(ServerIP, ServerPort);

			/*Creates an Input Stream (BufferedReader) & an Output Stream (PrintWriter); both will utilze the Socket 
			 *created above (serverHost) to send and recieve messages from the Server Program*/
			BufferedReader socketIn = new BufferedReader(new InputStreamReader(serverHost.getInputStream()));
			PrintWriter socketOut = new PrintWriter(serverHost.getOutputStream(), true);

			/*Upon connecting to the Server Program, the user of the client program will be notified & given instructions*/
			System.out.println("Connection to Server: Connected!");
			System.out.println("Acceptable Commands: JOIN, LEAVE, & LIST\n");

			/*This variable will get the users input*/
			String stdin = Client.in.nextLine();

			/*In this while-loop, the client program will be able to accept & initiate a chat, look up who is online, and join & leave the chat room*/
			while(stdin != null)
			{
				/*If the Socket's Input Stream (BufferedReader) is ready to be read, then another client/user must have initiated a chat session with this user.
				 *The current user of this client program will be asked if they want to confirm the chat session, if yes then one will be open,
				 *otherwise the request will be terminated.
				 *
				 *Only executed if current user of this client has already joined the chat room.*/
				if(socketIn.ready())
				{
					/*These variables will hold information pertaining to the requesting client/user of the chat room.
					 *The String peer will hold the following information in the following format: username|ip-address*/
					String peer = socketIn.readLine();
					String peer_ip = peer.substring(peer.indexOf("|") + 2);
					String peer_name = peer.substring(0, peer.indexOf("|"));

					/*Client prints out following message to screen and allows the client to supply input response*/
					String confirm = "Peer <" + peer_name + "> wishes to chat with you. Enter Y to accept or N to reject session.";
					System.out.println(confirm);
					String confirmation = Client.in.nextLine();

					/*If the user of the client enters in anything other than 'Y', 'y', 'N', 'n', repeat the prompt to the user
					 *until they enter in an accepted response*/
					while(!confirmation.toUpperCase().equals("Y") && !confirmation.toUpperCase().equals("N"))
					{
						System.out.println("Please Enter Y to accept request or N to reject request.");
						confirmation = Client.in.nextLine();
					}

					/*Sends the current user's response to the user that initiated the chat*/
					socketOut.println(confirmation);

					/*If the current user selects 'N' or 'n' then the chat request is rejected,
					 *otherwise a chat session is opened by the following steps:
					 *1 - The current user (who accepted the chat request) waits for an incomming connection at port 53131
					 *2 - Once the requesting user connects to the current user, a method from this class, 'chatSession()', is invoked
					 *3 - Once the chat session is over, both users return to the Chatroom*/
					if(confirmation.toUpperCase().equals("Y"))
					{
						ServerSocket connecting_peer = new ServerSocket(53131);
						Socket current_chat = connecting_peer.accept();

						/*The chatSession method facilitates the chat session between the two users.
						 *See method below for more information.*/
						Client.chatSession(peer_ip, peer_name, current_chat);

						current_chat.close();
						connecting_peer.close();
											
						/*When current user exits chat, client program will send a message 'LOBBY' to a thread that facilitates the chat on the server side (initiateChat)*/
						socketOut.println("LOBBY");
					}
					else
						System.out.println("Chat request rejected.");
				}

				/*If the current user enters the join command,"JOIN", into standard input, they will join the Server's chatroom and be available to chat with other users*/
				if(stdin.equals(Commands.join))
				{
					/*Once the current user selects to join the chatroom, they must select a username to be identified with.*/
					System.out.println("Please enter a username.");
					String username = Client.in.nextLine();

					/*These lines of code will send the join command and the username to the Server.
					 *The Server in-turn will update the list of connected clients, such that the information associated with this current client will
					 *reflect this client being online and available to chat with in the chatroom.*/
					socketOut.println(Commands.join);
					socketOut.println(username);

					/*Once the above information is sent to the Server's chatroom, the chatroom will send a message back to the current client.
					 *If this client selected a username that is already taken then a message stating such will be returned to the current user of this client program
					 *and they will be prompted to join again with a different username, otherwise if the username is valid for this client then the returned messsage
					 *from the Server will reflect that.*/
					String result = socketIn.readLine();
					System.out.printf("%n%s%n%n", result);
				}

				else if(stdin.equals(Commands.leave))
				{
					/*This block of code will allow the newly connected client to disconnect from the ChatRoom & Server.*/
					System.out.println("\nDisconnecting from Chatroom ... ");
					socketOut.println(Commands.leave);

					String result = socketIn.readLine();
					System.out.printf("%s%n%n", result);
					if(result.equals("Good Bye!"))
					{
						System.out.println("Disconnected from Server.");
						break;
					}
				}
				else if(stdin.equals(Commands.list))
				{
					socketOut.println(Commands.list);
					String peer_list = socketIn.readLine();

					System.out.println("\nOnline Peers:\nIP-Address   \t\tUsername");
					while(!peer_list.equals(""))
					{
						int delimiterIndex = peer_list.indexOf("|");
						String user = peer_list.substring(0, delimiterIndex);

						peer_list = peer_list.substring(delimiterIndex + 1, peer_list.length());
						System.out.println(user);
					}

					System.out.println("");
				}
				else if(stdin.equals(Commands.chat))
				{
					socketOut.println(Commands.chat);
					System.out.println("Please Enter the Username of the Peer you wish to chat with.");
					String target_peer = Client.in.nextLine();
					System.out.println("\nSending request ...");
					socketOut.println(target_peer);

					String request_response = socketIn.readLine();

					if(request_response.equals("Y"))
					{
						String target_ip = socketIn.readLine();
						target_ip = target_ip.substring(target_ip.indexOf("/") + 1);

						Socket chatee = new Socket(target_ip, 53131);
						Client.chatSession(target_ip, target_peer, chatee);

						//When the ChatRoom recieves this message it will modify CLientInfo object related to this user in Server.clients
						socketOut.println("LOBBY");
					}
					else if(request_response.equals("N"))
					{
						//the other peer has rejected request
						System.out.printf("Peer <%s> has rejected the chat request.%n", target_peer);
					}
				}

				stdin = Client.in.nextLine();
			}

			serverHost.close();
			socketIn.close();
			socketOut.close();
		}
		catch(Exception e)
		{
			System.err.println("Exception Caught in Method: main(String[] args)");
			System.out.println(e);

			System.out.printf("%nError Message: %s%nStack Trace:", e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

	}

	public static void chatSession(String peer_address, String peer_username, Socket peer_chat)
	{
		try
		{
			BufferedReader peer_in = new BufferedReader(new InputStreamReader(peer_chat.getInputStream()));
			PrintWriter peer_out = new PrintWriter(peer_chat.getOutputStream(), true);

			//String send_to_peer = Client.in.nextLine();
			boolean continue_chat = true;
			String send_to_peer = "";

			System.out.printf("Chat Session with peer<%s> has Started.%nType EXIT-CHAT to exit the chat session.%n", peer_username);

			while(continue_chat)
			{
				String peer_response = "";
				if(peer_in.ready())
				{
					peer_response = peer_in.readLine();
					System.out.printf("<%s>: %s%n", peer_username, peer_response);
				}

				System.out.print("<YOU>: ");
				send_to_peer = Client.in.nextLine();

				if(send_to_peer.equals("EXIT-CHAT"))
				{
					System.out.printf("%nExiting chat session with user %s%n", peer_username);
					continue_chat = false;
					peer_out.println(send_to_peer);
				}
				else if(peer_response.equals("EXIT-CHAT"))
				{
					System.out.printf("%n%s has exited chat.%nSending you back to ChatRoom Lobby.%n", peer_username);
					continue_chat = false;
				}
				else
					peer_out.println(send_to_peer);
			}

			peer_in.close();
			peer_out.close();
		}
		catch(Exception e)
		{
			System.err.println("Exception Caught in Method: chatSession(String peer_address, String peer_username)");
			System.out.println(e);

			System.out.printf("%nError Message: %s%nStack Trace:", e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}		
	}
	
}
