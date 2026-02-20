# PGR301 DevOps - Kontinuasjonseksamen 2026

## Innlevering

**Lever i Canvas:**

Lag et nytt GitHub repository. Kopier filene fra dette over.

 **VIKTIG: Repository må være PUBLIC**
- Security scanning som dere skal jobbe med (Trivy + GitHub Code Scanning) er gratis for public repositories
- Private repositories krever betaling (betalt funksjon $30 per måned per bruker)

1. Lag en `SVAR.md` med oppgave spesifikke svar. Hva som skal beskrives og leveres er beskrevet per oppgave
2. Lever GitHub Repository URL for ditt repo som PDF eller txt fil.

## Vurderingskriterier

| Oppgave | Poeng | Vurdering |
|---------|-------|-----------|
| Oppgave 1: DevOps-prosess | 20p | Forståelse av teamarbeid, branch-strategi, automatisering |
| Oppgave 2: Docker | 20p | Multi-stage build, image-størrelse, beste praksis |
| Oppgave 3: GitHub Actions + Sikkerhet | 35p | Pipeline-logikk, test-automatisering, Trivy scanning, Docker Hub push |
| Oppgave 4: Teori | 25p | Refleksjon og forståelse av AI + DevOps sammenheng |
| **Total** | **100p** | |

---

## Introduksjon

AI-verktøy som GitHub Copilot, ChatGPT og Claude har revolusjonert hvordan vi utvikler programvare. Det som tidligere tok dager kan nå gjøres på timer. Du kan spørre en AI om å lage en applikasjon, og få fungerende kode tilbake nærmest umiddelbart.

Dette er fantastisk for produktivitet. Men det kommer med en utfordring: høy utviklingshastighet krever også høy kvalitet på prosessene rundt. Når du kan "shippe" kode raskere enn noensinne, blir plutselig spørsmål om testing, code review, deployment-strategier og teamarbeid kritisk viktige.

AI-verktøy er gode til å generere kode, men de er også gode til å introdusere antipatterns i samme hastighet: hardkodede credentials, over-deployment, manglende tester, og suboptimale Docker-images. Det er lett å havne i "vibe coding"-modus hvor fokuset er på å få noe til å fungere, uten å tenke på konsekvensene når teamet vokser.

## Scenario

Du har utviklet en quiz-generator ved hjelp av AI-verktøy. Applikasjonen fungerer og er deployet til produksjon. Men nå skal teamet vokse fra én til 3-5 utviklere, og plutselig blir det tydelig at løsningen mangler etablerte DevOps-prosesser.

Den eksisterende koden har flere problemer:

- Ingen automatiske tester
- Suboptimal Docker-konfigurasjon
- Ingen bruk av container registry
- Ingen GitHub actions workflow

I denne eksamenen skal du etablere en profesjonell DevOps-praksis som gjør at teamet kan samarbeide effektivt, deploye trygt til produksjon, og opprettholde kvalitet selv når utviklingshastigheten er høy.



## Om Applikasjonen

Applikasjonen er et Spring Boot REST API som bruker AWS Bedrock (AWS Nova model) til å generere quiz-spørsmål om DevOps-emner. Gjør deg kjent med kildekoden.
 

### Om AWS Bedrock og Nova-modellene

AWS Bedrock er Amazons plattform for å jobbe med generative AI-modeller. Det som gjør Bedrock interessant er at det fungerer som en **abstraksjon over flere forskjellige AI-modeller** - både Amazons egne modeller og modeller fra tredjeparts-leverandører som **Anthropic (Claude)**, **OpenAI** og andre. Dette betyr at du som utvikler kan bytte mellom ulike modeller uten å måtte endre koden din drastisk, og du får tilgang til et bredt spekter av AI-kapabiliteter gjennom én felles API.

**AWS Nova** er Amazons egen familie av AI-modeller som er optimalisert for ulike use cases. I denne oppgaven bruker vi **Nova Pro**, som er en allsidig multimodal modell med god balanse mellom hastighet, nøyaktighet og kostnad. Nova Pro er spesielt godt egnet til applikasjoner som vår quiz-generator, hvor vi trenger strukturerte svar (JSON-format) basert på komplekse instruksjoner og domenekunnskap.


### Teste applikasjonen

Du kan velge å løse oppgaven i et **GitHub Codespace** eller på egen maskin.

**GitHub Codespaces (anbefalt):**
- I ditt repo
- Klikk på "Code" → "Codespaces" → "Create codespace on main"
- Alt du trenger er forhåndskonfigurert: Java 21, Maven, Docker, AWS CLI
- Port 8080 er automatisk eksponert for testing

**Lokal utvikling:**

For å teste applikasjonen lokalt trenger du:
- Java 21 eller nyere
- Maven 3.9+
- **AWS credentials** med tilgang til AWS Bedrock - Dette blir sendt som melding til studentene på e-post samme dag som eksamen leveres ut

**Sett opp AWS credentials:**

Mac/Linux:
```bash
export AWS_ACCESS_KEY_ID="din-access-key"
export AWS_SECRET_ACCESS_KEY="din-secret-key"
export AWS_REGION="eu-west-1"
```

**Kjør applikasjonen:**
```bash
mvn spring-boot:run
```

**API-endepunkter:**
- `POST /api/quiz/generate` - Generer quiz basert på tema
- `GET /api/quiz/health` - Helsekontroll

**Eksempel:**
```bash
curl -X POST http://localhost:8080/api/quiz/generate \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "docker",
    "count": 5,
    "difficulty": "medium"
  }'
```

Applikasjonen vil returnere en JSON-respons med quiz-spørsmål generert av Nova Pro-modellen. Lek gjerne med koden, 
du kan lage quiz-spørsmål på ulike emner! 

Eksempel response:

```json
{
  "quizId" : "quiz-20260216-235111-bd28cf1c",
  "topic" : "docker",
  "difficulty" : "medium",
  "questionCount" : 5,
  "generatedAt" : "2026-02-16T23:51:11.561332",
  "questions" : [ {
    "id" : 1,
    "question" : "Which command is used to create a new Docker image from a Dockerfile?",
    "options" : [ "A) docker build", "B) docker create", "C) docker run", "D) docker commit" ],
    "correctAnswer" : "A",
    "explanation" : "The 'docker build' command is used to build a Docker image from a Dockerfile."
  }, {
    "id" : 2,
    "question" : "How do you list all running Docker containers?",
    "options" : [ "A) docker ps -a", "B) docker ps", "C) docker list", "D) docker container ls" ],
    "correctAnswer" : "B",
    "explanation" : "The 'docker ps' command lists all running containers. 'docker ps -a' lists all containers, including stopped ones."
  }, {
    "id" : 3,
    "question" : "Which Docker command is used to remove a Docker image?",
    "options" : [ "A) docker rm", "B) docker delete", "C) docker rmi", "D) docker remove" ],
    "correctAnswer" : "C",
    "explanation" : "The 'docker rmi' command is used to remove Docker images. 'docker rm' is used to remove containers."
  }, {
    "id" : 4,
    "question" : "What does the 'EXPOSE' instruction in a Dockerfile do?",
    "options" : [ "A) It opens a port on the host machine", "B) It makes a port available to the container", "C) It maps a port from the container to the host", "D) It sets environment variables" ],
    "correctAnswer" : "B",
    "explanation" : "The 'EXPOSE' instruction in a Dockerfile informs Docker that the container listens on the specified network ports at runtime."
  }, {
    "id" : 5,
    "question" : "Which Docker command is used to start a stopped container?",
    "options" : [ "A) docker unpause", "B) docker start", "C) docker restart", "D) docker resume" ],
    "correctAnswer" : "B",
    "explanation" : "The 'docker start' command is used to start one or more stopped containers."
  } ]
}
```


## Oppgaver

### Oppgave 1: DevOps-prosess for team (20 poeng)

**Situasjon:** Teamet skal vokse fra én til 3-5 utviklere. Det er nødvendig å etablere en arbeidsflyt som sikrer kvalitet og forhindrer problemer.

**Oppgave:** Skriv en beskrivelse (300-500 ord) i `SVAR.md` av hvordan teamet skal jobbe.

**Dekk følgende punkter:**

1. **Branch-strategi**
   - Hvordan skal teamet bruke branches?
   - Når oppretter man en ny branch?
   - Hvordan navngir man branches?

2. **Pull Request-prosess**
   - Når skal man lage en Pull Request?
   - Hvem skal gjennomgå Pull Requests?
   - Hva må være på plass før en Pull Request kan merges?

3. **Branch Protection**
   - Hvilke regler skal main-branch ha?
   - Hvorfor er disse viktige for teamarbeid?

4. **Automatisering**
   - Hvilke automatiske sjekker skal kjøres?
   - Når skal de kjøre (Pull Request, merge, etc.)?

**Leveranse i `SVAR.md`:**
- Beskrivelse av DevOps-prosess
- Gjør nødvendige endringer i ditt repository i henhold til anbefalningene du selv kommer med
- Lever relevante skjermbilde(r) og beskrivelse av konfigurasjon du gjør i GitHub for ditt repo.

---

### Oppgave 2: Dockerize applikasjonen (20 poeng)

**Problemet:** Den eksisterende `Dockerfile` fungerer, men er ikke optimalisert for produksjon.

Nåværende problemer:
- Single-stage build (inkluderer build-verktøy i final image)
- Stor image-størrelse (~700MB)
- `docker run` starter ikke applikasjonen

**Oppgave:** Lag en produksjonsklar Dockerfile med multi-stage build.

**Krav:**
- **Build stage:** Bruk Maven image til å bygge JAR
- **Runtime stage:** Bruk minimal Java runtime 
- Image skal være under 400MB 
- Eksponer port 8080

**Test lokalt:**

Mac/Linux:
```bash
docker build -t quiz-app .
docker run -p 8080:8080 \
  -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID \
  -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY \
  quiz-app
```

**Leveranse:**
- Forbedret `Dockerfile`
- Sammenligning av image-størrelser (før/etter) i `SVAR.md`

---

### Oppgave 3: GitHub Actions-pipeline (35 poeng)

**Om GitHub Actions og tredjeparts actions:**

GitHub Actions er bygget på en **utvidbar arkitektur** som lar deg komponere workflows ved å kombinere egne steg med ferdiglagde actions fra community. Dette gjør at du ikke trenger å bygge alt fra scratch - du kan gjenbruke løsninger som allerede er testet og vedlikeholdt av eksperter.

I denne oppgaven skal du bruke **Trivy** - et open-source sikkerhetsskanner-verktøy fra Aqua Security - som et eksempel på hvordan man integrerer tredjeparts actions. Trivy er tilgjengelig som `aquasecurity/trivy-action` og kan enkelt integreres i din workflow for å skanne container images og dependencies for sårbarheter.

**Trivy Security Scanner:**

Mer om hvordan Trivy, og GitHub Security Scanning fungerer: 

- https://github.com/aquasecurity/trivy-action
- https://aquasecurity.github.io/trivy/

Etter et GitHub actions bygg, kan en rapport se for eksempel slik ut

![img.png](img.png)

**Oppgave:** Lag en profesjonell CI/CD-pipeline med sikkerhetsskanning.

**Krav:**

**På Pull Requests:**
- Kjør unit tests (`mvn test`)
- Bygg applikasjonen (`mvn package`)
- Bygg container image
- Skann filesystem for sårbarheter i dependencies (med Trivy)
- Skann Docker image for sårbarheter (med Trivy)
- Pipeline skal **feile** hvis CRITICAL eller HIGH sårbarheter oppdages
- Generer SARIF-rapport og last opp til GitHub Security tab


**På push til main-branch:**
- Kjør unit tests
- Bygg applikasjonen
- Bygg Docker image
- Push Docker image til Docker Hub
- Tag med commit SHA
- Tag med `latest`

**GitHub Secrets som må konfigureres:**
- `DOCKER_HUB_USERNAME` - Ditt Docker Hub brukernavn
- `DOCKER_HUB_TOKEN` - Docker Hub access token

**Leveranse i `SVAR.md`:**

- Lenke til vellykket workflow-kjøring på main (med Docker push)
- Lenke til vellykket Pull Request-validering (uten Docker push)
- Skjermbilde av GitHub Security tab med Trivy-resultater

---

### Oppgave 4: Teori - DevOps i en AI-akselerert verden (25 poeng)

**Oppgave:** Skriv en refleksjon (400-600 ord) i `SVAR.md`.

**Tema:** "Hvorfor blir DevOps viktigere når AI gjør oss mer produktive?"

AI-verktøy som GitHub Copilot, ChatGPT og Claude har dramatisk økt utviklingshastigheten. Reflekter over hvordan dette påvirker behovet for DevOps-praksis i et team.

Bruk gjerne konkrete eksempler fra dette prosjektet for å illustrere poengene dine.

**Leveranse:**
- Refleksjon i `SVAR.md` (400-600 ord)

---


## Ressurser

**GitHub Actions:**
- https://docs.github.com/en/actions


**Docker Multi-Stage Builds:**
- https://docs.docker.com/build/building/multi-stage/

**Branch Protection:**
- https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches

## Spørsmål

Kontakt faglærer på glenn.bech@gmail.com hvis det er noe som er uklart.
