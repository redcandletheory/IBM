# IBM i DCM Return Code & Symptom Search Web Application

### Target Runtime: **IBM WebSphere Liberty Server 24.0.0.12 and Above**
* **Operating System**: IBM i V7R5, V7R6, and future releases
* **Target Application Server**: IBM WebSphere Application Server Liberty Profile (`24.0.0.12+`) / Integrated Application Server (IAS)
* **Notice**: **This application is built for WebSphere Liberty Server 24.0.0.12+ and is NOT intended for IWS (Integrated Web Services) servers.**

---

## 🌟 Features & Architecture

1. **Native WebSphere Liberty 24.0.0.12+ Compliance**:
   * Uses standard Java EE 8 / Servlet 3.1 / JAX-RS specifications provided by WebSphere Liberty 24.0.0.12+.
   * Standard `.war` packaging with zero external runtime dependencies.

2. **DCM Return Code Deconstruction Engine**:
   * Parses and deconstructs cryptic Digital Certificate Manager return codes using the formula:
     $$\text{Point of Failure} = \mathbf{xxx} + \mathbf{yyyy} + \mathbf{zz}$$
     * **`xxx` (Major Entry Point)**: Multiples of 1,000,000 across all 9 Service Program Managers (`QYCUAPPMGR`, `QYCULCAMGR`, `QYCUCTMGR`, `QYCUCSMGR`, etc.).
     * **`yyyy` (Utility Point of Failure)**: Point of failure within 95+ utility functions in `qycuutl.H`.
     * **`zz` (Local Point of Failure)**: Internal manager offset codes.

3. **Multi-Tier Search Capabilities**:
   * **Exact Symptom String Matching**: `5770 SP/QYCULCAMGR MOD/QYCULCAMGR RC7027500`
   * **Fuzzy & Closest Match**: If an exact code is not found, calculates $\Delta$ offsets and lists the closest documented return codes.
   * **Keyword & Regex Search**: Search across constants like `RECEIVECERT`, `STORE`, `APPLIST`, or regular expressions.
   * **Technician Action Support**: Prompts support technicians to *"Search iAI assisted support searches in CSP"* when non-standard codes are identified.

4. **Zero-404 Browser Engine**:
   * Web UI runs completely self-contained in the browser without network latency or 404 errors.

---

## 📁 Project Structure

```
iws-service-v7r5/
├── pom.xml                                    # Maven build configuration for Liberty 24.0.0.12+
├── README.md                                  # Documentation
├── src/
│   └── main/
│       ├── java/com/ibmi/iws/
│       │   ├── RestApplication.java          # JAX-RS Application root (/api)
│       │   ├── api/
│       │   │   ├── DcmSearchResource.java    # REST search & decode endpoints
│       │   │   ├── StatusResource.java       # Health & OS diagnostics
│       │   │   └── ProgramResource.java      # Execute native IBM i *PGM via JT400
│       │   └── service/
│       │       ├── DcmSearchService.java     # Multi-tier fuzzy & closest-match DCM engine
│       │       └── ProgramCallerService.java # JT400 native program call handler
│       └── webapp/
│           ├── WEB-INF/web.xml               # Web deployment descriptor
│           └── index.html                    # Interactive DCM search tool UI
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

## 🚀 How to Deploy on WebSphere Liberty (24.0.0.12+) on IBM i

### Option 1: Dropins Directory Deployment (Fastest)
1. Transfer `iws-service.war` to the IBM i IFS (e.g. `/tmp/iws-service.war`).
2. Copy the WAR into your Liberty server instance's `dropins` folder:
   ```cl
   CPY OBJ('/tmp/iws-service.war') +
       TODIR('/www/<LIBERTY_SERVER_NAME>/wlp/usr/servers/<LIBERTY_SERVER_NAME>/dropins') +
       REPLACE(*YES)
   ```
3. Liberty 24.0.0.12+ will dynamically discover and start the application.

---

### Option 2: Server Configuration (`server.xml`)
Add the web application declaration to your Liberty server's `server.xml`:
```xml
<server description="Liberty Server 24.0.0.12+">
    <featureManager>
        <feature>webProfile-8.0</feature>
        <feature>jaxrs-2.1</feature>
        <feature>servlet-4.0</feature>
    </featureManager>

    <webApplication id="dcmSearch" 
                    location="iws-service.war" 
                    name="dcmSearch" 
                    contextRoot="/iws-service"/>
</server>
```

---

### Option 3: Using IBM Web Administration for i (IAS Instance)
1. Open `http://<YOUR_IBMI_HOST>:2001/HTTPAdmin`.
2. Under the **Manage** tab, select **Integrated Application Servers** (Liberty 24.0.0.12+).
3. Select your Liberty server instance.
4. Click **Install New Application** &rarr; Upload `iws-service.war`.
5. Set context root to `/iws-service` and finish the wizard.

---

## 🌐 Service Access URLs

| Resource | URL Pattern | Description |
| :--- | :--- | :--- |
| **Interactive Search Dashboard** | `http://<YOUR_IBMI_HOST>:<PORT>/iws-service/` | Web UI for symptom strings, closest match & regex search |
| **DCM Single Decode API** | `http://<YOUR_IBMI_HOST>:<PORT>/iws-service/api/dcm/decode?symptom=5770%20SP/QYCULCAMGR%20MOD/QYCULCAMGR%20RC7027500` | REST API for single symptom deconstruction |
| **DCM Batch Search API** | `http://<YOUR_IBMI_HOST>:<PORT>/iws-service/api/dcm/search` | REST API (POST) for batch searches |
| **Server Health & Diagnostics** | `http://<YOUR_IBMI_HOST>:<PORT>/iws-service/api/status` | Returns OS version, Java version, and runtime status |

---

## 🔍 DCM Search Examples

| Symptom String | Service Program | Entry Point | Utility Function |
| :--- | :--- | :--- | :--- |
| `5770 SP/QYCULCAMGR MOD/QYCULCAMGR RC7027500` | `QYCULCAMGR` (Local CA) | `QYCU_RECEIVESERVERCERT` (`+7000000`) | `QYCU_RECEIVECERT` (`+27500`) |
| `5722 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC5003000` | `QYCUAPPMGR` (App Mgr) | `QYCU_APPASSIGNCERT` (`+5000000`) | `QYCU_ASSIGNCERTTOAPP` (`+3000`) |
| `5722 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC14000000` | `QYCUAPPMGR` (App Mgr) | `QYCU_APPUPDATEDEFINITION` (`+14000000`) | None (Direct Module Failure) |
| `5722 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC12000000` | `QYCUAPPMGR` (App Mgr) | `QYCU_APPREMOVE` (`+12000000`) | None (Direct Module Failure) |
| `5770 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC7000100` | `QYCUAPPMGR` (App Mgr) | `QYCU_APPLISTASSIGNEDCERT` (`+7000000`) | `QYCU_RTVAPPS` (`+100`) |
