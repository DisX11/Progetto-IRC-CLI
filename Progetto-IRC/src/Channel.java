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

	public void whisper(String clientReceiver, Pacchetto pacchetto){
		clientConnectionList.forEach((thread) -> {
			if (thread.getClientName().equals(clientReceiver)) {
				thread.invia(pacchetto);
			}
		});
	}

	public String nomeClientCheck(ThreadCommunication caller, String requestedName) {
		if(!isNomeClientOK(caller, requestedName)) {
			requestedName = generaNomeClient();
			while (!isNomeClientOK(caller, requestedName)) {
				requestedName = generaNomeClient();
			}
		}
		return "Client-"+requestedName;
	}

	private boolean isNomeClientOK(ThreadCommunication caller, String requestedName) {
		if(requestedName==null || requestedName.contains(" ") || requestedName.contains("\\") || requestedName.contains("/") || requestedName.contains("Client-")) { //convertire controlli in regex
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

	private String generaNomeClient() {
		/*genero una stringa alfanumerica casuale
		byte[] array = new byte[5];
		new Random().nextBytes(array);
		String generatedString = new String(array, Charset.forName("UTF-16"));
		return generatedString;*/
		return UUID.randomUUID().toString().replaceAll("_", "").substring(0,5);
	}

	public void chiudiSocket(ThreadCommunication threadToClose) {
		clientConnectionList.remove(threadToClose);
		System.out.println("Rimuovo un client dal channel.");
	}
}
