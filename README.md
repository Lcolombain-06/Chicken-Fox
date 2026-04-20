# Chicken-Fox


## Livrables : (Jalon 1)

 1. Présenter 2 stratégies du joueur ordinateur
	-> description + évaluation des performances + évaluation de l'efficacité
2. Vidéo 5min
	-> présentation de la façon de jouer du mode texte (tuto)
 3. Programmation en mode texte 


## To do list : 

#### Phase 1 : Développement du Modèle
- [ ] Implémenter classes de Pions 
- [ ] Définir la grille du jeu et sa visualisation
- [ ] Logique de déplacement
	- [ ] mouvement renard (toutes directions, saut pour capturer)
	- [ ] mouvements des Poules (avant/côtés uniquement, pas de diagonale/arrière)
- [ ] Conditions de victoire
	- [ ] Detection du blocage du renard (Victoire poule)
	- [ ] Détection du nombre insuffisant de Poules (Victoire Renard)
- [ ] Tests Unitaires (JUnit) : Un test pour chaque règle (déplacement valide/invalide, capture, fin de partie).

#### Phase 2 : Vue et Contrôleur
- [ ] Affichage Console : Créer les classes View pour dessiner le plateau et les pions
- [ ] Analyse des commandes : transforme la ligne de jeu entrée en action
- [ ] Gestion du "Stop" : S'assurer que taper "stop" coupe proprement le programme
- [ ] Mode de jeu & Paramètres : Gérer le choix (H-H, H-IA, IA-IA) via les paramètres de lancement
- [ ] Fichiers d'entrée : Créer les fichiers `.txt` de démonstration (Partie normale, erreurs de syntaxe, mouvements illégaux).

#### Phase 3 : Intelligence Artificielle
- [ ] Stratégie 1 basique
- [ ] Stratégie 2 Avancée
- [ ] Comparaison des 2

#### Phase 4 : Livrables Finaux
- [ ] Rapport Technique (Anglais) : Description des IA, performances, et explications des fichiers de test.
- [ ] Vidéo de Démonstration : Capture d'écran commentée (voix ou sous-titres) présentant les scénarios demandés.

## Règles communes d'organisation

### 1. Gestion de Git (Workflow)

- **Branches :** Une branche par personne (ex: `dev-prenom`) ou par fonctionnalité (ex: `feat-logic-rules`). **Interdiction** de push directement sur la branche `main`.
- **Merge Requests :** Pour fusionner votre code, demandez une relecture par un autre membre du groupe.
- **Commits :** Messages clairs en français.

### 2. Standards de Code & Documentation

- **Suivre l'exemple du prof sur cours-info et utiliser les fonction déjà faite etc...**

- **Langue :** 100% Anglais (nom des variables, méthodes, classes, commentaires et affichage console) et vérifier la syntaxe pour qu'on évite d'avoir une journée correction des commentaires sur 30 milliard de fichiers.
    
- **Auto-Documentation :** Chaque fonction doit avoir une Javadoc succincte expliquant :
    
    - `@param` : Ce qu'elle reçoit.
    - `@return` : Ce qu'elle renvoie.
    - `Logic` : Ce qu'elle fait en une phrase.

- **Tests Unitaires** : Les tests des méthodes seront évalué pour la SAE donc les faire au fur et à mersure (pendant que c'est encore frais dans votre tête), pour qu'on ait pas à les faire au dernier moment en ayant tout oublié. Et aussi pour éviter d'envoyer des versions trop bugger sur la branche principale.

- **Clean Code :** Pas de code "en dur" (nombres magiques). Utilisez des constantes (ex: `BOARD_SIZE = 7`).

### 3. Validation croisée (Quality Check)

- Avant de considérer une tâche comme finie :
    
    1. Le code doit compiler sans erreur.
    2. Le test JUnit associé doit passer au vert.
    3. Un autre membre doit tester la saisie clavier pour vérifier que le programme ne crash pas avec une valeur aberrante.
