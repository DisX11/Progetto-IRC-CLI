import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ThreadCommunication extends Thread{
	private final Socket clientSocket;
	private final Channel channel;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String clientName;
	private boolean confermaRicezione;

	public ThreadCommunication(Channel channel, Socket clientSocket) {
		super();
		this.channel=channel;
		this.clientSocket=clientSocket;

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
	
	@Override
    public void run() {
		try {
			Pacchetto pacchetto = (Pacchetto) in.readObject();
			System.out.println("Pacchetto dal client: " + pacchetto);
			if(pacchetto.getCode()==100 && pacchetto.getMess()!=null) {
				clientName=pacchetto.getMess();
				invia(new Pacchetto("Connessione stabilita.",101));
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
            boolean closed=false;
            while (!closed) {
                Pacchetto pacchetto=(Pacchetto) in.readObject();
                System.out.println("Oggetto ricevuto da "+clientName+": " + pacchetto);
				switch (pacchetto.getCode()) {
					case 200 -> {
						invia(new Pacchetto("OK",201));
						pacchetto.setMess(clientName+"!"+pacchetto.getMess());
						channel.inoltro(pacchetto, this.getId());
                    }
					case 201 -> {
						confermaRicezione=true;
					}
					case 210 | 211 -> {
						String[] split=pacchetto.getMess().split("!",2);
						channel.whisper(split[0], new Pacchetto(clientName+"!"+split[1],pacchetto.getCode()));
                    }
					case 410 -> {
						chiudiSocket();
						closed = true;
                    }
				}
            }
        } catch (IOException | ClassNotFoundException e) {
            //e.printStackTrace();
			System.out.println("Client disconnesso in modo inopportuno");
			chiudiSocket();
        }
    }

	public void invia(Pacchetto pacchetto) {
		try {
			confermaRicezione=pacchetto.getCode()%10==1;
			//i codici esenti sono le conferme entranti di avvenuta consegna
			do {
				out.writeObject(pacchetto);
				System.out.println(channel.getNomeChannel()+" invia a "+clientName+": "+pacchetto);
				Thread.sleep(100);
			}while(!confermaRicezione);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void chiudiSocket() {
		channel.chiudiSocket(this);
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
}
