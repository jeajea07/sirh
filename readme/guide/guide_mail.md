# Guide — configurer l'envoi des e-mails (SIRH TeamUp)

Ce guide explique comment chaque membre de l'équipe active l'envoi **réel** des e-mails sur son poste,
sans jamais mettre de mot de passe dans le dépôt Git.

> **En bref :** copier `config/mail.properties.example` vers `~/.sirh/mail.properties`, y mettre une adresse
> et un **mot de passe d'application**, passer `app.mail.actif=true`, redémarrer, vérifier la ligne
> « Envoi des e-mails aux candidats : ACTIF » dans le journal.

---

## 1. Ce qui part par e-mail (et ce qui n'en part pas)

| Destinataire | Canal | Exemples |
|---|---|---|
| **Candidats** (recrutement, Blocs 2 et 3) | **E-mail** | candidature reçue, candidature non retenue, convocation au test technique, convocation / report / résultat d'entretien, offre annulée |
| Personnel interne (RH, DG, Finance, managers, salariés) | **Notification dans l'application** (cloche en haut à droite), jamais d'e-mail | demandes à valider, contrats, plannings, congés, anomalies de pointage… |

Seul `NotificationRecrutementService` envoie des e-mails. Configurer le mail ne change donc rien aux
Blocs 4 à 7.

---

## 2. Comportement par défaut : envoi **simulé**

Sans configuration, `app.mail.actif=false` : aucun e-mail ne part. Le contenu est seulement écrit dans
le journal de l'application :

```text
Envoi des e-mails aux candidats : simulé (app.mail.actif=false) — voir config/mail.properties.example
...
Simulation d'envoi e-mail — objet: Votre dossier est en cours d'étude ; contenu: ...
```

C'est suffisant pour tester les parcours. Activez l'envoi réel seulement si vous devez voir les e-mails
arriver dans une vraie boîte.

> ⚠️ Une notification « envoyée » en mode simulé est enregistrée comme `ENVOYE` : activer le mail plus tard
> **ne renvoie pas** les anciens messages.

---

## 3. Où mettre la configuration

L'application lit, en plus de `application.properties`, un fichier **personnel, hors du dépôt** :

| Système | Emplacement |
|---|---|
| Linux / macOS | `~/.sirh/mail.properties` |
| Windows | `%USERPROFILE%\.sirh\mail.properties` (ex. `C:\Users\vous\.sirh\mail.properties`) |

C'est la ligne `spring.config.import=optional:file:${user.home}/.sirh/mail.properties` de
`application.properties`. Le fichier est **facultatif** : s'il est absent, l'application démarre en mode
simulé. S'il existe, ses valeurs **remplacent** celles de `application.properties`.

Pourquoi un fichier hors du projet ? Parce qu'il contient un secret. Il ne peut pas être commité par erreur,
il survit aux `git pull` et chacun a le sien.

---

## 4. Mise en place pas à pas (Gmail)

### 4.1 Créer un mot de passe d'application

Le mot de passe habituel du compte Google **ne fonctionne pas** en SMTP. Il faut un mot de passe
d'application (16 caractères) :

1. Compte Google → **Sécurité** → activer la **validation en deux étapes** (obligatoire).
2. Toujours dans **Sécurité**, ouvrir **Mots de passe des applications**.
3. Créer un mot de passe nommé par exemple « SIRH local », puis copier les 16 caractères (sans les espaces).

Utilisez de préférence une adresse **dédiée aux tests** plutôt que votre adresse personnelle.

### 4.2 Créer le fichier

Linux / macOS :

```bash
mkdir -p ~/.sirh
cp config/mail.properties.example ~/.sirh/mail.properties
chmod 600 ~/.sirh/mail.properties        # lisible par vous seul
```

Windows (PowerShell) :

```powershell
New-Item -ItemType Directory -Force "$env:USERPROFILE\.sirh"
Copy-Item config\mail.properties.example "$env:USERPROFILE\.sirh\mail.properties"
```

### 4.3 Le renseigner

```properties
app.mail.actif=true
app.mail.expediteur=sirh.tests@gmail.com
app.mail.nom-expediteur=TeamUp Recrutement

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=sirh.tests@gmail.com
spring.mail.password=abcdefghijklmnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

- `app.mail.expediteur` doit être **la même adresse** que `spring.mail.username` : Gmail réécrit ou rejette un autre expéditeur.
- `spring.mail.password` : le mot de passe d'application, **sans espaces**.

### 4.4 Redémarrer et vérifier

Redémarrez l'application (`./scripts/recette-locale.sh run`, ou `start`, ou votre IDE). Au démarrage, le
journal doit afficher :

```text
Envoi des e-mails aux candidats : ACTIF (expéditeur sirh.tests@gmail.com)
```

Si vous voyez encore « simulé », le fichier n'est pas lu (voir §7).

### 4.5 Test de bout en bout

1. Ouvrir la vitrine publique `http://localhost:8080/offres` et choisir une offre publiée.
2. Postuler en indiquant **votre propre adresse e-mail**.
3. Vous devez recevoir « Votre dossier est en cours d'étude » en moins d'une minute. Pensez à regarder dans les **spams**.
4. Dans le journal : `E-mail envoyé au candidat id=… — CANDIDATURE_RECUE`.

---

## 5. Autres fournisseurs

| Fournisseur | `spring.mail.host` | Port | Remarques |
|---|---|---|---|
| Gmail | `smtp.gmail.com` | 587 (STARTTLS) | mot de passe d'application obligatoire |
| Outlook / Office 365 | `smtp.office365.com` | 587 (STARTTLS) | l'authentification SMTP doit être autorisée sur le compte, ce qui est souvent désactivé sur les comptes d'organisation |
| Brevo (ex-Sendinblue) | `smtp-relay.brevo.com` | 587 (STARTTLS) | identifiant et clé SMTP fournis par Brevo ; l'expéditeur doit être validé dans Brevo |
| **Mailtrap** (bac à sable) | `sandbox.smtp.mailtrap.io` | 2525 ou 587 | **recommandé pour l'équipe** : les e-mails sont capturés dans une boîte web Mailtrap et n'arrivent jamais chez de vrais destinataires |

Pour les tests à plusieurs, préférez Mailtrap : aucun risque d'écrire à de vraies personnes depuis les
données de démonstration, et pas de souci de spam.

Pour un serveur en SSL direct (port 465) au lieu de STARTTLS :

```properties
spring.mail.port=465
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.starttls.enable=false
spring.mail.properties.mail.smtp.starttls.required=false
```

---

## 6. Référence des réglages

| Propriété | Variable d'environnement équivalente | Défaut | Rôle |
|---|---|---|---|
| `app.mail.actif` | `APP_MAIL_ACTIF` | `false` | `true` = envoi réel, `false` = simulation dans le journal |
| `app.mail.expediteur` | `APP_MAIL_EXPEDITEUR` | `no-reply@example.invalid` | adresse « De » |
| `app.mail.nom-expediteur` | `APP_MAIL_NOM_EXPEDITEUR` | `TeamUp Recrutement` | nom affiché |
| `spring.mail.host` | `MAIL_HOST` | `localhost` | serveur SMTP |
| `spring.mail.port` | `MAIL_PORT` | `25` | port SMTP |
| `spring.mail.username` | `MAIL_USERNAME` | vide | identifiant SMTP |
| `spring.mail.password` | `MAIL_PASSWORD` | vide | mot de passe SMTP (secret) |
| `spring.mail.properties.mail.smtp.auth` | `MAIL_SMTP_AUTH` | `false` | authentification SMTP |
| `spring.mail.properties.mail.smtp.starttls.enable` / `.required` | `MAIL_SMTP_STARTTLS` | `false` | chiffrement STARTTLS |
| `app.site.url` | `APP_SITE_URL` | `http://localhost:8080` | adresse utilisée dans les liens des e-mails (espace candidat, offres) |
| `app.recrutement.notification-delai-relance-ms` | — | `120000` | intervalle de relance des envois en échec |

**Variables d'environnement ou fichier ?** Sur un poste de développement, utilisez le fichier `~/.sirh/mail.properties`.
Les variables d'environnement servent surtout à un serveur ou un conteneur. Attention : si le fichier existe,
ses valeurs l'emportent sur ces variables.

**Liens dans les e-mails :** avec `localhost`, un lien ne fonctionne que sur votre machine. Pour qu'un
destinataire puisse cliquer, mettez dans `app.site.url` une adresse joignable (IP du poste sur le réseau
local, ou URL publique).

---

## 7. Dépannage

| Symptôme (journal) | Cause probable | Solution |
|---|---|---|
| Toujours « simulé » au démarrage | fichier absent, mal nommé ou mal placé, ou `app.mail.actif` resté à `false` | vérifier le chemin exact (`ls ~/.sirh/mail.properties`) et la valeur, puis redémarrer |
| `AuthenticationFailedException` / `535 Authentication failed` | mot de passe du compte au lieu d'un mot de passe d'application, ou espaces dans le mot de passe | créer un mot de passe d'application (§4.1) et le coller sans espaces |
| `MailConnectException`, `Connection timed out` | port 587 bloqué (réseau de l'université, VPN, pare-feu) | essayer sur un autre réseau (partage de connexion), ou le port 465 en SSL (§5) |
| `Could not convert socket to TLS` | STARTTLS demandé sur un port SSL, ou l'inverse | port 587 + STARTTLS, ou port 465 + `ssl.enable=true` |
| `Échec d'envoi de notification id=… (tentative n)` | erreur SMTP temporaire ou de configuration | l'envoi est retenté toutes les 2 minutes, jusqu'à 5 tentatives ; corriger la configuration puis redémarrer |
| E-mail parti mais jamais reçu | rangé en spam, ou adresse du candidat erronée | regarder les spams ; vérifier l'adresse dans la candidature |

Pour voir l'état des envois en base :

```sql
SELECT id_notification_recrutement, type_notification, statut_envoi, tentatives, erreur, date_envoi
  FROM notification_recrutement
 WHERE canal = 'EMAIL'
 ORDER BY date_creation DESC
 LIMIT 20;
```

`EN_ATTENTE` : pas encore traité ; `ENVOYE` : parti (ou simulé) ; `ECHEC` : la cause est dans `erreur`.
Une notification `ECHEC` ayant atteint 5 tentatives n'est plus retentée.

---

## 8. Règles de sécurité

- **Ne jamais commiter** un mot de passe, ni dans `application.properties`, ni dans `config/`, ni dans un script.
  Seul le modèle `config/mail.properties.example`, sans vrai secret, est versionné.
- Ne partagez pas votre fichier : chacun crée son propre mot de passe d'application.
- En cas de fuite (capture d'écran, message, commit), **révoquez immédiatement** le mot de passe d'application
  dans le compte Google, puis créez-en un nouveau.
- Avec les **données de démonstration**, évitez l'envoi réel vers des adresses qui ne sont pas les vôtres :
  utilisez Mailtrap, ou laissez le mode simulé.
