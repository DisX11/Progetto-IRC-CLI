
public class ThreadElaborazione extends Thread{

    private final ThreadCommunication tC;
	private final Pacchetto pacchetto;

    public ThreadElaborazione(ThreadCommunication creator, Pacchetto p){
        tC=creator;
        this.pacchetto=p;
    }
    @Override
    public void run() {
		elabora();
    }

    private void elabora() {
        
		System.out.println("Oggetto ricevuto da "+tC.getClientName()+": " + pacchetto);
		
		if(pacchetto.getCode()%10==1) tC.setConfermaRicezione(true); //tutti i messaggi **1 sono conferme di avvenuta ricezione
		
		if(pacchetto.getCode()%10==0 && pacchetto.getCode()!=410){//invia la conferma di ricezione a tutti i messaggi che non sono **0 tranne per la disconnessione
			invia(new Pacchetto("Pacchetto ricevuto",pacchetto.getCode()+1));
		} 
		switch (pacchetto.getCode()) {
			case 110 ->{
				//implementa cambio canale
				tC.switchChannel(pacchetto.getMess());
			}
			case 200 -> {
				tC.messInUscita(pacchetto);
			}
			case 210 -> {
				tC.messInUscita(pacchetto);
			}
			case 301 -> {
				System.out.println("Join alert received by "+tC.getClientName());
			}
			case 310 -> {
				System.out.println(tC.getClientName()+" has requested the '"+pacchetto.getMess()+"' type info of "+tC.getChannel().getNomeChannel()+".");
				//invio informazioni (310)
				invia(new Pacchetto(tC.getChannel().retrieveInfo(pacchetto.getMess()),310));
			}
			case 320 -> {
				String[] content=pacchetto.getMess().split(" ", 2);//[0]=currentName [1]=requestedName

				if(tC.getChannel().isNomeClientOK(tC, content[1])) {
					System.out.println("Richiesta da "+tC.getClientName()+" di cambio nickname approvata: {"+tC.getClientName()+"} diventa {"+content[1]+"}.");
					tC.setClientName(content[1]);
					invia(new Pacchetto(content[1],320));
				} else {
					System.out.println("Richiesta da "+tC.getClientName()+" di cambio nickname non approvata.");
				}
				//risponde con il nome "definitivo" del client
			}
			case 331 -> {
				System.out.println(tC.getClientName()+": "+pacchetto.getMess());
			}
			case 341 -> {
				System.out.println(tC.getClientName()+": "+pacchetto.getMess());
			}
			case 361 -> {
				System.out.println("Conferma ricezione ricevuta di mancati privilegi per /kick.");
			}
			case 410 -> {
				tC.chiudiSocket();
			}
			case 510 -> {
				tC.kick(pacchetto.getMess());
			}
			case 520 -> {
				tC.promote(pacchetto.getMess());
			}
			case 530 -> {
				String targetName=pacchetto.getMess().split(" ",2)[0];
				int timeSpan=Integer.parseInt(pacchetto.getMess().split(" ",2)[1]);
				System.out.println(tC.getClientName()+" has requested to mute "+targetName+" for "+timeSpan+" seconds.");
				
				if(tC.isAdmin()){
					tC.getChannel().mute(targetName,timeSpan);
				}else{
					invia(new Pacchetto("Privilegi necessari non rilevati. Impossibile eseguire /mute.",340));
				}
			}
			case 540 -> {
				
				tC.renameChannel(pacchetto.getMess());
			}
		}        
    }
	private void invia(Pacchetto pacchetto){
		tC.invia(pacchetto);
	}
}
