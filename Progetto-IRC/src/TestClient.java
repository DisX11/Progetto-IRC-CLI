import java.util.Scanner;

public class TestClient {
	public static void main(String[] args) {
		Client client = new Client("localhost", 42069, "Client-A");

		Scanner scanner=new Scanner(System.in);
		String input;
		do {
			System.out.print("Inserisci un nuovo messaggio da inviare al server ('/quit' per terminare): ");
			input = scanner.nextLine();
			if(input.startsWith("/whisper")) {
				String[] segments = input.split(" ");
				client.invia(new Pacchetto(segments[1]+"!"+segments[2],200));
			} else if(input.equals("/quit")) {
				client.invia(new Pacchetto("",410));
			} else {
				client.invia(new Pacchetto(input,200));
			}
		} while (!input.equals("/quit"));
		scanner.close();
	}
}
