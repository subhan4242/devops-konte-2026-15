# SVAR.md

## Oppgave 1: DevOps-prosess for team

### Branch-strategi

Teamet bruker **GitHub Flow** som branch-strategi. Main-branchen er alltid produksjonsklar og ingen pusher direkte til den. For hver ny feature, bugfix eller oppgave opprettes en egen branch fra main. Branches navngis etter mønsteret `feature/kort-beskrivelse`, `bugfix/kort-beskrivelse` eller `hotfix/kort-beskrivelse`, for eksempel `feature/add-quiz-endpoint` eller `bugfix/fix-null-response`. Dette gjør det enkelt å forstå hva en branch inneholder uten å åpne koden.

### Pull Request-prosess

Når en utvikler er ferdig med arbeidet på sin branch, opprettes en Pull Request (PR) mot main. PR skal alltid opprettes selv om endringen er liten, fordi det gir teamet mulighet til å gjennomgå koden før den merges. Minimum én annen utvikler må godkjenne PR-en før den kan merges. Revieweren sjekker at koden er lesbar, at tester er inkludert og at endringen løser det den skal. Utvikleren som laget PR-en er ansvarlig for å rydde opp i kommentarer og feil før merge.

### Branch Protection

Main-branchen beskyttes med følgende regler i GitHub:
- **Require a pull request before merging** – ingen kan pushe direkte til main
- **Require approvals (minimum 1)** – minst én annen utvikler må godkjenne
- **Require status checks to pass** – alle automatiske tester må være grønne før merge
- **Do not allow bypassing the above settings** – gjelder også admins

Disse reglene er kritiske når teamet vokser. Uten dem er det lett at én utvikler ved et uhell pusher kode som brekker produksjon, eller at kode som ikke er gjennomgått havner i main.

### Automatisering

Følgende automatiske sjekker kjøres via GitHub Actions:

- **På Pull Request:** Kjør unit tester (`mvn test`), bygg applikasjonen (`mvn package`), bygg Docker image, skann for sikkerhetssårbarheter med Trivy. Pipeline feiler hvis CRITICAL eller HIGH sårbarheter oppdages. Dette sikrer at ingen usikker eller ødelagt kode merges inn.
- **På push til main:** Kjør tester, bygg og push Docker image til Docker Hub med commit SHA og `latest` tag. Dette gir en automatisk og sporbar deployment-prosess.

Automatiseringen gjør at teamet ikke er avhengig av manuelle sjekker, og alle endringer går gjennom samme kvalitetsport uavhengig av hvem som har skrevet koden.

### Skjermbilder og konfigurasjon


![alt text](images/image.png)

![alt text](images/image-1.png)

![alt text](images/image-2.png)



## Oppgave 2: Docker multi-stage build

### Endringer i Dockerfile

Den originale Dockerfile brukte single-stage build med Maven-imaget som base,
noe som resulterte i et image på ~700MB siden alle build-verktøy ble med i det finale imaget.
I tillegg manglet den CMD/ENTRYPOINT, så applikasjonen startet ikke ved `docker run`.

Den nye Dockerfile bruker multi-stage build:
- **Stage 1 (build):** Maven image bygger JAR-filen
- **Stage 2 (runtime):** eclipse-temurin:21-jre-alpine kopierer kun JAR-filen

### Sammenligning av image-størrelse

| | Før | Etter |
|---|---|---|
| Base image | maven:3.9.5-eclipse-temurin-21 | eclipse-temurin:21-jre-alpine |
| Image størrelse | ~700MB | 97.2 MB |
| Build-verktøy inkludert | Ja | Nei |
| CMD/ENTRYPOINT | Mangler | Ja |

### Skjermbilder
![alt text](images/image-4.png)
![alt text](images/image-5.png)
---

## Oppgave 3: GitHub Actions pipeline

### Pipeline-beskrivelse

CI/CD-pipelinen er implementert i `.github/workflows/ci.yml` med to jobs:

**På Pull Request (test-and-build):**
- Kjører unit tester med `mvn test`
- Bygger applikasjonen med `mvn package`
- Bygger Docker image
- Scanner filesystem for sårbarheter med Trivy (CRITICAL/HIGH)
- Scanner Docker image med Trivy (CRITICAL/HIGH)
- Pipeline feiler hvis CRITICAL eller HIGH sårbarheter oppdages
- SARIF-rapport lastes opp til GitHub Security-fanen

**På push til main (docker-push):**
- Kjører tester og bygger applikasjonen
- Logger inn på Docker Hub
- Pusher Docker image med `latest` og commit SHA tag

### Lenker

- Vellykket workflow-kjøring på main (med Docker push):(https://github.com/subhan4242/devops-konte-2026-15/actions/runs/22211247372)
- Pull Request med Trivy-scanning:(https://github.com/subhan4242/devops-konte-2026-15/pull/3)

### Skjermbilder
![alt text](images/image-6.png)
![alt text](images/image-7.png)
![alt text](images/image-8.png)
![alt text](images/image-9.png)



## Oppgave 4: Teori - DevOps i en AI-akselerert verden

### Hvorfor blir DevOps viktigere når AI gjør oss mer produktive?

AI-verktøy som GitHub Copilot, ChatGPT og Claude har endret måten vi skriver kode på. 
Det som tidligere tok en hel dag kan nå gjøres på under en time. Du kan beskrive hva 
du vil ha, og få tilbake fungerende kode nesten umiddelbart. Dette prosjektet er selv 
et godt eksempel quiz-applikasjonen ble laget ved hjelp av AI, og det gikk raskt.

Men her er problemet: når man kan lage kode så fort, er det lett å glemme å tenke på 
hva som skjer etterpå. Koden må jo faktisk fungere over tid, og det er der det kan gå 
galt. AI er flink til å skrive kode som ser riktig ut, men den sjekker ikke alltid om 
avhengighetene den bruker har kjente sikkerhetsproblemer. I dette prosjektet fant 
Trivy hele 17 slike problemer i bibliotekene applikasjonen bruker blant annet 
alvorlige feil i Tomcat og Spring Boot som i verste fall kunne la uvedkommende ta 
kontroll over serveren. Ingen hadde nødvendigvis oppdaget dette uten automatisk 
skanning.

Det er nettopp her DevOps kommer inn. Når vi satte opp GitHub Actions til å kjøre 
tester og sikkerhetsskanning automatisk hver gang noen prøver å legge til ny kode, 
fikk vi en automatisk sikkerhetsnet. Pipelinen stopper og gir beskjed hvis noe er galt før koden når produksjon.
Det betyr at teamet kan jobbe raskt uten å være redde for at de slipper gjennom noe farlig.

Arbeidsflyt og samarbeid blir også mye viktigere når teamet vokser og alle bruker AI. 
Tenk deg at fem utviklere alle genererer kode raskt og prøver å legge det inn i samme 
prosjekt uten noen felles regler. Det blir fort kaos. Med en tydelig branch-strategi 
og krav om at noen andre ser over koden din før den merges, holder man styr på hvem 
som gjør hva og man fanger opp feil som man kanskje ikke ser selv. Vi opplevde 
dette på kroppen i dette prosjektet da AWS-nøkler nesten havnet i et offentlig repo. 
En god prosess rundt code review og branch protection hadde stoppet det.

Docker er et annet eksempel på hvordan struktur hjelper når tempoet er høyt. Når 
kode lages raskt av flere personer med ulike datamaskiner og oppsett, kan ting fort 
fungere på én maskin men ikke på en annen. En gjennomtenkt Dockerfile løser det 
problemet ved å pakke applikasjonen på en forutsigbar måte. I dette prosjektet 
krympet vi image-størrelsen fra rundt 700MB til bare 97MB ved å bruke en to-stegs 
byggeprosess noe som gjør det raskere og billigere å kjøre i produksjon.

Konklusjonen er egentlig litt paradoksal: jo raskere AI hjelper oss å skrive kode, 
jo viktigere blir de tingene som bremser oss litt ned som testing, code review og 
automatisert sikkerhetsskanning. AI senker terskelen for å lage noe, men det betyr 
ikke at kvaliteten kommer gratis. DevOps handler om å bygge gode vaner og 
automatiserte sjekker inn i hverdagen, slik at teamet kan holde høy fart over tid 
uten at det går på bekostning av stabilitet og sikkerhet.
