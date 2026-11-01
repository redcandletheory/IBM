# IBM i IWS 3.0 Portable Web Service (V7R5 / V7R6)

This project provides a portable Java RESTful Web Service packaged as a **WAR file** for **IBM Integrated Web Services (IWS 3.0)** running on **IBM i V7R5, V7R6, and future releases**.

---

## 🌟 Features & Portability
- **Runtime Compatibility**: Built on standard Java EE / JAX-RS specifications supported by IWS 3.0 (Liberty Profile) on IBM i V7R5 and V7R6.
- **Native IBM i Integration**: Includes IBM Toolbox for Java (`jt400`) wrapper to seamlessly execute native RPG/COBOL `*PGM` and `*SRVPGM` objects.
- **Zero OS Lock-in**: Can be moved across different IWS server instances and IBM i OS versions without recompilation.

---

## 📁 Project Structure

```
iws-service-v7r5/
├── pom.xml                                    # Maven build descriptor
├── README.md                                  # Documentation
├── src/
│   └── main/
│       ├── java/
│       │   └── com/ibmi/iws/
│       │       ├── RestApplication.java       # JAX-RS Application root (/api)
│       │       ├── api/
│       │       │   ├── StatusResource.java    # /api/status (Health & OS diagnostics)
│       │       │   └── ProgramResource.java   # /api/program/call (Execute native *PGM)
│       │       └── service/
│       │           └── ProgramCallerService.java # JT400 program call handler
│       └── webapp/
│           ├── WEB-INF/
│           │   └── web.xml                    # Web deployment descriptor
│           └── index.html                     # Landing page
```

---

## 🔨 How to Build the WAR File

From the `iws-service-v7r5` directory, run:

```bash
mvn clean package
```

The compiled WAR file will be generated at:
```
target/iws-service.war
```

---

## 🚀 How to Deploy / Install on IBM i (V7R5 & V7R6)

### Option 1: Using IBM Web Administration for i (GUI)
1. Open a browser and navigate to `http://<YOUR_IBMI_IP>:2001/HTTPAdmin`.
2. Log in with your IBM i user credentials (requires `*IOSYSCFG` authority).
3. Under the **Integrated Web Services** tab, select your IWS 3.0 Server.
4. Click **Deploy New Service**.
5. Select **REST Web Service** and upload `iws-service.war`.
6. Follow the wizard steps to complete deployment and start the service.

### Option 2: Command Line (QShell / Scripting)
You can deploy using the IBM i Web Services CLI script:
```bash
/qibm/proddata/os/webservices/bin/installWebService.sh -server <SERVER_NAME> -service iwsService -war /path/to/iws-service.war
```

### Option 3: Direct Liberty Dropins
Copy `iws-service.war` directly to the server's dropins folder:
```bash
/www/<SERVER_NAME>/wlp/usr/servers/<SERVER_NAME>/dropins/iws-service.war
```

---

## 🌐 API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/iws-service/api/status` | Returns OS version, Java runtime, and server health |
| `POST` | `/iws-service/api/program/call?lib=MYLIB&pgm=MYPGM&outLen=100` | Calls an IBM i native `*PGM` and returns the output |

---

## 🐙 How to Save and Upload to GitHub

### 1. Install Git (if not already installed)
In PowerShell:
```powershell
winget install --id Git.Git -e --source winget
```
*(Close and reopen your terminal after installation so `git` is recognized)*

### 2. Initialize and Commit
From inside the `iws-service-v7r5` folder:
```powershell
cd iws-service-v7r5
git init
git add .
git commit -m "Initial commit of portable IWS 3.0 web service for V7R5/V7R6"
```

### 3. Create a GitHub Repository and Push
1. Go to [GitHub.com](https://github.com/new) and create a new repository (e.g., `iws-web-service-v7r5`).
2. Link your local project to GitHub and push:
```powershell
git branch -M main
git remote add origin https://github.com/<YOUR-GITHUB-USERNAME>/<YOUR-REPO-NAME>.git
git push -u origin main
```
