
import java.net.*;
import java.io.*;
import java.awt.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class ClientReceiveThread extends Thread {
	Socket socket;
	List list;
	TextArea chatBox;
	TextField textfield;
	ObjectInputStream in;
	ObjectOutputStream out;
	User user;
	InetAddress ip;
	int port;
	ServerSocket serversocket;
	int selectedPort;
	TextArea userInput;

	// constructor
	public ClientReceiveThread(User user, Socket socket, ObjectInputStream in,
							   ObjectOutputStream out, List list, TextArea chatBox,
							   TextArea userInput, TextField textfield, InetAddress ip, int port,
							   int selectedPort) {
		this.user = user;
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.list = list;
		this.chatBox = chatBox;
		this.userInput = userInput;
		this.textfield = textfield;
		this.ip = ip;
		this.port = port;
		this.selectedPort = selectedPort;
	}

	public void run() {

		try {
			// client as a server
			serversocket = new ServerSocket(this.port);

			while (true) {
				// accept connections
				Socket clientSocket = serversocket.accept();
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
				String type = (String) in.readObject();
				// when get Chat Messages
				if (type.equalsIgnoreCase("Chat Messages")) {
					// Gets the sender and message content, and displays it in the chat box
					String name = (String) in.readObject();
					String mess = (String) in.readObject();
					chatBox.append(name + " sends  "  + ":" + "\n");
					chatBox.append(" " + mess + "\n");
				}
				out.close();
				in.close();
//				clientSocket.close();
			}
		} catch (Exception e1) {
			chatBox.append("error47" + e1.toString());
			e1.printStackTrace();
		}
	}


	static class ClientSendThread extends Thread {
		private InetAddress remoteAddress = null;
		private int remotePort = 0;
		private String message = null;
		private String name = null;

		public ClientSendThread(InetAddress address, int port, String message, String name) {
			this.remoteAddress = address;
			this.remotePort = port;
			this.message = message;
			this.name = name;
		}

		public void run() {
			try {
				// The information is passed to the target client
				Socket socket = new Socket(this.remoteAddress, this.remotePort);
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				// Writes the sent information
				out.writeObject("Chat Messages");
				out.writeObject(name);
				out.writeObject(this.message);
//				out.writeObject("all");
				out.flush();
				// out.close();
				//socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}