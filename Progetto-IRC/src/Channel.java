import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

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

	private boolean isInChannel(String clientNameToCheck) {
		for (ThreadCommunication thread : clientConnectionList) {
			if(thread.getClientName().equals(clientNameToCheck)) {
				return true;
			}
		}
		return false;
	}

	public void whisper(ThreadCommunication sender, String clientReceiver, Pacchetto pacchetto){
		if(isInChannel(clientReceiver) && sender.getClientName()!=clientReceiver) {
			clientConnectionList.forEach((thread2) -> {
				if (thread2.getClientName().equals(clientReceiver)) {
					thread2.invia(pacchetto);
				}
			});
		} else {
			sender.invia(new Pacchetto("Nome client destinatario errato oppure non presente nel canale.", pacchetto.getCode()+1));
		}
	}

	public boolean isNomeClientOK(ThreadCommunication caller, String requestedName) {
		if(requestedName==null || requestedName.isEmpty() || requestedName.matches("^[^a-zA-Z0-9_]*$") || requestedName.startsWith("Client")) {
			return false;
		} else {
			for (ThreadCommunication thread : clientConnectionList) {
				if(thread.threadId()!=caller.threadId() && thread.getClientName().equals(requestedName)) {
					return false;
				}
			}
		}
		return true;
	}

	public String generaNomeClient() {
		//genero una stringa alfanumerica casuale
		return "Client-"+UUID.randomUUID().toString().replaceAll("_", "").substring(0,5);
	}

	public String getPartString() {
		String s="";
		for (ThreadCommunication thread : clientConnectionList) {
			s+=thread.getClientName()+" ";
		}
		return s;
	}

	public void chiudiSocket(ThreadCommunication threadToClose) {
		clientConnectionList.remove(threadToClose);
		System.out.println("Rimuovo un client dal channel.");
	}
}
