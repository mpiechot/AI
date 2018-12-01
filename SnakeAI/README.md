# HorstAI
Teamprojekt um eine KI für Snake zu programmieren

## Verwendete Algorithmen:
* Alpha-Beta Pruning
* HamiltonPathfinding(wie auch immer der Algo. richtig heißt)
* A*-Pathfinding Algorithmus

## A*-Pathfinding Algorithmus
Um den kürzesten Weg zum Ziel zu berechnen verwenden wir den A* Algorithmus. 
Dieser läuft mit einer openList und einer closedList. Die openList ist als PriorityQueue 
implementiert, damit wir an erster Stelle immer den besten nächsten Point haben und die 
Liste nicht selbst sortieren müssen. Haben wir den Weg gefunden, können wir anhand der 
closedList den genauen Pfad vom Ziel zurück zum Start bestimmen durch Rekursion.

## Alpha-Beta Pruning
Alpha-Beta Pruning wird verwendet um der KI beizubringen in die Zukunft zu blicken und die
eigenen Optionen abzuwägen. Das heißt wir tun so als ob der Gegner genauso optimal spielt wie
wir und simulieren seine Bewegungen mit. Je nachdem wie viel Züge wie vorraus blicken möchten
simulieren wir diese Züge abwechselnd und bewerten dann das Ergebnis der Züge anhand des daraus
resultierenden Spielbretts. Falls eine Schlange sich vor dem erreichen der Tiefe umbringt,
bewerten wir bereits an diesem Punkt.

Alternativ könnte man auch den MinMax Algorithmus verwenden. Alpha-Beta Pruning ist allerdings die
Verbesserung des MinMax Algorithmus um sehr unwahrscheinliche Züge gar nicht erst zu betrachten.

## HamiltonPathfinding
Falls sich unsere Schlange eingeschlossen hat, möchten wir nicht mehr den kürzesten Pfad bestimmen
sondern den längsten zum Schwanz der Schlange. Diesen Pfad können wir anhand eines Hamilton-Path Algorithmus
berechnen. Außerdem kann man über den Längsten Pfad Situationen vermeiden, in denen man sich einschließt.

## JUnit Tests
Über Unit-Tests testen wir unsere Algorithmen soweit dies möglich ist. Bis jetzt sind das allerdings
noch nicht sehr ausführliche Tests, aber dienen Hauptsächlich dem Zweck selbst mal Unit-Tests geschrieben
zu haben ^^'

## Optimierungsbedarf
Bis jetzt laufen folgende Dinge noch nicht optimal und benötigen Verbesserungen
* Alpha-Beta Pruning (Neue Features einbauen, also ist das ständig auf der Liste ^^)
* Hamilton-Pathfinding (Hier stimmt in der Berechnung noch etwas nicht.)
* Verwendung des Hamilton-Path

## ToDo-Liste 17.5. - 31.5.
* Exception beseitigen (check)
* Optimierung Pfadsuche (check)
* Alpha-Beta Pruning Einsatzmöglichkeiten (check)

## ToDo-Liste 31.5. - 14.6.
* Alpha-Beta korrigieren (check)
* Unit-Tests einfügen (check)
* GIT einrichten (check)
* WallFeature einbauen (check)

## ToDo-Liste 14.6. - 28.6.
* Laufzeitverbesserungen (teilweise check)
* 2 Features einbauen (check)
    1. Schlangen tauschen (check)
    2. Kopf mit Schwanz tauschen (check)
* KI Verbesserungen(NewBrain)