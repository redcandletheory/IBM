package com.ibmi.ias.service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Robust Multi-Tier Search and Deconstruction Service for IBM i Digital Certificate Manager (DCM).
 * Supports exact symptom strings, partial return codes, regex/keyword queries, and closest-match suggestions.
 */
public class DcmSearchService {

    public static class ProgramInfo {
        public String name;
        public Map<Long, String> entryPoints = new HashMap<>();
        public Map<Long, String> localFailures = new HashMap<>();

        public ProgramInfo(String name) {
            this.name = name;
        }
    }

    public static class UtilityInfo {
        public String name;
        public String category;

        public UtilityInfo(String name, String category) {
            this.name = name;
            this.category = category;
        }
    }

    private final Map<String, ProgramInfo> programs = new HashMap<>();
    private final Map<Long, UtilityInfo> utilityFunctions = new HashMap<>();
    private final List<Long> sortedUtilityCodes = new ArrayList<>();

    public DcmSearchService() {
        initDatabase();
    }

    private void initDatabase() {
        // Table 1: QYCUAPPMGR
        ProgramInfo appMgr = new ProgramInfo("Application Manager");
        appMgr.entryPoints.put(1000000L, "QYCU_APPLIST");
        appMgr.entryPoints.put(2000000L, "QYCU_APPLISTFORCATRUST");
        appMgr.entryPoints.put(3000000L, "QYCU_APPLISTCAS");
        appMgr.entryPoints.put(4000000L, "QYCU_APPREMOVECERT");
        appMgr.entryPoints.put(5000000L, "QYCU_APPASSIGNCERT");
        appMgr.entryPoints.put(6000000L, "QYCU_APPSETCATRUST");
        appMgr.entryPoints.put(7000000L, "QYCU_APPLISTASSIGNEDCERT");
        appMgr.entryPoints.put(8000000L, "QYCU_APPLISTTRUSTINGCA");
        appMgr.entryPoints.put(9000000L, "QYCU_APPLISTUSINGSYSTEMSTORE");
        appMgr.entryPoints.put(10000000L, "QYCU_APPLISTUSINGOBJECTSIGNSTORE");
        appMgr.entryPoints.put(11000000L, "QYCU_APPGETSELECTEDINFO");
        appMgr.entryPoints.put(12000000L, "QYCU_APPREMOVE");
        appMgr.entryPoints.put(13000000L, "QYCU_APPREGISTER");
        appMgr.entryPoints.put(14000000L, "QYCU_APPUPDATEDEFINITION");
        appMgr.entryPoints.put(15000000L, "QYCU_APPLISTFORSIGNING");
        appMgr.entryPoints.put(16000000L, "QYCU_APPVALIDATE");
        programs.put("QYCUAPPMGR", appMgr);

        // Table 2: QYCUCRLMGR
        ProgramInfo crlMgr = new ProgramInfo("Certificate Revocation List Manager");
        crlMgr.entryPoints.put(1000000L, "QYCU_ADDCRLLOCATION");
        crlMgr.entryPoints.put(2000000L, "QYCU_GETCRLLOCATION");
        crlMgr.entryPoints.put(3000000L, "QYCU_LISTCRLLOCATION");
        crlMgr.entryPoints.put(4000000L, "QYCU_REMOVECRLLOCATION");
        crlMgr.entryPoints.put(5000000L, "QYCU_UPDATECRLLOCATION");
        crlMgr.localFailures.put(2L, "ADD_UPDATE_CRL_LOCATION");
        programs.put("QYCUCRLMGR", crlMgr);

        // Table 3: QYCUCSMGR
        ProgramInfo csMgr = new ProgramInfo("Certificate Store Manager");
        csMgr.entryPoints.put(1000000L, "QYCU_CHANGECERTIFICATESTOREPASSWORD");
        csMgr.entryPoints.put(2000000L, "QYCU_CREATECERTIFICATESTORE");
        csMgr.entryPoints.put(3000000L, "QYCU_DELETECERTIFICATESTORE");
        csMgr.entryPoints.put(4000000L, "QYCU_SETCERTIFICATESTOREDEFAULTCERTIFICATE");
        csMgr.entryPoints.put(5000000L, "QYCU_STOREVERIFY");
        csMgr.entryPoints.put(6000000L, "QYCU_STOREVERIFYFILE");
        csMgr.entryPoints.put(7000000L, "QYCU_RESETCERTIFICATESTOREPASSWORD");
        programs.put("QYCUCSMGR", csMgr);

        // Table 4: QYCUCTMGR
        ProgramInfo ctMgr = new ProgramInfo("Certificate Manager");
        ctMgr.entryPoints.put(1000000L, "QYCU_ADDCERTIFICATETRUST");
        ctMgr.entryPoints.put(2000000L, "QYCU_CREATELOCALCERTIFICATE");
        ctMgr.entryPoints.put(3000000L, "QYCU_CREATENONLOCALCERTIFICATE");
        ctMgr.entryPoints.put(4000000L, "QYCU_CREATESTOREANDCERTIFICATE");
        ctMgr.entryPoints.put(5000000L, "QYCU_DELETECERTIFICATE");
        ctMgr.entryPoints.put(6000000L, "QYCU_DELETECERTIFICATEREQUEST");
        ctMgr.entryPoints.put(7000000L, "QYCU_EXPORTCERTIFICATE");
        ctMgr.entryPoints.put(8000000L, "QYCU_IMPORTCERTIFICATE");
        ctMgr.entryPoints.put(9000000L, "QYCU_LISTCERTIFICATES");
        ctMgr.entryPoints.put(10000000L, "QYCU_LISTCERTIFICATEREQUESTS");
        ctMgr.entryPoints.put(11000000L, "QYCU_REMOVECERTIFICATETRUST");
        ctMgr.entryPoints.put(12000000L, "QYCU_RETRIEVECERTIFICATEINFORMATION");
        ctMgr.entryPoints.put(13000000L, "QYCU_RETRIEVECERTIFICATEREQUESTINFORMATION");
        ctMgr.entryPoints.put(14000000L, "QYCU_REMOVECERTIFICATEREQUESTFILE");
        ctMgr.entryPoints.put(15000000L, "QYCU_EXPORTCERTIFICATETOSTORE");
        ctMgr.entryPoints.put(16000000L, "QYCU_EXPORTCERTIFICATEWITHOUTKEY");
        ctMgr.entryPoints.put(17000000L, "QYCU_EXPORTCERTIFICATEWITHOUTKEYTOSTORE");
        ctMgr.entryPoints.put(18000000L, "QYCU_CHECKFORBASE64CERTIFICATE");
        ctMgr.entryPoints.put(19000000L, "QYCU_RETRIEVECACERTIFICATEINFORMATION");
        ctMgr.entryPoints.put(20000000L, "QYCU_ADDCRLASSOCIATIONTOCA");
        ctMgr.entryPoints.put(21000000L, "QYCU_REMOVECRLASSOCIATIONFROMCA");
        ctMgr.entryPoints.put(22000000L, "QYCU_CREATESTOREANDCERTIFICATEREQUEST");
        ctMgr.entryPoints.put(23000000L, "QYCU_EXPORTALLCERTIFICATESWITHOUTKEYTOSTORE");
        ctMgr.entryPoints.put(24000000L, "QYCU_LISTCERTIFICATESWITHKEYINHARDWARE");
        ctMgr.entryPoints.put(25000000L, "QYCU_VALIDATECERTIFICATE");
        ctMgr.entryPoints.put(26000000L, "QYCU_VALIDATECACERTIFICATE");
        ctMgr.entryPoints.put(27000000L, "QYCU_RENEWCERTIFICATE");
        ctMgr.entryPoints.put(28000000L, "QYCU_CHECKFORX509CERTIFICATE");
        ctMgr.entryPoints.put(29000000L, "QYCU_PREVIEWCERTIFICATEINFORMATION");
        ctMgr.entryPoints.put(30000000L, "QYCU_CHECKCERTIFICATEEXPIRATION");
        ctMgr.entryPoints.put(31000000L, "QYCU_IMPORTCERTIFICATEWITHLABEL");
        ctMgr.entryPoints.put(32000000L, "QYCU_LISTCAFILES");
        ctMgr.entryPoints.put(33000000L, "QYCU_LISTCACERTIFICATEFORPOPULATE");
        ctMgr.entryPoints.put(34000000L, "QYCU_POPULATETRUSTCACERTIFICATE");
        ctMgr.entryPoints.put(35000000L, "QYCU_RETRIEVEPOPULATECERTIFICATEINFORMATION");
        ctMgr.localFailures.put(5L, "QYCU_OKAYTODELETEORREMOVETRUST");
        programs.put("QYCUCTMGR", ctMgr);

        // Table 5: QYCUGENMGR
        ProgramInfo genMgr = new ProgramInfo("General DCM/PKIX Manager");
        genMgr.entryPoints.put(1000000L, "QYCU_DCMSTARTUP");
        genMgr.entryPoints.put(2000000L, "QYCU_REMOVEPKIXCAURL");
        genMgr.entryPoints.put(3000000L, "QYCU_RETRIEVEPKIXCAURL");
        genMgr.entryPoints.put(4000000L, "QYCU_UPDATEPKIXCAURL");
        genMgr.localFailures.put(10L, "YCU_INSERTURLENTRY");
        genMgr.localFailures.put(20L, "YCU_REMOVEURLENTRY");
        genMgr.localFailures.put(30L, "YCU_RETRIEVEURLENTRY");
        programs.put("QYCUGENMGR", genMgr);

        // Table 6: QYCUHDWMGR (Cryptographic Coprocessors 4767, 4769, 4770, PCIe Crypto)
        ProgramInfo hdwMgr = new ProgramInfo("Hardware Cryptographic Device Manager");
        hdwMgr.entryPoints.put(1000000L, "QYCU_GETCRYPTODEVICELIST");
        hdwMgr.entryPoints.put(2000000L, "QYCU_GETOTHERCRYPTODEVICES");
        hdwMgr.entryPoints.put(3000000L, "QYCU_ADDCRYPTODEVICEASSIGNMENT");
        hdwMgr.entryPoints.put(4000000L, "QYCU_CHANGECRYPTODEVICEASSIGNMENT");
        hdwMgr.entryPoints.put(5000000L, "QYCU_GETALLCRYPTODEVICESBYCERT");
        hdwMgr.entryPoints.put(6000000L, "QYCU_GETCRYPTODEVICESFORCERT");
        hdwMgr.entryPoints.put(7000000L, "QYCU_REMOVECRYPTODEVICEASSIGNMENT");
        programs.put("QYCUHDWMGR", hdwMgr);

        // Table 7: QYCULCAMGR
        ProgramInfo lcaMgr = new ProgramInfo("Local Certificate Authority Manager");
        lcaMgr.entryPoints.put(1000000L, "QYCU_CREATELOCALCA");
        lcaMgr.entryPoints.put(2000000L, "QYCU_DELETELOCALCA");
        lcaMgr.entryPoints.put(3000000L, "QYCU_EXPORTLOCALCA");
        lcaMgr.entryPoints.put(4000000L, "QYCU_UPDATELOCALCA");
        lcaMgr.entryPoints.put(5000000L, "QYCU_GETLOCALCAPOLICY");
        lcaMgr.entryPoints.put(6000000L, "QYCU_RECEIVECACERT");
        lcaMgr.entryPoints.put(7000000L, "QYCU_RECEIVESERVERCERT");
        lcaMgr.entryPoints.put(8000000L, "QYCU_SETLOCALCAPOLICY");
        lcaMgr.entryPoints.put(9000000L, "QYCU_GETCACERTIFICATE");
        lcaMgr.entryPoints.put(10000000L, "QYCU_GETISSUECLIENTPOLICY");
        lcaMgr.entryPoints.put(11000000L, "QYCU_EXPORTLOCALCATOSTORE");
        lcaMgr.entryPoints.put(12000000L, "QYCU_RETRIEVELOCALCACERTIFICATEINFORMATION");
        lcaMgr.localFailures.put(1L, "YCU_NO_CERT_FILE");
        lcaMgr.localFailures.put(2L, "YCU_CERT_OPEN");
        lcaMgr.localFailures.put(3L, "YCU_CERT_READ");
        lcaMgr.localFailures.put(4L, "YCU_CONVERT_CERT");
        programs.put("QYCULCAMGR", lcaMgr);

        // Table 8: QYCUUCMGR
        ProgramInfo ucMgr = new ProgramInfo("User Certificate Manager");
        ucMgr.entryPoints.put(1000000L, "QYCU_USERCERTCREATEFROMOTHERBROWSER");
        ucMgr.entryPoints.put(2000000L, "QYCU_USERCERTCREATEFROMMSIEBROWSER");
        ucMgr.entryPoints.put(3000000L, "QYCU_USERCERTASSIGN");
        ucMgr.entryPoints.put(4000000L, "QYCU_USERCERTASSIGN_2");
        ucMgr.entryPoints.put(5000000L, "QYCU_USERCERTREMOVE");
        ucMgr.entryPoints.put(6000000L, "QYCU_USERCERTGETVALIDITYPERIOD");
        ucMgr.entryPoints.put(7000000L, "QYCU_USERCERTREMOVEFORCURRENT");
        ucMgr.entryPoints.put(8000000L, "QYCU_USERCERTRETRIEVE");
        ucMgr.entryPoints.put(9000000L, "QYCU_USERCERTRETRIEVEFORCURRENT");
        ucMgr.entryPoints.put(10000000L, "QYCU_USERCERTLIST");
        ucMgr.entryPoints.put(11000000L, "QYCU_USERCERTLISTFORCURRENT");
        ucMgr.entryPoints.put(12000000L, "QYCU_USERGENANDSIGNCERTREQUEST");
        ucMgr.entryPoints.put(13000000L, "QYCU_USERSIGNCERTREQUEST");
        ucMgr.entryPoints.put(14000000L, "QYCU_USERCERTEXPLIST");
        ucMgr.entryPoints.put(15000000L, "QYCU_USERCERTEXPLISTFORCURRENT");
        ucMgr.entryPoints.put(16000000L, "QYCU_REMOVEDIGITALIDCONFIGINFO");
        ucMgr.entryPoints.put(17000000L, "QYCU_RETRIEVEDIGITALIDCONFIGINFO");
        ucMgr.entryPoints.put(18000000L, "QYCU_SETDIGITALIDCONFIGINFO");
        ucMgr.localFailures.put(10L, "YCU_CREATEUSRSPCOBJECT");
        ucMgr.localFailures.put(40L, "YCU_CONVERTBROWSERINFO");
        ucMgr.localFailures.put(50L, "YCU_CONVERTTOPKCS7");
        ucMgr.localFailures.put(60L, "YCU_GETUSERCERTLIST");
        ucMgr.localFailures.put(70L, "YCU_REMOVEUSERCERT");
        ucMgr.localFailures.put(80L, "YCU_ADDUSERCERT");
        ucMgr.localFailures.put(90L, "YCU_FINDCERTUSER");
        ucMgr.localFailures.put(95L, "YCU_PARSEUSERCERT");
        programs.put("QYCUUCMGR", ucMgr);

        // Table 9: QYCUYDOMGR
        ProgramInfo ydoMgr = new ProgramInfo("Object Signing Manager");
        ydoMgr.entryPoints.put(1000000L, "QYCU_SIGNOBJECT");
        ydoMgr.entryPoints.put(2000000L, "QYCU_VERIFYSIGNATURE");
        ydoMgr.entryPoints.put(3000000L, "QYCU_SIGNOBJECTBATCH");
        ydoMgr.entryPoints.put(4000000L, "QYCU_VERIFYSIGNATUREBATCH");
        ydoMgr.entryPoints.put(5000000L, "QYCU_RETRIEVESIGNATURE");
        ydoMgr.entryPoints.put(6000000L, "QYCU_BROWSEDIRECTORY");
        ydoMgr.entryPoints.put(7000000L, "QYCU_SIGNOBJECTINBATCH");
        ydoMgr.entryPoints.put(8000000L, "QYCU_VERIFYSIGNATUREINBATCH");
        programs.put("QYCUYDOMGR", ydoMgr);

        // Table 10: Utility Functions (qycuutl.H)
        addUtility(100L, "QYCU_RTVAPPS", "Application Utility");
        addUtility(1000L, "QYCU_UPDATEAPPCATRUST", "Application Utility");
        addUtility(2000L, "QYCU_REMOVEAPPSASSIGNEDCERT", "Application Utility");
        addUtility(3000L, "QYCU_ASSIGNCERTTOAPP", "Application Utility");
        addUtility(10000L, "QYCU_ADDCRLASSOCIATION", "Certificate Utility");
        addUtility(10500L, "QYCU_BASE64DECODEBUFTOBUF", "Certificate Utility");
        addUtility(11000L, "QYCU_BASE64ENCODEBUFTOBUF", "Certificate Utility");
        addUtility(11500L, "QYCU_CHANGECRYPTODEVICELIST", "Certificate Utility");
        addUtility(12000L, "QYCU_CHECKFORBASE64FILE", "Certificate Utility");
        addUtility(12500L, "QYCU_CHECKFORX509CERT", "Certificate Utility");
        addUtility(13000L, "QYCU_CHECKIFTHEREANDSTORE", "Certificate Utility");
        addUtility(13500L, "QYCU_CHECKIFTHEREANDSTORE2", "Certificate Utility");
        addUtility(14000L, "QYCU_CREATECERTREQ", "Certificate Utility");
        addUtility(14500L, "QYCU_CREATESELFSIGNED", "Certificate Utility");
        addUtility(15000L, "QYCU_CREATESIGNEDCERT", "Certificate Utility");
        addUtility(15500L, "QYCU_DELETECERT", "Certificate Utility");
        addUtility(16000L, "QYCU_DELETEREQUESTCERT", "Certificate Utility");
        addUtility(16500L, "QYCU_ENCRYPTWITHPRIVATEKEY", "Certificate Utility");
        addUtility(17000L, "QYCU_ENCRYPTWITHPUBLICKEY", "Certificate Utility");
        addUtility(17500L, "QYCU_EXPORTCERT", "Certificate Utility");
        addUtility(18000L, "QYCU_EXPORTCERTDATA", "Certificate Utility");
        addUtility(18500L, "QYCU_EXPORTCERT_2", "Certificate Utility");
        addUtility(19000L, "QYCU_EXTRACTCERTDATA", "Certificate Utility");
        addUtility(19500L, "QYCU_EXTRACTFILETOBUF", "Certificate Utility");
        addUtility(20000L, "QYCU_EXTRACTFILETOBUF2", "Certificate Utility");
        addUtility(20500L, "QYCU_EXTRACTFILETOBUF3", "Certificate Utility");
        addUtility(21000L, "QYCU_GETCERTIFICATEITEM", "Certificate Utility");
        addUtility(21500L, "QYCU_GETCERTIFICATEITEM_2", "Certificate Utility");
        addUtility(22000L, "QYCU_GETCERTIFICATELABELLIST", "Certificate Utility");
        addUtility(22500L, "QYCU_GETCERTIFICATESTATUS", "Certificate Utility");
        addUtility(23000L, "QYCU_GETCERTSIGNERS", "Certificate Utility");
        addUtility(23500L, "QYCU_GETCERTSIGNERS2", "Certificate Utility");
        addUtility(24000L, "QYCU_GETCRLASSOCIATION", "Certificate Utility");
        addUtility(24500L, "QYCU_GET1STCERTWITHPRIVATEKEY", "Certificate Utility");
        addUtility(25000L, "QYCU_GETREQUESTCERTIFICATEITEM", "Certificate Utility");
        addUtility(25500L, "QYCU_GETREQUESTCERTIFICATEITEM_2", "Certificate Utility");
        addUtility(26000L, "QYCU_GETREQUESTCERTIFICATELABELLIST", "Certificate Utility");
        addUtility(26500L, "QYCU_IMPORTCERT", "Certificate Utility");
        addUtility(26700L, "QYCU_IMPORTCERTWITHLABEL", "Certificate Utility");
        addUtility(27000L, "QYCU_ISSUECERT", "Certificate Utility");
        addUtility(27500L, "QYCU_RECEIVECERT", "Certificate Utility");
        addUtility(28000L, "QYCU_REMOVECRLASSOCIATION", "Certificate Utility");
        addUtility(28500L, "QYCU_REMOVECRLASSOCIATION2", "Certificate Utility");
        addUtility(29000L, "QYCU_REMOVEPRIVATEKEYINFO", "Certificate Utility");
        addUtility(29500L, "QYCU_RENEWCERT", "Certificate Utility");
        addUtility(30000L, "QYCU_RETRIEVECERTITEM", "Certificate Utility");
        addUtility(30500L, "QYCU_SETCERTIFICATETRUSTSTATUS", "Certificate Utility");
        addUtility(31000L, "QYCU_STORECACERTIFICATE", "Certificate Utility");
        addUtility(31500L, "QYCU_VALIDATECERT", "Certificate Utility");
        addUtility(50000L, "QYCU_SETDEFAULTCERT", "Certificate Store Utility");
        addUtility(51000L, "QYCU_OPENCERTIFICATESTORE3", "Certificate Store Utility");
        addUtility(52000L, "QYCU_CERTSTOREPASSWORD", "Certificate Store Utility");
        addUtility(53000L, "QYCU_CHANGESTASHFILEPASSWORD", "Certificate Store Utility");
        addUtility(54000L, "QYCU_OPENCERTIFICATESTORE", "Certificate Store Utility");
        addUtility(55000L, "QYCU_OPENCERTIFICATESTORE2", "Certificate Store Utility");
        addUtility(56000L, "QYCU_CONVETRKDBTOKYR", "Certificate Store Utility");
        addUtility(57000L, "QYCU_GETSTASHFILENAME", "Certificate Store Utility");
        addUtility(58000L, "QYCU_GETREQUESTFILENAME", "Certificate Store Utility");
        addUtility(59000L, "QYCU_CREATECERTSTORE", "Certificate Store Utility");
        addUtility(60000L, "QYCU_STASHPASSWORD", "Certificate Store Utility");
        addUtility(62000L, "QYCU_OPENCERTIFICATESTORE4", "Certificate Store Utility");
        addUtility(63000L, "QYCU_GETCERTSTOREPASSWORD", "Certificate Store Utility");
        addUtility(70000L, "QYCU_RENAME", "Certificate Store Utility");
        addUtility(70500L, "QYCU_CHGOWN", "Certificate Store Utility");
        addUtility(71000L, "QYCU_CPYOBJ", "Certificate Store Utility");
        addUtility(71500L, "QYCU_TOD_ERROR", "Certificate Store Utility");
        addUtility(72000L, "QYCU_TOD_ERROR2", "Certificate Store Utility");
        addUtility(72500L, "QYCU_ALLOCATE_IOVEC1", "Certificate Store Utility");
        addUtility(73000L, "QYCU_ALLOCATE_IOVEC2", "Certificate Store Utility");
        addUtility(73500L, "QYCU_MALLOC_STORAGE", "Certificate Store Utility");
        addUtility(74000L, "QYCU_CREATE_USRSPC_OBJECT", "Certificate Store Utility");
        addUtility(74500L, "QYCU_CONVERT_DATA_TO_ASCII", "Certificate Store Utility");
        addUtility(75000L, "QYCU_CONVERT_DATA_TO_JOB_CCSID", "Certificate Store Utility");
        addUtility(80000L, "QYCU_READ_POLICY", "Local Certificate Authority Utility");
        addUtility(81000L, "QYCU_READ_POLICY2", "Local Certificate Authority Utility");
        addUtility(82000L, "QYCU_READ_POLICY3", "Local Certificate Authority Utility");
        addUtility(83000L, "QYCU_CREATE_POLICY", "Local Certificate Authority Utility");
        addUtility(84000L, "QYCU_FLUSH_POLICY", "Local Certificate Authority Utility");
        addUtility(85000L, "QYCU_FLUSH_POLICY2", "Local Certificate Authority Utility");
        addUtility(86000L, "QYCU_FLUSH_POLICY3", "Local Certificate Authority Utility");
        addUtility(87000L, "QYCU_CHGOWN_POLICY", "Local Certificate Authority Utility");
        addUtility(90000L, "QYCU_CREATE_CRL_VLDL", "Certificate Revocation List Utility");
        addUtility(91000L, "QYCU_FIND_CRL_VLDL", "Certificate Revocation List Utility");
        addUtility(92000L, "QYCU_LOCK_CRL_VLDL", "Certificate Revocation List Utility");
        addUtility(93000L, "QYCU_RTV_VLDL_ENTRY", "Certificate Revocation List Utility");
        addUtility(100000L, "QYCU_RTV_CRYPTO_RESOURCES", "Hardware Device Utility");
        addUtility(101000L, "QYCU_LIST_CRYPTO_DEVICES", "Hardware Device Utility");

        sortedUtilityCodes.addAll(utilityFunctions.keySet());
        Collections.sort(sortedUtilityCodes, Collections.reverseOrder());
    }

    private void addUtility(long code, String name, String category) {
        utilityFunctions.put(code, new UtilityInfo(name, category));
    }

    public Map<String, Object> decode(String rawInput) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (rawInput == null || rawInput.trim().isEmpty()) {
            result.put("success", false);
            result.put("error", "Empty search input provided.");
            result.put("cspReminder", "Reminder for technicians: Search iAI assisted support searches in CSP.");
            return result;
        }

        String input = rawInput.trim();
        result.put("input", input);

        // Pattern 1: Standard IBM symptom string
        Pattern symptomPat = Pattern.compile("(?:(\\d{4})\\s+)?(?:SP/)?([A-Z0-9_]+)\\s+(?:MOD/)?([A-Z0-9_]+)\\s+(?:RC)?(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = symptomPat.matcher(input);

        if (matcher.find()) {
            String release = matcher.group(1) != null ? matcher.group(1) : "N/A";
            String sp = matcher.group(2).toUpperCase();
            String mod = matcher.group(3).toUpperCase();
            long rc = Long.parseLong(matcher.group(4));
            return deconstructWithProgram(input, release, sp, mod, rc);
        }

        // Pattern 2: Service Program + RC
        Pattern spRcPat = Pattern.compile("([A-Z0-9_]+)\\s+(?:RC)?(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher spRcMatcher = spRcPat.matcher(input);
        if (spRcMatcher.find() && programs.containsKey(spRcMatcher.group(1).toUpperCase())) {
            String sp = spRcMatcher.group(1).toUpperCase();
            long rc = Long.parseLong(spRcMatcher.group(2));
            return deconstructWithProgram(input, "N/A", sp, sp, rc);
        }

        // Pattern 3: Pure numeric return code
        Pattern numPat = Pattern.compile("^(?:RC\\s*)?(\\d+)$", Pattern.CASE_INSENSITIVE);
        Matcher numMatcher = numPat.matcher(input);
        if (numMatcher.find()) {
            long rc = Long.parseLong(numMatcher.group(1));
            return findBestGlobalMatch(input, rc);
        }

        // Pattern 4: Keyword / Regex search
        return searchKeyword(input);
    }

    private Map<String, Object> deconstructWithProgram(String raw, String release, String sp, String mod, long rc) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("input", raw);
        result.put("release", release);
        result.put("serviceProgram", sp);
        result.put("module", mod);
        result.put("returnCode", rc);

        ProgramInfo prog = programs.get(sp);
        if (prog == null) {
            result.put("success", false);
            result.put("isClosest", true);
            result.put("error", "Service Program '" + sp + "' not found in DCM catalog.");
            result.put("cspReminder", "Reminder for technicians: Search iAI assisted support searches in CSP.");
            return result;
        }

        result.put("serviceProgramName", prog.name);

        long entryVal = (rc / 1000000L) * 1000000L;
        long remainder = rc - entryVal;
        String entryName = prog.entryPoints.get(entryVal);

        if (entryName == null && prog.entryPoints.containsKey(rc)) {
            entryName = prog.entryPoints.get(rc);
            entryVal = rc;
            remainder = 0;
        }

        long utilVal = 0;
        String utilName = null;
        String utilCategory = null;
        long localVal = 0;
        String localName = null;

        if (remainder > 0) {
            if (utilityFunctions.containsKey(remainder)) {
                utilVal = remainder;
                UtilityInfo u = utilityFunctions.get(remainder);
                utilName = u.name;
                utilCategory = u.category;
            } else {
                for (Long uCode : sortedUtilityCodes) {
                    if (uCode <= remainder) {
                        long diff = remainder - uCode;
                        if (diff <= 99) {
                            utilVal = uCode;
                            UtilityInfo u = utilityFunctions.get(uCode);
                            utilName = u.name;
                            utilCategory = u.category;
                            localVal = diff;
                            if (prog.localFailures.containsKey(localVal)) {
                                localName = prog.localFailures.get(localVal);
                            }
                            break;
                        }
                    }
                }
                if (utilName == null && prog.localFailures.containsKey(remainder)) {
                    localVal = remainder;
                    localName = prog.localFailures.get(remainder);
                }
            }
        }

        boolean isExact = (entryName != null || entryVal == 0) && (remainder == 0 || utilName != null || localName != null);

        StringBuilder useCase = new StringBuilder();
        useCase.append("During processing in ").append(prog.name).append(" (").append(sp).append(")");
        if (entryName != null) {
            useCase.append(", entry point '").append(entryName).append("' was executing");
        } else if (entryVal > 0) {
            useCase.append(", entry point code ").append(entryVal).append(" was invoked");
        }
        if (utilName != null) {
            useCase.append(". A failure occurred in utility function '").append(utilName).append("' (").append(utilCategory).append(")");
        }
        if (localName != null) {
            useCase.append(", specifically at local point of failure '").append(localName).append("' (code ").append(localVal).append(")");
        } else if (localVal > 0) {
            useCase.append(", specifically at local offset code ").append(localVal);
        }
        useCase.append(".");

        result.put("success", true);
        result.put("isExact", isExact);
        result.put("isClosest", !isExact);
        result.put("majorEntryPointCode", entryVal);
        result.put("majorEntryPointName", entryName != null ? entryName : "N/A");
        result.put("utilityFunctionCode", utilVal);
        result.put("utilityFunctionName", utilName != null ? utilName : "None (Direct Module Failure)");
        result.put("utilityFunctionCategory", utilCategory);
        result.put("localFailureCode", localVal);
        result.put("localFailureName", localName);
        result.put("useCaseExplanation", useCase.toString());

        if (!isExact) {
            result.put("cspReminder", "Reminder for technicians: Search iAI assisted support searches in CSP.");
        }

        return result;
    }

    private Map<String, Object> findBestGlobalMatch(String raw, long rc) {
        for (String sp : programs.keySet()) {
            Map<String, Object> dec = deconstructWithProgram(raw, "N/A", sp, sp, rc);
            if (Boolean.TRUE.equals(dec.get("isExact"))) {
                return dec;
            }
        }

        long entryVal = (rc / 1000000L) * 1000000L;
        for (String sp : programs.keySet()) {
            if (programs.get(sp).entryPoints.containsKey(entryVal)) {
                return deconstructWithProgram(raw, "N/A", sp, sp, rc);
            }
        }

        if (utilityFunctions.containsKey(rc)) {
            UtilityInfo u = utilityFunctions.get(rc);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("isExact", true);
            result.put("input", raw);
            result.put("serviceProgram", "qycuutl.H");
            result.put("serviceProgramName", "DCM Utility Module");
            result.put("utilityFunctionCode", rc);
            result.put("utilityFunctionName", u.name);
            result.put("utilityFunctionCategory", u.category);
            result.put("useCaseExplanation", "Direct match for Utility Function '" + u.name + "' (" + u.category + ") with reason code " + rc + ".");
            return result;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("isClosest", true);
        result.put("input", raw);
        result.put("returnCode", rc);
        result.put("error", "Return code " + rc + " could not be directly matched.");
        result.put("cspReminder", "Reminder for technicians: Search iAI assisted support searches in CSP.");
        return result;
    }

    private Map<String, Object> searchKeyword(String query) {
        Pattern regex;
        try {
            regex = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            regex = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
        }

        List<Map<String, String>> matches = new ArrayList<>();

        for (Map.Entry<String, ProgramInfo> pEntry : programs.entrySet()) {
            String sp = pEntry.getKey();
            ProgramInfo p = pEntry.getValue();
            if (regex.matcher(sp).find() || regex.matcher(p.name).find()) {
                Map<String, String> m = new HashMap<>();
                m.put("type", "Service Program");
                m.put("location", sp);
                m.put("name", p.name);
                m.put("code", "N/A");
                matches.add(m);
            }
            for (Map.Entry<Long, String> ep : p.entryPoints.entrySet()) {
                if (regex.matcher(ep.getValue()).find() || regex.matcher(String.valueOf(ep.getKey())).find()) {
                    Map<String, String> m = new HashMap<>();
                    m.put("type", "Entry Point");
                    m.put("location", sp + " (" + p.name + ")");
                    m.put("name", ep.getValue());
                    m.put("code", "+" + ep.getKey());
                    matches.add(m);
                }
            }
            for (Map.Entry<Long, String> lf : p.localFailures.entrySet()) {
                if (regex.matcher(lf.getValue()).find() || regex.matcher(String.valueOf(lf.getKey())).find()) {
                    Map<String, String> m = new HashMap<>();
                    m.put("type", "Local Failure");
                    m.put("location", sp + " (" + p.name + ")");
                    m.put("name", lf.getValue());
                    m.put("code", "+" + lf.getKey());
                    matches.add(m);
                }
            }
        }

        for (Map.Entry<Long, UtilityInfo> uEntry : utilityFunctions.entrySet()) {
            UtilityInfo u = uEntry.getValue();
            if (regex.matcher(u.name).find() || regex.matcher(u.category).find() || regex.matcher(String.valueOf(uEntry.getKey())).find()) {
                Map<String, String> m = new HashMap<>();
                m.put("type", "Utility Function");
                m.put("location", "qycuutl.H (" + u.category + ")");
                m.put("name", u.name);
                m.put("code", "+" + uEntry.getKey());
                matches.add(m);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", !matches.isEmpty());
        result.put("isKeywordSearch", true);
        result.put("query", query);
        result.put("matches", matches);
        if (matches.isEmpty()) {
            result.put("cspReminder", "Reminder for technicians: Search iAI assisted support searches in CSP.");
        }
        return result;
    }
}
