package client;

import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import encrypt.Encipher;

public class Client {
	private static DataInputStream dis;
	private static DataOutputStream dos;
	private static Socket socket;
	private static final String prime ="359334085968622831041960188598043661065388726959079837";
	private static final String primitive = "7";
	private Encipher encipher;
	private String key;
	private static Boolean flagSignIn = false;
	private Scanner sc = new Scanner(System.in);
	private String command = "";
	private char[] pass = null;
	private Console console = null;

	public Client(String ipAdress, int port) {
		try {
			// Establish the connection with server
			Client.socket = new Socket();
			socket.connect(new InetSocketAddress(ipAdress, port), 3000);

			// Obtaining input and out stream
			Client.dis = new DataInputStream(socket.getInputStream());
			Client.dos = new DataOutputStream(socket.getOutputStream());
			
			if(exchangeKey()){
				encipher = new Encipher(this.key);
				// Now main program
				try {
					String select = "0";
					do {
						System.out.println(encipher.decrypted(dis.readUTF()));
						dos.writeUTF(encipher.encrypted((select = sc.nextLine())));
						if(select.equals("1")){
							doSignup();
						} else if(select.equals("2")){
							doLogin();
						} else if(select.equals("3")){
							socket.close();
							System.out.println("Bye!");
							System.exit(0);
						}
						if(flagSignIn) break;
					} while (!flagSignIn || (!select.equals("1") && !select.equals("2") && !select.equals("3")));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Failed to connect to server!");
					if (socket != null) {
						try {
							socket.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						System.out.println("Closed socket! Try again later!");
					}
				}
				// Login success
				if (flagSignIn) {
					Boolean flagConnect = dis.readBoolean();
					if (flagConnect) {
						System.out.println("Signed in!");
						System.out.print("~ $ ");
						while (true) {
							command = sc.nextLine();
							command = String.join("###", command.split("( )+(?=([^\"]*\"[^\"]*\")*[^\"]*$)"));
							dos.writeUTF(encipher.encrypted(command));
							if (command.equals("exit")) {
								System.out.println("Exit!");
								socket.close();
								break;
							} else {
								String plaintext = encipher.decrypted(dis.readUTF());
								System.out.print(plaintext);
							}
						}
					} else {
						System.out.println("Server is overloaded! Try again later!");
					}
					sc.close();
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Failed to connect to server!");
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.out.println("Closed socket! Try again later!");
			}
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void doSignup() {

	}

	private void doLogin(){
		try{
			do {
				sc.reset();
				String usrname;
				System.out.print(encipher.decrypted(dis.readUTF()));
				dos.writeUTF(encipher.encrypted((usrname = sc.nextLine())));

				System.out.print(encipher.decrypted(dis.readUTF()));
				console = System.console();
				pass = console.readPassword();
				String pwd = Arrays.toString(pass);
				pwd = pwd.substring(1, pwd.length() - 1);
				pwd = pwd.replace(", ", "");
				dos.writeUTF(encipher.encrypted(pwd));
				flagSignIn = (Boolean) dis.readBoolean();
				if(flagSignIn == false){
					System.out.print("Username '"+usrname+"' incorect or another user using this account in server.\n");
				} 
			} while(!flagSignIn);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Failed to connect to server!");
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.out.println("Closed socket! Try again later!");
			}
		}
	}

	private boolean exchangeKey(){
		try{
			BigInteger p = new BigInteger(prime);
			BigInteger alpha = new BigInteger(primitive);
			int a = new Random().nextInt(1000);
			dos.writeUTF(alpha.pow(a).mod(p).toString());
			BigInteger b = new BigInteger(dis.readUTF());
			this.key = b.pow(a).mod(p).toString();
			return true;
		}catch (Exception e){
			System.out.println("Error: Exchange key");
			return false;
		}
	}
	

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String IP = null;
		int PORT = -1;
		Scanner scanner;
		do {
			scanner = new Scanner(System.in);
			System.out.println("Input IP's server: ");
			IP = scanner.nextLine();
			scanner.reset();
			System.out.println("Input PORT's server: ");
			PORT = scanner.nextInt();
			if (PORT <= 1024 || PORT > 65535) {
				System.out.println("PORT was out of local_port_range ([1024 -> 65535])");
			}
		} while (PORT <= 1024 || PORT >= 60999);
		new Client(IP, PORT);
	}
}
