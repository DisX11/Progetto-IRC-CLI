import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
/*import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;*/
import java.util.ArrayList;

public class Channel {
	private final String nomeChannel;
	private ArrayList<ThreadCommunication> clientConnectionList;
	

	public Channel(String nome) {
		super();
		this.clientConnectionList = new ArrayList<ThreadCommunication>();
		this.nomeChannel = nome;
	}

	public String getNomeChannel() { return nomeChannel;}

	public void addClient(Socket clientSocket) {
		clientConnectionList.add(new ThreadCommunication(this, clientSocket));
	}

	public void inoltro(Pacchetto pacchetto, long threadMittenteId) {
		clientConnectionList.forEach((thread) -> {
			if (thread.getId()!=threadMittenteId) {
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
