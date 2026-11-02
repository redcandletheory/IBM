# IBM i IWS 3.0 Portable Web Service (V7R5 / V7R6)

This project provides a portable Java RESTful Web Service and Interactive Web UI packaged as a **WAR file** for **IBM Integrated Web Services (IWS 3.0)** running on **IBM i V7R5, V7R6, and future releases**.

---

## 🌟 Service Endpoints

Once deployed onto your IBM i server, access the tools using the following URLs:

| Resource | URL Pattern | Description |
| :--- | :--- | :--- |
| **Interactive Search Dashboard** | `http://<YOUR_IBMI_HOST>:<PORT>/iws-service/` | Web UI for symptom strings, closest match & regex search |
| **DCM Single Decode API** | `http://<YOUR_IBMI_HOST>:<PORT>/iws-service/api/dcm/decode?symptom=5770%20SP/QYCULCAMGR%20MOD/QYCULCAMGR%20RC7027500` | REST API for single symptom deconstruction |
| **DCM Batch Search API** | `http://<YOUR_IBMI_HOST>:<PORT>/iws-service/api/dcm/search` | REST API (POST) for batch searches |
| **Server Health & Diagnostics** | `http://<YOUR_IBMI_HOST>:<PORT>/iws-service/api/status` | Returns OS/400 version, Java version, and runtime status |
| **IBM Web Administration GUI** | `http://<YOUR_IBMI_HOST>:2001/HTTPAdmin` | Management console for IWS / IAS instances |

---

## 📁 Project Structure

```
iws-service-v7r5/
├── pom.xml                                    # Maven build descriptor (Java 8/11, WAR packaging)
├── README.md                                  # Documentation
├── src/
│   └── main/
│       ├── java/com/ibmi/iws/
│       │   ├── RestApplication.java          # JAX-RS Application root (/api)
│       │   ├── api/
│       │   │   ├── DcmSearchResource.java    # /api/dcm/* (REST search & decode endpoints)
│       │   │   ├── StatusResource.java       # /api/status (Health & OS diagnostics)
│       │   │   └── ProgramResource.java      # /api/program/call (Execute native *PGM)
│       │   └── service/
│       │       ├── DcmSearchService.java     # Multi-tier fuzzy & closest-match DCM engine
│       │       └── ProgramCallerService.java # JT400 program call handler
│       └── webapp/
│           ├── WEB-INF/web.xml               # Web deployment descriptor
│           └── index.html                    # Interactive DCM search tool UI (Zero-404 browser engine)
```

---

## 🔨 How to Build the WAR File

From the `iws-service-v7r5` directory:

```powershell
mvn clean package
```

The compiled WAR package is generated at:
```
target/iws-service.war
```

---

## 🚀 How to Deploy on IBM i

### Option 1: Direct Liberty Dropins (Recommended & Fastest)
Transfer `iws-service.war` to your IBM i system and copy it into your IWS server instance's dropins folder:
```cl
CPY OBJ('/tmp/iws-service.war') +
    TODIR('/www/<YOUR_IWS_SERVER>/wlp/usr/servers/<YOUR_IWS_SERVER>/dropins') +
    REPLACE(*YES)
```

### Option 2: Using IBM Web Administration for i (GUI)
1. Open `http://<YOUR_IBMI_HOST>:2001/HTTPAdmin`.
2. Log in with your IBM i user credentials (requires `*IOSYSCFG` authority).
3. Under the **Integrated Web Services** (or **Integrated Application Servers**) tab, select your server instance.
4. Click **Deploy New Service** (or **Install New Application**) and select `iws-service.war`.
5. Set context root to `/iws-service` and finish the wizard.

### Option 3: Command Line (QShell)
Run from QSH on your IBM i system:
```bash
/qibm/proddata/os/webservices/bin/installWebService.sh -server <SERVER_NAME> -service iwsService -war /tmp/iws-service.war -contextRoot /iws-service
```

---

## 🔍 DCM Search Capabilities & Examples

### Supported Symptom Strings:
1. `5770 SP/QYCULCAMGR MOD/QYCULCAMGR RC7027500` &rarr; Local CA Manager, Receive Server Cert (`QYCU_RECEIVESERVERCERT`), Receive Cert Utility (`QYCU_RECEIVECERT`)
2. `5722 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC5003000` &rarr; App Manager, Assign Cert (`QYCU_APPASSIGNCERT`), Assign Cert to App Utility (`QYCU_ASSIGNCERTTOAPP`)
3. `5722 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC14000000` &rarr; App Manager, Update Definition (`QYCU_APPUPDATEDEFINITION`)
4. `5722 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC12000000` &rarr; App Manager, Remove App (`QYCU_APPREMOVE`)
5. `5770 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC7000100` &rarr; App Manager, List Assigned Certs (`QYCU_APPLISTASSIGNEDCERT`), Retrieve Apps (`QYCU_RTVAPPS`)

### Unmatched / Unknown Symptom Strings:
If a return code cannot be resolved exactly, the system:
* Finds and presents the closest candidate entry points and utilities.
* Automatically displays the support reminder:
  > ⚠️ **Reminder for Technicians**: Search **iAI assisted support searches in CSP** (Customer Support Portal) for known APARs, PTFs, or defect tracking.
