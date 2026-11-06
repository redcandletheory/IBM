# IBM i DCM Return Code & Diagnostic Analyzer — Comprehensive Technical Manual & User Guide

### Target Environment: **Integrated Application Server (IAS) / WebSphere Liberty 24.0.0.12+** on **IBM i V7R5 / V7R6**
**Application Package**: `dcm-service.war`  
**Default Context Root**: `/dcm-service`  
**GitHub Repository**: `https://github.com/redcandletheory/IBM.git`

---

## 📖 Table of Contents
1. [Core Architectural Concepts & Mathematical Theory](#1-core-architectural-concepts--mathematical-theory)
2. [Interface Walkthrough & Operating Modes](#2-interface-walkthrough--operating-modes)
3. [Mastering Sorting, Filtering, and Matrix Navigation](#3-mastering-sorting-filtering-and-matrix-navigation)
4. [Diagnostic Use Cases & Practical Walkthroughs](#4-diagnostic-use-cases--practical-walkthroughs)
5. [Technician Tips & Power Tricks](#5-technician-tips--power-tricks)
6. [IBM i CL Diagnostic Commands Cheatsheet](#6-ibm-i-cl-diagnostic-commands-cheatsheet)
7. [Automated REST API Integration (CLI & Scripting)](#7-automated-rest-api-integration-cli--scripting)

---

## 1. Core Architectural Concepts & Mathematical Theory

IBM i Digital Certificate Manager (DCM) return codes often appear cryptic in system job logs (`DSPJOBLOG`), history logs (`DSPLOG`), or application traces (e.g., `5770 SP/QYCULCAMGR MOD/QYCULCAMGR RC7027500`). 

Behind these codes is a deterministic, additive mathematical schema:

$$\mathbf{\text{Point of Failure}} = \mathbf{xxx} \text{ (Major Entry Point)} + \mathbf{yyyy} \text{ (Utility Routine)} + \mathbf{zz} \text{ (Local Minor Offset)}$$

```
 ┌──────────────────────┬────────────────────────┬──────────────────────┐
 │ xxx (Major Module)   │ yyyy (Utility Routine) │ zz (Local Offset)    │
 │ Multiples of 1000000 │ Values 100 to 101000   │ Values 01 to 99      │
 └──────────────────────┴────────────────────────┴──────────────────────┘
```

### The Three Decomposition Layers:
1. **Major Entry Point ($\mathbf{xxx}$)**:
   * Identifies the primary API function invoked in one of the 9 core Service Program managers:
     * `QYCUAPPMGR` &mdash; Application Manager (Registration Facility & trust lists)
     * `QYCUCSMGR` &mdash; Certificate Store Manager (`*SYSTEM`, `DEFAULT.KDB`, stash passwords)
     * `QYCUCTMGR` &mdash; Certificate Manager (Import, export, CSR generation, validation)
     * `QYCULCAMGR` &mdash; Local Certificate Authority (Local CA creation, signing, policies)
     * `QYCUHDWMGR` &mdash; Hardware Device Manager (PCIe Cryptographic Coprocessors 4767, 4769, 4770, Enterprise PKCS#11)
     * `QYCUCRLMGR` &mdash; Revocation List Manager (CRL distribution points, `*VLDL`)
     * `QYCUGENMGR` &mdash; General DCM / PKIX Manager (Startup, URL tables)
     * `QYCUUCMGR` &mdash; User Certificate Manager (Digital IDs, user space objects)
     * `QYCUYDOMGR` &mdash; Object Signing Manager (Signature verification, batch signing)
2. **Utility Routine ($\mathbf{yyyy}$)**:
   * Originates from the shared C header file `qycuutl.H` containing 95+ reusable cryptographic, Base64, ASN.1, storage allocation, and file I/O helper routines.
3. **Local Minor Offset ($\mathbf{zz}$)**:
   * Internal failure codes (01 to 99) signaling specific file access or parameter validations inside the service program module.

---

## 2. Interface Walkthrough & 5 Operating Modes

The web dashboard is a high-performance Single Page Application (SPA) providing **5 dedicated diagnostic modes**, an automated **ADMIN3 / Liberty Trace Log Analyzer**, an **EBCDIC CP037 Hex Decoder**, and a complete **500+ Code Return Matrix**.

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────┐
│  🔐 IBM i DCM Return Code & Diagnostic Analyzer            [ IBM i V7R5 / V7R6 ] [ 500+ Codes ] │
│  IAS v8.5+ & WebSphere Liberty 24.0.0.12+ | Context Root: /dcm-service                          │
├──────────────────────────────────────────────────────────────────────────────────────────────────┤
│  [ 🔍 1. Symptom String / WRKPRB ]  [ 📋 2. ADMIN3 Log Analyzer ]  [ ⚙️ 3. GSKit & API Codes ]     │
│  [ 🔎 4. Keyword / Regex Search ]   [ 📊 5. Complete Matrix Directory (500+) ]                   │
├──────────────────────────────────────────────────────────────────────────────────────────────────┤
│  Input Area (Multi-line textarea) & Example Presets:                                             │
│  [ Sample 1: Symptom String ]  [ Sample 2: ADMIN3 Trace Log ]  [ Sample 3: GSKit RC: 6 ]        │
├──────────────────────────────────────────────────────────────────────────────────────────────────┤
│  [ 🚀 Execute Analysis ]  [ 🧹 Clear Input ]                                                     │
└──────────────────────────────────────────────────────────────────────────────────────────────────┘
```

### Mode 1: Symptom String / WRKPRB Mode (Default)
* **What to enter**:
  * Full IBM Symptom String: `5770 SP/QYCULCAMGR MOD/QYCULCAMGR RC7027500`
  * Service Program + Return Code: `QYCUAPPMGR 5003000`
  * Pure Return Code Number: `7027500` or `RC5003000`
* **Output**:
  * Deconstructs into a **4-Column Decomposition Grid** (Service Program, Major Entry Point, Utility Routine, Local Offset).
  * Generates an English diagnostic narrative explaining what the program was trying to do.
  * Recommends exact **IBM i CL verification commands**.

### Mode 2: ADMIN3 / Liberty SystemOut Log Analyzer
* **What to enter**:
  * Paste multi-line raw log snippets directly from `/www/ADMIN3/wlp/usr/servers/ADMIN3/logs/SystemOut.log`.
* **Output**:
  * Extracts executing User Profile (e.g., `QSECOFR`) and IBM i Host Job (e.g., `123456/QTMHHTTP/ADMIN3`).
  * Maps native SPI method calls (e.g., `qycu_listCertificatesJ`).
  * **Decodes EBCDIC (CP037) Hex Parameters** into plaintext IFS paths and option tags.
  * Resolves underlying **GSKit / GSKKM return codes** (e.g., `rc: 6` &rarr; `GSKKM_ERR_DATABASE_OPEN`).
  * Generates tailored recovery checklists and CL commands.

### Mode 3: GSKit & API Return Codes Mode
* **What to enter**:
  * GSKit / GSKKM codes: `6`, `16`, `1`, `5`, `78`, `311`
  * QYCU API / SPI return codes: `3845`, `3856`, `3945`, `4011`, `-992`, `-2`
* **Output**:
  * Symbolic C constant name (`GSKKM_ERR_DATABASE_OPEN`, `YCU_RC_AUTH_PROB`, `YCU_SWAP_USER`).
  * Source header definition file (`gskkmerr.h`, `qycuretcodes.H`).
  * Technical root-cause explanation and suggested authority / configuration fixes.

### Mode 4: Keyword & Regular Expression Search
* **What to enter**:
  * Function keywords: `RECEIVECERT`, `APPLIST`, `STORE`, `KEYSTORE`, `ALLOCATE`, `DATABASE_OPEN`
  * Regular expressions: `QYCU_.*CA`, `^QYCU_APP.*`, `.*PASSWORD.*`, `GSKKM_ERR_.*`
* **Output**:
  * Filtered table of all matching Symbolic Constants, Source Files, Code Offsets, and Categories.

### Mode 5: Complete Matrix Directory (500+ Codes)
* **What it offers**:
  * Live searchable matrix spanning all 9 Service Program Managers, 95+ `qycuutl.H` utility routines, 107 `qycuretcodes.H` API codes, and 187 `gskkmerr.h` GSKit codes.
  * 1-click Domain Filtering Chips.
  * Multi-column ascending and descending sorting.

---

## 3. Mastering Sorting, Filtering, and Matrix Navigation

### A. Domain Category Filtering Chips
Located at the top of the **Complete Matrix Directory (500+)** tab:
* **All (500+ Codes)**: Entire unified database across all 13 source tables.
* **GSKit Errors (gskkmerr.h)**: 187 GSKit / GSKKM cryptography & key database error codes.
* **API RetCodes (qycuretcodes.H)**: 107 public and internal QYCU return codes.
* **Application Integration**: Covers `QYCUAPPMGR` and application trust utilities (`+100` to `+3000`).
* **Store & Passwords**: Covers `QYCUCSMGR` and key store/stash routines (`+50000` to `+75000`).
* **Certificate Lifecycle**: Covers `QYCUCTMGR` and CSR/Import/Export routines (`+10000` to `+31500`).
* **Local CA**: Covers `QYCULCAMGR` and CA policy routines (`+80000` to `+87000`).
* **Hardware Crypto**: Covers `QYCUHDWMGR` and PCIe Coprocessor hardware routines (`+100000` to `+101000`).
* **Revocation & CRL**: Covers `QYCUCRLMGR` and Validation List routines (`+90000` to `+93000`).
* **User Certificates**: Covers `QYCUUCMGR` and user space digital ID routines.
* **Object Signing**: Covers `QYCUYDOMGR` object signing routines.

### B. Multi-Column Sorting
Click any table column header with the `↕` indicator to sort:
* **Click Column 1 (Type / Category)**: Groups by lifecycle type (Entry Point vs. Utility Function).
* **Click Column 2 (Service Program / Source)**: Groups by module (`QYCUAPPMGR`, `QYCUCSMGR`, `qycuutl.H`).
* **Click Column 3 (Code / Value)**: Sorts numerically by offset value (e.g. `+100`, `+3000`, `+7000000`).
* **Click Column 4 (Symbolic Name / Constant)**: Sorts alphabetically by C constant name (e.g. `QYCU_ADDCERTIFICATETRUST`).
* **Toggle Order**: Click once for Ascending ($\Delta \uparrow$), click again for Descending ($\Delta \downarrow$).

### C. Proximity & Delta ($\Delta$) Closest-Match Sorting
When a return code does not have a 100% exact decomposition (for instance, if an operating system patch introduced a new offset):
* The engine calculates $|\text{Target Return Code} - \text{Known Constant}|$.
* Returns the top closest matches ranked by the smallest $\Delta$ offset.
* Prompts support technicians to *"Search iAI assisted support searches in CSP"*.

---

## 4. Diagnostic Use Cases & Practical Walkthroughs

### Scenario A: Application Cannot Bind Certificate (`RC5003000`)
* **Input**: `5722 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC5003000`
* **Decomposition**:
  * Service Program: `QYCUAPPMGR` (Application Manager)
  * Major Entry Point ($+5000000$): `QYCU_APPASSIGNCERT`
  * Utility Function ($+3000$): `QYCU_ASSIGNCERTTOAPP`
* **Diagnostic Translation**:
  * An administrator or job attempted to bind an SSL/TLS certificate to an Application ID, but the assignment utility failed.
* **Root Cause Verification**:
  1. Check if the Application ID is registered: `WRKREGINF`
  2. Verify IFS data authorities on the certificate store: `WRKAUT OBJ('/QIBM/UserData/ICSS/Cert/Server')`

---

### Scenario B: Local CA Certificate Receive Failure (`RC7027500`)
* **Input**: `5770 SP/QYCULCAMGR MOD/QYCULCAMGR RC7027500`
* **Decomposition**:
  * Service Program: `QYCULCAMGR` (Local CA Manager)
  * Major Entry Point ($+7000000$): `QYCU_RECEIVESERVERCERT`
  * Utility Function ($+27500$): `QYCU_RECEIVECERT`
* **Diagnostic Translation**:
  * While executing `QYCU_RECEIVESERVERCERT` in Local CA Manager, the helper function `QYCU_RECEIVECERT` encountered an issue receiving/parsing the certificate data.

---

### Scenario C: ADMIN3 Trace & Keystore Open Failure (`RC: 6`)
* **Log Signature**: `function: qycu_listCertificatesJ / rc: 6`
* **Function Trace**:
  * Origin C API: `gskkm_open_database() / gskkm_open_key_database()`
  * DCM Manager: `QYCUCSMGR` (`QYCU_OPENSTORE`), `QYCUCTMGR` (`QYCU_LISTCERTIFICATES`)
  * JNI Bridge: `qycu_listCertificatesJ`
  * Target File: `/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB`
* **Root Cause**:
  * User profile `QTMHHTTP` lacks `*RWX` data authority to `/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB`.
* **Actionable Remediation**:
  ```cl
  WRKLNK OBJ('/QIBM/UserData/ICSS/Cert/Server/*')
  CHGAUT OBJ('/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB') USER(QTMHHTTP) DTAAUT(*RWX)
  ```

---

### Scenario D: Password Stash File Mismatch (`RC: 16`)
* **Log Signature**: `function: qycu_listCertificatesJ / rc: 16`
* **Function Trace**:
  * Origin C API: `gskkm_verify_password() / gskkm_check_stash()`
  * DCM Manager: `QYCUCSMGR` (`QYCU_VALIDATEPASSWORD`)
  * JNI Bridge: `qycu_listCertificatesJ`
  * Target File: `/QIBM/UserData/ICSS/Cert/Server/DEFAULT.STH`
* **Root Cause**:
  * The password stash file (`DEFAULT.STH`) does not match the active password of the `.KDB` file.
* **Actionable Remediation**:
  1. Open DCM web interface (`http://<host>:2001/HTTPAdmin`).
  2. Navigate to *Manage Certificate Store* &rarr; *Select Store* &rarr; *Change Password / Re-create Password Stash*.

---

### Scenario E: TLS Peer Certificate Not Trusted (`RC: 6000`)
* **Job Log / Socket Failure**: `GSK_AS400_ERROR_NOT_TRUSTED (Message CPDBC01)`
* **Function Trace**:
  * Origin C API: `gsk_secure_soc_init()`
  * Module: `QSYS/QFLAGSK`
  * Subsystem: Native TLS Handshake / Trust Store
* **Root Cause**:
  * The peer certificate presented during TLS handshake was signed by a CA not present in the local certificate store (`DEFAULT.KDB`).
* **Actionable Remediation**:
  1. Export the root and intermediate CA certificates from the remote server.
  2. Import into DCM under `*SYSTEM` certificate store.

---

### Scenario F: Application ID Not Registered (`RC: 6007`)
* **Job Log / Socket Failure**: `GSK_AS400_ERROR_APPLICATION_NOT_REGISTERED (Message CPDBC07)`
* **Function Trace**:
  * Origin C API: `gsk_environment_init()`
  * Module: `QSYS/QFLAGSK` / `QSYSINC/H(QWTSECA)`
  * Subsystem: IBM i Registration Facility
* **Root Cause**:
  * The Application ID specified in `GSK_OS400_APPLICATION_ID` has not been registered in the Registration Facility.
* **Actionable Remediation**:
  ```cl
  WRKREGINF (Inspect registered Application IDs)
  ```
  Register the application using `QWTSECA` API or via DCM *Manage Applications*.

---

## 5. Technician Tips & Power Tricks

### Tip 1: Batch Pasting from IBM i Job Logs (`DSPJOBLOG`)
You do not need to clean up symptom strings. You can copy several lines directly from a 5250 emulator job log or spool file:
```text
5770 SP/QYCULCAMGR MOD/QYCULCAMGR RC7027500
5722 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC5003000
5722 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC14000000
```
Paste this block into the query box and click **Search & Deconstruct**. The engine iterates through each line and builds an individual visual card for each symptom string.

### Tip 2: Quick RegEx Filtering for Unknown Functions
If you only know a fragment of a function name:
1. Switch to **Keyword / Regex Search**.
2. Type `^QYCU.*STORE` to find all store-related routines.
3. Type `.*PKCS.*` to find all PKCS#7, PKCS#11, and PKCS#12 formatting routines.

### Tip 3: Zero Network Latency & Offline Capability
Because all 110+ tables and parsing algorithms are compiled directly into the client-side bundle, you can save the web page locally (`Ctrl+S` &rarr; `Webpage, Complete`) or run it on a disconnected laptop in secure data centers without internet access.

---

## 6. IBM i CL Diagnostic Commands Cheatsheet

When diagnosing DCM errors, use these IBM i commands in 5250:

| Diagnostic Objective | IBM i CL Command |
| :--- | :--- |
| **Start DCM Admin Server** | `STRTCPSVR SERVER(*HTTP) HTTPSVR(*ADMIN)` |
| **Verify Admin Server Jobs** | `WRKACTJOB SBS(QHTTPSVR) JOB(ADMIN*)` |
| **Verify Application Registration** | `WRKREGINF` |
| **Inspect *SYSTEM Key Store Files** | `WRKLNK OBJ('/QIBM/UserData/ICSS/Cert/Server/*')` |
| **Inspect Local CA Files** | `WRKLNK OBJ('/QIBM/UserData/ICSS/Cert/CertAuth/LOCAL_CERTIFICATE*.txt')` |
| **Inspect Validation Lists** | `WRKOBJ OBJ(QUSRSYS/*ALL) OBJTYPE(*VLDL)` |
| **Fix IFS Authority for HTTP Server** | `CHGAUT OBJ('/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB') USER(QTMHHTTP) DTAAUT(*RWX)` |
| **Work with Crypto Coprocessors** | `WRKHDWRSC TYPE(*CRP)` |
| **Check System History Log** | `DSPLOG MSGID(CPF* CPD*)` |

---

## 7. Automated REST API Integration (CLI & Scripting)

In addition to the interactive Web UI, `dcm-service.war` exposes REST API endpoints for automated monitoring scripts:

### Single Symptom Decode (`GET /api/dcm/decode`)
```bash
# Using curl:
curl -G "http://<YOUR_IBMI_HOST>:<PORT>/dcm-service/api/dcm/decode" \
     --data-urlencode "symptom=5770 SP/QYCULCAMGR MOD/QYCULCAMGR RC7027500"
```

**JSON Output**:
```json
{
  "success": true,
  "isExact": true,
  "serviceProgram": "QYCULCAMGR",
  "serviceProgramName": "Local Certificate Authority Manager",
  "returnCode": 7027500,
  "majorEntryPointCode": 7000000,
  "majorEntryPointName": "QYCU_RECEIVESERVERCERT",
  "utilityFunctionCode": 27500,
  "utilityFunctionName": "QYCU_RECEIVECERT",
  "utilityFunctionCategory": "Certificate Utility",
  "localFailureCode": 0,
  "localFailureName": null,
  "operationalDomain": "Local Certificate Authority",
  "useCaseExplanation": "During processing in Local Certificate Authority Manager (QYCULCAMGR), entry point 'QYCU_RECEIVESERVERCERT' was executing. A failure occurred in utility function 'QYCU_RECEIVECERT' (Certificate Utility).",
  "targetObjects": [
    "/QIBM/UserData/ICSS/Cert/CertAuth/LOCAL_CERTIFICATE*.txt",
    "Local CA Policy Database (*VLDL)"
  ],
  "diagnosticCommands": [
    "WRKLNK OBJ('/QIBM/UserData/ICSS/Cert/CertAuth/LOCAL_CERTIFICATE*.txt')",
    "WRKOBJ OBJ(QUSRSYS/*ALL) OBJTYPE(*VLDL)"
  ]
}
```

### Batch Symptom Search (`POST /api/dcm/search`)
```bash
curl -X POST "http://<YOUR_IBMI_HOST>:<PORT>/dcm-service/api/dcm/search" \
     -H "Content-Type: application/json" \
     -d '{
       "symptoms": [
         "5770 SP/QYCULCAMGR MOD/QYCULCAMGR RC7027500",
         "5722 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC5003000",
         "RC14000000"
       ]
     }'
```

---

## 8. Integrated Application Server (IAS v8.5+) In-App Server Deployment Guide

### Overview
IBM Integrated Application Server (IAS) v8.5 and above provides a lightweight Java EE runtime on IBM i. Follow these instructions to add `dcm-service.war` directly to your IAS server instance.

---

### Dropin Folder Location
The official location of the dropin folder on IBM i for IAS and Liberty server instances is:
```
/www/<instance>/wlp/usr/servers/<instance>/dropins
```
*(where `<instance>` is the name of your specific IAS server)*

---

### Option A: Direct IFS Dropin Deployment (Zero Configuration)
1. Transfer `dcm-service.war` to `/tmp/dcm-service.war` on IBM i.
2. Run the CL copy command from 5250:
   ```cl
   CPY OBJ('/tmp/dcm-service.war') +
       TODIR('/www/<instance>/wlp/usr/servers/<instance>/dropins') +
       REPLACE(*YES)
   ```
3. The server instance automatically discovers and starts `/dcm-service`.

---

### Option B: IBM Web Administration for i GUI (`HTTPAdmin`)
1. **Start Admin Server**: Ensure `STRTCPSVR SERVER(*HTTP) HTTPSVR(*ADMIN)` is active.
2. **Open Console**: Navigate to `http://<YOUR_IBMI_HOST>:2001/HTTPAdmin`.
3. **Select Server**: Go to **Manage** &rarr; **Application Servers** &rarr; select `<instance>` from the dropdown list.
4. **Install Application**: Under **Server Tasks** (left menu), click **Install New Application**.
5. **Select WAR Package**:
   * If uploaded to IBM i IFS: Select **"The application is located on the system"** &rarr; enter `/tmp/dcm-service.war`.
   * If on local workstation: Select **"The application is located on your local file system"** &rarr; click Browse &rarr; select `dcm-service.war`.
6. **Set Context Root**: Set **Application Name** to `dcm-service` and **Context Root** to `/dcm-service`.
7. **Complete Wizard**: Click **Finish**.
8. **Test Dashboard**: Open `http://<YOUR_IBMI_HOST>:<IAS_PORT>/dcm-service/` in your browser.

---

### Option C: QShell Script Utility (`installapp.sh`)
Execute from QShell (`QSH`):
```bash
/QIBM/ProdData/OS/OS400/bin/installapp.sh -server '<instance>' -app '/tmp/dcm-service.war' -contextRoot '/dcm-service'
```

---

### Deployment Verification & Log Diagnostics
1. **Check Server Jobs**:
   ```cl
   WRKACTJOB SBS(QHTTPSVR) JOB(<instance>*)
   ```
2. **Inspect Runtime Server Log**:
   ```cl
   DSPF STMF('/www/<instance>/wlp/usr/servers/<instance>/logs/messages.log')
   ```
   Confirm success message:
   ```text
   CWWKZ0001I: Application dcm-service started in X.XXX seconds.
   ```
