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
		connetti();
    }

	private void connetti(){
		try {
			Pacchetto pacchetto = (Pacchetto) in.readObject();
			System.out.println("Pacchetto dal client: " + pacchetto);
			if(pacchetto.getCode()==100 && pacchetto.getMess()!=null) {
				clientName=channel.generaNomeClient();
				invia(new Pacchetto(clientName,101));//channel risponde con il nome client generato
				try {
					Thread.sleep(10);
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
            boolean closed=false;
            while (!closed) {
                Pacchetto pacchetto=(Pacchetto) in.readObject();
                System.out.println("Oggetto ricevuto da "+clientName+": " + pacchetto);

				if(pacchetto.getCode()%10==1) confermaRicezione=true; //tutti i messaggi **1 sono conferme di avvenuta ricezione
				switch (pacchetto.getCode()) {
					case 200 -> {
						invia(new Pacchetto("OK",201));
						pacchetto.setMess(clientName+" "+pacchetto.getMess());
						channel.inoltro(pacchetto, this.threadId());
                    }
					case 210 -> {
						invia(new Pacchetto("",211));
						//la conferma o meno dell'invio del pacchetto al ricevente viene gestita da channel.whisper()
						String[] split=pacchetto.getMess().split(" ",2);
						channel.whisper(this, split[0], new Pacchetto(clientName+" "+split[1],pacchetto.getCode()));//invio del whisper al destinatario
                    }
					case 301 -> {
						System.out.println("Join alert received by "+clientName);
					}
					case 310 -> {
						System.out.println(clientName+" has requested the participant list of "+channel.getNomeChannel()+".");
						invia(new Pacchetto(channel.getPartString(),pacchetto.getCode()+1));
					}
					case 320 -> {
						String[] content=pacchetto.getMess().split(" ", 2);//[0]=currentName [1]=requestedName
						if(channel.isNomeClientOK(this, content[1])) {
							System.out.println("Richiesta da "+clientName+" di cambio nickname approvata: {"+clientName+"} diventa {"+content[1]+"}.");
							clientName=content[1];
						} else {
							System.out.println("Richiesta da "+clientName+" di cambio nickname non approvata.");
							clientName=content[0];
						}
						invia(new Pacchetto(clientName,pacchetto.getCode()+1));
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
			//i codici esenti indicano messaggi che non necessitano di conferma della ricezione
			do {
				out.writeObject(pacchetto);
				System.out.println(channel.getNomeChannel()+" invia a "+clientName+": "+pacchetto);
				Thread.sleep(40);
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
