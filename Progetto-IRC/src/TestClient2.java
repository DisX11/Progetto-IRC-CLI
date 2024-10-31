import java.util.Scanner;

public class TestClient2 {
    public static void main(String[] args) {
        Client client = new Client("localhost", 42069, "Client-B");

        Scanner scanner=new Scanner(System.in);
        String input;
        do {
            System.out.print("Inserisci un nuovo messaggio da inviare al server ('/quit' per terminare): ");
            input = scanner.nextLine();
            if(input.startsWith("/whisper")) {
                String clientReceiver = scanner.nextLine();
                client.invia(new Pacchetto(clientReceiver+"!"+input,210));
            }
            else client.invia(new Pacchetto(input,200));
        } while (!input.equals("/quit"));
        scanner.close();
    }
}
