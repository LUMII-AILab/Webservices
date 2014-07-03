Morfoloģijas webservisu kopa
============================

## Priekšnosacījumi:
Java 1.7

## Palaišana:
`./webservice.sh`

`./webservice.sh -port 1234` lai norādītu citu web servisa portu (pēc noklusējuma lieto 8182)

`./webservice.sh --help` informācijai par pieejamiem servisiem

Ja ports netiek norādīts, tad pēc noklusējuma lieto 8182
	
## Piekļūšana:
http pieprasījumi, parametri caur URL; ne-ascii burtus, atstarpes un simbolus kodējot atbilstoši [standartam](http://en.wikipedia.org/wiki/Percent-encoding)

## Pieejamie servisi:
### Informācija par vārda morfoloģiju
```
http://localhost:8182/analyze/[vārds]
http://localhost:8182/analyze/roku
```

Tiek pieņemts, ka parametrs ir viena vienība/vārds. Rezultātā ir JSON masīvs ar vienu vai vairākiem morfoloģiskās analīzes variantiem, kas atbilst šim vārdam.

### Vārdformu ģenerēšana
```
http://localhost:8182/inflect/[format]/[vārds]
http://localhost:8182/inflect/rakt```

Tiek pieņemts, ka parametrs ir viens vārds pamatformā, tiek izvadīti visi vārdformas locījumi. Pieejamie izvada formāti ir `json` vai `xml`.

### Entītiju frāžu locīšana
```
http://localhost:8182/inflect_phrase/[entītijas vārds]
http://localhost:8182/inflect_phrase/[entītijas vārds]/category=[kategorija]
http://localhost:8182/inflect_phrase/Vaira Vīķe-Freiberga?category=person
```

Lokāmajam nosaukumam jābūt pamatformā. Korektiem rezultātiem ir ļoti ieteicams norādīt kategorijas:
org|organization
hum|person
loc|location

### Entītiju frāžu pamatformu noteikšana

```
http://localhost:8182/normalize_phrase/[entītijas vārds]
http://localhost:8182/normalize_phrase/[entītijas vārds]/category=[kategorija]
http://localhost:8182/normalize_phrase/Andra Bērziņa?category=person
```

Lokāmajam nosaukumam jābūt pamatformā. Korektiem rezultātiem ir ļoti ieteicams norādīt kategorijas:
org|organization
hum|person
loc|location

### Personvārdu visu locījumformu ģenerēšana, uzrunu veidošanai:
```
http://localhost:8182/inflect_people/[format]/[entītijas vārds]
http://localhost:8182/inflect_people/json/Vaira Vīķe-Freiberga
```

Katrs vārds tiek locīts neatkarīgi, pieņemot ka tas ir pamatformā. Pieejamie izvada formāti ir `json` vai `xml`.

### Teikuma sadalīšana vienībās
```
http://localhost:8182/tokenize/[teikums]
http://localhost:8182/tokenize/Šis žagaru saišķis
```

Pārveido teikumu par json masīvu ar vienībām, to pamatformām un tagiem. NB! Šeit netiek lietots statistiskais tageris daudznozīmības nošķiršanai, tāpēc tā ir ar zemu precizitāti; šī vietā ieteicams lietot morphotagger servisu.

### Teikuma morfoloģiskā analīze
```
http://localhost:8182/morphotagger/[teikums]
http://localhost:8182/morphotagger/Šis žagaru saišķis
```

Pārveido teikumu par tab-delimited tabulu, kur katrā rindiņā ir teksta vienība, tās pamatforma un tags, lietojot statistisko morfoloģisko analīzi ticamākās formas izvēlei.

### Atbalsta rīki verbu valenču anotācijas rīkam
```http://localhost:8182/verbs/[vārds]
http://localhost:8182/verbs/roku
http://localhost:8182/neverbs/[vārds]
http://localhost:8182/neverbs/roku
``` 

Atgriež 'pieļaujamos' valenču veidus šim vārdam, atkarībā no tā vai tas ir 'centrālais' vārds (kas parasti ir verbs) vai necentrālais vārds (kur priekšroku dod analīzes variantiem, kas nav verbi)

### Eksperimentālas iestrādnes
NB! Šobrīd nefunkcionē

LNB vecās drukas tekstu transliterācija
```
http://localhost:8182/normalize/[ruleset]/[vārds]
http://localhost:8182/explain/[vārds]
```

Nosaukto vienību detektēšana (NER)

`http://localhost:8182/ner/[teksts]` 