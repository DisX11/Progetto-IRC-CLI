import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

public class Channel {
	private final String nomeChannel;
	private ArrayList<ThreadCommunication> clientConnectionList;
	private Server server;

	public Channel(String nome, Server server) {
		super();
		this.clientConnectionList = new ArrayList<>();
		this.nomeChannel = nome;
		this.server=server;
	}

	public String getNomeChannel() { return nomeChannel;}

	public void addClient(Socket clientSocket) {
		ThreadCommunication c = new ThreadCommunication(this, clientSocket);
		if(clientConnectionList.isEmpty())c.giveAdmin();
		clientConnectionList.add(c);
		
	}
	public void addClient(ThreadCommunication client) {
		clientConnectionList.add(client);
		if (!isNomeClientOK(client, client.getClientName())) {
			client.setClientName(generaNomeClient());
		}
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
		if(!sender.getClientName().equals(clientReceiver) && isInChannel(clientReceiver)) {
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
		//se rispetta i requisiti per i nomi client
		if(requestedName==null || requestedName.isEmpty() || requestedName.matches("^[^a-zA-Z0-9_]*$") || requestedName.contains(" ") || requestedName.startsWith("Client")) {
			return false;
		} else {
			//se non è già presente un client con lo stesso nome richiesto
			for (ThreadCommunication thread : clientConnectionList) {
				if(thread.threadId()!=caller.threadId() && thread.getClientName().equals(requestedName)) {
					return false;
				}
			}
		}
		//allora il nome richiesto è accettabile
		return true;
	}

	public String generaNomeClient() {
		//genero una stringa alfanumerica casuale
		return "Client-"+UUID.randomUUID().toString().replaceAll("_", "").substring(0,5);
	}

	public String getPartString() {
		String s="";
		for (ThreadCommunication thread : clientConnectionList) {
			s+=thread.toString()+" ";
		}
		return s;
	}

	public void switchChannel(String channelName, ThreadCommunication caller) {
		server.switchChannel(channelName, caller);
		removeClient(caller);
	}

	private void removeClient(ThreadCommunication caller) {
		clientConnectionList.remove(caller);
		if (clientConnectionList.size()==0) {
			server.removeChannel(this);
		}
	}
	
	/* 
	public void mute(String targetName, int timeSpan) {
		clientConnectionList.forEach((thread) -> {
			if (thread.getClientName().equals(targetName)) {
				thread.mute(true, timeSpan);
			}
		});
	}
	*/

	public void kick(String clientName) {
		clientConnectionList.forEach((thread) -> {
			if(thread.getClientName().equals(clientName)) {
				removeClient(thread);
				return;
			}
		});
	}

	public void updateAdmin(String electedClientName) {
		if(electedClientName==null) {
			clientConnectionList.getFirst().giveAdmin();
		} else {
			clientConnectionList.forEach((thread)->{
				if(thread.getClientName().equals(electedClientName)) {
					thread.giveAdmin();
					return;
				}
			});
		}
	}

	public void chiudiSocket(ThreadCommunication threadToClose) {
		removeClient(threadToClose);
		System.out.println("Rimuovo un client dal channel.");
	}
}
