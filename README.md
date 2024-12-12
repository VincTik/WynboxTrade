# WynboxTrade

Un plugin de trade avancé pour Minecraft (Spigot 1.20.4)

## Fonctionnalités

- Système de trade sécurisé entre joueurs
- Interface graphique personnalisable
- Échange d'items et d'argent (via Vault)
- Système de compte à rebours pour la validation
- Vérification de la distance entre les joueurs
- Logs des transactions
- Messages entièrement configurables

## Commandes

- `/trade <pseudo>` - Envoyer une demande de trade
- `/tradeaccept [pseudo]` - Accepter une demande de trade
- `/tradedeny [pseudo]` - Refuser une demande de trade

## Permissions

- `wynbox.trade` - Permission de base pour utiliser le système de trade
- `wynbox.trade.money` - Permission pour échanger de l'argent
- `wynbox.trade.bypass.distance` - Permission pour ignorer la distance maximale
- `wynbox.trade.admin` - Permission d'administration

## Configuration

Le plugin est entièrement configurable via le fichier `config.yml`. Vous pouvez modifier :
- La taille de l'interface
- Les messages
- La distance maximale entre les joueurs
- Les paramètres de logging
- Et plus encore...

## Dépendances

- Spigot 1.20.4
- Vault (pour les échanges d'argent)

## Installation

1. Téléchargez la dernière version du plugin
2. Placez le fichier .jar dans le dossier `plugins` de votre serveur
3. Redémarrez votre serveur
4. Configurez le plugin via le fichier `config.yml`

## Support

Pour toute question ou problème, n'hésitez pas à ouvrir une issue sur GitHub.
