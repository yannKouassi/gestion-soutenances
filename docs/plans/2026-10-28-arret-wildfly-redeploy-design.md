# Arrêt de WildFly avant redéploiement

## Objectif

Permettre à `.scripts/redeploy.ps1` de relancer WildFly avec les variables SMTP
chargées depuis `.env`, y compris quand une instance écoute déjà sur le port
8080.

## Conception retenue

Réutiliser le test existant sur `127.0.0.1:8080` comme détection de WildFly actif.
Dans ce cas, appeler `bin\jboss-cli.bat --connect --command=":shutdown"` depuis
le répertoire de WildFly, puis attendre que le port 8080 se libère. Après cet
arrêt gracieux, le flux normal démarre WildFly à nouveau ; il hérite alors des
variables importées depuis `.env`.

Si la CLI échoue ou si le port ne se libère pas dans un délai borné, arrêter le
script avec une erreur explicite. Ne pas terminer le processus de force et ne
pas poursuivre le redéploiement sur une instance qui n'a pas été arrêtée.

## Vérification

Vérifier statiquement le flux PowerShell et les chemins construits à partir de
`$WILDFLY`. Éviter de lancer le script pendant la validation, car il démarre
MySQL/WildFly et déploie l'application sur la machine locale.
