
# DAAR Projet 1: Clone of egrep

## Auteurs
Kessal Yacine 21311739
Bouzarkouna Malek 28706508

## Description

Ce projet a pour but de fournir un clone de egrep pour de la recherche de motifs (expressions régulières) dans des fichiers texte. Il utilise deux approches différentes pour le traitement des expressions régulières : 

1. **Algorithme KMP (Knuth-Morris-Pratt)** : Un algorithme efficace pour rechercher des motifs dans des chaînes de caractères sans automates.
2. **Automates finis (NFA/DFA)** : L'expression régulière est d'abord convertie en un automate non déterministe (NFA), puis en un automate déterministe minimal (DFA) pour la recherche de motifs dans le texte.


## Prérequis

Avant de lancer le projet, vous devez disposer de l'environnement suivant :

- **Java JDK 8 ou supérieur** : Le projet est implémenté en Java.
- **Graphviz**  : pour visualiser les automates générés en fichier `.dot`. vous pouvez installer en faisant : sudo apt install graphviz


## Compilation

Pour compiler le projet vous pouvez utiliser les deux scripts mis à votre disposition soit pour :
- **lancer le projet** : ./launch.sh pattern nom_du_fichier
- **lancer les tests** : ./launchTest.sh

**Ramarque** : si votre pattern est une chaine de caractères contenant uniquement des concaténations l'algorithme utilisé est KMP, et si c'est 
une expression régulière ahu-ullman est utilisé. 
