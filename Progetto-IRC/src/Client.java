import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends Thread{
	private Socket client;
	private String ip;
	private int porta;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String nome;
	private boolean confermaRicezione;

	public Client(String ip, int porta, String nome) {
		super();
		
		this.ip = ip;
		this.porta = porta;
		setNome(nome);
		in=null;
		out=null;
		
		connetti();
	}
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}

	public Socket getSocket() {
        return client;
    }
	
	private void connetti() {
		try {
			client=new Socket(ip,porta);
			out=new ObjectOutputStream(client.getOutputStream());
			in=new ObjectInputStream(client.getInputStream());

			System.out.println("\nDopo essermi connesso al server invio il pacchetto per la richiesta di connessione");
			out.writeObject(new Pacchetto(nome, 100));

			Pacchetto risposta = (Pacchetto) in.readObject();
			System.out.println("Risposta dal server: "+risposta);
			if(risposta.getCode() != 101) {
				System.out.println("Il server non ha approvato la connessione");
				chiudiSocket();
			} else {
				nome=risposta.getMess();
				System.out.println("nome ricevuto: "+nome);
				this.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Problemi di apertura del socket");
			chiudiSocket();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Problemi nella ricezione dell'oggetto ricevuto dal server");
			chiudiSocket();
		}
	}

	@Override
	public void run() {
		ricevi();
	}

	private void ricevi(){
		boolean closed=false;
		while (!closed) {
            try {
				Pacchetto entrata = (Pacchetto) in.readObject();
				
				if(entrata.getCode()%10==1) confermaRicezione=true; //tutti i messaggi **1 sono conferme di avvenuta ricezione
				
				if(entrata.getCode()%10==0){//invia la conferma di ricezione a tutti i messaggi che non sono **0
					invia(new Pacchetto("Pacchetto ricevuto",entrata.getCode()+1));
				}
				switch (entrata.getCode()) {
					case 111 -> {
						System.out.println(entrata.getMess());
					}
					case 200 -> {//entrata messaggio di un altro utente per solo questo utente
						String[] messaggio=entrata.getMess().split(" ", 2);
						entrata.setMess(messaggio[1]);
						System.out.println(messaggio[0]+": "+entrata);
                    }
					case 201 -> {
						System.out.println("risposta server: "+entrata);//debug
					}
					case 210 -> {//entrata messaggio di un altro utente per solo questo utente
						String[] split=entrata.getMess().split(" ",2);
						System.out.println(split[0]+" has whispered to you: "+split[1]);
                    }
					case 211 -> {
						System.out.println("Conferma consegna del whisper: "+entrata);
					}
					case 300 -> {//alert del server per tutti gli utenti
						System.out.println(entrata.getMess());
					}
					case 310 -> {//entrata pacchetto con informazioni precedentemente richieste
						System.err.println(entrata.getMess());
					}
					case 311 -> {//conferma ricezione richiesta /info
						System.err.println(entrata.getMess());
					}
					case 321 -> {
						nome=entrata.getMess();
						System.out.println("Risposta dal server sulla richiesta di cambio nickname. Nome attuale: "+nome);
					}
					case 330 -> {//alert dal server per questo utente
						System.out.println(entrata.getMess());
					}case 340 -> {//errore dal server nell'eseguire l'azione
						System.out.println(entrata.getMess());
					}
					case 350 -> {//errore dal server per mancati privilegi dell'utente nell'eseguire l'azione
						System.out.println(entrata.getMess());
					}
					case 411 -> {//
						System.out.println("Termina comunicazione con: "+entrata);
						chiudiSocket();
						closed=true;
                    }
					case 521 -> {
						System.out.println(entrata.getMess());
					}
					case 531 -> {
						System.out.println(entrata.getMess());
					}
					default -> {
						System.out.println("Codice in entrata non valido: "+entrata);
                    }
				}
            } catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				closed=true;
            }
		}
	}
	//invio pacchetto al server
	private void invia(Pacchetto pacchetto) {
		try {
			confermaRicezione=pacchetto.getCode()%10==1;
			//i codici esenti indicano messaggi che non necessitano di conferma della ricezione
			do {
				out.writeObject(pacchetto);
				System.out.println("invio: " + pacchetto);//debug
				Thread.sleep(200);
			} while (!confermaRicezione);
		} catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

	//generazione pacchetto di messaggio
	public void mess(String message) {
		invia(new Pacchetto(nome+" "+message, 200));
	}

	//generazione pacchetto di whisper
	public void whisper(String message) {
		invia(new Pacchetto(nome+" "+message, 210));
	}
	
	//generazione pacchetto di cambio canale
	public void switchChannel(String channelName) {
		invia(new Pacchetto(channelName, 110));
	}

	//generazione pacchetto di richiesta informazioni
	public void retrieveInfo(String type) {
		invia(new Pacchetto(type,310));
	}

	//generazione pacchetto di cambio nick
	public void changeNick(String newNick) {
		invia(new Pacchetto(nome+" "+newNick, 320));
	}
	
	//generazione pacchetto di kick
	public void kick(String targetName) {
		invia(new Pacchetto(targetName, 510));
	}

	//generazione pacchetto di cambio admin
	public void promote(String electedClientName) {
		invia(new Pacchetto(electedClientName, 520));
	}

	//generazione pacchetto di mute
	public void mute(String targetName, int timeSpan) {
		invia(new Pacchetto(targetName+" "+timeSpan, 530));
	}

	//generazione pacchetto di cambio nome canale
	public void renameChannel(String requestedChannelName) {
		invia(new Pacchetto(requestedChannelName, 540));
	}

	//generazione pacchetto di quit
	public void quit() {
		invia(new Pacchetto("",410));
	}

	private void chiudiSocket() {
		try {
			client.close();
			System.out.println("Chiusura socket.");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Problemi nella chiusura del socket");
			System.exit(1);
		}
	}    
}
