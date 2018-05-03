package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Server {
    final int MAX_CLIENTS = 5;
    final int PORT = 6969;
    static int CURRENT_CLIENTS = 0;

    private ServerSocket server;

    public Server(String ipAddress) {
		try {
			if(ipAddress != null && !ipAddress.isEmpty()) {
                this.server = new ServerSocket(PORT, MAX_CLIENTS + 1, InetAddress.getByName(ipAddress));
				System.out.println("Server is running on "+ipAddress+"/"+PORT);
			} else {
				this.server = new ServerSocket(PORT, MAX_CLIENTS + 1, InetAddress.getByName("localhost"));
				System.out.println("Server is running on localhost/"+PORT);
			}
		} catch (UnknownHostException e) {
			System.out.println("Have problem with this IP address. Try again!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Have problem with this IP address. Try with localhost!");
			e.printStackTrace();
		}
    }
    
    public void start() {
        try {
			while(true) {
				Socket socket = null;
				
				// Listen to client request
				socket = server.accept();
				
				System.out.println("*=*=*=*=*=*=* New client want to connect *=*=*=*=*=*=*");
				System.out.println("A client is being want to connect to server");
				System.out.println("Client: " + socket);
                System.out.println("Wating client login ... ");
                System.out.println("*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*");
                
                // Obtaining input and out stream
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				
				// create thread to communicate
				Thread t = new ClientHandler(socket, dis, dos);
				
				//Invoking start() method
				t.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private /**
     * ClientHandler
     */
    class ClientHandler extends Thread {
        final 	DataInputStream  dis;
		final 	DataOutputStream dos;
        final 	Socket 			 socket;
        final   String           homedir = "/home/jenda";
        private String[]         cmd = {"/bin/sh","-c","command"};
        private String           command = "";
        private String           currentDir = "/home/jenda";
        private Runtime          runtime;
        private Process          process;
        private String           namepoint = "~ $ ";
        
		public ClientHandler(Socket socket, DataInputStream dis, DataOutputStream dos) {
            this.socket = socket;
			this.dis  = dis;
            this.dos = dos;
        }

        public void run(){
            // Requiring user login 
            Boolean flagSignIN = false;
            do {
                Account ac = new Account("root", "12345");
                try{
                    // Get username and password from client
                    this.dos.writeUTF("You need login to access into server\nUser name: ");
                    String userName = dis.readUTF();
                    dos.writeUTF("Password: ");
                    String userPass = dis.readUTF();
                    
                    if(userName != null && !userName.isEmpty() && ac.userLogin(userName, userPass)){
                        dos.writeBoolean(true);
                        flagSignIN = true;
                    } else {
                    	dos.writeBoolean(false);
                    	flagSignIN = false;
                    }
                } catch (IOException e) {
                    System.out.println(".=================== Warning ===================.");
                    System.out.println("|Connection to client have just been interupted!|");
                    System.out.println("'======================...======================'");
                    System.out.println("Numbers of client connecting is: "+CURRENT_CLIENTS);
                    System.out.println();
                    try {
                        socket.close();
                        break;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } while (!flagSignIN);

            // Login successfull
            if(flagSignIN) {
                Boolean flagConnect = false;
            	if(CURRENT_CLIENTS < MAX_CLIENTS) {
                    CURRENT_CLIENTS++;
                    flagConnect = true;
                } else{
                    flagConnect = false;
                }
                try {
					dos.writeBoolean(flagConnect);
				} catch (IOException e3) {
					e3.printStackTrace();
				}
                if(flagConnect){
                    // Connect Success
                    System.out.println("_______________________________________________");
                    System.out.println("New client has connected is: "+socket);
                    System.out.println("Number of client connecting is: "+CURRENT_CLIENTS);

                    // Init for request handler
                    runtime = Runtime.getRuntime();
                    String replyString = "";
                    while (true) { 
                        // Now command handler from client
                        try {
                            command = dis.readUTF();
                            if(command.equals("exit")){
                                System.out.println("Disconneted to client");
                                System.out.println("Client: "+socket);
                                CURRENT_CLIENTS--;
                                System.out.println("Numbers of client connecting is: "+CURRENT_CLIENTS);
                                socket.close();
                                break;
                            } else {
                            	// Receive command from client here:
                                replyString = commandHandler(command);
                                
                                // Response command request:
                                dos.writeUTF(replyString);
                            }
	                    } catch (IOException e) {
	                        System.out.println("Disconneted to client");
                            System.out.println("Client: "+socket);
                            CURRENT_CLIENTS--;
                            System.out.println("Numbers of client connecting is: "+CURRENT_CLIENTS);
	                        try {
	                            socket.close();
	                            break;
	                        } catch (IOException e1) {
	                            e1.printStackTrace();
	                        }
	                    }
	                }
                } else {
                	System.out.println("Server do not accept a client on "+socket);
                	System.out.println("Reason reject: Server is overloaded");
                	try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
                }
            }
        }

        private String commandHandler(String command) {
            String result = "";
            String[] command_arr = command.split("###");
            BufferedReader bReader;
            if(command_arr[0].equals("cd")){
                if (command_arr.length == 1) {
                    currentDir = homedir;
                } else {
                    List<String> subdir = getSubdir();
                    if(command_arr[1].equals(".")){
                        result = "";
                    } else if(command_arr[1].equals("..")){
                        if(currentDir.equals(homedir)){
                            result = "Permission denied! Can't move to this directory.\n";
                        } else {
                            String[] dirpath = currentDir.split("/");
                            currentDir = String.join("/", Arrays.copyOf(dirpath, dirpath.length-1));
                        }
                    } else if(subdir.contains(command_arr[1])){
                        if(!(new File(currentDir+"/"+command_arr[1]).isDirectory())){
                            result = "error: "+command_arr[1]+": Not a directory\n";
                        } else {
                            currentDir += "/"+command_arr[1];
                        }
                    } else {
                        result = "error: "+ command_arr[1] +": No such file or directory\n";
                    }
                }
            } else if(command_arr[0].equals("ls")){
                if(command_arr.length == 1){
                    List<String> subdir = getSubdir();
                    for(String string : subdir){
                        result += string + "\n";
                    }
                } else {
                    if(command_arr[1].startsWith("-")){
                        cmd[2] = "cd "+currentDir+"; ls "+command_arr[1];
                        try {
                            process = runtime.exec(cmd);
                            int exitvalue = process.waitFor();
                            if(exitvalue != 0) {
                                bReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                                String line = null;
                                while ((line = bReader.readLine()) != null) {
                                    result += line + "\n";
                                }
                            } else {
                                bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                String line = null;
                                while ((line = bReader.readLine()) != null) {
                                    result += line + "\n";
                                }
                            }
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
                    } else {
                        result += "ls: Syntax error\n";
                    }
                }
            } else if(command_arr[0].equals("pwd")){
                if(command_arr.length == 1) {
                    result = currentDir + "\n";
                } else {
                    result = "pwd: "+command_arr[1] + ": Invalid option.\n";
                    result += "Usage: pwd\n";
                }
            } else if(command_arr[0].equals("date")){
                if(command_arr.length == 1) {
                    cmd[2] = "cd "+currentDir+"; date";
                    try {
                        process = runtime.exec(cmd);
                        bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line = null;
                        while((line = bReader.readLine()) != null){
                            result += line + "\n";
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    result += "Server do not support this option!\n";
                    result += "Usage: date {To show current server's time}\n";
                }
            } else if(command_arr[0].equals("rm")){
                
            } else if(command_arr[0].isEmpty() || command.isEmpty()){
                result = "";
            } else {
            	result = "bash: "+command +": Command not found!\n";
            }
            namepoint = currentDir.replace(homedir, "~") + " $ ";
            result += namepoint;
            return result;
        }

        private List<String> getSubdir(){
            List<String> result = new ArrayList<>();
            BufferedReader bReader;
            cmd[2] = "cd "+currentDir+"; ls";
            try {
                process = runtime.exec(cmd);
                bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                while((line = bReader.readLine()) != null){
                    result.add(line);
                }
			} catch (IOException e) {
				e.printStackTrace();
			}
            return result;
        }
    }

    public static void main(String[] args) {
    	Scanner sc = new Scanner(System.in);
    	System.out.println("Input IP address in which server will run on: ");
    	String IPString = sc.nextLine();
    	Server server = new Server(IPString);
        server.start();
        sc.close();
    }
}
