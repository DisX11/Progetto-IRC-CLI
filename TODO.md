# TODO:
### priorità alta:
- [ ] pulizia codice(print di pacchetti e messaggi, uniformare nomi variabili, ordinamento codici, ecc.)
- [ ] /assign user (pacchetto unico per aggiunta/cambio admin) (520)
- [ ] admin può cambiare nome al channel (?)
- [ ] completare opzioni parametri di /info
- [ ] thread.toString() completo di ruoli
- [ ] nomi channel con prefisso '#'
- [ ] /info admin restituisce il nick dell'admin presente sul canale (?)
- [x] /mute (530)
- [x] fix problema se client quitta prima della fine del mute
- [x] thread mancante in threadCommunication per ricezione conferma (matei)
- [x] assegnazione admin quando il vecchio admin esce
- [x] /kick (510)
- [x] threadCommunication si salva se il proprio client ha o men o i privilegi da admin.
- [x] fix confermaRicezione
- [x] ordinamento delle operazioni in metodi opportuni
- [x] whisper
- [x] codice 310
- [x] codice 320
- [x] disponibilità nomeClient alla connessione
- [x] /nick
- [x] whisper: nome inserito errato/non valido
- [x] codice 300
- [ ] ...

### priorità bassa:
- [ ] [text](https://github.com/inspircd/inspircd/releases/tag/v4.4.0)
- [ ] Controllo Duplicati (?)
- [ ] Replica protocollo reale
- [ ] studio protocollo reale (x idee implementazioni)
- [ ] documentazione completa codice
- [ ] documentazione completa protocollo
- [ ] unit testing
- [x] cambio numero porta
- [x] sleep 100 -> 10 ms
- [x] Nomi client univoci
- [x] Canali Multipli
- [x] Cambio canale
- [ ] ...

## Altro:
- [x] Git
- [x] README e TODO
- [ ] ...
  
---
## dettagli:
ordinamento dei metodi:
    dalla ricezione dentro run al chiamare un metodo ricevi() dentro run [Client]
    dall'inizializzazione di connessione dentro run al chiamare un metodo connetti() [ThreadCommunication]

cambio numero porta:
    la porta 4321 non è nel range di porte libere [49152 - 65535] quindi va cambiata in un'altra es. 42069

codice 300:
    alert avviso entrata di client nei canali
    [conferma delivery: 301]

codice 310:
    /info [partList, all, ...]
    partList: retrieve lista participanti al canale
    all: tutte le info del canale
    [conferma delivery: 311]

codice 320:
    /nick
    (integrato nel codice 100 alla prima connessione)
    il client richiede al canale la validità/disponibilità del nome utente inserito
    se il nome richiesto è accettabile risponde con lo stesso, altrimenti risponde con il vecchio nome
    (fa eccezione l'inizializzazione, tramite codice 100, dove viene forzatamente generato e assegnato un nome client generato casualmente (l'utente potrà poi richiedere di modificarlo in futuro))
    in poche parole, la risposta contiene il nome da assegnare/sovrascrivere al client
    [conferma delivery: 321]

integrità nome client inseriti dall'utente:
    ammette solo a-z A-Z 0-9
    non inizia con 'Client'
    no null o vuoto

codice 330:
    alert mute on/off
    [conferma delivery: 331]

codice 340:
    errore x mute
    [conferma delivery: 341]

codice 350: (DA FARE)
    alert uscita canale
    [conferma delivery: 351]

codice 360:
    errore impossibilità /kick per mancati privilegi
    [conferma delivery: 361]

---
CLASSI DI CODICI

100: connessione
200: messaggistica (client source)
300: retrieve info (server source)
400: disconnessione/errori vari
500: admin



-----
Test doc codici:

100-199 per entrare e connettersi o cambiare, OK
200-290 per messaggi vari nel canale(User-to-User), OK
300-399 per messaggi con o da il server(User-ToFro-Server),OK
410 per uscire e disconnettersi, OK
500-599 per comandi dell'admin del canale(Admin-To-Server), OK

300-399 includono le casistiche di:
richieste di info dal canale,
alert dal canale,
errori dal canale,
informazioni per il canale,
comandi(nick,info,switch,admin?,)