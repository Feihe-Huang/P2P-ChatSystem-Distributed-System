import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;

public class ServerReceiveThread extends Thread{
	TextArea chatBox;
	List list;
	TextField textfield;
	ClientList clientList;
	User user;
	Socket socket;
	ObjectOutputStream out;
	ObjectInputStream in;
	ArrayList<ObjectOutputStream> allOut;
	boolean isStop;
	String message;

	public ServerReceiveThread(Socket socket, TextArea chatBox, TextField textfield, List list, User user, ClientList clientList, ObjectInputStream in, ObjectOutputStream out, ArrayList<ObjectOutputStream> allOut, String message)
	{
		this.socket=socket;
		this.chatBox = chatBox;
		this.textfield=textfield;
		this.list=list;
		this.user = user;
		this.clientList = clientList;
		this.in=in;
		this.out=out;
		this.isStop=false;
		this.allOut = allOut;
		this.message=message;
	}
	public void run()
	{
		sendUserList();
		while(true)
		{
			try
			{
				// read object from the ObjectInputStream
				String request = (String) in.readObject();
				// Obtain the user logout message
				if(request.equalsIgnoreCase("User Offline"))
				{
					// client offline
					String username = user.username;
					removeUserByName(username);
					break;
				} else if (request.equalsIgnoreCase("Stats Report"))
				{
					chatBox.append("Stats of user " + user.username + "\n");
					int lines = Integer.parseInt(in.readObject().toString());
					for (int i = 0; i < lines; i++)
						chatBox.append(in.readObject().toString() + "\n");
				}
			}catch(Exception e)
			{
				chatBox.append("error1"+e.toString()+"\n");
			}
		}
	}

	// Gets all that the user entered commands
	public void requestCommandHistory(String username)  {
		int position = getPosition(clientList, username);
		// Find the ObjectOutputStream of the kicked client and pass the kicked information
		ObjectOutputStream stream = allOut.get(position);
		try {
			stream.writeObject("Stats Request");
			stream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// sever kick one client
	public void kickUserByName(String username) {
		// find kicked client 在list中的位置/序号
		int position = getPosition(clientList, username);
		// Find the ObjectOutputStream of the kicked client and pass the kicked information
		ObjectOutputStream stream = allOut.get(position);
		try {
			// write kicked
			stream.writeObject("Kicked");
			stream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// client offline
		removeUserByName(username);
	}

	// client offline
	private void removeUserByName(String username) {
		User client = clientList.find(username);
		// find client position/serial number in the list
		int position = getPosition(clientList, username);
		// delete a client
		clientList.deleteClient(client);
		this.allOut.remove(position);
		list.removeAll();
		// Gets the number of clients currently still online
		int count = clientList.getCount();
		// The chat box displays information about offline users and the number of online users
		chatBox.append( "User "+ username +" offline.\n");
		chatBox.append("Now there are " + count + " users online."+"\n");

		// traverse online clients
		int i = 0;
		while(i < count)
		{
			client = clientList.find(i);
			if(client == null) {
				i++;
				continue;
			}
			// add online client to list
			list.add(client.username);
			i++;
		}

		// Update the number of people currently online
		message = "Online User " + clientList.getCount() + "\n";
		textfield.setText(this.message);
		sendUserList();
		// Send offline messages to all clients
		offlineToAll( "User "+ username +" offline\n");
	}


	// Send offline messages to all clients
	public void offlineToAll(String msg)
	{
		try
		{
			// Iterate Output Streams
			Iterator<ObjectOutputStream> it = this.allOut.iterator();
			while(it.hasNext())
			{
				ObjectOutputStream tout = it.next();
				// write offline messages
				tout.writeObject("Offline Information");
				tout.flush();
				tout.writeObject(msg);
				tout.flush();
				//socket.close();
			}

		}
		catch(Exception e)
		{
			chatBox.append("error2"+e.toString());
		}
	}


	public void sendUserList()
	{
		StringBuilder userList= new StringBuilder();
		// Get the number of users
		int count= clientList.getCount();
		int i=0;
		while(i < count) {
			// Get all the clients in turn
			User client = clientList.find(i);
			// When there is no cline, skip
			if(client == null) {
				i++;
				continue;
			}
			// Add the client username to the user list and separate it with @@
			userList.append(client.username);
			userList.append("@@");
			i++;
		}

		try {
			for (ObjectOutputStream tout : this.allOut) {
				// Write the information in the user list and compare it in the client
				tout.writeObject("User List");
				tout.flush();
				tout.writeObject(userList.toString()); // Delimited string with client username

				tout.flush();
				tout.writeObject(this.message);
				tout.flush();

				if (tout != out) {
					tout.writeObject(clientList.find(clientList.getCount() - 1));
//					System.out.println("+++++++++++++++"+ clientinfo.getCount()+"---------------");
					tout.flush();
				} else {
					tout.writeObject(clientList);
					tout.flush();
				}
			}
		}
		catch(Exception e) {
			chatBox.append("error09"+e.toString()+"\n");
		}
	}

	// find a specific client
	public int getPosition(ClientList clientList, String name)
	{
		int count = clientList.getCount();
		int i = 0;
		// Compare the client information one by one
		while(i < count) {
			User client = clientList.find(i);
			// If the client is not found, continue until the client location is returned
			if(!name.equalsIgnoreCase(client.username)) {
				i++;
			} else {
				return i;
			}
		}
		return i;
	}
}