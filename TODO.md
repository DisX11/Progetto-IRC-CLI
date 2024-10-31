# TODO:
### priorità alta:
- [x] fix confermaRicezione
- [x] ordinamento delle operazioni in metodi opportuni
- [ ] whisper
- [ ] ...
### priorità bassa:
- [x] cambio numero porta
- [ ] Nomi client univoci
- [ ] Cambio canale
- [ ] Canali Multipli
- [ ] Controllo Duplicati
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