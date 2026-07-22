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

Servisiem, kuru atbildes ir JSON formātā, ir pieejama valodas izvēle atribūtu kodējumam – noklusētā latviešu un `en` (angļu). Servisiem `analyze_*`, `inflect_general_*` un `tokenize_*` ir pieejama leksikonā neiekļauto vārdu minēšanas funkcionalitāte, kas ieslēdzama ar parametru `guess=true` un izslēdzama ar parametru `guess=false`. Ieslēgta minēšana dod papildu variantus neatpazītiem vārdiem, taču nedod jaunus variantus leksikonā iekļautiem vārdiem.

Latviešu standartvalodas servisi par leksonu izmanto tezaurs.lv. Latgaliešu valodas servisi šobrīd tiek aktīvi izstrādāti un pilnveidoti. Tie izmanto ltg.tezaurs.lv un to pašreizējais paradigmu pārklājums ir nepilnīgs (trūkst vairāku svarīgu darbības vārdu grupu).


## Vienmēr pieejamie servisi

### Informācija par vārda morfoloģiju (JSON)

- `http://localhost:8182/analyze_lvs/[vārds]`: http://localhost:8182/analyze_lvs/roku
- `http://localhost:8182/analyze_lvs/en/[vārds]`: http://localhost:8182/analyze_lvs/en/roku
- `http://localhost:8182/analyze_ltg/[vārds]`: http://localhost:8182/analyze_ltg/muosys
- `http://localhost:8182/analyze_ltg/en/[vārds]`: http://localhost:8182/analyze_ltg/en/muosys

Tiek pieņemts, ka parametrs ir viena vienība/vārds. Rezultātā ir JSON masīvs ar vienu vai vairākiem morfoloģiskās analīzes variantiem, kas atbilst šim vārdam. Ar `?guess=[true/false]` var regulēt leksikonā neesošo vārdu minēšanu, pēc noklusējuma nemin.


### Vārdformu ģenerēšana bez papildinformācijas(JSON)

- `http://localhost:8182/inflect_general_lvs/[vārds]`: http://localhost:8182/inflect_general_lvs/rakt
- `http://localhost:8182/inflectinflect_general_lvs/en/[vārds]`: http://localhost:8182/inflect_general_lvs/en/rakt
- `http://localhost:8182/inflect_general_ltg/[vārds]`: http://localhost:8182/inflect_general_lvs/muosa
- `http://localhost:8182/inflectinflect_general_ltg/en/[vārds]`: http://localhost:8182/inflect_general_lvs/en/muosa

Tiek pieņemts, ka parametrs ir viens vārds pamatformā, tiek izvadīti visi vārdformas locījumi. Ar `?guess=[true/false]` var regulēt leksikonā neesošo vārdu minēšanu, pēc noklusējuma nemin.

Zināmās problēmas: homonīmiem atdod vienu variantu.


### Vārdformu ģenerēšana ar papildinformāciju(JSON)

- `http://localhost:8182/inflect_with_data/[vārds]&paradigm=[paradigma]`: http://localhost:8182/inflect_with_data/aita?paradigm=noun-4f
- `http://localhost:8182/inflect_with_data/[vārds]&paradigm=[paradigma]&stem1=[nenoteiksmes celms]&stem2=[tagadnes celms]&stem3=[pagātnes celms]`: http://localhost:8182/inflect_with_data/aust?paradigm=verb-1&stem1=aus&stem2=aust&stem3=aus (pirmās konjugācijas verbiem vajag celmus, lai pareizi locītu)
- `http://localhost:8182/inflect_with_data/[vārds]&paradigm=[paradigma]&inflmisc=[papildus informācija]`: https://localhost:8182/inflect_with_data/%C4%BCaudis?paradigm=noun-6a&inflmisc=Daudzskaitlis%2CV%C4%ABrie%C5%A1u_dzimte (atpazītās papildinformācijas vienības ir: `Vīriešu_dzimte`, `Sieviešu_dzimte`, `Daudzskaitlis`, `Vienskaitlis`, `Noliegums`)

Šo informāciju ņem no Tēzaura un lieto Tēzauram, šim servisam paradigmu norādīt ir **obligāti**.


### Pieļaujamo paradigmu piemeklētājs (JSON)

- `http://localhost:8182/suitable_paradigm_lvs/[lemma]`: http://localhost:8182/suitable_paradigm_lvs/desmaizels
- `http://localhost:8182/suitable_paradigm_ltg/[lemma]`: http://localhost:8182/suitable_paradigm_ltg/desmaizels


### Teikuma sadalīšana vienībās (JSON)

- `http://localhost:8182/tokenize_lvs/[teikums]`: http://localhost:8182/tokenize_lvs/Šis%20žagaru%20saišķis
- `http://localhost:8182/tokenize_lvs/en[teikums]`: http://localhost:8182/tokenize_lvs/en/Šis%20žagaru%20saišķis
- `http://localhost:8182/tokenize_ltg/[teikums]`: http://localhost:8182/tokenize_ltg/meitine%20laseja%20viestuli
- `http://localhost:8182/tokenize_ltg/en[teikums]`: http://localhost:8182/tokenize_ltg/en/meitine%20laseja%20viestuli
- POST `http://localhost:8182/tokenize_lvs/` un `http://localhost:8182/tokenize_ltg/`, dati JSON formātā kā izsaukuma piemērā (NB: pēdējā slīpsvītra pieprasījuma adresē ir obligāta):
```
curl --header "Content-Type: application/json" \
  --request POST \
  --data '{"query": "vīrs ar cirvi", "guess": "true", "language": "en"}' \
  http://localhost:8182/tokenize_lvs/
```

Pārveido teikumu par json masīvu ar vienībām, to pamatformām un tagiem. Ar `?guess=[true/false]` GET parametros vai `"guess": "true"` / `"guess": "false"` POST datos var regulēt leksikonā neesošo vārdu minēšanu, pēc noklusējuma nemin. NB! Šeit netiek lietots statistiskais tageris daudznozīmības nošķiršanai, tāpēc tā ir ar zemu precizitāti; šī vietā ieteicams lietot morphotagger servisu.



## Ar tagotāju pieejamie servisi

Lai lietotu šos servisus, ar serviss jāstartē ar karodziņu karodziņu `-morphotagger`, kas ieslēdz morfoloģiskā tagotāja lietošanu. Tie nav pieejami latgaliešu valodai.


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

Lai lietotu šos servisus, ar serviss jāstartē ar karodziņu karodziņu `-transcription`, kas ieslēdz fonētiskā transkribētāja lietošanu. Jārēķinās, ka šobrīd lietotais transkribētājs ir novecojis, un tiks nākotnē aizvietots ar jaunāku. Tie nav pieejami latgaliešu valodai.


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

