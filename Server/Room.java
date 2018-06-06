/*Student Name: Heaten Mistry
 *Student Number: 211 869 476
 *CSE account: cse13131
 *Assignment: 2
 *Course: EECS 3214*/

import java.net.Socket;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Room implements Runnable
{
	public static ArrayList<String> peers_available = new ArrayList<String>();

	private String name;

	public Room(String threadName)
	{
		this.name = threadName;
	}

	public void start()
	{
		this.run();
	}

	public void run()
	{
		while(true)
		{
			//Variables to hold reference to the client that has just entered input for the server-chatroom thread to process
			ClientInfo choosen = null;
			try
			{
				//These three lines of code were originally on line 62
				Server.lock.acquire();
				int list_size = Server.clients.size();
				Server.lock.release();

				String message = "";

				//This for loop will find any client that has just sent input through their BufferedInput stream
				if(list_size > 0)
				{
					for(int i = 0; i < list_size; i++)
					{

						//Set the variable choosen to refer to the ClientInfo object currently being looked at
						ClientInfo selected = Server.clients.get(i);

						/*If the choosen objects socket is ready to be read (the client had sent input through their BufferedInput stream)
						 *then select the choosen object by breaking out of the for loop*/
						if(selected.getInStream().ready() && !selected.getChatStatus())
						{
							choosen = selected;
							break;
						}
					}
					if(choosen != null)
						message = choosen.getInStream().readLine();
				}
				
				//Modify later to include username selection (for CLIENT AND ROOM) when using JOIN
				//command on client side
				if(message.equals(Commands.join))
				{
					System.out.printf("Client <%s>: joining ChatRoom ...%n", choosen.getSocket().getInetAddress().toString());

					String return_to_client = "";

					String client_name = choosen.getInStream().readLine();

					if(choosen.getVisible())
					{
						return_to_client = "Already 'JOIN'ed. Cannot JOIN again. Leave first.";
						System.out.printf("Client <%s>: failed joining ChatRoom!%n%n", choosen.getSocket().getInetAddress().toString());
					}
					else
					{
						//Determines if the user selected username is already in use or not.
						//If in use, then the client will not be able to join the ChatRoom and hence will be asked to try again with a different username
						boolean unique_name = true;
						for(int i = 0; i < list_size; i++)
						{
							if(Server.clients.get(i).getUserName().equals(client_name))
							{
								unique_name = false;
								i = list_size + 1;
							}
						}

						if(!unique_name)
						{
							return_to_client = "Cannot Join ChatRoom. Username already in use. Please try again.";
						}
						else
						{
							int index = Server.clients.indexOf(choosen);
							choosen.setVisible(true);
							choosen.setUserName(client_name);
						
							Server.lock.acquire();
							Server.clients.remove(index);
							Server.clients.add(choosen);
							Server.lock.release();
						
							peers_available.add(choosen.getSocket().getInetAddress().toString());

							return_to_client = "Welcome to the ChatRoom. You may now CHAT with other peers.";
							System.out.printf("Client <%s>: joined ChatRoom!%n%n", choosen.getSocket().getInetAddress().toString());
						}
					}
					choosen.getOutStream().println(return_to_client);

				}
				else if(message.equals(Commands.leave))
				{
					String return_to_client = "";
					
					if(choosen.getVisible())
					{
						System.out.printf("Client <%s>: attempting to leave ChatRoom ...%n", choosen.getSocket().getInetAddress().toString());
						return_to_client = "Thank you for using the ChatRoom. No Peer will be able to see you now.";
						
						//Remove the current peers IP address from peers_available to show that they will now not be able to be seen from other peers, hence not online
						int index = peers_available.indexOf(choosen.getSocket().getInetAddress().toString());
						peers_available.remove(index);

						index = Server.clients.indexOf(choosen);
						choosen.setVisible(false);
						
						Server.lock.acquire();
						Server.clients.remove(index);
						Server.clients.add(choosen);
						Server.lock.release();
						
						System.out.printf("Client <%s>: left ChatRoom ...%n%n", choosen.getSocket().getInetAddress().toString());
						choosen.getOutStream().println(return_to_client);
					}
					else
					{
						System.out.printf("Client <%s>: attempting to disconnect from Server!%n", choosen.getSocket().getInetAddress().toString());
						return_to_client = "Good Bye!";

						Server.lock.acquire();
                        Server.clients.remove(choosen);
                        Server.lock.release();

						choosen.getOutStream().println(return_to_client);
						System.out.printf("Client <%s> is now disconnected.%n%n", choosen.getSocket().getInetAddress().toString());

						choosen.getInStream().close();
						choosen.getOutStream().close();
						choosen.getSocket().close();
					}
				}
				else if(message.equals(Commands.list))
				{
					System.out.printf("Client <%s>: issued LIST command ...%n", choosen.getSocket().getInetAddress().toString());
					
					String return_to_client = "";
					for(int i = 0; i < peers_available.size(); i++)
					{
						//Finds the right username of the right client to add to the string
						String client_in_list_username = "";
						for(int j = 0; j < list_size; j++)
						{
							if(Server.clients.get(j).getSocket().getInetAddress().toString().equals(peers_available.get(i)))
							{
								client_in_list_username = Server.clients.get(j).getUserName();
								j = list_size;
							}
						}

						return_to_client = return_to_client + "<" + peers_available.get(i) + ">\t"+ client_in_list_username;

						if(peers_available.get(i).equals(choosen.getSocket().getInetAddress().toString()))
							return_to_client = return_to_client + "\t<YOU>";
						return_to_client = return_to_client + "|";
					}
					choosen.getOutStream().println(return_to_client);

					System.out.printf("Peers List sent to Client <%s>!%n%n", choosen.getSocket().getInetAddress().toString());
				}
				else if(message.equals(Commands.chat))
				{
					String return_to_client = null;//"";
					String target_client = choosen.getInStream().readLine();

					if(!choosen.getVisible())
						return_to_client = "You must first join the ChatRoom before you can chat with a peer!";
					else
					{
						ClientInfo target = null;

						//find the target client
						for(int i = 0; i < list_size; i++)
						{
							if(Server.clients.get(i).getUserName().equals(target_client))
							{
								target = Server.clients.get(i);
								i = list_size;
							}
						}

						if(target == null)
							return_to_client = "Peer could not be found. Possibly disconnected!";
						else if(!target.getVisible())
							return_to_client = "Selected Peer has not joined the ChatRoom. No chat session can be open!";//TEST later
						else if(target.getChatStatus())
							return_to_client = "Selected peer is already in a chat. Please try again later when they are free.";//TEST later
						else
						{
							Thread requesting = new Thread(new InitiateChat(choosen, target));
							requesting.start();
						}
					}

					//If return_to_client is not null then a chat session could not be set up
					if(return_to_client != null)
						choosen.getOutStream().println(return_to_client);
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception error occured.");
				System.out.println(e.getStackTrace());
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
