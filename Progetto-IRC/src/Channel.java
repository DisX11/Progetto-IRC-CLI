import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.UUID;
public class Channel {
	private final Server server;
	private String nomeChannel;
	private final ArrayList<ThreadCommunication> clientConnectionList;

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
		if(clientConnectionList.isEmpty())client.giveAdmin();
		
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
		} else {//cambio codice in un classe 300
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

	public String retrieveInfo(String type) {
		switch (type) {
			case "partList" -> {
				return getPartList();
            }
			case "channelsList" -> {
				return getChannelsList();
			}
			case "admin" -> {
				return getAdmin();
			}
			default -> {
				return "Wrong syntax for the command. Type not found.";
			}
		}
	}

	private String getPartList() {
		String s="#"+nomeChannel+"\n";
		for (ThreadCommunication thread : clientConnectionList) {
			s+=thread.toString()+"\n";
		}
		return s;
	}

	private String getChannelsList() {
		return server.getChannelsList();
	}

	private String getAdmin() {
		for(ThreadCommunication thread : clientConnectionList) {
			if(thread.isAdmin()) {
				return thread.getClientName();
			}
		}
		return "Admin not found.";
	}

	public void switchChannel(String channelName, ThreadCommunication caller) {
		server.switchChannel(channelName, caller);
		removeClient(caller);
		System.out.println("Rimuovo un client dal channel.");
	}
	
	public void mute(String targetName, int timeSpan) {
		clientConnectionList.forEach((thread) -> {
			if (thread.getClientName().equals(targetName)) {
				thread.mute(true, timeSpan);
			}
		});
	}

	public void kick(String clientName) {
		try {
			clientConnectionList.forEach((thread) -> {
				if(thread.getClientName().equals(clientName)) {
					thread.chiudiSocket();
					return;
				}
			});
		} catch (ConcurrentModificationException e) {
			//
		}
	}

	public void updateAdmin(ThreadCommunication previousAdmin, String electedClientName) {
		
		if(electedClientName==null && !clientConnectionList.isEmpty()) {
			previousAdmin.revokeAdmin();
			clientConnectionList.getFirst().giveAdmin();
		} else {
			clientConnectionList.forEach((thread)->{
				if(thread.getClientName().equals(electedClientName)) {
					previousAdmin.revokeAdmin();
					thread.giveAdmin();
					return;
				}
			});
		}
	}

	public void renameChannel(ThreadCommunication caller, String requestedChannelName) {
		if(this.nomeChannel.equals("MasterChannel")) {
			caller.invia(new Pacchetto("Richiesta /rename non approvata. Impossibile rinominare #MasterChannel", 370));
		} else {
			if(server.isNomeChannelOK(requestedChannelName)) {
				nomeChannel=requestedChannelName;
				caller.invia(new Pacchetto("Richiesta /rename approvata.", 370));
			} else {
				caller.invia(new Pacchetto("Richiesta /rename non approvata.", 370));
			}
		}
	}

	public void removeClient(ThreadCommunication caller) {
		clientConnectionList.remove(caller);
		if (clientConnectionList.isEmpty()) {
			server.removeChannel(this);
		}
	}
}
