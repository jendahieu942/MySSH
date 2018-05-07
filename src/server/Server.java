package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.mysql.cj.api.xdevapi.ColumnDefinition.StaticColumnDefinition;

import encrypt.Encipher;

public class Server {
	final int MAX_CLIENTS = 5;
	final int PORT = 6969;
	static int CURRENT_CLIENTS = 0;
	static int COUNT_CLIENTS_WANNA_CONNECT = 0;
	static int COUNT_CLIENTS_CONNECTED = 0;

	private ServerSocket server;

	public Server(String ipAddress) {
		try {
			if (ipAddress != null && !ipAddress.isEmpty()) {
				this.server = new ServerSocket(PORT, MAX_CLIENTS + 1, InetAddress.getByName(ipAddress));
				System.out.println("Server is running on " + ipAddress + "/" + PORT);
			} else {
				this.server = new ServerSocket(PORT, MAX_CLIENTS + 1, InetAddress.getByName("localhost"));
				System.out.println("Server is running on localhost/" + PORT);
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
			while (true) {
				Socket socket = null;

				// Listen to client request
				socket = server.accept();

				System.out.println("*=*=*=*=*=*=* New client want to connect *=*=*=*=*=*=*");
				System.out.println("A client is being want to connect to server");
				System.out.println("Client: " + socket);
				System.out.println("Wating client login ... ");
				System.out.println("*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*");
				COUNT_CLIENTS_WANNA_CONNECT++;

				// Obtaining input and out stream
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

				// create thread to communicate
				Thread t = new ClientHandler(socket, dis, dos);

				// Invoking start() method
				t.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public void stopServer(){
        try {
            System.out.println("Closing server . . . \n");
			server.close();
            System.out.println("Server closed!\n");
		} catch (IOException e) {
			System.out.println("Program exiting . . .");
		}
    }
	private /**
			 * ClientHandler
			 */
	class ClientHandler extends Thread {
		final DataInputStream dis;
		final DataOutputStream dos;
		final Socket socket;
		final String homedir = "/home/jenda";
		private String[] cmd = { "/bin/sh", "-c", "command" };
		private String command = "";
		private String currentDir = "/home/jenda";
		private Runtime runtime;
		private Process process;
		private String namepoint = "~ $ ";
		private BufferedReader bReader;
		private int role;
		private static final String prime ="359334085968622831041960188598043661065388726959079837";
		private static final String primitive = "7";
		private String key;
		private Encipher encipher;

		public ClientHandler(Socket socket, DataInputStream dis, DataOutputStream dos) {
			this.socket = socket;
			this.dis = dis;
			this.dos = dos;
			if(exchangeKey()){
				this.encipher = new Encipher(this.key);
			}
		}

		public void run() {
			// Requiring user login
			Boolean flagSignIN = false;
			do {
				Account ac = new Account("root", "12345");
				try {
					// Get username and password from client
					this.dos.writeUTF(encipher.encrypted("You need login to access into server\nUser name: "));
					String userName = encipher.decrypted(dis.readUTF());
					dos.writeUTF(encipher.encrypted("Password: "));
					String userPass = encipher.decrypted(dis.readUTF());

					if (userName != null && !userName.isEmpty() && ac.userLogin(userName, userPass)) {
						dos.writeBoolean(true);
						flagSignIN = true;
						if (userName.equals("admin")) {
							role = 1;
						} else {
							role = 0;
						}
					} else {
						dos.writeBoolean(false);
						flagSignIN = false;
					}
				} catch (IOException e) {
					System.out.println(".=================== Warning ===================.");
					System.out.println("|Connection to client have just been interupted!|");
					System.out.println("'======================...======================'");
					System.out.println("Numbers of client connecting is: " + CURRENT_CLIENTS);
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
			if (flagSignIN) {
				Boolean flagConnect = false;
				if (CURRENT_CLIENTS < MAX_CLIENTS) {
					CURRENT_CLIENTS++;
					COUNT_CLIENTS_CONNECTED++;
					flagConnect = true;
				} else {
					flagConnect = false;
				}
				try {
					dos.writeBoolean(flagConnect);
				} catch (IOException e3) {
					e3.printStackTrace();
				}
				if (flagConnect) {
					// Connect Success
					System.out.println("_______________________________________________");
					System.out.println("New client has connected is: " + socket);
//					System.out.println("Key: " + this.key);
					System.out.println("Number of client connecting is: " + CURRENT_CLIENTS);

					// Init for request handler
					runtime = Runtime.getRuntime();
					String replyString = "";
					while (true) {
						// Now command handler from client
						try {
							command = encipher.decrypted(dis.readUTF());
							if (command.equals("exit")) {
								System.out.println("Disconneted to client");
								System.out.println("Client: " + socket);
								CURRENT_CLIENTS--;
								System.out.println("Numbers of client connecting is: " + CURRENT_CLIENTS);
								socket.close();
								break;
							} else {
								// Receive command from client here:
								// Encryptophic if need here
								replyString = commandHandler(command);

								// Response command request:
								// Cryptophic here to sent
								dos.writeUTF(encipher.encrypted(replyString));
							}
						} catch (IOException e) {
							System.out.println("Disconneted to client");
							System.out.println("Client: " + socket);
							CURRENT_CLIENTS--;
							System.out.println("Numbers of client connecting is: " + CURRENT_CLIENTS);
							try {
								socket.close();
								break;
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				} else {
					System.out.println("Server do not accept a client on " + socket);
					System.out.println("Reason reject: Server is overloaded");
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
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

		private String commandHandler(String command) {
			String result = "";
			String[] command_arr = command.split("###");

			if (command_arr[0].equals("cd")) {
				result = cd_command(command_arr);
			} else if (command_arr[0].equals("ls")) {
				result = ls_command(command_arr);
			} else if (command_arr[0].equals("pwd")) {
				result = pwd_command(command_arr);
			} else if (command_arr[0].equals("date")) {
				result = date_command(command_arr);
			} else if (command_arr[0].equals("rm")) {
				result = rm_command(command_arr);
			} else if (command_arr[0].equals("mkdir")) {
				result = mkdir_command(command_arr);
			} else if (command_arr[0].equals("echo")){
				result = echo_command(command_arr);
			} else if (command_arr[0].equals("nano")) {
				result = nano_command(command_arr);
			} else if (command_arr[0].equals("stop")) {
				result = stop_command(command_arr);
			} else if (command_arr[0].endsWith("statistic")) {
				result = statisitic_command(command_arr);
			} else if (command_arr[0].isEmpty() || command.isEmpty()) {
				result = "";
			} else {
				result = "bash: " + command_arr[0] + ": Command not found!\n";
			}
			namepoint = currentDir.replace(homedir, "~") + " $ ";
			result += namepoint;
			return result;
		}

		private String statisitic_command(String[] command_arr) {
			String result = "";
			if(role == 1){
				if(command_arr.length==1){
					result = " - Numbers of client tried connect: " + COUNT_CLIENTS_WANNA_CONNECT + "\n";
					result += " - Numbers of client connected: " + COUNT_CLIENTS_CONNECTED + "\n";
					result += " - Numbers of client connecting: " + CURRENT_CLIENTS + "\n";
				} else {
					result = "statistic: Syntax error\n";
				}
			} else {
				result = "Permision denied!\n";
			}
			return result;
		}

		private String stop_command(String[] command_arr) {
			String result = "";
			if(role  == 1){
				if(command_arr.length == 1){
					extracted();
					try {
						if(server.isClosed()){
							this.socket.close();
							result = "Server closed!\n";
							System.exit(0);
						} else {
							result = "Server still running!\n";
						}
					} catch (IOException e) {
						System.out.println("Exit program by admin, you need start manually!\n");
					}
				} else {
					result = "stop: Syntax error\n";
				}
			} else {
				result = "Permision denied!\n";
			}
			return result;
		}

		private void extracted() {
			stopServer();
		}

		private String nano_command(String[] command_arr) {
			String result = "";
			if(getSubdir().contains(command_arr[1])){
				String filepath = currentDir + "/" + command_arr[1];
				try {
					bReader = new BufferedReader(new FileReader(filepath));
					String line = "";
					while((line = bReader.readLine())!= null){
						result += line +"\n";
					}
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				result += "nano: File not found\n";
			}
			return result;
		}

		private String echo_command(String[] command_arr) {
			String result = "";
			if(command_arr.length == 2){
				result = command_arr[1];
			} else if(command_arr.length == 4){
				cmd[2] = "cd " + currentDir + "; echo " + command_arr[1] + " " + command_arr[2] + " " + command_arr[3];
				try {
					process = runtime.exec(cmd);
					int exitvalue = process.waitFor();
					if(exitvalue != 0) {
						bReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
						String line = "";
						while ((line = bReader.readLine())!= null) {
							result += line + "\n";
						}
					}			
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			} else {
				result = "echo: Error syntax\n";
			}
			return result;
		}

		private String mkdir_command(String[] command_arr) {
			String result = "";
			if(command_arr.length == 1){
				result = "mkdir: The directory's name could not be empty\n";
			} else {
				List<String> subdir = getSubdir();
				if(subdir.contains(command_arr[1])){
					result = "mkdir: Directory named " + command_arr[1] +" already exist!\n";
				} else {
					cmd[2] = "cd " + currentDir + "; mkdir " + command_arr[1];
					try {
						process = runtime.exec(cmd);
						if(process.waitFor() != 0 ){
							result = "";
						} else {
							bReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
							String line = "";
							while((line = bReader.readLine()) != null){
								result += line +"\n";
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			return result;
		}

		private String date_command(String[] command_arr) {
			String result = "";
			if (command_arr.length == 1) {
				cmd[2] = "cd " + currentDir + "; date";
				try {
					process = runtime.exec(cmd);
					bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = null;
					while ((line = bReader.readLine()) != null) {
						result += line + "\n";
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				result += "Server do not support this option!\n";
				result += "Usage: \'date\' {To show current server's time}\n";
			}
			return result;
		}

		private String pwd_command(String[] command_arr) {
			String result = "";
			if (command_arr.length == 1) {
				result = currentDir + "\n";
			} else {
				result = "pwd: " + command_arr[1] + ": Invalid option.\n";
				result += "Usage: pwd\n";
			}
			return result;
		}

		private String rm_command(String[] command_arr) {
			String result = "";
			List<String> subdir = getSubdir();
			if (command_arr.length == 1) {
				result = "rm: missing operand\n";
			} else if (command_arr.length == 2) {
				if (command_arr[1].startsWith("-")) {
					if (command_arr[1].equals("-") || command_arr[1].equals("--")) {
						result = "rm: Missing operand\n";
					} else if (command_arr[1].equals("--help")) {
						result = "USAGE: rm [OPTION] ... [FILE] ... \n";
						result += "[OPTION]\n";
						result += "  -d\t     remove empty directory\n";
						result += "  -r, -R   remove directory and their content\n\n";
						result += "By default, rm does not remove directories.  Use the --recursive (-r or -R)\n"
								+ "option to remove each listed directory, too, along with all of its contents.\n\n"
								+ "To remove a file whose name starts with a '-', for example '-foo',\n"
								+ "use one of these commands:\n" + "rm -- -foo\n" + "rm ./-foo\n";
					}
				} else {
					if (subdir.contains(command_arr[1])) {
						cmd[2] = "cd " + currentDir + "; rm " + command_arr[1];
						try {
							process = runtime.exec(cmd);
							int exitvalue = process.waitFor();
							if (exitvalue == 0) {
								result = "";
							} else {
								bReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
								String line = "";
								while ((line = bReader.readLine()) != null) {
									result += line + "\n";
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				if (command_arr[1].startsWith("-")) {
					if (command_arr[1].equals("-d")) {
						if (subdir.contains(command_arr[2])) {
							if ((new File(currentDir + "/" + command_arr[2]).isDirectory())) {
								String realcurrentDir = currentDir.toString();
								currentDir = currentDir + "/" + command_arr[2];
								List<String> subdir1 = getSubdir();
								currentDir = realcurrentDir;
								if (subdir1.size() == 0) {
									cmd[2] = "cd " + currentDir + "; rm -d " + command_arr[2];
									try {
										process = runtime.exec(cmd);
										int exitvalue = process.waitFor();
										if (exitvalue == 0) {
											result = "";
										} else {
											result = "rm: Can not remove this directory.\n";
										}
									} catch (IOException e) {
										e.printStackTrace();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								} else {
									result = "rm: This directory is not empty\n";
									result += "Use \'-r\' or \'-R\' argument to remove non-empty directory.\n";
								}
							} else {
								result = "rm: " + "\'" + command_arr[2] + "\': Is not directory\n";
								result += "\'-d\' is optional argument for directory.\n";
							}
						} else {
							result = "rm: " + command_arr[2] + ": Is not file or directory!\n";
						}
					} else if (command_arr[1].equals("-r") || command_arr[1].equals("-R")) {
						if (subdir.contains(command_arr[2])) {
							cmd[2] = "cd " + currentDir + "; rm " + command_arr[1] + " " + command_arr[2];
							try {
								process = runtime.exec(cmd);
								int exitvalue = process.waitFor();
								if (exitvalue == 0) {
									result = "";
								} else {
									result = "rm: Can not remove " + "\'" + command_arr[2] + "\'\n";
								}
							} catch (IOException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else {
							result = "rm: " + command_arr[2] + ": Is not directory or file.\n";
						}
					}
				} else {
					result = "rm: Error syntax \n";
				}
			}
			return result;
		}

		private String ls_command(String[] command_arr) {
			String result = "";
			if (command_arr.length == 1) {
				List<String> subdir = getSubdir();
				for (String string : subdir) {
					result += string + "\n";
				}
			} else {
				if (command_arr[1].startsWith("-")) {
					cmd[2] = "cd " + currentDir + "; ls " + command_arr[1];
					try {
						process = runtime.exec(cmd);
						int exitvalue = process.waitFor();
						if (exitvalue != 0) {
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
			return result;
		}

		private List<String> getSubdir() {
			List<String> result = new ArrayList<>();
			cmd[2] = "cd " + currentDir + "; ls";
			try {
				process = runtime.exec(cmd);
				bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = null;
				while ((line = bReader.readLine()) != null) {
					result.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		private String cd_command(String[] command_arr) {
			String result = "";
			if (command_arr.length == 1) {
				currentDir = homedir;
			} else {
				List<String> subdir = getSubdir();
				if (command_arr[1].equals(".")) {
					result = "";
				} else if (command_arr[1].startsWith("..")) {
                    if(command_arr[1].equals("..")){
                        if (currentDir.equals(homedir)) {
                            result = "Permission denied! Can't move to this directory.\n";
                        } else {
                            String[] dirpath = currentDir.split("/");
                            currentDir = String.join("/", Arrays.copyOf(dirpath, dirpath.length - 1));
                        }
                    } else {
                        String[] dirpath = command_arr[1].split("/");
                        String[] newdirpath = currentDir.split("/");

						// initial
                        String realcurrentDir = currentDir;
                        String newcurrentDir = String.join("/", Arrays.copyOf(newdirpath, newdirpath.length - 1));
                        currentDir = newcurrentDir;
                        List<String> subdirList = getSubdir();
                        currentDir = realcurrentDir;

						// do check
                        if(dirpath.length == 2){
                            if(subdirList.contains(dirpath[1])){
                                if (!(new File(newcurrentDir + "/" + dirpath[1]).isDirectory())){
                                    result += "cd: That's not directory\n";
                                } else {
                                    currentDir = newcurrentDir + "/" + dirpath[1];
                                }
                            } else {
                                result += "cd: Not found directory\n";
                            }
                        } else {
                            result += "Please, you should move directory step by step\n";
                        }
                    }
				} else if (subdir.contains(command_arr[1])) {
					if (!(new File(currentDir + "/" + command_arr[1]).isDirectory())) {
						result = "error: " + command_arr[1] + ": Not a directory\n";
					} else {
						currentDir += "/" + command_arr[1];
					}
				} else {
					result = "error: " + command_arr[1] + ": No such file or directory\n";
				}
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
