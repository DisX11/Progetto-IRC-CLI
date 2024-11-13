# TODO:
### priorità alta:
- [ ] pulizia codice(print di pacchetti e messaggi, uniformare nomi variabili, unificazione codici, ecc.)
- [x] unificazione codici in categorie: alert, not permitter per non-admin, errors(sintassi, target not found, ecc.)
- [x] rimozione duplicati del codice di client e ThreadElaborazione(invio conferma ricezione per i messaggi)
- [x] nomi channel con prefisso '#'
- [x] /info admin : restituisce il nick dell'admin presente sul canale (?)
- [x] /info channelsList : restituisce la lista dei canali attivi sul server, per permettere di scegliere/esserne al corrente
- [x] thread.toString() completo di ruoli
- [x] codice 350 : alert uscita canale
- [x] /rename requestedChannelName : admin può cambiare nome al channel (540)
- [x] /promote clientName (520)
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
- [ ] Replica protocollo reale
- [ ] studio protocollo reale (x idee implementazioni)
- [ ] documentazione completa codice
- [ ] documentazione completa protocollo
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
    alerts from server to all users

codice 310:
    /info [partList, all, ...]
    partList: retrieve lista participanti al canale
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
    alerts from server to single user

codice 340:
    errors from server

codice 350: 
    errors of no privileges


---
CLASSI DI CODICI

100-199 per entrare e connettersi o cambiare
200-299 per messaggi vari nel canale(User-to-User)
300-399 per messaggi con o da il server(User-ToFro-Server)
410 per uscire e disconnettersi
500-599 per comandi dell'admin del canale(Admin-To-Server)

300-399 includono le casistiche di:
richieste di info dal canale,
alert dal canale,
errori dal canale,
informazioni per il canale,
comandi(nick,info,switch,admin?,)