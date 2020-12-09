package robprakt;
import robprakt.graphics.MainFrame;

/**
 * The main class
 */
public class Main {	
	
	public static void main(String[] args) {
		MainFrame frame = new MainFrame("Robotik Praktikum");
	}
	
	
	
	
	
//
//	/**
//	 * the main entrance method
//	 * @param args the program start-arguments
//	 */
//	public static void main(String[] args) {
//		if(args.length==0) {
//			System.out.println("Starte das Programm mit\n\"A1\" als Parameter fuer Aufgabe 1\n\"A2\" als Parameter fuer Aufgabe 2\n\"A3\" als Parameter fuer Aufgabe 3");
//			System.exit(0);
//		}else {
//			if(args[0].toLowerCase().equals("a1")) {
//				aufgabe1();
//			}else if(args[0].toLowerCase().equals("a2")) {
//				aufgabe2();
//			}else if(args[0].toLowerCase().equals("a3")) {
//				aufgabe3();
//			}else if(args.length==3) {
//				Server(args[0], Integer.valueOf(args[1]));
//			}else if(args.length==2) {
//				Test(args[0], Integer.valueOf(args[1]));
//			}
//		}
//	}
	
	
	
//	public static void Server(String ip, int port) {
//		TCPServer server = new TCPServer(port);
//		server.accept();
//		boolean quit = false;
//		while(quit==false) {
//			String data = server.receiveData();
//			if(data!=null) {
//				if(data.toLowerCase().equals("quit")) {
//					quit = true;
//					System.out.println("quitting...");
//				}
//				System.out.println("Message from Client: "+data);
//				System.out.println("sending back...");
//				server.sendData(data);
//			}
//		}
//		server.closeConnection();
//	}
//	
//	public static void Test(String ip, int port) {
//		TCPClient client = new TCPClient(ip, port);
//		if(!client.connect()) {
//			System.exit(1);
//		}
//		System.out.println("connected");
//		System.out.println("Sending...");
//		client.sendData("Helo");
//		System.out.println("received: " +client.receiveData());
//		System.out.println("Sending...");
//		client.sendData("Test Hello Robot");
//		System.out.println("received: " +client.receiveData());
//		client.sendData("quit");
//		System.out.println("Trying to quit...");
//		client.closeConnection();
//		System.out.println("Connection closed");
//	}
//	
//	/**
//	 * Excercise 1
//	 * - Connects to robot server
//	 * - logging in
//	 * - Get Version
//	 * - Get active Robot
//	 * - Set Verbosity
//	 * - Move PTP Joints
//	 * - do a forward calculation
//	 */
//	public static void aufgabe1() {
//		TCPClient client = new TCPClient("127.0.0.1", 5005);
//		if(!client.connect()) {
//			System.exit(1);
//		}
//		
//		System.out.print(client.receiveData());
//		client.sendData("Hello Robot");
//		String answer = client.receiveData();
//		System.out.println("login: " +answer);
//		client.sendData("GetVersion");
//		answer = client.receiveData();
//		System.out.println("server version: "+answer);
//		client.sendData("GetRobot");
//		answer = client.receiveData();
//		System.out.println("active robot: "+answer);
//		client.sendData("SetVerbosity 0");
//		answer = client.receiveData();
//		System.out.println("Setting Verbosity to 0: "+answer);
//		client.sendData("MovePTPJoints 0 10 0 0 0 10");
//		System.out.println("Move Joints: "+client.receiveData());
//		client.sendData("ForwardCalc 10 10 10 10 10 10");
//		System.out.println("ForwardCalc: "+client.receiveData());		
//		client.sendData("Quit");
//		System.out.println("Quitting");
//	}
//		
//	/**
//	 * Excercise 2
//	 * - Connects to robot server and logs in
//	 * - command line interface, to get Infos from robot
//	 * - and send commands direkt to robot
//	 */
//	public static void aufgabe2() {
//		Scanner userInput = new Scanner(System.in);
//		String command = "";
//		TCPClient client = new TCPClient("127.0.0.1", 5005);
//				
//		do {
//			System.out.println("Geben Sie ein: ");
//			System.out.println("1 fuer Verbindung zum Server");
//			System.out.println("2 fuer Status");
//			System.out.println("3 fuer direkte Kommandoeingabe");
//			System.out.println("4 zum Beenden");
//			command = userInput.nextLine();
//			switch (command) {
//			case "1":
//				if(client.isClosed()) {
//					System.out.println("Verbinde mit Server...");
//					client.connect();
//					System.out.print(client.receiveData());
//					client.sendData("Hello Robot");
//				}else {
//					System.out.println("Du bist schon verbunden!\n");
//					continue;
//				}
//				break;
//			case "2":
//				if(!client.isClosed()) {
//					System.out.println("Frage Status ab...");
//					client.sendData("GetStatus");
//				}else {
//					System.out.println("Verbinde dich erst mit dem Server.\n");
//					continue;
//				}
//				break;
//			case "3":
//				if(!client.isClosed()) {
//					System.out.println("Gib deinen Befehl ein");
//					command = userInput.nextLine();
//					client.sendData(command);
//				}else {
//					System.out.println("Verbinde dich erst mit dem Server.\n");
//					continue;
//				}
//				break;
//			case "4":
//				System.out.println("Programm wird beendet");
//				client.sendData("Quit");
//				client.closeConnection();
//				userInput.close();
//				System.exit(1);
//				break;
//			default:
//				System.out.println("Unbekannte Eingabe");
//				continue;
//			}
//			System.out.print(client.receiveData()+"\n");
//		} while(!command.equals("4"));
//	}
//	
//	/**
//	 * Excercise 3
//	 * - Connects to tracking server
//	 * - logging in
//	 * - get status, Tracking system and Marker 
//	 * - Select Marker
//	 * - continuous marker location updates in Matrix row format
//	 */
//	public static void aufgabe3() {
//		Scanner userInput = new Scanner(System.in);
//		String msg, name;
//		TCPClient client = new TCPClient("127.0.0.1", 5000);
//		
//		if(!client.connect()) {
//			System.exit(1);
//		}
//		
//		client.sendData("CM_GETSYSTEM");
//		msg = client.receiveData();
//		System.out.println(msg);
//		
//		boolean wrong = true;
//		
//		// BUGS:
//		// Wenn beim Tracking-Simulationsserver zuerst ein falscher/nicht existenter Marker ausgewählt wird,
//		// akzeptiert der Simulations-Server keine weiteren Eingaben dieser TCP-Verbindung
//		// mehr und die Verbindung muss neu aufgebaut (Hier das Programm als neu gestartet) werden.
//		do {
//			System.out.println("Waehlen Sie einen Marker aus.");
//			System.out.println("Dazu geben Sie bitte NUR den Namen des Markers ein");
//			
//			name = userInput.nextLine();
//			client.sendData(name);
//			msg = client.receiveData();
//			System.out.println(msg);
//			if(msg.contains("Got No Data")) {
//				System.out.println("Falsch, versuchen Sie es nochmal");
//				wrong = true;
//			}else {
//				wrong = false;
//			}
//		} while(wrong==true);
//		
//		
//		System.out.println("OK! Gleich wird die Positionsmatrix angezeigt");
//		
//		client.sendData("FORMAT_MATRIXROWWISE");
//		msg = client.receiveData();
//		
//		
//		long time = System.currentTimeMillis();
//		System.out.println("Alle 1000ms wird die Matrix aktualisiert. Druecken Sie CTRL+C um zu beenden.\n");
//		while(true) {
//			if(System.currentTimeMillis()-time>1000) {
//				client.sendData("CM_NEXTVALUE");
//				msg = client.receiveData();
//				System.out.println(msg);
//				time = System.currentTimeMillis();
//			}
//		}
//	}

}