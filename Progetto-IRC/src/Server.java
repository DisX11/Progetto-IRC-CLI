import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

public class Server{
	private ServerSocket server;
	private int porta;
	private  Socket clientSocket;
	private ArrayList<Channel> channelList;
	private Channel mainChannel;
	
	public Server(int porta) {
		super();
		this.porta = porta;
		clientSocket=null;
		channelList = new ArrayList<>();
		
		mainChannel = new Channel("MasterChannel", this);
	}
	
	public void avvia() {
		try {
			server=new ServerSocket(porta);
			Channel mainChannel = new Channel("MasterChannel", this);
			channelList.add(mainChannel);

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

	public void switchChannel(String channelName, ThreadCommunication caller) {
		Channel destinationChannel=addChannel(channelName);
		caller.setChannel(destinationChannel);
		destinationChannel.addClient(caller);
	}

	private Channel addChannel(String channelName) {
		//se è presente il channel richiesto, lo restituisce
		for(Channel channel : channelList) {
			if(channel.getNomeChannel().equals(channelName)) {
				return channel;
			}
		}
		//se no viene creato e restituito un nuovo channel
		String newName;
		if(isNomeChannelOK(channelName)) {
			newName=channelName;
		} else {
			newName=generaNomeChannel();
		}
		Channel channel=new Channel(newName, this);
		channelList.add(channel);	
		return channel;
	}

	public void removeChannel(Channel channel){
		if(channel==mainChannel)
		channelList.remove(channel);
	}

	public boolean isNomeChannelOK(String requestedName) {
		//se rispetta i requisiti per i nomi client
		if(requestedName==null || requestedName.isEmpty() || requestedName.matches("^[^a-zA-Z0-9_]*$") || requestedName.contains(" ") || requestedName.startsWith("Client")) {
			return false;
		} else {
			return true;
		}
	}

	public String generaNomeChannel() {
		//genero una stringa alfanumerica casuale
		return "Channel-"+UUID.randomUUID().toString().replaceAll("_", "").substring(0,5);
	}

	public String getChannelsList() {
		String s="";
		for (Channel channel : channelList) {
			s+=channel.getNomeChannel()+"\n";
		}
		return s;
	}
}
