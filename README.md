Morfoloģijas servisu kopa
============================

## Priekšnosacījumi
Java 1.25


## Palaišana

- `./webservice.sh`
- `./webservice.sh -port 1234` lai norādītu citu web servisa portu (pēc noklusējuma lieto 8182)
- `./webservice.sh -h` or `./webservice.sh --help` informācijai par pieejamiem servisiem

	
## Piekļūšana

HTTP pieprasījumi, parametri caur URL; ne-ascii burtus, atstarpes un simbolus kodējot atbilstoši [standartam](http://en.wikipedia.org/wiki/Percent-encoding)

Servisiem, kam pieejama valodas izvēle atribūtu kodējumam, pieejamās valodas ir noklusētā latviešu un `en` (angļu).


## Vienmēr pieejamie servisi

### Informācija par vārda morfoloģiju (JSON)

- `http://localhost:8182/analyze/[vārds]`: http://localhost:8182/analyze/roku
- `http://localhost:8182/analyze/en/[vārds]`: http://localhost:8182/analyze/en/roku

Tiek pieņemts, ka parametrs ir viena vienība/vārds. Rezultātā ir JSON masīvs ar vienu vai vairākiem morfoloģiskās analīzes variantiem, kas atbilst šim vārdam.


### Vārdformu ģenerēšana (JSON)

- `http://localhost:8182/inflect/[vārds]`: http://localhost:8182/inflect/rakt
- `http://localhost:8182/inflect/en/[vārds]`: http://localhost:8182/inflect/en/rakt

Tiek pieņemts, ka parametrs ir viens vārds pamatformā, tiek izvadīti visi vārdformas locījumi.

Parmetra `[vārds]` vietā var būt vairāk norāžu par vārda paradigmu un locīšanu (šo informāciju ņem no Tēzaura un lieto Tēzauram):
- `http://localhost:8182/inflect/[vārds]&guess=[true/false]`: http://localhost:8182/inflect/zzzs?guess=false (minēt vai neminēt leksikonā neesošo vārdu locīšanu, pēc noklusējuma min)
- `http://localhost:8182/inflect/[vārds]&paradigm=[paradigma]`: http://localhost:8182/inflect/aita?paradigm=noun-4f
- `http://localhost:8182/inflect/[vārds]&paradigm=[paradigma]&stem1=[nenoteiksmes celms]&stem2=[tagadnes celms]&stem3=[pagātnes celms]`: http://localhost:8182/inflect/aust?paradigm=verb-1&stem1=aus&stem2=aust&stem3=aus (pirmās konjugācijas verbiem vajag celmus, lai pareizi locītu)
- `http://localhost:8182/inflect/[vārds]&paradigm=[paradigma]&inflmisc=[papildus informācija]`: https://localhost:8182/inflect/%C4%BCaudis?paradigm=noun-6a&inflmisc=Daudzskaitlis%2CV%C4%ABrie%C5%A1u_dzimte (atpazītās papildinformācijas vienības ir: `Vīriešu_dzimte`, `Sieviešu_dzimte`, `Daudzskaitlis`, `Vienskaitlis`, `Noliegums`)


### Pieļaujamo paradigmu piemeklētājs (JSON)

- `http://localhost:8182/suitable_paradigm/[lemma]`: http://localhost:8182/suitable_paradigm/desmaizels


### Teikuma sadalīšana vienībās (JSON)

- `http://localhost:8182/tokenize/[teikums]`: http://localhost:8182/tokenize/Šis%20žagaru%20saišķis
- `http://localhost:8182/tokenize/en[teikums]`: http://localhost:8182/tokenize/en/Šis%20žagaru%20saišķis
- POST `http://localhost:8182/tokenize/`

Pārveido teikumu par json masīvu ar vienībām, to pamatformām un tagiem. NB! Šeit netiek lietots statistiskais tageris daudznozīmības nošķiršanai, tāpēc tā ir ar zemu precizitāti; šī vietā ieteicams lietot morphotagger servisu.



## Ar tagotāju pieejamie servisi

Lai lietotu šos servisus, ar serviss jāstartē ar karodziņu karodziņu `-morphotagger`, kas ieslēdz morfoloģiskā tagotāja lietošanu.


### Teikuma morfoloģiskā analīze

- `http://localhost:8182/morphotagger/[teikums]`: http://localhost:8182/morphotagger/Šis%20žagaru%20saišķis

Pārveido teikumu par tab-delimited tabulu, kur katrā rindiņā ir teksta vienība, tās pamatforma un tags, lietojot statistisko morfoloģisko analīzi ticamākās formas izvēlei.

Zināmās problēmas: `http://localhost:8182/morphotagger/[formāts]/[teikums]` nereaģē uz formāta parametru.

### Personvārdu visu locījumformu ģenerēšana, uzrunu veidošanai (JSON)

- `http://localhost:8182/inflect_people/[personas vārds]`: http://localhost:8182/inflect_people/Vaira%20Vīķe-Freiberga
- `http://localhost:8182/inflect_people/en/[personas vārds]&gender=[dzimte]`: http://localhost:8182/inflect_people/en/Baraks%20Obama?gender=m

Katrs vārds tiek locīts neatkarīgi, pieņemot ka tas ir pamatformā. Pieejamās dzimtes ir `m` (vīriešu) un `f` (sieviešu)


### Nosaukumfrāžu locīšana (JSON)

- `http://localhost:8182/inflect_phrase/[frāze]`: http://localhost:8182/inflect_phrase/Latvijas%20Universitātes%20Matemātikas%20un%20informātikas%20institūts
- `http://localhost:8182/inflect_phrase/en/[frāze]`: http://localhost:8182/inflect_phrase/en/Latvijas%20Universitātes%20Matemātikas%20un%20informātikas%20institūts
- `http://localhost:8182/inflect_phrase/[frāze]/category=[kategorija]`: http://localhost:8182/inflect_phrase/prezidente%20Vaira%20Vīķe-Freiberga?category=person

Lokāmajam nosaukumam jābūt pamatformā. Vislabākā rezultāta sasniegšanai ir ļoti ieteicams norādīt frāzes kategoriju:
- `org`, `organization` organizācijām
- `hum`, `person` personvārdiem un amatu nosaukumiem
- `loc`, `location` vietu nosaukumiem


### Nominālu frāžu pamatformu noteikšana (TEKSTS)

- `http://localhost:8182/normalize_phrase/[frāze]`: http://localhost:8182/normalize_phrase/Latvijas%20Siera
- `http://localhost:8182/normalize_phrase/[entītijas vārds]/category=[kategorija]`: http://localhost:8182/normalize_phrase/Andra%20Bērziņa?category=person

Neatbalsta verbālas frāzes. Vislabākā rezultāta sasniegšanai ir ļoti ieteicams norādīt frāzes kategoriju:
- `org`, `organization` organizācijām
- `hum`, `person` personvārdiem un amatu nosaukumiem
- `loc`, `location` vietu nosaukumiem



## Ar transkribētāju pieejamie servisi

Lai lietotu šos servisus, ar serviss jāstartē ar karodziņu karodziņu `-transcription`, kas ieslēdz fonētiskā transkribētāja lietošanu. Jārēķinās, ka šobrīd lietotais transkribētājs ir novecojis, un tiks nākotnē aizvietots ar jaunāku.


### Fonētiskā transkripcija (TEKSTS)

- `http://localhost:8182/transcribe/[frāze]`: http://localhost:8182/phonetic_transcriber/vīrs%20ar%20
- `http://localhost:8182/transcribe/[frāze]?phoneme_set=IPA`: http://localhost:8182/phonetic_transcriber/vīrs%20ar%20?phoneme_set=IPA IPA transkripcijām


## Specializētie šaura lietojuma servisi

### Leksikona pārlāde

Priekšnosacījumi:
- Pieejama _Python_ vide ar `python3` komandu un `psycopg` paku.
- Pieejams _PostgreSQL_ un atbilstošās Tēzaura datubāzes.
- Pieejams leksikona konvertēšanas rīks no  https://github.com/LUMII-AILab/TezaursMorphoDump un tam sakonfigurēta pieeja Tēzaura.
- Serviss iestartēts ar karodziņu `-lexreloader` vai `-lexreloader=[ceļš/uz/TezaursMorphoDump/mapi]`

Tad ar POST pieprasījumu:
- `http://localhost:8182/reload_lexicon/latvian` pārladē latviešu standartvalodas leksikonu (vajag Tēzaura datubāzi)
- `http://localhost:8182/reload_lexicon/latgalian` pārladē latgaliešu valodas leksikonu (vajag LTG Tēzaura datubāzi)

