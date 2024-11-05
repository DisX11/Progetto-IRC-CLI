import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server{
	private ServerSocket server;
	private int porta;
	private  Socket clientSocket;
	
	public Server(int porta) {
		super();
		this.porta = porta;
		clientSocket=null;
	}
	
	public void avvia() {
		try {
			server=new ServerSocket(porta);
			Channel mainChannel = new Channel("Channel-Z");

			while(true) {
				System.out.println("In attesa di connessioni...");
				
				clientSocket = server.accept();
				mainChannel.addClient(clientSocket);
				System.out.println("Client connesso e aggiunto");
                
                try {
					Thread.sleep(200);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.out.println("Impossibile avviare il server sulla porta "+porta);
			System.exit(1);
			e.printStackTrace();
		}
	}
}
