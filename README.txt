Morfoloģijas webservisu kopa
----------------------------

Priekšnosacījumi:
	Java 1.7

Palaišana:
	./webservice.sh

Lietošana:
	Entītiju frāžu locīšana pieejama caur URL http://localhost:8182/inflect_phrase/[entītijas vārds], piemēram
		http://localhost:8182/inflect_phrase/Vaira Vīķe-Freiberga?category=person
	Lokāmajam nosaukumam jābūt pamatformā, kategorijas ir ļoti ieteicamas, formā person/org/loc/... (vai no semantiskās DB entitycategories tabulas angliskie nosaukumi)
	Citus iekļautos webservisus skat.  ./webservice.sh --help
