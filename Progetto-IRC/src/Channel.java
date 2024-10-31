import java.net.Socket;
import java.util.ArrayList;

public class Channel {
	private final String nomeChannel;
	private ArrayList<ThreadCommunication> clientConnectionList;
	

	public Channel(String nome) {
		super();
		this.clientConnectionList = new ArrayList<>();
		this.nomeChannel = nome;
	}

	public String getNomeChannel() { return nomeChannel;}

	public void addClient(Socket clientSocket) {
		clientConnectionList.add(new ThreadCommunication(this, clientSocket));
	}

	public void inoltro(Pacchetto pacchetto, long threadMittenteId) {
		clientConnectionList.forEach((thread) -> {
			if (thread.threadId()!=threadMittenteId) {
				thread.invia(pacchetto);
			}
		});
	}

	public void whisper(String clientReceiver, Pacchetto pacchetto){
		clientConnectionList.forEach((thread) -> {
			if (thread.getClientName().equals(clientReceiver)) {
				thread.invia(pacchetto);
			}
		});
	}

	public void chiudiSocket(ThreadCommunication threadToClose) {
		clientConnectionList.remove(threadToClose);
		System.out.println("Rimuovo un client dal channel.");
	}
}
