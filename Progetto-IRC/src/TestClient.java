import java.util.Scanner;

public class TestClient {
	public static void main(String[] args) {
		Client client = new Client("10.202.28.19", 42069, "");

		Scanner scanner=new Scanner(System.in);
		String input;
		do {
			System.out.print("Inserisci un nuovo messaggio da inviare al server ('/quit' per terminare): ");
			input = scanner.nextLine();
			if(input.equals("/?")) {
				System.out.println("/quit : Disconnect;\n/whisper : Send a direct message;\n/nick : Request to change your nickname;\n/info : Request information of the given type.\n(all the commands above are valid in the current channel's domain.)");
			} else if (input.equals("/info ?")) {
				System.out.println("Action: request information of the given type.\nSyntax: /info infoType\nTypes: all, partList, ...");			
			} else if (input.equals("/whisper ?")) {
				System.out.println("Action: send a direct message\nSyntax: /whisper recipientName\n(recipientName has to be a valid client name)");	
			} else if (input.equals("/nick ?")) {
				System.out.println("Action: request to change your nickname\nSyntax: /nick newNickname\n(newNickname has to be a valid nickname)");
			} else if(input.equals("/quit")) {
				client.invia(new Pacchetto("",410));
			} else if(input.startsWith("/whisper")) {
				String[] segments = input.split(" ",3);
				client.invia(new Pacchetto(segments[1]+" "+segments[2],210));
			} else if(input.startsWith("/nick")) {
				String[] segments = input.split(" ",2);
				client.changeNick(segments[1]);
			} else if(input.startsWith("/info")) {
				String[] segments = input.split(" ",2);
				client.retrieveInfo(segments[1]);
			} else {
				client.invia(new Pacchetto(input,200));
			}
		} while (!input.equals("/quit"));
		scanner.close();
	}
}
