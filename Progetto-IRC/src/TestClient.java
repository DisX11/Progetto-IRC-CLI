import java.util.Scanner;

public class TestClient {
	public static void main(String[] args) {
		Client client = new Client("localhost", 42069, "");

		Scanner scanner=new Scanner(System.in);
		String input;
		do {
			System.out.print("Inserisci un nuovo messaggio da inviare al server ('/quit' per terminare): ");
			input = scanner.nextLine();
			if(input.equals("/?")) {
				System.out.println("/info : Request information of the given type;\n/whisper : Send a direct message;\n/nick : Request to change your nickname;\n/mute : Prevent someone else to send messages;\n/quit : Disconnect.\n(all the commands above are valid in the current channel's domain.)");
			} else if (input.equals("/info ?")) {
				System.out.println("Action: request information of the given type.\nSyntax: /info infoType\nTypes: all, partList, ...");			
			} else if (input.equals("/whisper ?")) {
				System.out.println("Action: send a direct message\nSyntax: /whisper recipientName\n(recipientName has to be a valid client name)");	
			} else if (input.equals("/nick ?")) {
				System.out.println("Action: request to change your nickname\nSyntax: /nick newNickname\n(newNickname has to be a valid nickname)");
			} else if(input.equals("/mute ?")) {
				System.out.println("Action: deny another user to send any kind of messages for a given span of time\nSyntax: /mute targetName timeSpan(seconds)");
			} else if(input.equals("/quit ?")) {
				System.out.println("Action: disconnect from the channel\nSyntax: /quit\n(this action cannot be undone)");
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
			} else if(input.startsWith("/mute")) {
				String[] segments = input.split(" ",3);
				try {
					client.mute(segments[1], Integer.parseInt(segments[2]));
				} catch(NumberFormatException ex) {
					System.out.println("Wrong type input (targetName must be Sting ; timeSpan must be integer).");
				}
			} else {
				client.invia(new Pacchetto(input,200));
			}
		} while (!input.equals("/quit"));
		scanner.close();
	}
}
