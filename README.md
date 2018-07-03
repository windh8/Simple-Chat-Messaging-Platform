# Simple-Chat-Messaging-Platform (Java) 
 ![stability-stable](https://img.shields.io/badge/stability-stable-green.svg)

This is a Simple Chat Messaging Platform that I have built, from scratch, for an assignment in my EECS3214 class at York University.
It is composed of two applications: a Server application, and a Client application.



## <a name='SetUp'></a>How do I set it up?
  1. Download the contents of this Repository
  2. Once downloaded, go to the root directory of where the project files are located and run the following command in either cmd or shell
      ```sh
          javac *
      ```
    
  3. Take the Server folder and place it on a device that you would like to run the Server application on
     (*Note: Java must be installed*)
    
  4. Take the Client folder and place it on the device(s) that you would like to run the Client application on 
     (*Note: Java must be installed*)
  
  5. Run the following command to start the Server
      ```sh 
          java Server <PORT_NUMBER>
      ```
        where <PORT_NUMBER> would correspond to a free port on your device
      ```sh
          example: java Server 15823
      ```
      
  6. Once the Server application is running, run the Client application(s), by using the following commands
      ```sh 
          java Client <SERVER_IP> <SERVER_PORT>
      ```
        where <SERVER_IP> is the IP Address of the Server that you had set up in step 5
        and <SERVER_PORT> is the Port on the Server, where the Server application is listening for connection requests
      ```sh
          example: java Client 132.185.0.25 15823
      ```
  
  7. What next? Start using it to chat with other connected peers.
     Don't know how? Go to the section [How does it work?](#HowItWorks)
  
## <a name='HowToUse'></a>How do i use it?
Once you are connected to the Server, you are not yet in the ChatRoom.
To join the ChatRoom, type in
```sh JOIN```

Once connected, you can use either of the following commands:
  - **LIST** - List all connected peers 
  - **CHAT** - Chat with a connected peer
  - **LEAVE** - Leave the ChatRoom/Server
  
  *Note: The CHAT command will ask you which peer you would like to chat to, and hence it would be best to list all connected peers before you use the CHAT command*
  
## <a name='HowItWorks'></a>How does it work?
The Server application consists of two threads: 
	 1. The Server Thread
	 2. The ChatRoom Thread

Both threads of the Server application serve different purposes; the Server thread handles incomming connection requests and the ChatRoom thread facilitates client requests. 

Upon starting the Server application, both threads will be up and running. The Server thread will start handling incomming connection requests, and the ChatRoom thread will begin servicing the clients that are already in the shared thread-safe collection (upon start-up the collection will be empty, and hence the ChatRoom thread will not really be doing anything).

When a client application connects, the Server thread takes the client information and stores it on the shared thread-safe collection. The information stored would correspond to the socket used to communicate with the client, as well as the streams needed to send and recieve information to/from the client.

While the Server thread handles connection requests, the ChatRoom thread will be busy facilitating the requests of the connected clients. The requests submitted by the connected clients, will be one of the three commands specified above in [How do i use it?](#HowToUse).
