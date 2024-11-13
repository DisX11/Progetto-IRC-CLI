import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ThreadCommunication extends Thread {
	private final Socket clientSocket;
	private Channel channel;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String clientName;
	private boolean confermaRicezione;
	private boolean currentlyMuted;
	private boolean hasAdminRole;
	private boolean closed=false;

	public ThreadCommunication(Channel channel, Socket clientSocket) {
		super();
		this.channel=channel;
		this.clientSocket=clientSocket;
		this.currentlyMuted=false;
		this.hasAdminRole=false;

		try {
			out=new ObjectOutputStream(clientSocket.getOutputStream());
			in = new ObjectInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Impossibile creare gli stream di Input/Output");
			chiudiSocket();
		}

		this.start();
	}

	public String getClientName(){
		return clientName;
	}
	public void setClientName(String newName) {
		this.clientName=newName;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	public Channel getChannel() {
		return channel;
	}

	public void setConfermaRicezione(boolean confermaRicezione) {
		this.confermaRicezione=confermaRicezione;
	}
	
	public void giveAdmin() {
		hasAdminRole=true;
	}
	public void revokeAdmin(){
		hasAdminRole=false;
	}
	public boolean isAdmin(){
		return hasAdminRole;
	}

	@Override
    public void run() {
		connetti();
    }

	private void connetti(){
		try {
			Pacchetto pacchetto = (Pacchetto) in.readObject();
			System.out.println("Pacchetto dal client: " + pacchetto);
			if(pacchetto.getCode()==100 && pacchetto.getMess()!=null) {
				setClientName(channel.generaNomeClient());
				invia(new Pacchetto(clientName,101));//channel risponde con il nome client generato
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				channel.inoltro(new Pacchetto(clientName+" has joined the channel.",300), this.threadId());//segnala agli altri client connessi la sua unione al canale
				ricevi();
			} else {
				chiudiSocket();
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			chiudiSocket();
		}
	}
	
	private void ricevi() {
        try {
            
            while (!closed) {
                Pacchetto pacchetto=(Pacchetto) in.readObject();
				
				ThreadElaborazione t=new ThreadElaborazione(this, pacchetto);
				t.start();
				
			}
        } catch (IOException | ClassNotFoundException e) {
            //e.printStackTrace();
			//System.out.println("Client disconnesso in modo inopportuno.");
			chiudiSocket();
        }
	}

	public void invia(Pacchetto pacchetto) {
		try {
			confermaRicezione=pacchetto.getCode()%10==1;
			//i codici esenti indicano messaggi che non necessitano di conferma della ricezione
			do {
				out.writeObject(pacchetto);
				System.out.println(channel.getNomeChannel()+" invia a "+clientName+": "+pacchetto);
				Thread.sleep(200);
			}while(!confermaRicezione);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void messInUscita(Pacchetto pacchetto) {
		if (!currentlyMuted) {
			switch (pacchetto.getCode()) {
				case 200 -> {
					channel.inoltro(pacchetto, this.threadId());
				}
				case 210 -> {
					String[] split=pacchetto.getMess().split(" ",2);
					channel.whisper(this, split[0], new Pacchetto(clientName+" "+split[1],pacchetto.getCode()));//invio del whisper al destinatario
				}
			}
		} else {
			invia(new Pacchetto("Azione temporaneamente non consentita, sei stato mutato.",330));
		}
	}

	public void mute(boolean bool, int timeSpan) {
		if(!currentlyMuted) {
			invia(new Pacchetto("Sei stato mutato per "+timeSpan+" secondi.",340));
			currentlyMuted=bool;
			Timer timer=new Timer();
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					if(!clientSocket.isClosed()){
						currentlyMuted=false;
						invia(new Pacchetto("Sei stato riattivato.",340));
					}
					
				}
			};
			timer.schedule(task, timeSpan*1000);
		}
	}

	public void switchChannel(String channelName){
		channel.switchChannel(channelName, this);
	}

	public void kick(String clientName) {
		if(hasAdminRole) {
			channel.kick(clientName);
		} else {
			invia(new Pacchetto("Privilegi necessari non rilevati. Impossibile eseguire /kick.",360));
		}
	}

	public void promote(String partialInput) {
		channel.updateAdmin(this, partialInput);
	}

	public void chiudiSocket() {
		channel.chiudiSocket(this);
		if(hasAdminRole)channel.updateAdmin(this, null);
		if(clientSocket.isClosed())return;
		try {
			out.writeObject(new Pacchetto("Connessione terminata.", 411));
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Problemi nella chiusura del socket");
		}
		System.out.println("Chiudo il socket.");
	}

	public String toString() {
		return clientName+" [muted: "+currentlyMuted+"]";
	}

	
}
