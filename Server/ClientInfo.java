/*Student Name: Heaten Mistry
 *Student Number: 211 869 476
 *CSE account: cse13131
 *Assignment: 2
 *Course: EECS 3214*/

import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class ClientInfo
{
	private Socket client;
	private boolean on_list; 
	private BufferedReader inStream;
	private PrintWriter outStream;
	private String username;
	private boolean inChat;

	public ClientInfo(Socket host, BufferedReader in, PrintWriter out)
	{
		this.client = host;
		this.on_list = false;
		this.inStream = in;
		this.outStream = out;
		this.username = "unknown";
		this.inChat = false;
	}

	/*This mutator method will set the property of whether the rest of the chatroom users
	 *can see the current user or not. This is useful due to the fact that when a client
	 *joins the server (and hence gets put into the ChatRoom), they are set not to be 
	 *visible, but one the client issues the command JOIN, then the client will be visible.*/
	public void setVisible(boolean value)
	{
		if(!value)
			this.on_list = false;
		else
			this.on_list = true;
	}

	public Socket getSocket()
	{
		return this.client;
	}

	public boolean getVisible()
	{
		return this.on_list;
	}

	public BufferedReader getInStream()
	{
		return this.inStream;
	}

	public PrintWriter getOutStream()
	{
		return this.outStream;
	}

	public void setUserName(String name)
	{
		this.username = name;
	}

	public String getUserName()
	{
		return this.username;
	}

	public boolean getChatStatus()
	{
		return this.inChat;
	}

	public void setChatStatus(boolean stat)
	{
		this.inChat = stat;
	}
}