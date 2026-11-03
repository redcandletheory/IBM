# IBM i DCM Return Code & Symptom Search Web Application

### Target Runtime: **Integrated Application Server (IAS) / WebSphere Liberty 24.0.0.12+**
* **Operating System**: IBM i V7R5, V7R6, and future releases
* **Target Application Server**: Integrated Application Server (IAS) powered by IBM WebSphere Application Server Liberty Profile (`24.0.0.12+`)
* **Notice**: **This application is built for Integrated Application Server (IAS) / WebSphere Liberty Server 24.0.0.12+.**

---

## 🌟 Features & Architecture

1. **Native WebSphere Liberty 24.0.0.12+ / IAS Compliance**:
   * Uses standard Java EE 8 / Servlet 3.1 / JAX-RS specifications provided by IAS / WebSphere Liberty 24.0.0.12+.
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
   * **Red Herring & Known Issue Detection**: Automatically recognizes benign diagnostic events (e.g. `5770 SP/QYCULCAMGR MOD/QYCULCAMGR RC7027500` Base64 vs PKCS#12 handling) and links to official IBM Support pages.

4. **Interactive Web UI vs. Raw Postman Output**:
   * Provides a rich client-side dashboard with multi-line input, sample presets, dynamic 4-column decomposition cards, and color-coded status badges, eliminating the need for Postman or manual JSON inspection.
   * Web UI runs completely self-contained in the browser with zero network failure risk.

---

## 📁 Project Structure

```
dcm-service-v7r5/
├── pom.xml                                    # Maven build configuration for Liberty 24.0.0.12+ / IAS
├── README.md                                  # Documentation
├── src/
│   └── main/
│       ├── java/com/ibmi/ias/
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

From the project directory:

```powershell
mvn clean package
```

The compiled WAR package is generated at:
```
target/dcm-service.war
```

---

## 🚀 How to Deploy on Integrated Application Server (IAS / Liberty 24.0.0.12+) on IBM i

### Option 1: Dropins Directory Deployment (Fastest)
1. Transfer `dcm-service.war` to the IBM i IFS (e.g. `/tmp/dcm-service.war`).
2. Copy the WAR into your IAS server instance's `dropins` folder:
   ```cl
   CPY OBJ('/tmp/dcm-service.war') +
       TODIR('/www/<IAS_SERVER_NAME>/wlp/usr/servers/<IAS_SERVER_NAME>/dropins') +
       REPLACE(*YES)
   ```
3. Liberty 24.0.0.12+ will dynamically discover and start the application.

---

### Option 2: Server Configuration (`server.xml`)
Add the web application declaration to your IAS server's `server.xml`:
```xml
<server description="IAS Liberty Server 24.0.0.12+">
    <featureManager>
        <feature>webProfile-8.0</feature>
        <feature>jaxrs-2.1</feature>
        <feature>servlet-4.0</feature>
    </featureManager>

    <webApplication id="dcmSearch" 
                    location="dcm-service.war" 
                    name="dcmSearch" 
                    contextRoot="/dcm-service"/>
</server>
```

---

### Option 3: Using IBM Web Administration for i (IAS Instance Wizard)
1. Open `http://<YOUR_IBMI_HOST>:2001/HTTPAdmin`.
2. Under the **Manage** tab, select **Integrated Application Servers** (Liberty 24.0.0.12+).
3. Select your IAS server instance.
4. Click **Install New Application** &rarr; Upload `dcm-service.war`.
5. Set context root to `/dcm-service` and finish the wizard.

---

## 🌐 Service Access URLs

| Resource | URL Pattern | Description |
| :--- | :--- | :--- |
| **Interactive Search Dashboard** | `http://<YOUR_IBMI_HOST>:<PORT>/dcm-service/` | Web UI for symptom strings, closest match & regex search |
| **DCM Single Decode API** | `http://<YOUR_IBMI_HOST>:<PORT>/dcm-service/api/dcm/decode?symptom=5770%20SP/QYCULCAMGR%20MOD/QYCULCAMGR%20RC7027500` | REST API for single symptom deconstruction |
| **DCM Batch Search API** | `http://<YOUR_IBMI_HOST>:<PORT>/dcm-service/api/dcm/search` | REST API (POST) for batch searches |
| **Server Health & Diagnostics** | `http://<YOUR_IBMI_HOST>:<PORT>/dcm-service/api/status` | Returns OS version, Java version, and runtime status |

---

## 🔍 DCM Search Examples

| Symptom String | Service Program | Entry Point | Utility Function |
| :--- | :--- | :--- | :--- |
| `5770 SP/QYCULCAMGR MOD/QYCULCAMGR RC7027500` | `QYCULCAMGR` (Local CA) | `QYCU_RECEIVESERVERCERT` (`+7000000`) | `QYCU_RECEIVECERT` (`+27500`) |
| `5722 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC5003000` | `QYCUAPPMGR` (App Mgr) | `QYCU_APPASSIGNCERT` (`+5000000`) | `QYCU_ASSIGNCERTTOAPP` (`+3000`) |
| `5722 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC14000000` | `QYCUAPPMGR` (App Mgr) | `QYCU_APPUPDATEDEFINITION` (`+14000000`) | None (Direct Module Failure) |
| `5722 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC12000000` | `QYCUAPPMGR` (App Mgr) | `QYCU_APPREMOVE` (`+12000000`) | None (Direct Module Failure) |
| `5770 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC7000100` | `QYCUAPPMGR` (App Mgr) | `QYCU_APPLISTASSIGNEDCERT` (`+7000000`) | `QYCU_RTVAPPS` (`+100`) |
