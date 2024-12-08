import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;


public class Server extends JFrame implements ActionListener{
	// Declare the components of the interface
	JButton sendBtn, closeBtn, startBtn, showClientsBtn, kickBtn;
	TextArea chatBox, userInput;
	TextField textfield;
	TextField severIP = new TextField();
	TextField severPort = new TextField();

	List list;
	// declaration socket
	ClientList clientList;
	ServerSocket serversocket;
	Socket socket;

	// Declare input and output streams
	ObjectOutputStream out;
	ObjectInputStream in;
	ArrayList<ObjectOutputStream> allOut;

	// Declare ip, port, sever .ect
	static InetAddress ip;
	static boolean isStop;
	static Server server;

	ServerThread serverlistenthread;
	ServerReceiveThread serverReceiveThread;


	// main method start sever
	public static void main(String[] args) {
		server = new Server();
	}

	public Server() {

		// initialize allOuts
		this.allOut = new ArrayList<ObjectOutputStream>();

		// set the name of each button
		sendBtn =new JButton("Send");
		sendBtn.setMnemonic('S');
		closeBtn =new JButton("Close");
		startBtn =new JButton("Start");
		showClientsBtn = new JButton("Show Clients");
		kickBtn = new JButton("Kick");
		closeBtn.setEnabled(false);
		sendBtn.setEnabled(false);
		showClientsBtn.setEnabled(false);
		kickBtn.setEnabled(false);


		chatBox = new TextArea("",14,50);
		chatBox.setBackground(Color.lightGray);
		userInput = new TextArea("",4,50);
		userInput.setBackground(Color.lightGray);
		textfield = new TextField();
		severIP = new TextField();
		textfield.setBackground(Color.lightGray);
		severIP.setBackground(Color.lightGray);
		severPort.setBackground(Color.lightGray);

		chatBox.setEditable(false);
		textfield.setEditable(false);
		severIP.setEditable(true);
		severPort.setEditable(true);


		// clients list
		list = new List();

		// create user list
		Panel p1 = new Panel();
		p1.setLayout(new BorderLayout());
		p1.add(new Label("User List"), BorderLayout.NORTH);
		p1.add(textfield, BorderLayout.CENTER);

		// show clients and kick button
		Panel p10 = new Panel();
		p10.setLayout(new GridLayout(1,2,5,5));
		p10.add(showClientsBtn);
		p10.add(kickBtn);

		// user list sidebar
		Panel p2=new Panel();
		p2.setLayout(new BorderLayout());
		p2.add(p1, BorderLayout.NORTH);
		p2.add(list, BorderLayout.CENTER);
		p2.add(p10, BorderLayout.SOUTH);

		// start, lose part
		Panel p3=new Panel();
		p3.setLayout(new GridLayout(1,6,4,4));
		p3.add(new Label("   Sever IP:"));
		p3.add(severIP);
		p3.add(new Label("   Sever Port:"));
		p3.add(severPort);
		p3.add(startBtn);
		p3.add(closeBtn);

		// chat box
		Panel p4=new Panel();
		p4.setLayout(new BorderLayout());
		p4.add(new Label("Chat History"),BorderLayout.NORTH);
		p4.add(chatBox,BorderLayout.CENTER);

		// chat box
		Panel p9=new Panel();
		p9.setLayout(new BorderLayout());
		p9.add(p3,BorderLayout.NORTH);
		p9.add(p4,BorderLayout.CENTER);

		// Chat box and input box
		Panel p5=new Panel();
		p5.setLayout(new BorderLayout(5,9));
		p5.add(p9,BorderLayout.CENTER);
		p5.add(userInput,BorderLayout.SOUTH);

		// Send, clear button part
		Panel p6=new Panel();
		p6.setLayout(new GridLayout(1,1));
		p6.add(sendBtn);

		// Chat box and input box, send, clear button section
		Panel p7=new Panel();
		p7.setLayout(new BorderLayout());
		p7.add(p5,BorderLayout.CENTER);
		p7.add(p6,BorderLayout.SOUTH);

		// Side frame with all parts on the right
		Panel p8=new Panel();
		p8.setLayout(new BorderLayout());
		p8.add(p2,BorderLayout.EAST);
		p8.add(p7,BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(p8,BorderLayout.CENTER);

		// Set the width, height, and title
		setSize(800,500);
		setTitle("Sever");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		// Action Listener
		sendBtn.addActionListener(this);
		closeBtn.addActionListener(this);
		startBtn.addActionListener(this);
		showClientsBtn.addActionListener(this);
		kickBtn.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		// click start sever
		if(e.getSource() == startBtn) {
			startServer();
		}
		// click close sever
		else if(e.getSource() == closeBtn)
		{
			stopServer();
			System.exit(0);
		}
		// click send button
		else if(e.getSource() == sendBtn) {
			// Decide whether to use command or type directly
			send();
		}
		else if (e.getSource() == showClientsBtn){
			// Displays all connected clients
			showClients();
		}
		// select one client to kick out
		else if (e.getSource() == kickBtn){
			if (list.getSelectedItem() != null) {
				String kickedClient = list.getSelectedItem().toString();
				// kick client
				kick(kickedClient);
			} else {
				JOptionPane.showMessageDialog(this, "Please select a client first.","Information" , JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	// determine to use command or GUI
	private void send() {
		boolean useCommand = false;

		// determine to use command or GUI
		String[] command = userInput.getText().split("\\{|_|\\}");

		if (command.length >= 2) {
			// Determine whether to use commands
			useCommand = command[1].equalsIgnoreCase("LIST")|| command[1].equalsIgnoreCase("KICK") || command[1].equalsIgnoreCase("STATS");
			// determine command
			if (useCommand){
				// displays all connected clients
				if (command[1].equalsIgnoreCase("LIST")) {
					// displays all connected clients
					showClients();
					userInput.setText("");
				} else if (command[1].equalsIgnoreCase("KICK")){  // kick one client
					// find the kicked client
					String kickedClient = command[2];
					// kick the client
					kick(kickedClient);
					userInput.setText("");
				} else if (command[1].equalsIgnoreCase("STATS")){ // close the connection
					serverReceiveThread.requestCommandHistory(command[2]);

					chatBox.append("\n");
					userInput.setText("");
				}
				else {
					// Prompt to use the correct commands
					JOptionPane.showMessageDialog(this, "Please use the correct command.", "Information", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
		// Determine whether to use the command line
		if (!useCommand) {
			// Detects whether there is input information
			if(userInput.getText().equalsIgnoreCase("") || userInput.getText()==null) {
				// if no information is entered, and a dialog box is displayed
				JOptionPane.showMessageDialog(this, "No system information has been entered!","Information" , JOptionPane.INFORMATION_MESSAGE);
			}
			// send message
			sendSeverMessage(null);
		}
	}

	// Displays all connected clients
	public void showClients() {
		if (clientList.getCount() != 0) {
			chatBox.append("Currently connected clients are:");
			for (int c = 0; c < clientList.getCount(); c++) {
				chatBox.append(" " + clientList.find(c).username);
			}
			chatBox.append(" " + "\n");
		} else {
			chatBox.append("No clients are connected to the sever" + "\n");
		}
	}

	// kick a client
	private void kick(String kickedClient) {
		// use kickUserByName() in serverReceiveThread
		serverReceiveThread.kickUserByName(kickedClient);
	}


	// start sever
	public void startServer() {
		try {
			// set sever port
			serversocket = new ServerSocket(Integer.parseInt(severPort.getText()));

			chatBox.append("Sever starts successfully" + "\n");
			chatBox.append("The sever IP is: " + severIP.getText() + "   The sever port is: " + severPort.getText() + "\n");

			startBtn.setEnabled(false);
			closeBtn.setEnabled(true);
			sendBtn.setEnabled(true);
			showClientsBtn.setEnabled(true);
			kickBtn.setEnabled(true);
			severIP.setEditable(false);
			severPort.setEditable(false);

			isStop = false;

			// Start the sever thread
			clientList = new ClientList();
			serverlistenthread = new ServerThread(serversocket, chatBox,textfield,list, clientList);
			serverlistenthread.start();
		} catch(Exception e) {
			chatBox.append("error0");
		}
	}

	// close sever
	public void stopServer() {
		try {
			// close sever
			isStop=true;
			serversocket.close();
			socket.close();
			list.removeAll(); // logout all clients
		}
		catch(Exception e) {
			chatBox.append("close");
		}
	}

	// send message
	public void sendSeverMessage(String msg) {
		String message = "";
		if (msg == null) {
			// get input text
			message = userInput.getText();
		} else {
			message = msg;
		}
		if (!Objects.equals(userInput.getText(), "") && userInput != null) {
			// show in the cha tbox
			chatBox.append("Sever Message: " + "\n" + userInput.getText() + "\n");
		}
		// Set the input box to blank
		userInput.setText("");

		try {
			for (ObjectOutputStream tout : this.allOut) {
				// write sever send msg
				tout.writeObject("Sever");
				tout.flush();
				tout.writeObject(message);
				tout.flush();
			}
		}
		catch(Exception e)
		{
			chatBox.append("error92"+e.toString());
		}
	}

	// sever thread
	public class ServerThread extends Thread{
		ServerSocket serversocket;
		TextArea taRecord;
		List list;
		TextField textfield;
		ClientList clientList;
		User user;

		// constructor
		public ServerThread(ServerSocket serversocket, TextArea taRecord, TextField textfield, List list, ClientList clientList)
		{
			this.serversocket=serversocket;
			this.taRecord=taRecord;
			this.textfield=textfield;
			this.list=list;
			this.clientList = clientList;
		}


		public void run() {
			// Judge that the sever is not stopped or closed
			while(!isStop && !serversocket.isClosed())
			{
				try {
					user = new User();
					// The connection request from the client is received
					socket = serversocket.accept();
					// Obtain the entered ip address. The default value is 127.0.0.1
					InetAddress ip = InetAddress.getByName(severIP.getText());
					// Set the sever ip address and client ip address
					server.setIp(ip);
					user.ip = ip;

					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					allOut.add(out);
					in = new ObjectInputStream(socket.getInputStream());
					// get client ip address and port number
					user.username=(String)in.readObject();
					user.port = in.readInt();
					// add a new user list
					list.add(user.username);
					clientList.add(user);
					// The chat box displays the online user
					taRecord.append("User " + user.username + " is on line\n");
					sendSeverMessage("User " + user.username + " is on line\n");
					// Information in the side user list
					String message = clientList.getCount() + " online Users." + "\n";
					textfield.setText(message);

					server.setOut(out);

					// start server receive thread
					serverReceiveThread = new ServerReceiveThread(socket,taRecord,textfield,list, user, clientList,in,out,Server.this.allOut,message);
					serverReceiveThread.start();
				}
				catch(Exception e)
				{
					taRecord.append("error85"+e.toString());
				}
			}
		}
	}

	// define set, get methods
	public void setOut(ObjectOutputStream out)
	{
		this.out=out;
	}
	public void setIp(InetAddress ip)
	{
		this.ip = ip;
	}
}