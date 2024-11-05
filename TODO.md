# TODO:
### priorità alta:
- [ ] codice errore server->client (ex. 50)
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
- [ ] Controllo Duplicati
- [ ] Canali Multipli
- [ ] Cambio canale
- [ ] Replica protocollo reale
- [ ] studio protocollo reale (x idee implementazioni)
- [ ] documentazione completa codice
- [ ] documentazione completa protocollo
- [ ] unit testing
- [x] cambio numero porta
- [x] sleep 100 -> 10 ms
- [x] Nomi client univoci
- [ ] ...

## Altro:
- [x] Git
- [x] README e TODO
- [ ] ...

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