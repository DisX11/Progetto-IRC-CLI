
public class ThreadElaborazione extends Thread{

    private final ThreadCommunication tC;
	private final Pacchetto pacchetto;

    public ThreadElaborazione(ThreadCommunication creator, Pacchetto p){
        tC=creator;
        this.pacchetto=p;
    }
    @Override
    public void run() {
		ricevi();
    }

    private void ricevi() {
        
		System.out.println("Oggetto ricevuto da "+tC.getClientName()+": " + pacchetto);
		if(pacchetto.getCode()%10==1) tC.setConfermaRicezione(true); //tutti i messaggi **1 sono conferme di avvenuta ricezione
		switch (pacchetto.getCode()) {
			case 110 ->{
				//implementa cambio canale
				tC.invia(new Pacchetto("Richiesta switch channel ricevuta",pacchetto.getCode()+1));
				tC.switchChannel(pacchetto.getMess());
			}
			case 200 -> {
				tC.invia(new Pacchetto("",pacchetto.getCode()+1));
				pacchetto.setMess(tC.getClientName()+" "+pacchetto.getMess());
				tC.messInUscita(pacchetto);
			}
			case 210 -> {
				tC.invia(new Pacchetto("",pacchetto.getCode()+1));
				tC.messInUscita(pacchetto);
			}
			case 301 -> {
				System.out.println("Join alert received by "+tC.getClientName());
			}
			case 310 -> {
				System.out.println(tC.getClientName()+" has requested the participant list of "+tC.getChannel().getNomeChannel()+".");
				tC.invia(new Pacchetto(tC.getChannel().getPartString(),pacchetto.getCode()+1));
			}
			case 320 -> {
				String[] content=pacchetto.getMess().split(" ", 2);//[0]=currentName [1]=requestedName

				if(tC.getChannel().isNomeClientOK(tC, content[1])) {
					System.out.println("Richiesta da "+tC.getClientName()+" di cambio nickname approvata: {"+tC.getClientName()+"} diventa {"+content[1]+"}.");
					tC.setClientName(content[1]);
				} else {
					System.out.println("Richiesta da "+tC.getClientName()+" di cambio nickname non approvata.");
				}
				//risponde con il nome "definitivo" del client
				tC.invia(new Pacchetto(tC.getClientName(),pacchetto.getCode()+1));
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
				tC.invia(new Pacchetto("Richiesta /kick ricevuta.",pacchetto.getCode()+1));
				tC.kick(pacchetto.getMess());
			}
			case 530 -> {
				tC.invia(new Pacchetto("",531));
				String targetName=pacchetto.getMess().split(" ",2)[0];
				int timeSpan=Integer.parseInt(pacchetto.getMess().split(" ",2)[1]);
				System.out.println(tC.getClientName()+" has requested to mute "+targetName+" for "+timeSpan+" seconds.");
				
				if(tC.isAdmin()){
					tC.getChannel().mute(targetName,timeSpan);
				}else{
					tC.invia(new Pacchetto("Privilegi necessari non rilevati. Impossibile eseguire /mute.",340));
				}
			}
		}
            
        
    }
}
