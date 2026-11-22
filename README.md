# Police et Voleur (Java, JavaFX)

Jeu de stratégie à deux joueurs sur deux ordinateurs reliés au même réseau Wi-Fi :

- **PC 1** héberge le serveur et joue les **3 policiers** (pions bleus 1, 2, 3).
- **PC 2** se connecte et joue le **voleur** (pion rouge V).

Aucun serveur externe ni hébergement payant : tout passe par `ServerSocket` / `Socket` sur le réseau local.

## Prérequis (sur chacun des deux PC)

- JDK 17 ou plus récent (`java -version`)
- Maven 3.8 ou plus récent (`mvn -version`)
- Une connexion internet la première fois, pour que Maven télécharge JavaFX et JUnit

## Lancer le jeu

```
mvn compile javafx:run
```

ou double-clic sur `run.bat` (Windows) / `./run.sh` (Linux, macOS).

### Partie sur deux PC

1. **PC 1** : cliquer sur « Héberger et attendre le joueur 2 ». L'écran affiche l'adresse IP à donner au joueur 2 (port 5555 par défaut).
2. **PC 2** : saisir cette adresse IP, puis « Rejoindre ».
3. Le voleur joue en premier.

Si la connexion échoue : vérifier que les deux PC sont sur le même Wi-Fi, que l'adresse est bonne, et autoriser Java dans le pare-feu Windows du PC 1 (ou ouvrir le port 5555 en entrée).

Le bouton « Jouer en local » permet de tester seul, avec les deux camps sur le même écran.

## Règles

- Plateau : graphe non orienté de 21 sommets et 36 arêtes, lu sur le dessin de référence (`src/main/resources/board.txt`, plan dans `docs/plateau.png`).
- Départ : voleur au sommet 0 (centre), policiers en 1, 2, 3 (petit cercle). Le départ n'est jamais une capture.
- Les déplacements se font uniquement vers un sommet directement relié, sans saut.
- Le voleur joue en premier, puis les tours alternent. Le déplacement est obligatoire.
- Le voleur ne peut pas aller sur un sommet occupé par un policier.
- Un seul policier se déplace par tour, puis le tour passe au voleur.
- Les policiers peuvent aller partout, petit cercle et centre compris.
- **Le voleur gagne** s'il revient précisément au sommet 0 après en être sorti, sans avoir été capturé.
- **Les policiers gagnent** quand, après leur coup, le voleur n'a plus aucun déplacement possible.

### Hypothèses retenues

Un seul policier bouge par tour : les tours alternent **voleur → 1 policier → voleur → 1 policier…**. Le coup est appliqué dès que le pion et sa destination sont choisis, sans bouton de validation.

Deux policiers ne peuvent pas occuper le même sommet, un policier ne peut pas aller sur la case du voleur (la victoire se fait par blocage), et il n'y a pas de limite de tours.

## Jouer à la souris

- **Voleur** : cliquer sur le pion rouge, puis sur un cercle vert.
- **Policiers** : cliquer sur un pion bleu, puis sur un cercle vert. Le déplacement est enregistré immédiatement et c'est au tour du voleur.
- « Suggestion (Minimax) » propose un coup calculé par l'IA. « Recommencer » relance la partie.

## Architecture

```
src/main/java/com/policevoleur/
  graph/     Graph (liste + matrice d'adjacence), BoardLoader (lit board.txt)
  algo/      Bfs, Minimax, GameModel, GameTreeNode (arbres de jeu du cours)
  game/      GameState, GameEngine (règles, victoires), GameRules, Role, Winner
  ai/        PoliceThiefModel (heuristique basée sur BFS), GameAI (suggestions)
  network/   Protocol, HostSession (serveur), ClientSession, LocalSession, NetworkInfo
  ui/        BoardGeometry, BoardCanvas, GameView, MenuView, PoliceVoleurApp
  Main.java  point d'entrée
src/test/java/   tests JUnit 5 de chaque couche
```

- **Réseau** : protocole texte, une ligne par message. Le serveur fait autorité : il vérifie chaque coup, gère les tours et renvoie l'état complet aux deux PC (voir `Protocol`).
- **Minimax** : implémentation générique conforme au pseudo-code du cours (`Minimax.minimax(modèle, état, profondeur, estMax)`), vérifiée sur les deux arbres du cours. Le mode principal oppose deux humains ; l'IA ne sert qu'à la suggestion.
- **BFS** : parcours, distances et plus court chemin (avec sommets bloqués), utilisés par l'heuristique.

## Tests

```
mvn test
```
