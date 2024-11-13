import java.util.Scanner;

public class TestClient2 {
	public static void main(String[] args) {
		

		Scanner scanner=new Scanner(System.in);
		String input;
		do{
			Client client = new Client("localhost", 42069, "");

			do {
				System.out.print("Inserisci un nuovo messaggio da inviare al server ('/quit' per terminare): ");
				input = scanner.nextLine();
				if(client.getSocket().isClosed()){
					
				} else if(input.equals("/?")) {
					System.out.println("--public domain commands--\n/info : Request information of the given type;\n/whisper : Send a direct message;\n/nick : Request to change your nickname;\n/switch : Move to another channel;\n/quit : Disconnect.\n--'admin' role only commands--\n/rename : Rename this channel\n/mute : Prevent someone else to send messages;\n/kick : Remove a user from the channel;\n/promote : Give up the admin role\n(all the commands above are valid in the current channel's domain.)");
				} else if (input.equals("/info ?")) {
					System.out.println("Action: request information of the given type.\nSyntax: /info infoType\nTypes: partList, channelsList, admin, ...");
				} else if (input.equals("/whisper ?")) {
					System.out.println("Action: send a direct message\nSyntax: /whisper recipientName\n(recipientName has to be a valid client name)");	
				} else if (input.equals("/nick ?")) {
					System.out.println("Action: request to change your nickname\nSyntax: /nick newNickname\n(newNickname has to be a valid nickname)");
				} else if(input.equals("/switch ?")) {
					System.out.println("Action: move from the current channel to the requested one\nSyntax: /switch destinationChannelName alreadyExists\n(destinationChannelName has to be a valid channel name)\n(you're going to loose all the activity history of the current channel)");
				} else if(input.equals("/quit ?")) {
					System.out.println("Action: disconnect from the channel\nSyntax: /quit\n(this action cannot be undone)");
				} else if(input.equals("/mute ?")) {
					System.out.println("Action: deny another user to send any kind of messages for a given span of time\nSyntax: /mute targetName timeSpan(seconds)");
				} else if(input.equals("/kick ?")) {
					System.out.println("Action: kick another user out of the current channel\nSyntax: /kick clientName\n(this action cannot be undone)");
				} else if(input.equals("/promote ?")) {
					System.out.println("Action: Give up the admin role in favor of another user\nSyntax: /promote clientName\n(this action cannot be undone)");
				} else if(input.equals("/rename ?")) {
					System.out.println("Action: Change this channel's name\nSyntax: /rename requestedChannelName\n");
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
				} else if(input.startsWith("/switch")) {
					String[] segments = input.split(" ",2);
					client.invia(new Pacchetto(segments[1], 110));
				} else if(input.startsWith("/kick")) {
					String[] segments = input.split(" ",2);
					client.kick(segments[1]);
				} else if(input.startsWith("/promote")) {
					String[] segments = input.split(" ",2);
					client.promote(segments[1]);
				} else if(input.startsWith("/rename")) {
					String[] segments = input.split(" ",2);
					client.renameChannel(segments[1]);
				} else {
					client.invia(new Pacchetto(input,200));
				}

			} while (!input.equals("/quit") && !client.getSocket().isClosed());

			System.out.println("Disconnected, want to reconnect? (Yes/No):");
			input = scanner.nextLine();
			
		}while (input.equalsIgnoreCase("Yes"));
		scanner.close();
	}
}
