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
				switch (entrata.getCode()) {
					case 111 -> {
						System.out.println(entrata.getMess());
					}
					case 200 -> {
						String[] messaggio=entrata.getMess().split(" ", 2);
						entrata.setMess(messaggio[1]);
						System.out.println(messaggio[0]+": "+entrata);
						invia(new Pacchetto("messaggio ricevuto",entrata.getCode()+1));
                    }
					case 201 -> {
						System.out.println("risposta server: "+entrata);//debug
					}
					case 210 -> {
						String[] split=entrata.getMess().split(" ",2);
						System.out.println(split[0]+" has whispered to you: "+split[1]);
						invia(new Pacchetto("whisper ricevuto",entrata.getCode()+1));
                    }
					case 211 -> {
						System.out.println("Conferma consegna del whisper: "+entrata);
					}
					case 300 -> {
						System.out.println(entrata.getMess());
						invia(new Pacchetto("",entrata.getCode()+1));
					}
					case 311 -> {
						System.err.println("Participants list received:\n"+entrata.getMess().replace(" ", "\n"));
					}
					case 321 -> {
						nome=entrata.getMess();
						System.out.println("Risposta dal server sulla richiesta di cambio nickname. Nome attuale: "+nome);
					}
					/*case 330 -> {
						System.out.println(entrata.getMess());
						invia(new Pacchetto("errore 'muted' ricevuto",entrata.getCode()+1));
					}
					case 340 -> {
						System.out.println(entrata.getMess());
						invia(new Pacchetto("alert 'muted' ricevuto",entrata.getCode()+1));
					}*/
					case 360 -> {
						invia(new Pacchetto("",entrata.getCode()+1));
						System.out.println(entrata.getMess());
					}
					case 411 -> {
						System.out.println("Termina comunicazione con: "+entrata);
						chiudiSocket();
						closed=true;
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
	
	public void invia(Pacchetto pacchetto) {
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

	public void changeNick(String newNick) {
		invia(new Pacchetto(nome+" "+newNick, 320));
	}

	public void retrieveInfo(String type) {
		if(type==null) return;
		switch (type) {
			case "partList" -> {
				invia(new Pacchetto("",310));
                }
			default -> {
				System.err.println("Wrong syntax for the command.");
			}
		}
	}

	public void mute(String targetName, int timeSpan) {
		invia(new Pacchetto(targetName+" "+timeSpan, 530));
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
