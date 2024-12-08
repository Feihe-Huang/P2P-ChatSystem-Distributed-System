import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Client extends JFrame implements ActionListener{
	 static final long serialVersionUID = 42L;
	// Declaration of each button, text
	JButton sendBtn, logoutBtn, loginBtn;
	TextArea chatBox, userInput;
	TextField textfield, name;
	TextField clientIP = new TextField();
	TextField clientPort = new TextField();
	List list;
	// Declaration of IO, socket, client information
	ObjectOutputStream out;
	ObjectInputStream in;
	ClientList clientList;
	Socket socket;
	User user;

	int selectedPort;
	int clientListenPort ;
	InetAddress ip;
	int port;
	ClientReceiveThread clientreceivethread;
	String username;
	static Client client;


	// main method in client
	public static void main(String[] args)
	{
		// start a client
		client = new Client();
	}

	// constructor
	public Client()
	{
		// Sets the text and style of the UI interface
		sendBtn =new JButton("Send");
	    sendBtn.setMnemonic('S');
		logoutBtn =new JButton("Close");
		loginBtn =new JButton("Login");
		logoutBtn.setEnabled(false);
		sendBtn.setEnabled(false);

		chatBox =new TextArea("",14,50);
		chatBox.setBackground(Color.lightGray);
		userInput =new TextArea("",4,50);
		userInput.setBackground(Color.lightGray);
		textfield=new TextField();
		textfield.setBackground(Color.lightGray);
		name =new TextField();
		name.setBackground(Color.lightGray);
		clientIP.setBackground(Color.lightGray);
		clientPort.setBackground(Color.lightGray);
		chatBox.setEditable(false);
		textfield.setEditable(false);

		// first add all to the user list
		list=new List();
		list.add("all");

		// layout and styling
		Panel p1=new Panel();
		p1.setLayout(new BorderLayout());
		p1.add(new Label("User List"),BorderLayout.NORTH);
		p1.add(textfield,BorderLayout.CENTER);

		Panel p2=new Panel();
		p2.setLayout(new BorderLayout());
		p2.add(p1,BorderLayout.NORTH);
		p2.add(list,BorderLayout.CENTER);

		Panel p3=new Panel();
		p3.setLayout(new GridLayout(1,8));
		p3.add(new Label("  Username:"));
		p3.add(name);
		p3.add(new Label("  Sever IP:"));
		p3.add(clientIP);
		p3.add(new Label("    Port:"));
		p3.add(clientPort);
		p3.add(loginBtn);
		p3.add(logoutBtn);

		Panel p4=new Panel();
		p4.setLayout(new BorderLayout());
		p4.add(new Label("Chat History"),BorderLayout.NORTH);
		p4.add(chatBox,BorderLayout.CENTER);

		Panel p9=new Panel();
		p9.setLayout(new BorderLayout());
		p9.add(p3,BorderLayout.NORTH);
		p9.add(p4,BorderLayout.CENTER);

		Panel p5=new Panel();
		p5.setLayout(new BorderLayout(5,9));
		p5.add(p9,BorderLayout.CENTER);
		p5.add(userInput,BorderLayout.SOUTH);

		Panel p6=new Panel();
		p6.setLayout(new GridLayout(1,1));
		p6.add(sendBtn);

		Panel p7=new Panel();
		p7.setLayout(new BorderLayout());
		p7.add(p5,BorderLayout.CENTER);
		p7.add(p6,BorderLayout.SOUTH);

		Panel p8=new Panel();
		p8.setLayout(new BorderLayout(5,5));
		p8.add(p2,BorderLayout.EAST);
		p8.add(p7,BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(p8,BorderLayout.CENTER);

	    setSize(800,500);
		setTitle("Client");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setVisible(true);
		sendBtn.addActionListener(this);
		loginBtn.addActionListener(this);
		logoutBtn.addActionListener(this);
		list.addActionListener(this);
		//list.addMouseListener(this);
	}

	// Called when an event is triggered
	@Override
	public void actionPerformed(ActionEvent e)
	{
		// click login button
		if(e.getSource() == loginBtn)
		{
			Login();
		}
		// click logout
		else if(e.getSource() == logoutBtn)
		{
			logout();
//			System.exit(0);
		}
		// click send button
		else if(e.getSource() == sendBtn)
		{
			send();
		}
	}

	// Determine whether to use command or GUI operations
	private void send() {
		String toName = list.getSelectedItem();
		boolean useCommand = false;

		// Whether to use the command line
		String input = userInput.getText();
		String[] command = userInput.getText().split("\\{|_|\\}");

		User user = clientList.find(name.getText());

		if (command.length >= 2) {
			// judge whether to use commands
			useCommand = command[1].equalsIgnoreCase("BROADCAST")|| command[1].equalsIgnoreCase("MESSAGE") || command[1].equalsIgnoreCase("STOP");
			// determine to use command
			if (useCommand){
				// send BROADCAST
				if (command[1].equalsIgnoreCase("BROADCAST")){
					String message = command[3];
					toName = "all";
					sendMessage(message, toName);
					user.commands.add(input);
				} else if (command[1].equalsIgnoreCase("MESSAGE")){  // send to client
					String message = command[4];
					toName = command[2];
					sendMessage(message, toName);
					user.commands.add(input);
				} else if (command[1].equalsIgnoreCase("STOP")){ // close the connection
					logout();
					userInput.setText("");
					user.commands.add(input);
				}
				else {
					// Prompt to use the correct commands
					JOptionPane.showMessageDialog(this, "Please use the correct command.", "Information", JOptionPane.INFORMATION_MESSAGE);
				}
			}

		}
		// Determine whether to use the command line
		if (!useCommand) {
			// If there is no input message
			if (userInput.getText().equalsIgnoreCase("") || userInput.getText() == null) {
				// No input information
				JOptionPane.showMessageDialog(this, "You have not entered the chat information!", "Information", JOptionPane.INFORMATION_MESSAGE);
			}

			String message = userInput.getText();
			sendMessage(message, toName);
		}
	}

	// start
	public void Login()
	{
		new Thread(new ComWithServer()).start();
	}

	public void setUsername(String username)
	{
		this.username= name.getText();
	}

	public void setOut(ObjectOutputStream out)
	{
		this.out=out;
	}

	public ObjectOutputStream getOut()
	{
		return out;
	}

	public void logout() {
		logout(false);
	}
	// close client
	public void logout(boolean kicked)
	{
		logoutBtn.setEnabled(false);
		sendBtn.setEnabled(false);
		loginBtn.setEnabled(true);
		try
		{
			// first remove usernames in user list
			list.removeAll();
			// Iterate all clients, add into user list
			list.add("all");
			textfield.setText("");

			// write User Offline
			if (!kicked) {
				ObjectOutputStream out = client.getOut();
				out.writeObject("User Offline");
				out.flush();
			}
//			in.close();
//			out.close();
		}
		catch(Exception e)
		{
			chatBox.append("error92"+e.toString());
		}
	}

	// send messages
	public void sendMessage(String message, String toName)
	{
		try
		{
			// sent to all
			if(toName == null || toName.equalsIgnoreCase("all"))
			{
				// get client username and message
				String name = this.name.getText();
			    for(int j = 0; j< clientList.getCount(); j++)
				{
					// get all online clients' ips and port numbers
					InetAddress ip1 = clientList.find(j).ip;
					int port1 = clientList.find(j).port;
					// start thread
					new ClientReceiveThread.ClientSendThread(ip1, port1, message, name).start();
				}
				// After sending, set the content of the input box to empty
				userInput.setText("");
			} else  //Sends a message to the specified user
			{
				// Displays the sent message in the chat box
				chatBox.append(name.getText() + " sends to " + toName + " :"+"\n");
				chatBox.append(" "+ message +"\n");

				String name = this.name.getText();
				//  find client through username
				InetAddress ip = clientList.find(toName).ip;
				int port = clientList.find(toName).port;
				// assign to client
				client.setSelectedPort(port);
				String ip1 = ip.getHostAddress();
				// create new client socket
				Socket clientSocket = new Socket(ip1, port);
				// write into output stream
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				out.writeObject("Chat Messages");
				out.writeObject(name);
				out.writeObject(message);
//				out.writeObject(toName);
				out.flush();
				out.close();
				clientSocket.close();
				// Set the input to ""
				userInput.setText("");
			}
		}
		catch(Exception ec)
		{
			chatBox.append("An error occurred while chatting with another online user!"+ec.toString());
		}
	}


    public class ComWithServer implements Runnable{
		public void run() {
			try
			{
				// create new cilent
				user = new User();
				// set ip and port number
				socket = new Socket(clientIP.getText(),Integer.parseInt(clientPort.getText()));
				// set ip and port number for client
				ip = socket.getLocalAddress();
				client.setIp(ip);
				client.setPort(Client.this.clientListenPort);
				// the chat box displays a welcome statement
				chatBox.append("Hello!  You have connected successfully." + "\n");
				chatBox.append("Sever IP is: " + clientIP.getText() + "   The port number is: " + clientPort.getText() + "\n");
				// Randomly generate one port number
				clientListenPort = getClientSever();

				// write client username and port number
				out = new ObjectOutputStream(socket.getOutputStream());
				out.writeObject(name.getText()); // username
				out.flush();
				out.writeInt(Client.this.clientListenPort); // port number
				out.flush();
				// set output stream and username for client
				client.setOut(out);
				client.setUsername(name.getText());
				in = new ObjectInputStream(socket.getInputStream());
				int selectedPort = client.getSelectedPort();
				// start client receive thread
				clientreceivethread = new ClientReceiveThread(user,socket,in,out,list, chatBox, userInput,textfield,ip,Client.this.clientListenPort,selectedPort);
				clientreceivethread.start();
				loginBtn.setEnabled(false);
				logoutBtn.setEnabled(true);
				sendBtn.setEnabled(true);

				while(true)
				{
					try
					{
						// read data
						String type = in.readObject().toString();
						// get user list information
						if(type.equalsIgnoreCase("User List"))
						{
							// get usernames in user list
							String userlist = (String)in.readObject();
							String username[] = userlist.split("@@");
							// remove usernames in user list
							list.removeAll();
							// Iterate clients, add into user list
							int i = 0;
							list.add("all");
							while(i < username.length)
							{
								list.add(username[i]);
								i++;
							}

							String msg = (String)in.readObject();
							textfield.setText(msg);

							// receive a list of clients
							Object o = in.readObject();
							if(o instanceof ClientList)
							{
								clientList = (ClientList) o;
							} else {
								clientList.add((User) o);
							}
						}
						// get sever message
						else if(type.equalsIgnoreCase("Sever"))
						{
							// clients: the chat box displays the message sent by sever
							String mes = (String)in.readObject();
							chatBox.append("Sever: " + mes + "\n");
						}
						// Obtain user logout information
						else if(type.equalsIgnoreCase("Offline Information"))
						{
							// Obtain user logout information
							String msg = (String)in.readObject();
							chatBox.append("User offline:" + msg + "\n");
						}
						// Get kicked information and then logout
						else if (type.equalsIgnoreCase("Kicked")) {
							chatBox.append("You are kicked by the servers\n");
							// Self logout, which contains the part that passes the offline
							logout(true);
						}
						else if (type.equals("Stats Request")) {
							User user = clientList.find(name.getText());
							ObjectOutputStream ostream = client.getOut();
							ostream.writeObject("Stats Report");
							ostream.writeObject(user.commands.size());
							user.commands.forEach(s -> {
								try {
									ostream.writeObject(s);
								} catch (IOException e) {
									e.printStackTrace();
								}
							});
							ostream.flush();
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			catch(Exception e)
			{
				chatBox.append("error12"+e.toString());
			}
		}

		// Generates a random port
		private int getClientSever() {
			Random rand = new Random();
			while(true){
				try{
					int port = rand.nextInt(65500);
					// create sever socket (client as sever)
					ServerSocket socket = new ServerSocket(port);
					socket.close();
					return port;
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
	}


	// set, get methods
    public void setIp(InetAddress ip)
    {
    	this.ip=ip;
    }
    public void setPort(int port)
    {
    	this.port=port;
    }

    public void setSelectedPort(int port)
    {
    	this.selectedPort=port;
    }
    public int getSelectedPort()
    {
    	return selectedPort;
    }
}