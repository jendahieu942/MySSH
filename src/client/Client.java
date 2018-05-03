package client;

import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    private static DataInputStream dis;
    private static DataOutputStream dos;
    private static Socket socket;

    public Client(String ipAdress, int port){
        try {
            // Establish the connection with server
            Client.socket = new Socket();
            socket.connect(new InetSocketAddress(ipAdress, port), 3000);
            
            // Obtaining input and out stream
            Client.dis = new DataInputStream(socket.getInputStream());
            Client.dos = new DataOutputStream(socket.getOutputStream());

            // Now main program
            Scanner sc = new Scanner(System.in);
            Boolean flagSignIn = false;
            String command = "";
            char[] pass = null;
            Console console = null;
            // Login
            do {
                sc.reset();
                System.out.print(dis.readUTF());
                dos.writeUTF(sc.nextLine());
                
                System.out.print(dis.readUTF());
                console = System.console();
                pass = console.readPassword();
                String pwd = Arrays.toString(pass);
                pwd = pwd.substring(1, pwd.length()-1);
                pwd = pwd.replace(", ", "");
                dos.writeUTF(pwd);
                flagSignIn = (Boolean) dis.readBoolean();
            } while(!flagSignIn);

            // Login success
            if(flagSignIn){
                Boolean flagConnect = dis.readBoolean();
                if(flagConnect) {
                    System.out.println("Signed in!");
                    System.out.print("~ $ ");
                    while(true) {
                        command = sc.nextLine();
                        command = String.join("###", command.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)"));
                        dos.writeUTF(command);
                        if(command.equals("exit")) {
                        	System.out.println("Exit!");
                        	socket.close();
                        	break;
                        } else {
                        	System.out.print(dis.readUTF());
                        }
                    }
                } else {
                    System.out.println("Server is overloaded! Try again later!");
                }
                sc.close();
            }
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Failed to connect to server!");
			if(socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.out.println("Closed socket! Try again later!");
			}
		} finally {
			if(socket != null) {
	            try {
	                socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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
	        if(PORT <= 1024 || PORT > 65535) {
	        	System.out.println("PORT was out of local_port_range ([1024 -> 65535])");
	        }
    	} while (PORT <= 1024 || PORT >= 60999);
    	new Client(IP, PORT);
    }
}