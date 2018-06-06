/*Student Name: Heaten Mistry
 *Student Number: 211 869 476
 *CSE account: cse13131
 *Assignment: 2
 *Course: EECS 3214*/

public class InitiateChat implements Runnable
{
	//This private variable will hold the information of the peer that requested the chat with the target_peer
	private ClientInfo source_peer;

	//This privaye variable will hold the information of the peer that will be requested to chat
	private ClientInfo target_peer;

	public InitiateChat(ClientInfo peer1, ClientInfo peer2)
	{
		this.source_peer = peer1;
		this.target_peer = peer2;
	}

	public void start()
	{
		this.run();
	}

	public void run()
	{
		try
		{
			int source_index = Server.clients.indexOf(this.source_peer);
			int target_index = Server.clients.indexOf(this.target_peer);

			//acquire lock to Server.clients
			Server.lock.acquire();
			//change the status's of the specified peers to show that they will possibly be in a chat session.
			//This is done so that the Room.java thread doesnt read the inputStream
			Server.clients.get(source_index).setChatStatus(true);
			Server.clients.get(target_index).setChatStatus(true);
			//release lock here
			Server.lock.release();

			this.source_peer.setChatStatus(true);
			this.target_peer.setChatStatus(true);

			String confirm = source_peer.getUserName() + "|" + source_peer.getSocket().getInetAddress().toString();
			target_peer.getOutStream().println(confirm);
		
			String confirmation = target_peer.getInStream().readLine();

			if(confirmation.toUpperCase().equals("Y"))
			{
				source_peer.getOutStream().println("Y");

				String send_to_peer = target_peer.getSocket().getRemoteSocketAddress().toString();
				//String send_to_peer = target_peer.getSocket().getLocalSocketAddress().toString();
				send_to_peer = send_to_peer.substring(0, send_to_peer.indexOf(":"));
				source_peer.getOutStream().println(send_to_peer);

				System.out.println(send_to_peer);
				//source_peer.getOutStream().println(target_peer.getSocket().getInetAddress().toString());

				//this will allow this thread to wait until the specified peers are done chatting
				int peer_count = 2;
				while(peer_count != 0)
				{
					if(this.source_peer.getInStream().ready() && this.source_peer.getInStream().readLine().equals("LOBBY"))
					{
						//acquire lock to Server.clients
						Server.lock.acquire();
						//change the status of both peers to reflect that they are not in a chat session
						Server.clients.get(source_index).setChatStatus(false);
						//release lock here
						Server.lock.release();

						peer_count--;
					}
					else if(this.target_peer.getInStream().ready() && this.target_peer.getInStream().readLine().equals("LOBBY"))
					{
						//acquire lock to Server.clients
						Server.lock.acquire();
						//change the status of both peers to reflect that they are not in a chat session
						Server.clients.get(target_index).setChatStatus(false);
						//release lock here
						Server.lock.release();

						peer_count--;
					}
				}
			}
			else
			{
				//they reject the chat session request with N
				source_peer.getOutStream().println("N");

				source_index = Server.clients.indexOf(this.source_peer);
				target_index = Server.clients.indexOf(this.target_peer);
				
				//acquire lock to Server.clients
				Server.lock.acquire();
				//change the status of both peers to reflect that they are not in a chat session
				Server.clients.get(source_index).setChatStatus(false);
				Server.clients.get(target_index).setChatStatus(false);
				//release lock here
				Server.lock.release();
			}
			//The thread program then terminates at this point
			//test with System.exit(0) here maybe?
		}
		catch(Exception e)
		{
			System.err.println("Exception Caught in (Class: InitiateChat.java) Method: run()");
			System.out.println(e);

			System.out.printf("%nError Message: %s%nStack Trace:", e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}