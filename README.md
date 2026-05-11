# MojeFinance

MojeFinance is a personal finance management application designed to centralize and monitor private bank
accounts from Czech financial institutions in one place. This is a backend part of the project.

## Bank Sandbox Registrations

To aggregate data from Czech banks, you must register as a developer on their respective portals to obtain Sandbox
credentials (Client ID and Client Secret). Once obtained, these credentials need to be configured in
`client-registration.yaml` file.

1. **Česká spořitelna**: Register at the [Erste Developer Portal](https://developers.erstegroup.com/).
2. **ČSOB**: Register at the [ČSOB Developer Portal](https://developers.csob.cz/#/).
3. **Komerční banka (KB)**: Register at the [KB Developer Portal](https://developers.kb.cz/).
4. **Air Bank**: Go to
   the [Air Bank Open API Portal](https://www.airbank.cz/open-api/open-api-account-info-v7-0.html#header-sandbox-1) and
   copy the provided Sandbox Client ID and Secret.

After gathering your credentials, pass them into your `client-registration.yaml` file like this:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          ceska-sporitelna:
            client-id: your_cs_client_id_here
            client-secret: your_cs_client_secret_here
          csob:
            client-id: your_csob_client_id_here
            client-secret: your_csob_client_secret_here
          kb:
            client-id: your_kb_client_id_here
            client-secret: your_kb_client_secret_here
          air-bank:
            client-id: your_airbank_client_id_here
            client-secret: your_airbank_client_secret_here
```

### External API Keys

Beyond the standard OAuth2 credentials, banking APIs require specific API keys
passed via headers. You must configure these in `external-api-specification.yaml` file:

1. **Česká spořitelna**: Copy the `apikey` from your Erste Developer Portal project.
2. **Raiffeisenbank**: Copy the `x-ibm-client-id` from the Raiffeisenbank portal.
3. **ČSOB**: Copy the `apikey` and `tpp-name` from your ČSOB Developer Portal project.
4. **Komerční banka (Exchange Rates)**: Go to the KB Developer Portal, navigate to the Exchange Rates API, copy the
   specific `apikey`, and pass it into the `exchange-rates` section.
5. **Google Gemini (AI Categorization)**: Register at the [Gemini Developer Portal](https://ai.google.dev/) to obtain an
   API key. Pass this key into the `google` section.

Update your `external-api-specification.yaml` accordingly:

```yaml
external:
  api:
    ceska-sporitelna:
      apikey: your_cs_api_key
    raiffeisenbank:
      x-ibm-client-id: your_raif_client_id
    csob:
      apikey: your_csob_api_key
      tpp-name: your_csob_tpp_name
    exchange-rates:
      kb:
        apikey: your_kb_exchange_rate_api_key
    google:
      gemini:
        apikey: your_gemini_api_key
```

## Docker

You have to install and run Docker Desktop. You will find all information you need on this
link: https://docs.docker.com/desktop/setup/install/windows-install/.

Before running the containers, you need to configure your database credentials and Keycloak, see the sections below.:

Open the `docker-compose.yml` file.
Under the postgres-db service, fill in your desired Postgres username and password.
Open your `application-docker.yml` file and copy those exact same credentials into the datasource configuration:

  ```yaml
spring:
  datasource:
    username: your_db_username
    password: your_db_password
```

When you have Docker Desktop running, go to `docker-compose.yml` and run all services in there. If everything was done
correctly, you will see successfully started and running containers. You can access the backend API at
`http://localhost:8081/mojefinance/api`.

## Keycloak

To securely connect your backend to Keycloak, you need to retrieve and configure your client credentials:

1. Ensure your keycloak Docker service is running, if not start it in `docker-compose.yml`, then open your browser and
   visit `http://localhost:8080` to enter the
   Keycloak dashboard.
2. Log in with your Keycloak admin credentials.
3. Select your designated realm and navigate to the **Clients** section.
4. Open the settings for your backend client and copy the **Client ID** and **Client Secret**.
5. Open your `application-docker.yml` file and paste these values into the following configuration path:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        opaque-token:
          client-id: your_client_id_here
          client-secret: your_client_secret_here
```

## Maven

<b>Intellij Idea uses Bundled Maven so you can skip this part if you use this IDE.
But you still can download and use your own maven in Intellij.
You can set it up in File -> Settings -> Build Tools -> Maven.</b>
<br></br>

1. Install maven from here https://maven.apache.org/download.cgi, extract files from archive.
2. Add the ```bin``` directory of the created directory ```apache-maven-3.9.11``` to the ```PATH``` environment
   variable.
3. Set the ```MAVEN_HOME``` environment variable to the path of your maven installation.
4. The result should look similar to:
   <br>MAVEN_HOME: /apache-maven-3.9.11
   <br>PATH: /apache-maven-3.9.11/bin</br>

You should restart your computer after editing environment variables.

## OpenJDK

<b>If you use Intellij Idea you can download jdk easily in the File -> Project Structure -> SDK.</b>
<br></br>

1. Install OpenJDK 17 from here https://jdk.java.net/archive/, extract files from archive.
2. Add the ```bin``` directory of the created directory ```jdk-17``` to the ```PATH``` environment variable.
3. Set the ```JAVA_HOME``` environment variable to the path of your jdk installation.
4. The result should look similar to:
   <br>JAVA_HOME: /jdk-17
   <br>PATH: /jdk-17/bin</br>

You should restart your computer after editing environment variables.

