package com.ibmi.ias.service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Robust Multi-Tier Search, GSKit & ADMIN3 Log Analyzer Service for IBM i Digital Certificate Manager (DCM).
 * Integrates 9 Service Program Managers, 95+ Utilities, 107 QYCU RetCodes, and 187 GSKKM/GSKit Return Codes.
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

    public static class GskDetail {
        public long code;
        public String constant;
        public String originFunction;
        public String serviceProgram;
        public String entryPoint;
        public String jniFunction;
        public String targetObject;
        public String failureDomain;
        public String rootCause;
        public String msgId;
        public List<String> recoveryCommands = new ArrayList<>();

        public GskDetail(long code, String constant, String originFunction, String serviceProgram, String entryPoint, String jniFunction, String targetObject, String failureDomain, String rootCause, String msgId, String... cmds) {
            this.code = code;
            this.constant = constant;
            this.originFunction = originFunction;
            this.serviceProgram = serviceProgram;
            this.entryPoint = entryPoint;
            this.jniFunction = jniFunction;
            this.targetObject = targetObject;
            this.failureDomain = failureDomain;
            this.rootCause = rootCause;
            this.msgId = msgId;
            if (cmds != null) {
                this.recoveryCommands.addAll(Arrays.asList(cmds));
            }
        }
    }

    private final Map<String, ProgramInfo> programs = new HashMap<>();
    private final Map<Long, UtilityInfo> utilityFunctions = new HashMap<>();
    private final List<Long> sortedUtilityCodes = new ArrayList<>();
    private final Map<Long, String> qycuretcodes = new HashMap<>();
    private final Map<Long, String> gskkmerr = new HashMap<>();
    private final Map<Long, GskDetail> gskDetails = new HashMap<>();

    public DcmSearchService() {
        initDatabase();
    }

    private void initDatabase() {
        ProgramInfo p_QYCUHDWMGR = new ProgramInfo("Hardware Device Manager");
        p_QYCUHDWMGR.entryPoints.put(4000000L, "QYCU_CHANGECRYPTODEVICEASSIGNMENT");
        p_QYCUHDWMGR.entryPoints.put(6000000L, "QYCU_GETCRYPTODEVICESFORCERT");
        p_QYCUHDWMGR.entryPoints.put(1000000L, "QYCU_GETCRYPTODEVICELIST");
        p_QYCUHDWMGR.entryPoints.put(3000000L, "QYCU_ADDCRYPTODEVICEASSIGNMENT");
        p_QYCUHDWMGR.entryPoints.put(5000000L, "QYCU_GETALLCRYPTODEVICESBYCERT");
        p_QYCUHDWMGR.entryPoints.put(7000000L, "QYCU_REMOVECRYPTODEVICEASSIGNMENT");
        p_QYCUHDWMGR.entryPoints.put(2000000L, "QYCU_GETOTHERCRYPTODEVICES");
        programs.put("QYCUHDWMGR", p_QYCUHDWMGR);
        ProgramInfo p_QYCUCTMGR = new ProgramInfo("Certificate Manager");
        p_QYCUCTMGR.entryPoints.put(6000000L, "QYCU_DELETECERTIFICATEREQUEST");
        p_QYCUCTMGR.entryPoints.put(32000000L, "QYCU_LISTCAFILES");
        p_QYCUCTMGR.entryPoints.put(10000000L, "QYCU_LISTCERTIFICATEREQUESTS");
        p_QYCUCTMGR.entryPoints.put(24000000L, "QYCU_LISTCERTIFICATESWITHKEYINHARDWARE");
        p_QYCUCTMGR.entryPoints.put(18000000L, "QYCU_CHECKFORBASE64CERTIFICATE");
        p_QYCUCTMGR.entryPoints.put(15000000L, "QYCU_EXPORTCERTIFICATETOSTORE");
        p_QYCUCTMGR.entryPoints.put(30000000L, "QYCU_CHECKCERTIFICATEEXPIRATION");
        p_QYCUCTMGR.entryPoints.put(27000000L, "QYCU_RENEWCERTIFICATE");
        p_QYCUCTMGR.entryPoints.put(31000000L, "QYCU_IMPORTCERTIFICATEWITHLABEL");
        p_QYCUCTMGR.entryPoints.put(5000000L, "QYCU_DELETECERTIFICATE");
        p_QYCUCTMGR.entryPoints.put(28000000L, "QYCU_CHECKFORX509CERTIFICATE");
        p_QYCUCTMGR.entryPoints.put(8000000L, "QYCU_IMPORTCERTIFICATE");
        p_QYCUCTMGR.entryPoints.put(5L, "QYCU_OKAYTODELETEORREMOVETRUST");
        p_QYCUCTMGR.entryPoints.put(17000000L, "QYCU_EXPORTCERTIFICATEWITHOUTKEYTOSTORE");
        p_QYCUCTMGR.entryPoints.put(12000000L, "QYCU_RETRIEVECERTIFICATEINFORMATION");
        p_QYCUCTMGR.entryPoints.put(22000000L, "QYCU_CREATESTOREANDCERTIFICATEREQUEST");
        p_QYCUCTMGR.entryPoints.put(25000000L, "QYCU_VALIDATECERTIFICATE");
        p_QYCUCTMGR.entryPoints.put(2000000L, "QYCU_CREATELOCALCERTIFICATE");
        p_QYCUCTMGR.entryPoints.put(33000000L, "QYCU_LISTCACERTIFICATEFORPOPULATE");
        p_QYCUCTMGR.entryPoints.put(34000000L, "QYCU_POPULATETRUSTCACERTIFICATE");
        p_QYCUCTMGR.entryPoints.put(7000000L, "QYCU_EXPORTCERTIFICATE");
        p_QYCUCTMGR.entryPoints.put(35000000L, "QYCU_RETRIEVEPOPULATECERTIFICATEINFORMATION");
        p_QYCUCTMGR.entryPoints.put(1000000L, "QYCU_ADDCERTIFICATETRUST");
        p_QYCUCTMGR.entryPoints.put(19000000L, "QYCU_RETRIEVECACERTIFICATEINFORMATION");
        p_QYCUCTMGR.entryPoints.put(20000000L, "QYCU_ADDCRLASSOCIATIONTOCA");
        p_QYCUCTMGR.entryPoints.put(11000000L, "QYCU_REMOVECERTIFICATETRUST");
        p_QYCUCTMGR.entryPoints.put(16000000L, "QYCU_EXPORTCERTIFICATEWITHOUTKEY");
        p_QYCUCTMGR.entryPoints.put(14000000L, "QYCU_REMOVECERTIFICATEREQUESTFILE");
        p_QYCUCTMGR.entryPoints.put(23000000L, "QYCU_EXPORTALLCERTIFICATESWITHOUTKEYTOSTORE");
        p_QYCUCTMGR.entryPoints.put(26000000L, "QYCU_VALIDATECACERTIFICATE");
        p_QYCUCTMGR.entryPoints.put(4000000L, "QYCU_CREATESTOREANDCERTIFICATE");
        p_QYCUCTMGR.entryPoints.put(3000000L, "QYCU_CREATENONLOCALCERTIFICATE");
        p_QYCUCTMGR.entryPoints.put(29000000L, "QYCU_PREVIEWCERTIFICATEINFORMATION");
        p_QYCUCTMGR.entryPoints.put(13000000L, "QYCU_RETRIEVECERTIFICATEREQUESTINFORMATION");
        p_QYCUCTMGR.entryPoints.put(9000000L, "QYCU_LISTCERTIFICATES");
        p_QYCUCTMGR.entryPoints.put(21000000L, "QYCU_REMOVECRLASSOCIATIONFROMCA");
        programs.put("QYCUCTMGR", p_QYCUCTMGR);
        ProgramInfo p_QYCUUCMGR = new ProgramInfo("User Certificate Manager");
        p_QYCUUCMGR.entryPoints.put(18000000L, "QYCU_SETDIGITALIDCONFIGINFO");
        p_QYCUUCMGR.entryPoints.put(1000000L, "QYCU_USERCERTCREATEFROMOTHERBROWSER");
        p_QYCUUCMGR.entryPoints.put(9000000L, "QYCU_USERCERTRETRIEVEFORCURRENT");
        p_QYCUUCMGR.entryPoints.put(95L, "YCU_PARSEUSERCERT");
        p_QYCUUCMGR.entryPoints.put(12000000L, "QYCU_USERGENANDSIGNCERTREQUEST");
        p_QYCUUCMGR.entryPoints.put(90L, "YCU_FINDCERTUSER");
        p_QYCUUCMGR.entryPoints.put(4000000L, "QYCU_USERCERTASSIGN_2");
        p_QYCUUCMGR.entryPoints.put(6000000L, "QYCU_USERCERTGETVALIDITYPERIOD");
        p_QYCUUCMGR.entryPoints.put(14000000L, "QYCU_USERCERTEXPLIST");
        p_QYCUUCMGR.entryPoints.put(5000000L, "QYCU_USERCERTREMOVE");
        p_QYCUUCMGR.entryPoints.put(80L, "YCU_ADDUSERCERT");
        p_QYCUUCMGR.entryPoints.put(2000000L, "QYCU_USERCERTCREATEFROMMSIEBROWSER");
        p_QYCUUCMGR.entryPoints.put(8000000L, "QYCU_USERCERTRETRIEVE");
        p_QYCUUCMGR.entryPoints.put(13000000L, "QYCU_USERSIGNCERTREQUEST");
        p_QYCUUCMGR.entryPoints.put(10L, "YCU_CREATEUSRSPCOBJECT");
        p_QYCUUCMGR.entryPoints.put(17000000L, "QYCU_RETRIEVEDIGITALIDCONFIGINFO");
        p_QYCUUCMGR.entryPoints.put(40L, "YCU_CONVERTBROWSERINFO");
        p_QYCUUCMGR.entryPoints.put(3000000L, "QYCU_USERCERTASSIGN");
        p_QYCUUCMGR.entryPoints.put(10000000L, "QYCU_USERCERTLIST");
        p_QYCUUCMGR.entryPoints.put(70L, "YCU_REMOVEUSERCERT");
        p_QYCUUCMGR.entryPoints.put(50L, "YCU_CONVERTTOPKCS7");
        p_QYCUUCMGR.entryPoints.put(16000000L, "QYCU_REMOVEDIGITALIDCONFIGINFO");
        p_QYCUUCMGR.entryPoints.put(11000000L, "QYCU_USERCERTLISTFORCURRENT");
        p_QYCUUCMGR.entryPoints.put(60L, "YCU_GETUSERCERTLIST");
        p_QYCUUCMGR.entryPoints.put(7000000L, "QYCU_USERCERTREMOVEFORCURRENT");
        p_QYCUUCMGR.entryPoints.put(15000000L, "QYCU_USERCERTEXPLISTFORCURRENT");
        programs.put("QYCUUCMGR", p_QYCUUCMGR);
        ProgramInfo p_QYCUGENMGR = new ProgramInfo("General DCM/PKIX Manager");
        p_QYCUGENMGR.entryPoints.put(4000000L, "QYCU_UPDATEPKIXCAURL");
        p_QYCUGENMGR.entryPoints.put(20L, "YCU_REMOVEURLENTRY");
        p_QYCUGENMGR.entryPoints.put(1000000L, "QYCU_DCMSTARTUP");
        p_QYCUGENMGR.entryPoints.put(3000000L, "QYCU_RETRIEVEPKIXCAURL");
        p_QYCUGENMGR.entryPoints.put(30L, "YCU_RETRIEVEURLENTRY");
        p_QYCUGENMGR.entryPoints.put(2000000L, "QYCU_REMOVEPKIXCAURL");
        p_QYCUGENMGR.entryPoints.put(10L, "YCU_INSERTURLENTRY");
        programs.put("QYCUGENMGR", p_QYCUGENMGR);
        ProgramInfo p_QYCULCAMGR = new ProgramInfo("Local Certificate Authority Manager");
        p_QYCULCAMGR.entryPoints.put(1000000L, "QYCU_CREATELOCALCA");
        p_QYCULCAMGR.entryPoints.put(9000000L, "QYCU_GETCACERTIFICATE");
        p_QYCULCAMGR.entryPoints.put(12000000L, "QYCU_RETRIEVELOCALCACERTIFICATEINFORMATION");
        p_QYCULCAMGR.entryPoints.put(2L, "YCU_CERT_OPEN");
        p_QYCULCAMGR.entryPoints.put(4000000L, "QYCU_UPDATELOCALCA");
        p_QYCULCAMGR.entryPoints.put(6000000L, "QYCU_RECEIVECACERT");
        p_QYCULCAMGR.entryPoints.put(5000000L, "QYCU_GETLOCALCAPOLICY");
        p_QYCULCAMGR.entryPoints.put(2000000L, "QYCU_DELETELOCALCA");
        p_QYCULCAMGR.entryPoints.put(8000000L, "QYCU_SETLOCALCAPOLICY");
        p_QYCULCAMGR.entryPoints.put(1L, "YCU_NO_CERT_FILE");
        p_QYCULCAMGR.entryPoints.put(3L, "YCU_CERT_READ");
        p_QYCULCAMGR.entryPoints.put(3000000L, "QYCU_EXPORTLOCALCA");
        p_QYCULCAMGR.entryPoints.put(10000000L, "QYCU_GETISSUECLIENTPOLICY");
        p_QYCULCAMGR.entryPoints.put(4L, "YCU_CONVERT_CERT");
        p_QYCULCAMGR.entryPoints.put(11000000L, "QYCU_EXPORTLOCALCATOSTORE");
        p_QYCULCAMGR.entryPoints.put(7000000L, "QYCU_RECEIVESERVERCERT");
        programs.put("QYCULCAMGR", p_QYCULCAMGR);
        ProgramInfo p_QYCUCRLMGR = new ProgramInfo("Certificate Revocation List Manager");
        p_QYCUCRLMGR.entryPoints.put(4000000L, "QYCU_REMOVECRLLOCATION");
        p_QYCUCRLMGR.entryPoints.put(1000000L, "QYCU_ADDCRLLOCATION");
        p_QYCUCRLMGR.entryPoints.put(3000000L, "QYCU_LISTCRLLOCATION");
        p_QYCUCRLMGR.entryPoints.put(5000000L, "QYCU_UPDATECRLLOCATION");
        p_QYCUCRLMGR.entryPoints.put(2000000L, "QYCU_GETCRLLOCATION");
        p_QYCUCRLMGR.entryPoints.put(2L, "ADD_UPDATE_CRL_LOCATION");
        programs.put("QYCUCRLMGR", p_QYCUCRLMGR);
        ProgramInfo p_QYCUAPPMGR = new ProgramInfo("Application Manager");
        p_QYCUAPPMGR.entryPoints.put(1000000L, "QYCU_APPLIST");
        p_QYCUAPPMGR.entryPoints.put(9000000L, "QYCU_APPLISTUSINGSYSTEMSTORE");
        p_QYCUAPPMGR.entryPoints.put(12000000L, "QYCU_APPREMOVE");
        p_QYCUAPPMGR.entryPoints.put(4000000L, "QYCU_APPREMOVECERT");
        p_QYCUAPPMGR.entryPoints.put(6000000L, "QYCU_APPSETCATRUST");
        p_QYCUAPPMGR.entryPoints.put(14000000L, "QYCU_APPUPDATEDEFINITION");
        p_QYCUAPPMGR.entryPoints.put(5000000L, "QYCU_APPASSIGNCERT");
        p_QYCUAPPMGR.entryPoints.put(2000000L, "QYCU_APPLISTFORCATRUST");
        p_QYCUAPPMGR.entryPoints.put(8000000L, "QYCU_APPLISTTRUSTINGCA");
        p_QYCUAPPMGR.entryPoints.put(13000000L, "QYCU_APPREGISTER");
        p_QYCUAPPMGR.entryPoints.put(3000000L, "QYCU_APPLISTCAS");
        p_QYCUAPPMGR.entryPoints.put(10000000L, "QYCU_APPLISTUSINGOBJECTSIGNSTORE");
        p_QYCUAPPMGR.entryPoints.put(16000000L, "QYCU_APPVALIDATE");
        p_QYCUAPPMGR.entryPoints.put(11000000L, "QYCU_APPGETSELECTEDINFO");
        p_QYCUAPPMGR.entryPoints.put(7000000L, "QYCU_APPLISTASSIGNEDCERT");
        p_QYCUAPPMGR.entryPoints.put(15000000L, "QYCU_APPLISTFORSIGNING");
        programs.put("QYCUAPPMGR", p_QYCUAPPMGR);
        ProgramInfo p_QYCUYDOMGR = new ProgramInfo("Object Signing Manager");
        p_QYCUYDOMGR.entryPoints.put(4000000L, "QYCU_VERIFYSIGNATUREBATCH");
        p_QYCUYDOMGR.entryPoints.put(8000000L, "QYCU_VERIFYSIGNATUREINBATCH");
        p_QYCUYDOMGR.entryPoints.put(6000000L, "QYCU_BROWSEDIRECTORY");
        p_QYCUYDOMGR.entryPoints.put(1000000L, "QYCU_SIGNOBJECT");
        p_QYCUYDOMGR.entryPoints.put(3000000L, "QYCU_SIGNOBJECTBATCH");
        p_QYCUYDOMGR.entryPoints.put(5000000L, "QYCU_RETRIEVESIGNATURE");
        p_QYCUYDOMGR.entryPoints.put(7000000L, "QYCU_SIGNOBJECTINBATCH");
        p_QYCUYDOMGR.entryPoints.put(2000000L, "QYCU_VERIFYSIGNATURE");
        programs.put("QYCUYDOMGR", p_QYCUYDOMGR);
        ProgramInfo p_QYCUCSMGR = new ProgramInfo("Certificate Store Manager");
        p_QYCUCSMGR.entryPoints.put(4000000L, "QYCU_SETCERTIFICATESTOREDEFAULTCERTIFICATE");
        p_QYCUCSMGR.entryPoints.put(6000000L, "QYCU_STOREVERIFYFILE");
        p_QYCUCSMGR.entryPoints.put(1000000L, "QYCU_CHANGECERTIFICATESTOREPASSWORD");
        p_QYCUCSMGR.entryPoints.put(3000000L, "QYCU_DELETECERTIFICATESTORE");
        p_QYCUCSMGR.entryPoints.put(5000000L, "QYCU_STOREVERIFY");
        p_QYCUCSMGR.entryPoints.put(7000000L, "QYCU_RESETCERTIFICATESTOREPASSWORD");
        p_QYCUCSMGR.entryPoints.put(2000000L, "QYCU_CREATECERTIFICATESTORE");
        programs.put("QYCUCSMGR", p_QYCUCSMGR);
        utilityFunctions.put(25500L, new UtilityInfo("QYCU_GETREQUESTCERTIFICATEITEM_2", "Certificate Utility"));
        utilityFunctions.put(10500L, new UtilityInfo("QYCU_BASE64DECODEBUFTOBUF", "Certificate Utility"));
        utilityFunctions.put(101000L, new UtilityInfo("QYCU_LIST_CRYPTO_DEVICES", "Hardware Device Utility"));
        utilityFunctions.put(28000L, new UtilityInfo("QYCU_REMOVECRLASSOCIATION", "Certificate Utility"));
        utilityFunctions.put(27000L, new UtilityInfo("QYCU_ISSUECERT", "Certificate Utility"));
        utilityFunctions.put(57000L, new UtilityInfo("QYCU_GETSTASHFILENAME", "Certificate Store Utility"));
        utilityFunctions.put(3000L, new UtilityInfo("QYCU_ASSIGNCERTTOAPP", "Application Utility"));
        utilityFunctions.put(92000L, new UtilityInfo("QYCU_LOCK_CRL_VLDL", "Certificate Revocation List Utility"));
        utilityFunctions.put(14500L, new UtilityInfo("QYCU_CREATESELFSIGNED", "Certificate Utility"));
        utilityFunctions.put(74500L, new UtilityInfo("QYCU_CONVERT_DATA_TO_ASCII", "Certificate Store Utility"));
        utilityFunctions.put(22500L, new UtilityInfo("QYCU_GETCERTIFICATESTATUS", "Certificate Utility"));
        utilityFunctions.put(20500L, new UtilityInfo("QYCU_EXTRACTFILETOBUF3", "Certificate Utility"));
        utilityFunctions.put(72000L, new UtilityInfo("QYCU_TOD_ERROR2", "Certificate Store Utility"));
        utilityFunctions.put(17500L, new UtilityInfo("QYCU_EXPORTCERT", "Certificate Utility"));
        utilityFunctions.put(31500L, new UtilityInfo("QYCU_VALIDATECERT", "Certificate Utility"));
        utilityFunctions.put(19000L, new UtilityInfo("QYCU_EXTRACTCERTDATA", "Certificate Utility"));
        utilityFunctions.put(55000L, new UtilityInfo("QYCU_OPENCERTIFICATESTORE2", "Certificate Store Utility"));
        utilityFunctions.put(50000L, new UtilityInfo("QYCU_SETDEFAULTCERT", "Certificate Store Utility"));
        utilityFunctions.put(10000L, new UtilityInfo("QYCU_ADDCRLASSOCIATION", "Certificate Utility"));
        utilityFunctions.put(90000L, new UtilityInfo("QYCU_CREATE_CRL_VLDL", "Certificate Revocation List Utility"));
        utilityFunctions.put(81000L, new UtilityInfo("QYCU_READ_POLICY2", "Local Certificate Authority Utility"));
        utilityFunctions.put(82000L, new UtilityInfo("QYCU_READ_POLICY3", "Local Certificate Authority Utility"));
        utilityFunctions.put(56000L, new UtilityInfo("QYCU_CONVETRKDBTOKYR", "Certificate Store Utility"));
        utilityFunctions.put(73000L, new UtilityInfo("QYCU_ALLOCATE_IOVEC2", "Certificate Store Utility"));
        utilityFunctions.put(26700L, new UtilityInfo("QYCU_IMPORTCERTWITHLABEL", "Certificate Utility"));
        utilityFunctions.put(75000L, new UtilityInfo("QYCU_CONVERT_DATA_TO_JOB_CCSID", "Certificate Store Utility"));
        utilityFunctions.put(60000L, new UtilityInfo("QYCU_STASHPASSWORD", "Certificate Store Utility"));
        utilityFunctions.put(26500L, new UtilityInfo("QYCU_IMPORTCERT", "Certificate Utility"));
        utilityFunctions.put(100000L, new UtilityInfo("QYCU_RTV_CRYPTO_RESOURCES", "Hardware Device Utility"));
        utilityFunctions.put(13000L, new UtilityInfo("QYCU_CHECKIFTHEREANDSTORE", "Certificate Utility"));
        utilityFunctions.put(21000L, new UtilityInfo("QYCU_GETCERTIFICATEITEM", "Certificate Utility"));
        utilityFunctions.put(22000L, new UtilityInfo("QYCU_GETCERTIFICATELABELLIST", "Certificate Utility"));
        utilityFunctions.put(71500L, new UtilityInfo("QYCU_TOD_ERROR", "Certificate Store Utility"));
        utilityFunctions.put(54000L, new UtilityInfo("QYCU_OPENCERTIFICATESTORE", "Certificate Store Utility"));
        utilityFunctions.put(84000L, new UtilityInfo("QYCU_FLUSH_POLICY", "Local Certificate Authority Utility"));
        utilityFunctions.put(91000L, new UtilityInfo("QYCU_FIND_CRL_VLDL", "Certificate Revocation List Utility"));
        utilityFunctions.put(73500L, new UtilityInfo("QYCU_MALLOC_STORAGE", "Certificate Store Utility"));
        utilityFunctions.put(59000L, new UtilityInfo("QYCU_CREATECERTSTORE", "Certificate Store Utility"));
        utilityFunctions.put(16000L, new UtilityInfo("QYCU_DELETEREQUESTCERT", "Certificate Utility"));
        utilityFunctions.put(11500L, new UtilityInfo("QYCU_CHANGECRYPTODEVICELIST", "Certificate Utility"));
        utilityFunctions.put(11000L, new UtilityInfo("QYCU_BASE64ENCODEBUFTOBUF", "Certificate Utility"));
        utilityFunctions.put(23000L, new UtilityInfo("QYCU_GETCERTSIGNERS", "Certificate Utility"));
        utilityFunctions.put(30500L, new UtilityInfo("QYCU_SETCERTIFICATETRUSTSTATUS", "Certificate Utility"));
        utilityFunctions.put(85000L, new UtilityInfo("QYCU_FLUSH_POLICY2", "Local Certificate Authority Utility"));
        utilityFunctions.put(86000L, new UtilityInfo("QYCU_FLUSH_POLICY3", "Local Certificate Authority Utility"));
        utilityFunctions.put(28500L, new UtilityInfo("QYCU_REMOVECRLASSOCIATION2", "Certificate Utility"));
        utilityFunctions.put(74000L, new UtilityInfo("QYCU_CREATE_USRSPC_OBJECT", "Certificate Store Utility"));
        utilityFunctions.put(93000L, new UtilityInfo("QYCU_RTV_VLDL_ENTRY", "Certificate Revocation List Utility"));
        utilityFunctions.put(62000L, new UtilityInfo("QYCU_OPENCERTIFICATESTORE4", "Certificate Store Utility"));
        utilityFunctions.put(12000L, new UtilityInfo("QYCU_CHECKFORBASE64FILE", "Certificate Utility"));
        utilityFunctions.put(13500L, new UtilityInfo("QYCU_CHECKIFTHEREANDSTORE2", "Certificate Utility"));
        utilityFunctions.put(29000L, new UtilityInfo("QYCU_REMOVEPRIVATEKEYINFO", "Certificate Utility"));
        utilityFunctions.put(21500L, new UtilityInfo("QYCU_GETCERTIFICATEITEM_2", "Certificate Utility"));
        utilityFunctions.put(14000L, new UtilityInfo("QYCU_CREATECERTREQ", "Certificate Utility"));
        utilityFunctions.put(53000L, new UtilityInfo("QYCU_CHANGESTASHFILEPASSWORD", "Certificate Store Utility"));
        utilityFunctions.put(19500L, new UtilityInfo("QYCU_EXTRACTFILETOBUF", "Certificate Utility"));
        utilityFunctions.put(23500L, new UtilityInfo("QYCU_GETCERTSIGNERS2", "Certificate Utility"));
        utilityFunctions.put(80000L, new UtilityInfo("QYCU_READ_POLICY", "Local Certificate Authority Utility"));
        utilityFunctions.put(72500L, new UtilityInfo("QYCU_ALLOCATE_IOVEC1", "Certificate Store Utility"));
        utilityFunctions.put(58000L, new UtilityInfo("QYCU_GETREQUESTFILENAME", "Certificate Store Utility"));
        utilityFunctions.put(17000L, new UtilityInfo("QYCU_ENCRYPTWITHPUBLICKEY", "Certificate Utility"));
        utilityFunctions.put(70000L, new UtilityInfo("QYCU_RENAME", "Certificate Store Utility"));
        utilityFunctions.put(70500L, new UtilityInfo("QYCU_CHGOWN", "Certificate Store Utility"));
        utilityFunctions.put(24000L, new UtilityInfo("QYCU_GETCRLASSOCIATION", "Certificate Utility"));
        utilityFunctions.put(30000L, new UtilityInfo("QYCU_RETRIEVECERTITEM", "Certificate Utility"));
        utilityFunctions.put(87000L, new UtilityInfo("QYCU_CHGOWN_POLICY", "Local Certificate Authority Utility"));
        utilityFunctions.put(18000L, new UtilityInfo("QYCU_EXPORTCERTDATA", "Certificate Utility"));
        utilityFunctions.put(29500L, new UtilityInfo("QYCU_RENEWCERT", "Certificate Utility"));
        utilityFunctions.put(51000L, new UtilityInfo("QYCU_OPENCERTIFICATESTORE3", "Certificate Store Utility"));
        utilityFunctions.put(63000L, new UtilityInfo("QYCU_GETCERTSTOREPASSWORD", "Certificate Store Utility"));
        utilityFunctions.put(12500L, new UtilityInfo("QYCU_CHECKFORX509CERT", "Certificate Utility"));
        utilityFunctions.put(1000L, new UtilityInfo("QYCU_UPDATEAPPCATRUST", "Application Utility"));
        utilityFunctions.put(24500L, new UtilityInfo("QYCU_GET1STCERTWITHPRIVATEKEY", "Certificate Utility"));
        utilityFunctions.put(15000L, new UtilityInfo("QYCU_CREATESIGNEDCERT", "Certificate Utility"));
        utilityFunctions.put(83000L, new UtilityInfo("QYCU_CREATE_POLICY", "Local Certificate Authority Utility"));
        utilityFunctions.put(26000L, new UtilityInfo("QYCU_GETREQUESTCERTIFICATELABELLIST", "Certificate Utility"));
        utilityFunctions.put(18500L, new UtilityInfo("QYCU_EXPORTCERT_2", "Certificate Utility"));
        utilityFunctions.put(71000L, new UtilityInfo("QYCU_CPYOBJ", "Certificate Store Utility"));
        utilityFunctions.put(27500L, new UtilityInfo("QYCU_RECEIVECERT", "Certificate Utility"));
        utilityFunctions.put(2000L, new UtilityInfo("QYCU_REMOVEAPPSASSIGNEDCERT", "Application Utility"));
        utilityFunctions.put(15500L, new UtilityInfo("QYCU_DELETECERT", "Certificate Utility"));
        utilityFunctions.put(25000L, new UtilityInfo("QYCU_GETREQUESTCERTIFICATEITEM", "Certificate Utility"));
        utilityFunctions.put(52000L, new UtilityInfo("QYCU_CERTSTOREPASSWORD", "Certificate Store Utility"));
        utilityFunctions.put(31000L, new UtilityInfo("QYCU_STORECACERTIFICATE", "Certificate Utility"));
        utilityFunctions.put(20000L, new UtilityInfo("QYCU_EXTRACTFILETOBUF2", "Certificate Utility"));
        utilityFunctions.put(16500L, new UtilityInfo("QYCU_ENCRYPTWITHPRIVATEKEY", "Certificate Utility"));
        utilityFunctions.put(100L, new UtilityInfo("QYCU_RTVAPPS", "Application Utility"));
        qycuretcodes.put(4080L, "YCU_RC_VALIDATE_SELF_SIGNED");
        qycuretcodes.put(-961L, "YCU_RENAME_CERT_FILE");
        qycuretcodes.put(4021L, "YCU_RC_SEC_APP_REMOVE_APP");
        qycuretcodes.put(4096L, "YCU_RC_YDO_OBJECT_NOT_FOUND");
        qycuretcodes.put(4013L, "YCU_RC_SEC_APP_RMV_CERT_USAGE");
        qycuretcodes.put(4000L, "YCU_RC_BAD_INPUT_PARAMETER");
        qycuretcodes.put(3952L, "YCU_RC_RENAME_NEW_ERROR");
        qycuretcodes.put(4057L, "YCU_RC_CRL_PORT_NUM_REQUIRED");
        qycuretcodes.put(3912L, "YCU_RC_SOURCE_FILE_NO_EXIST");
        qycuretcodes.put(3857L, "YCU_RC_ORGNAME_REQUIRED");
        qycuretcodes.put(4066L, "YCU_RC_CRYPTO_RTV_RESOURCES");
        qycuretcodes.put(3974L, "YCU_RC_ENVVAR_REQUIRED");
        qycuretcodes.put(3947L, "YCU_RC_POLFILE_CREATE_ERROR");
        qycuretcodes.put(3976L, "YCU_RC_EXPIREDATE_REQUIRED");
        qycuretcodes.put(3850L, "YCU_RC_RESETPW_NOT_ALLOWED");
        qycuretcodes.put(3944L, "YCU_RC_VALID_EXCEEDS_CA_EXPIRATION");
        qycuretcodes.put(4009L, "YCU_RC_APPLICATION_ID_REQUIRED");
        qycuretcodes.put(4056L, "YCU_RC_CRL_CONNECT_TYPE_REQUIRED");
        qycuretcodes.put(4007L, "YCU_RC_FUNC_NOT_AT_THIS_TIME");
        qycuretcodes.put(4055L, "YCU_RC_CRL_LDAP_SERVER_REQUIRED");
        qycuretcodes.put(4022L, "YCU_RC_SEC_APP_UPDATE_APP");
        qycuretcodes.put(-99L, "YCU_RC_UNEXPECTED_ERROR");
        qycuretcodes.put(4020L, "YCU_RC_SEC_APP_ADD_APP");
        qycuretcodes.put(3936L, "YCU_RC_POLFILE_READ_ERROR");
        qycuretcodes.put(3973L, "YCU_RC_NEWPASSWORD_REQUIRED");
        qycuretcodes.put(-951L, "YCU_RENAME_CAREQ_FILE");
        qycuretcodes.put(3939L, "YCU_RC_LISTKEYS_ZERO_KEYS");
        qycuretcodes.put(3969L, "YCU_RC_KEYRING_FILE_REQ");
        qycuretcodes.put(4049L, "YCU_RC_CRL_UPDATE_LOCATION");
        qycuretcodes.put(3968L, "YCU_RC_IMPORT_FILE_REQ");
        qycuretcodes.put(4101L, "YCU_RC_YDO_NO_OBJECTS");
        qycuretcodes.put(4064L, "YCU_RC_CRYPTO_NO_DEVICES");
        qycuretcodes.put(4082L, "YCU_RC_VALIDATE_NO_TRUSTED_CAS");
        qycuretcodes.put(3946L, "YCU_RC_FILENAME_CONFLICT_ERROR2");
        qycuretcodes.put(4027L, "YCU_RC_SEC_NO_LIST_CAS");
        qycuretcodes.put(4053L, "YCU_RC_CRL_LIST_LOCATIONS");
        qycuretcodes.put(4050L, "YCU_RC_CRL_REMOVE_LOCATION");
        qycuretcodes.put(3858L, "YCU_RC_VALID_TOO_LONG");
        qycuretcodes.put(3865L, "YCU_RC_NULL_CERT_REQ");
        qycuretcodes.put(3970L, "YCU_RC_KEYLABEL_REQ");
        qycuretcodes.put(4098L, "YCU_RC_YDO_VERIFY_SIGNATURE");
        qycuretcodes.put(3940L, "YCU_RC_CHECKEXPIRE_ZERO_KEYS");
        qycuretcodes.put(3925L, "YCU_RC_POLFILE_OPEN_ERROR");
        qycuretcodes.put(3933L, "YCU_RC_FILE_ALREADY_EXIST");
        qycuretcodes.put(3926L, "YCU_RC_POLFILE_WRITE_ERROR");
        qycuretcodes.put(4081L, "YCU_RC_VALIDATE_NOT_TRUSTED");
        qycuretcodes.put(4051L, "YCU_RC_CRL_GET_LOCATION");
        qycuretcodes.put(3864L, "YCU_RC_COMMNAME_SRV_REQUIRED");
        qycuretcodes.put(4024L, "YCU_RC_SEC_APP_EXISTS");
        qycuretcodes.put(4004L, "YCU_RC_PREV_REG");
        qycuretcodes.put(4065L, "YCU_RC_CRYPTO_LIST_DEVICES");
        qycuretcodes.put(4001L, "YCU_RC_INSUF_INPUT_PARAMETERS");
        qycuretcodes.put(-3L, "YCU_RC_USER_CERT_NOT_FOUND");
        qycuretcodes.put(4002L, "YCU_RC_BAD_FUNCTION_REQUEST");
        qycuretcodes.put(4008L, "YCU_RC_NO_APPS");
        qycuretcodes.put(3972L, "YCU_RC_ARMOR_PASSWORD_REQ");
        qycuretcodes.put(4017L, "YCU_RC_SEC_APP_LST_APP_BYCERT");
        qycuretcodes.put(4048L, "YCU_RC_CRL_ADD_LOCATION");
        qycuretcodes.put(4067L, "YCU_RC_CRYPTO_DEVICE_LIST_REQUIRED");
        qycuretcodes.put(4003L, "YCU_RC_NO_VAL_CERT");
        qycuretcodes.put(3948L, "YCU_RC_POLFILE_CONVERSION_ERROR");
        qycuretcodes.put(4015L, "YCU_RC_SEC_APP_RMV_CA_TRUST");
        qycuretcodes.put(4010L, "YCU_RC_TRUST_REQ");
        qycuretcodes.put(-902L, "YCU_RENAME_CA_FILE");
        qycuretcodes.put(3843L, "YCU_RC_SHORT_STATE");
        qycuretcodes.put(4018L, "YCU_RC_SEC_APP_UPD_CERT_USAGE");
        qycuretcodes.put(-1L, "YCU_RC_USRPRF_NOT_FOUND");
        qycuretcodes.put(3891L, "YCU_RC_EXISTS_EXPORTFILE");
        qycuretcodes.put(3860L, "YCU_RC_PASSWORD_REQUIRED");
        qycuretcodes.put(3975L, "YCU_RC_CERTREQ_FILE_REQ");
        qycuretcodes.put(3892L, "YCU_RC_EXISTS_IMPORTFILE");
        qycuretcodes.put(4014L, "YCU_RC_SEC_APP_ADD_CA_TRUST");
        qycuretcodes.put(3859L, "YCU_RC_COUNTRY_REQUIRED");
        qycuretcodes.put(3945L, "YCU_RC_KEYDB_FILENAME_ERROR");
        qycuretcodes.put(3856L, "YCU_RC_COMMNAME_CA_REQUIRED");
        qycuretcodes.put(4097L, "YCU_RC_YDO_SIGN_OBJECT");
        qycuretcodes.put(4058L, "YCU_RC_CRL_LOGIN_PWD_TOO_LONG");
        qycuretcodes.put(3846L, "YCU_RC_BAD_KEYRING_FILENAME");
        qycuretcodes.put(4052L, "YCU_RC_CRL_NO_LOCATIONS");
        qycuretcodes.put(3984L, "YCU_RC_CERT_NOT_FOUND3");
        qycuretcodes.put(3845L, "YCU_RC_AUTH_PROB");
        qycuretcodes.put(3956L, "YCU_RC_CA_NO_CREATE_CLIENT");
        qycuretcodes.put(3943L, "YCU_RC_FILENAME_CONFLICT_ERROR");
        qycuretcodes.put(-992L, "YCU_SWAP_USER");
        qycuretcodes.put(3861L, "YCU_RC_MISSING_INPUT");
        qycuretcodes.put(3913L, "YCU_RC_DEST_FILE_ALREADY_EXIST");
        qycuretcodes.put(4100L, "YCU_RC_YDO_BROWSE_DIRECTORY");
        qycuretcodes.put(4011L, "YCU_RC_SEC_APP_RTV_CERT_USAGE");
        qycuretcodes.put(4099L, "YCU_RC_YDO_RETRIEVE_SIGNATURE");
        qycuretcodes.put(4023L, "YCU_RC_CA_IN_USE");
        qycuretcodes.put(4012L, "YCU_RC_SEC_APP_LABEL_TOO_BIG");
        qycuretcodes.put(3953L, "YCU_RC_RENAME_EXISTING_ERROR");
        qycuretcodes.put(4016L, "YCU_RC_SEC_APP_LST_APP_BYCA");
        qycuretcodes.put(-2L, "YCU_RC_NOT_AUTH_USRPRF");
        qycuretcodes.put(4068L, "YCU_RC_CRYPTO_DEVICE_NAME_REQUIRED");
        qycuretcodes.put(3949L, "YCU_RC_POLFILE_LOCK_ERROR");
        qycuretcodes.put(-999L, "YCU_RC_LAST_ENTRY");
        qycuretcodes.put(3932L, "YCU_RC_KDB_FILE_ALREADY_EXIST2");
        qycuretcodes.put(0L, "YCU_RC_SUCCESS");
        qycuretcodes.put(4083L, "YCU_RC_VALIDATE_NO_CERT_ASSIGNED");
        qycuretcodes.put(4059L, "YCU_RC_CRL_SERVER_LOGIN_DN_TOO_LONG");
        qycuretcodes.put(3849L, "YCU_RC_PASSWORD_NOTEQUAL");
        qycuretcodes.put(3985L, "YCU_RC_LDAP_MAPPING_NOT_DEFINED");
        qycuretcodes.put(3844L, "YCU_RC_BAD_VALIDITY");
        qycuretcodes.put(4054L, "YCU_RC_CRL_LOCATION_REQUIRED");
        qycuretcodes.put(3971L, "YCU_RC_EXPORT_FILE_REQ");
        qycuretcodes.put(3941L, "YCU_RC_COUNTRYNAME_REQUIRED");
        gskkmerr.put(313L, "GSKKM_WEBDB_PK_chgpw_and_armor");
        gskkmerr.put(384L, "#define GSKKM_ERR_INDEX_NOT_FOUND");
        gskkmerr.put(31L, "GSKKM_ERR_CRYPTO_UNSUPPORTED_ALGORITHM");
        gskkmerr.put(117L, "GSKKM_ERR_ISSUING_CERTIFICATE");
        gskkmerr.put(59L, "GSKKM_ERR_NUMBER_SIZE");
        gskkmerr.put(363L, "#define GSKKM_PKCS12_CERTTYPE_UNSUP");
        gskkmerr.put(5L, "GSKKM_ERR_DATABASE");
        gskkmerr.put(53L, "GSKKM_ERR_VALIDATION_KEY_UNKNOWN_CRITICAL_EXTENSION");
        gskkmerr.put(47L, "GSKKM_ERR_VALIDATION_KEY_IS_REVOKED");
        gskkmerr.put(100L, "GSKKM_ERR_REQKEY_FOR_CERT_NULL");
        gskkmerr.put(329L, "GSKKM_ERR_RENAME_STASH_FILE");
        gskkmerr.put(78L, "GSKKM_ERR_FILE_OPEN");
        gskkmerr.put(308L, "GSKKM_ERR_StashKdbPwd");
        gskkmerr.put(89L, "GSKKM_ERR_DN_TAG_NULL");
        gskkmerr.put(108L, "GSKKM_ERR_INVALID_DN_INPUT");
        gskkmerr.put(310L, "GSKKM_ERR_NoTrustedDNs");
        gskkmerr.put(39L, "GSKKM_ERR_VALIDATION_KEY_VALIDITY");
        gskkmerr.put(110L, "GSKKM_ERR_LABEL_LIST_CORRUPT");
        gskkmerr.put(325L, "GSKKM_ERR_CPY_OBJ");
        gskkmerr.put(51L, "GSKKM_ERR_VALIDATION_ISSUER_ALTERNATIVE_NAME");
        gskkmerr.put(344L, "GSKKM_ERR_CCA_KEYID");
        gskkmerr.put(13L, "GSKKM_ERR_DATABASE_WRITE");
        gskkmerr.put(23L, "GSKKM_ERR_DATABASE_LDAP");
        gskkmerr.put(72L, "GSKKM_ERR_UNSUPPORTED_OPTION");
        gskkmerr.put(45L, "GSKKM_ERR_VALIDATION_KEY_SIGNATURE");
        gskkmerr.put(14L, "GSKKM_ERR_DATABASE_VALIDATION");
        gskkmerr.put(103L, "GSKKM_ERR_KEYREC_PRIVATE_KEY_NULL");
        gskkmerr.put(87L, "GSKKM_ERR_BASE64_ENCODING");
        gskkmerr.put(49L, "GSKKM_ERR_VALIDATION_KEY_PRIVATE_KEY_USAGE_PERIOD");
        gskkmerr.put(116L, "GSKKM_ERR_KDB2KYR");
        gskkmerr.put(81L, "GSKKM_ERR_FILE_OPEN_NOT_EXIST");
        gskkmerr.put(125L, "GSKKM_ERR_NO_HDW_ALG");
        gskkmerr.put(3L, "GSKKM_ERR_ASN_INITIALIZATION");
        gskkmerr.put(75L, "GSKKM_ERR_CMDCAT_OPEN");
        gskkmerr.put(351L, "GSKKM_ERR_HARDWARE_KEYSIZE_2_LARGE");
        gskkmerr.put(301L, "GSKKM_WEBDB_InvalidVersion");
        gskkmerr.put(94L, "GSKKM_ERR_KEYPAIRDB_NOT_EXIST");
        gskkmerr.put(101L, "GSKKM_ERR_KEYDB_PRIVATE_KEY_NULL");
        gskkmerr.put(85L, "GSKKM_ERR_BASE64_INVALID_DATA");
        gskkmerr.put(314L, "GSKKM_WEBDB_webdb_findarmorstring");
        gskkmerr.put(114L, "GSKKM_ERR_PBE_ALG_UNSUPPORT");
        gskkmerr.put(21L, "GSKKM_ERR_DATABASE_DUPLICATE_KEY");
        gskkmerr.put(362L, "#define GSKKM_PKCS12_CONTENT_TYPE_UNSUP");
        gskkmerr.put(24L, "GSKKM_ERR_CRYPTO");
        gskkmerr.put(370L, "#define GSKKM_MULTDN_SLOT_OVERFLOW");
        gskkmerr.put(52L, "GSKKM_ERR_VALIDATION_KEY_USAGE");
        gskkmerr.put(345L, "GSKKM_ERR_CCA_DESIGNATE");
        gskkmerr.put(43L, "GSKKM_ERR_VALIDATION_KEY_MISSING_REQUIRED_EXTENSIONS");
        gskkmerr.put(383L, "#define GSKKM_ERR_INDEX_EXISTS");
        gskkmerr.put(340L, "GSKKM_ERR_CCA");
        gskkmerr.put(309L, "GSKKM_ERR_RecoverKdbPwd");
        gskkmerr.put(331L, "GSKKM_ERR_RENEW_CERT_ISSUER");
        gskkmerr.put(32L, "GSKKM_ERR_CRYPTO_INPUT_GREATER_THAN_MODULUS");
        gskkmerr.put(20L, "GSKKM_ERR_DATABASE_KEY_INTEGRITY");
        gskkmerr.put(73L, "GSKKM_ERR_GET_TIME");
        gskkmerr.put(361L, "#define GSKKM_PKCS12_MULTIPLE_AUTHSAFES_UNSUP");
        gskkmerr.put(8L, "GSKKM_ERR_DATABASE_CREATE");
        gskkmerr.put(29L, "GSKKM_ERR_CRYPTO_DIGEST");
        gskkmerr.put(347L, "GSKKM_ERR_CCA_CIPHER_OUTPUTTOOLARGE");
        gskkmerr.put(2L, "GSKKM_ERR_ASN");
        gskkmerr.put(62L, "GSKKM_ERR_PASSWORD_EXPIRATION_TIME");
        gskkmerr.put(33L, "GSKKM_ERR_CRYPTO_UNSUPPORTED_MODULUS_SIZE");
        gskkmerr.put(84L, "GSKKM_ERR_FILE_REMOVE");
        gskkmerr.put(107L, "GSKKM_ERR_UNDEFINED_KEY_TYPE");
        gskkmerr.put(58L, "GSKKM_ERR_NULL_PARAMETER");
        gskkmerr.put(112L, "GSKKM_ERR_PKCS12_PWD_CORRUPTION");
        gskkmerr.put(48L, "GSKKM_ERR_VALIDATION_KEY_AUTHORITY_KEY_IDENTIFIER");
        gskkmerr.put(390L, "#define GSKKM_ERR_DIR_INDEX_ERROR");
        gskkmerr.put(326L, "GSKKM_ERR_SWAP_USER");
        gskkmerr.put(42L, "GSKKM_ERR_VALIDATION_KEY_ISSUER_NOT_FOUND");
        gskkmerr.put(65L, "GSKKM_ERR_THREAD_WAIT_FOR_EXIT");
        gskkmerr.put(302L, "GSKKM_WEBDB_WrongAlgorithmID1");
        gskkmerr.put(395L, "#define GSKKM_ERR_NO_ACX_INSTALLED");
        gskkmerr.put(35L, "GSKKM_ERR_VALIDATION_KEY");
        gskkmerr.put(60L, "GSKKM_ERR_OLD_PASSWORD");
        gskkmerr.put(315L, "GSKKM_WEBDB_PK_chgpw_and_dearmor");
        gskkmerr.put(115L, "GSKKM_ERR_KYR2KDB");
        gskkmerr.put(34L, "GSKKM_ERR_VALIDATION");
        gskkmerr.put(126L, "GSKKM_ERR_VALIDATION_DUP_COMMON_NAME");
        gskkmerr.put(104L, "GSKKM_ERR_KEYREC_CERTIFICATE_NULL");
        gskkmerr.put(28L, "GSKKM_ERR_CRYPTO_VERIFY");
        gskkmerr.put(76L, "GSKKM_ERR_ERRCAT_OPEN");
        gskkmerr.put(382L, "#define GSKKM_ERR_INDEX_DAMAGED");
        gskkmerr.put(50L, "GSKKM_ERR_VALIDATION_SUBJECT_ALTERNATIVE_NAME");
        gskkmerr.put(317L, "GSKKM_ERR_FileRead");
        gskkmerr.put(330L, "GSKKM_ERR_RENEW_CERT_NOT_FOUND");
        gskkmerr.put(106L, "GSKKM_ERR_INVALID_KEYDB_NAME");
        gskkmerr.put(80L, "GSKKM_ERR_FILE_OPEN_TO_WRITE");
        gskkmerr.put(19L, "GSKKM_ERR_DATABASE_PASSWORD_CORRUPTION");
        gskkmerr.put(10L, "GSKKM_ERR_DATABASE_DELETE");
        gskkmerr.put(9L, "GSKKM_ERR_DATABASE_ALREADY_EXISTS");
        gskkmerr.put(346L, "GSKKM_ERR_CCA_INVALIDKEYSTORE");
        gskkmerr.put(57L, "GSKKM_ERR_PARAMETER");
        gskkmerr.put(321L, "GSKKM_ERR_NotEnoughSpace");
        gskkmerr.put(64L, "GSKKM_ERR_THREAD_CREATE");
        gskkmerr.put(327L, "GSKKM_ERR_SWAP_BACK");
        gskkmerr.put(319L, "GSKKM_ERR_GetDNInCharacterFormat");
        gskkmerr.put(12L, "GSKKM_ERR_DATABASE_READ");
        gskkmerr.put(119L, "GSKKM_ERR_WEBDB_DATA_BAD_FORMAT");
        gskkmerr.put(99L, "GSKKM_ERR_KEYDB_TRUSTCA_NULL");
        gskkmerr.put(113L, "GSKKM_ERR_EXPORT_TYPE");
        gskkmerr.put(93L, "GSKKM_ERR_KEYDB_NOT_EXIST");
        gskkmerr.put(367L, "#define GSKKM_PKCS7_CONTENT_TYPE_UNSUP");
        gskkmerr.put(1L, "GSKKM_ERR_UNKNOWN");
        gskkmerr.put(66L, "GSKKM_ERR_IO");
        gskkmerr.put(303L, "GSKKM_WEBDB_ProbablyWrongPassword");
        gskkmerr.put(82L, "GSKKM_ERR_FILE_OPEN_NOT_ALLOWED");
        gskkmerr.put(306L, "GSKKM_ERR_GET_CERTIFICATE_BY_DN");
        gskkmerr.put(41L, "GSKKM_ERR_VALIDATION_KEY_VALIDITY_PRIVATE_KEY_USAGE");
        gskkmerr.put(311L, "GSKKM_ERR_NoMatchingKeyRecord");
        gskkmerr.put(37L, "GSKKM_ERR_VALIDATION_KEY_WRONG_VERSION");
        gskkmerr.put(111L, "GSKKM_ERR_INVALID_PKCS12_DATA");
        gskkmerr.put(91L, "GSKKM_ERR_DN_C_NULL");
        gskkmerr.put(365L, "#define GSKKM_PKCS12_DIGEST_ERROR");
        gskkmerr.put(79L, "GSKKM_ERR_FILE_OPEN_TO_READ");
        gskkmerr.put(68L, "GSKKM_ERR_PKCS11");
        gskkmerr.put(360L, "#define GSKKM_PKCS12_VERSION_UNSUP");
        gskkmerr.put(318L, "GSKKM_ERR_NotSystemState");
        gskkmerr.put(118L, "GSKKM_ERR_FIND_ISSUER_CHAIN");
        gskkmerr.put(18L, "GSKKM_ERR_DATABASE_CORRUPTION");
        gskkmerr.put(90L, "GSKKM_ERR_DN_CN_NULL");
        gskkmerr.put(366L, "#define GSKKM_PKCS12_PRIVATE_KEYPAIR_MISMATCH");
        gskkmerr.put(121L, "GSKKM_ERR_EXPIRE_DAYS_TOO_LARGE");
        gskkmerr.put(56L, "GSKKM_ERR_MUTEX");
        gskkmerr.put(341L, "GSKKM_ERR_CCA_RESOLVE");
        gskkmerr.put(83L, "GSKKM_ERR_FILE_WRITE");
        gskkmerr.put(92L, "GSKKM_ERR_INVALID_DB_HANDLE");
        gskkmerr.put(71L, "GSKKM_ERR_MEMORY_ALLOCATE");
        gskkmerr.put(0L, "GSKKM_OK");
        gskkmerr.put(305L, "GSKKM_WEBDB_WrongAlgorithmID2");
        gskkmerr.put(11L, "GSKKM_ERR_DATABASE_NOT_OPENED");
        gskkmerr.put(105L, "GSKKM_ERR_CRLS_NULL");
        gskkmerr.put(16L, "GSKKM_ERR_DATABASE_INVALID_PASSWORD");
        gskkmerr.put(77L, "GSKKM_ERR_FILENAME_NULL");
        gskkmerr.put(25L, "GSKKM_ERR_CRYPTO_ENGINE");
        gskkmerr.put(123L, "GSKKM_ERR_PWD_NO_NUMBER");
        gskkmerr.put(54L, "GSKKM_ERR_VALIDATION_KEY_PAIR");
        gskkmerr.put(343L, "GSKKM_ERR_CCA_ALLOC");
        gskkmerr.put(6L, "GSKKM_ERR_DATABASE_OPEN");
        gskkmerr.put(67L, "GSKKM_ERR_LOAD");
        gskkmerr.put(324L, "GSKKM_ERR_Bsafe_Call");
        gskkmerr.put(40L, "GSKKM_ERR_VALIDATION_KEY_VALIDITY_PERIOD");
        gskkmerr.put(307L, "GSKKM_ERR_Base64Conversion");
        gskkmerr.put(353L, "GSKKM_ERR_CERT_WITH_HDWPRIVKEY_FOUND");
        gskkmerr.put(27L, "GSKKM_ERR_CRYPTO_SIGN");
        gskkmerr.put(30L, "GSKKM_ERR_CRYPTO_PARAMETER");
        gskkmerr.put(96L, "GSKKM_ERR_PASSWORD_CHANGE_MATCH");
        gskkmerr.put(312L, "GSKKM_WEBDB_ConversionError");
        gskkmerr.put(97L, "GSKKM_ERR_KEYDB_NULL");
        gskkmerr.put(364L, "#define GSKKM_PKCS12_BAG_TYPE_UNKNOWN");
        gskkmerr.put(380L, "#define GSKKM_ERR_INDEX_NOT_AUTH");
        gskkmerr.put(352L, "GSKKM_ERR_CCA_CARD_NO_MEMORY");
        gskkmerr.put(4L, "GSKKM_ERR_ASN_PARAMETER");
        gskkmerr.put(46L, "GSKKM_ERR_VALIDATION_KEY_ROOT_KEY_NOT_TRUSTED");
        gskkmerr.put(120L, "GSKKM_ERR_WEBDB_NOTHING_TO_WRITE");
        gskkmerr.put(74L, "GSKKM_ERR_CREATE_MUTEX");
        gskkmerr.put(98L, "GSKKM_ERR_REQKEYDB_NULL");
        gskkmerr.put(70L, "GSKKM_ERR_DB_TABLE_CORRUPTED");
        gskkmerr.put(381L, "#define GSKKM_ERR_INDEX_LOCKED");
        gskkmerr.put(17L, "GSKKM_ERR_DATABASE_INVALID_FILE_TYPE");
        gskkmerr.put(69L, "GSKKM_ERR_NOT_INITIALIZED");
        gskkmerr.put(88L, "GSKKM_ERR_BASE64_DECODING");
        gskkmerr.put(385L, "#define GSKKM_ERR_INDEX_NO_ENTRY");
        gskkmerr.put(349L, "GSKKM_ERR_CCA_DEV_VOFF");
        gskkmerr.put(55L, "GSKKM_ERR_VALIDATION_CRL");
        gskkmerr.put(22L, "GSKKM_ERR_DATABASE_PASSWORD_ENCRYPTION");
        gskkmerr.put(44L, "GSKKM_ERR_VALIDATION_KEY_BASIC_CONSTRAINTS");
        gskkmerr.put(328L, "GSKKM_ERR_CHGOWN_OBJ");
        gskkmerr.put(323L, "GSKKM_ERR_Parsing_Certificate_Chain");
        gskkmerr.put(109L, "GSKKM_ERR_KEY_GET_BY_LABEL");
        gskkmerr.put(61L, "GSKKM_ERR_NEW_PASSWORD");
        gskkmerr.put(322L, "GSKKM_ERR_NotEnoughSpaceForChain");
        gskkmerr.put(342L, "GSKKM_ERR_CCA_MATDEV");
        gskkmerr.put(15L, "GSKKM_ERR_DATABASE_INVALID_VERSION");
        gskkmerr.put(102L, "GSKKM_ERR_KEYDB_DEFAULT_KEY_NULL");
        gskkmerr.put(86L, "GSKKM_ERR_BASE64_INVALID_MSGTYPE");
        gskkmerr.put(350L, "GSKKM_ERR_CCA_DUPLABEL");
        gskkmerr.put(26L, "GSKKM_ERR_CRYPTO_ALGORITHM");
        gskkmerr.put(122L, "GSKKM_ERR_PWD_TOO_SHORT");
        gskkmerr.put(36L, "GSKKM_ERR_VALIDATION_DUPLICATE_EXTENSIONS");
        gskkmerr.put(38L, "GSKKM_ERR_VALIDATION_KEY_EXTENSIONS_REQUIRED");
        gskkmerr.put(316L, "GSKKM_ERR_ChangeObjectAuthority");
        gskkmerr.put(7L, "GSKKM_ERR_DATABASE_RE_OPEN");
        gskkmerr.put(348L, "GSKKM_ERR_CCA_DEV_UNAVAIL");
        gskkmerr.put(63L, "GSKKM_ERR_THREAD");
        gskkmerr.put(124L, "GSKKM_ERR_PWD_NO_CONTROL_KEY");
        gskkmerr.put(95L, "GSKKM_ERR_PWDFILE_NOT_EXIST");

        // gskssl.h SSL / TLS protocol codes
        gskkmerr.put(406L, "GSK_ERROR_IO");
        gskkmerr.put(407L, "GSK_ERROR_BAD_KEYFILE_LABEL");
        gskkmerr.put(408L, "GSK_ERROR_BAD_KEYFILE_PASSWORD");
        gskkmerr.put(6000L, "GSK_AS400_ERROR_NOT_TRUSTED");
        gskkmerr.put(6001L, "GSK_AS400_ERROR_NO_TEMPORARY_STORAGE");
        gskkmerr.put(6002L, "GSK_AS400_ERROR_TIMED_OUT");
        gskkmerr.put(6003L, "GSK_AS400_ERROR_PASSWORD_EXPIRED");
        gskkmerr.put(6004L, "GSK_AS400_ERROR_ALREADY_INITIALIZED");
        gskkmerr.put(6005L, "GSK_AS400_ERROR_NOT_INITIALIZED");
        gskkmerr.put(6006L, "GSK_AS400_ERROR_NO_APPLICATION_ID");
        gskkmerr.put(6007L, "GSK_AS400_ERROR_APPLICATION_NOT_REGISTERED");
        gskkmerr.put(6008L, "GSK_AS400_ERROR_INVALID_CERTIFICATE_NAME");
        gskkmerr.put(6009L, "GSK_AS400_ERROR_CERT_EXPIRED");
        gskkmerr.put(6010L, "GSK_AS400_ERROR_CLOSED");
        gskkmerr.put(6011L, "GSK_AS400_ERROR_BAD_CIPHER_SUITE");
        gskkmerr.put(6012L, "GSK_AS400_ERROR_INVALID_STATE");
        gskkmerr.put(6013L, "GSK_AS400_ERROR_UNSUPPORTED_PROTOCOL");
        gskkmerr.put(6014L, "GSK_AS400_ERROR_CRYPTO_HARDWARE_FAILED");
        gskkmerr.put(6015L, "GSK_AS400_ERROR_BAD_CERT_STORE_PASSWORD");
        gskkmerr.put(6016L, "GSK_AS400_ERROR_NO_CERT_STORE_PASSWORD");
        gskkmerr.put(6017L, "GSK_AS400_ERROR_KEYRING_NOT_FOUND");
        gskkmerr.put(6018L, "GSK_AS400_ERROR_APPLICATION_ALREADY_DEFINED");
        gskkmerr.put(6019L, "GSK_AS400_ERROR_NO_DEFAULT_CERT");
        gskkmerr.put(6020L, "GSK_AS400_ERROR_NO_KEYRING_PASSWORD");

        initGskDetails();

        sortedUtilityCodes.addAll(utilityFunctions.keySet());
        Collections.sort(sortedUtilityCodes, Collections.reverseOrder());
    }

    private void initGskDetails() {
        // Detailed Functional, Module, and JNI mappings
        gskDetails.put(6L, new GskDetail(6L, "GSKKM_ERR_DATABASE_OPEN", "gskkm_open_database() / gskkm_open_key_database()", "QYCUCSMGR / QYCUCTMGR", "QYCU_OPENSTORE / QYCU_LISTCERTIFICATES", "qycu_listCertificatesJ / qycu_openStoreJ", "/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB", "Key Database Open & IFS Authority", "Cannot open certificate store file. The .KDB file does not exist or user profile QTMHHTTP lacks *RWX data authority.", "CPDB701", "WRKLNK OBJ('/QIBM/UserData/ICSS/Cert/Server/*')", "CHGAUT OBJ('/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB') USER(QTMHHTTP) DTAAUT(*RWX)", "WRKJOB JOB(*)"));
        gskDetails.put(16L, new GskDetail(16L, "GSKKM_ERR_DATABASE_INVALID_PASSWORD", "gskkm_verify_password() / gskkm_check_stash()", "QYCUCSMGR", "QYCU_VALIDATEPASSWORD / QYCU_CHANGESTOREPASSWORD", "qycu_listCertificatesJ / qycu_listCACertificatesJ / qycu_changePasswordJ", "/QIBM/UserData/ICSS/Cert/Server/DEFAULT.STH", "Password Stash & Decryption", "The certificate store password or password stash file (.STH) is invalid, corrupted, or does not match the key database.", "CPDB702", "WRKLNK OBJ('/QIBM/UserData/ICSS/Cert/Server/DEFAULT.STH')", "DCM -> Manage Certificate Store -> Select Store -> Change/Reset Password & Stash", "WRKOBJ OBJ(QUSRSYS/*ALL) OBJTYPE(*VLDL)"));
        gskDetails.put(18L, new GskDetail(18L, "GSKKM_ERR_DATABASE_CORRUPTION", "gskkm_read_record() / gskkm_parse_kdb_header()", "QYCUCSMGR / QYCUCTMGR", "QYCU_READSTORE / QYCU_LISTCERTIFICATES", "qycu_listCertificatesJ / qycu_getCertificateListJ", "/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB", "CMS Database Integrity", "Key database file header or record blocks are corrupted. Often caused by binary file transfer truncation or disk fault.", "CPDB703", "RST OBJ('/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB') from backup", "WRKOBJ OBJ(QUSRSYS/QYCU*)"));
        gskDetails.put(24L, new GskDetail(24L, "GSKKM_ERR_CRYPTO", "gskkm_crypto() / gskkm_rsa_operation()", "QYCUCSMGR / QYCUCTMGR", "QYCU_GENERATEKEYPAIR / QYCU_CRYPTOOP", "qycu_createCertificateJ / qycu_importCertificateJ", "Cryptographic Subsystem / Key Store", "Cryptographic Algorithm Operation", "Low-level cryptographic algorithm failure occurred during key generation, decryption, or digital signature computation.", "CPDBC15", "WRKHDWRSC TYPE(*CRP) (Inspect Cryptographic Resources)", "WRKACTJOB SBS(QSEC) (Verify Crypto Server Jobs)", "DSPLOG MSGID(CPF* CPD*)"));
        gskDetails.put(42L, new GskDetail(42L, "GSKKM_ERR_VALIDATION_KEY_ISSUER_NOT_FOUND", "gskkm_validate_certificate() / gskkm_build_certificate_chain()", "QYCUCTMGR / QYCUAPPMGR", "QYCU_VALIDATECERTIFICATE / QYCU_APPASSIGNCERT", "qycu_validateCertificateJ / qycu_validateCertificateTrustJ", "Trust Store (DEFAULT.KDB)", "X.509 Chain Building & Trust", "The Certificate Authority (CA) that issued the certificate could not be found in the current certificate store.", "CPDB704", "DCM -> Manage Certificate Store -> Populate / Import CA Certificate", "WRKLNK OBJ('/QIBM/UserData/ICSS/Cert/*')"));
        gskDetails.put(46L, new GskDetail(46L, "GSKKM_ERR_VALIDATION_KEY_ROOT_KEY_NOT_TRUSTED", "gskkm_check_root_trust()", "QYCUCTMGR / QYCUAPPMGR", "QYCU_VALIDATECERTIFICATE / QYCU_ADDCERTIFICATETRUST", "qycu_addCertificateTrustJ / qycu_validateCertificateJ", "Trust Store Root CA", "Root CA Trust Setting", "The Root CA is present in the database, but is marked as Untrusted in DCM.", "CPDB705", "DCM -> Manage Certificate Store -> Work with CA Certificates -> Trust Status -> Mark Trusted"));
        gskDetails.put(47L, new GskDetail(47L, "GSKKM_ERR_VALIDATION_KEY_IS_REVOKED", "gskkm_check_crl() / gskkm_query_ocsp()", "QYCUCRLMGR / QYCUCTMGR", "QYCU_CHECKREVOCATION / QYCU_VALIDATECERTIFICATE", "qycu_validateCertificateJ", "CRL / OCSP Responder", "Certificate Revocation", "Certificate has been revoked by the issuing Certificate Authority on the CRL or OCSP server.", "CPDB706", "PING <CRL_SERVER_IP>", "WRKOBJ OBJ(QUSRSYS/QYCU*) OBJTYPE(*VLDL)"));
        gskDetails.put(78L, new GskDetail(78L, "GSKKM_ERR_FILE_OPEN", "gskkm_file_open() / gskkm_open_stream()", "QYCUCSMGR / qycuutl.H", "QYCU_OPENFILE", "qycu_importCertificateJ / qycu_exportCertificateJ", "IFS File Stream", "File I/O Subsystem", "Generic file open error occurred on the IFS path.", "CPDBC05", "WRKLNK OBJ('<FILE_PATH>')", "DSPLOG MSGID(CPFA*)"));
        gskDetails.put(81L, new GskDetail(81L, "GSKKM_ERR_FILE_OPEN_NOT_EXIST", "gskkm_file_open()", "QYCUCSMGR / qycuutl.H", "QYCU_OPENFILE", "qycu_importCertificateJ", "Import/Export IFS Path", "IFS File Existence", "The target certificate file or directory specified does not exist.", "CPFA0A9", "WRKLNK OBJ('<IFS_PATH>')"));
        gskDetails.put(82L, new GskDetail(82L, "GSKKM_ERR_FILE_OPEN_NOT_ALLOWED", "gskkm_file_open()", "QYCUCSMGR / qycuutl.H", "QYCU_OPENFILE", "qycu_importCertificateJ", "Target IFS File", "IFS Authority & Permissions", "Permission denied accessing IFS file.", "CPFA09C", "CHGAUT OBJ('<IFS_PATH>') USER(QTMHHTTP) DTAAUT(*RWX)"));
        gskDetails.put(93L, new GskDetail(93L, "GSKKM_ERR_KEYDB_NOT_EXIST", "gskkm_locate_database()", "QYCUCSMGR / QYCULCAMGR", "QYCU_OPENSTORE / QYCU_OPENLOCALCA", "qycu_openStoreJ / qycu_listCertificatesJ", "/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB", "Certificate Store Location", "Key database does not exist at specified IFS path.", "CPDB707", "WRKLNK OBJ('/QIBM/UserData/ICSS/Cert/Server/*')"));
        gskDetails.put(95L, new GskDetail(95L, "GSKKM_ERR_PWDFILE_NOT_EXIST", "gskkm_read_stash()", "QYCUCSMGR", "QYCU_GETPASSWORDSTASH", "qycu_listCertificatesJ", "/QIBM/UserData/ICSS/Cert/Server/DEFAULT.STH", "Stash File Location", "Password stash file (.STH) does not exist.", "CPDB708", "DCM -> Manage Certificate Store -> Re-stash Password"));
        gskDetails.put(109L, new GskDetail(109L, "GSKKM_ERR_KEY_GET_BY_LABEL", "gskkm_find_record_by_label()", "QYCUCTMGR", "QYCU_RETRIEVECERTIFICATEINFORMATION", "qycu_retrieveCertificateInformationJ / qycu_assignCertToAppJ", "Certificate Label in .KDB", "Key Record Lookup", "Certificate label was not found inside the key database.", "CPDB709", "DCM -> Manage Certificate Store -> Work with Certificates -> Verify Label"));
        gskDetails.put(112L, new GskDetail(112L, "GSKKM_ERR_PKCS12_PWD_CORRUPTION", "gskkm_pkcs12_decrypt() / gskkm_parse_pfx()", "QYCUCTMGR", "QYCU_IMPORTCERTIFICATE", "qycu_importCertificateJ", ".P12 / .PFX Archive", "PKCS#12 Archive Password", "Password provided to decrypt PKCS#12 / PFX certificate file was incorrect or corrupted.", "CPDB710", "Verify PKCS#12 password on export workstation"));
        gskDetails.put(340L, new GskDetail(340L, "GSKKM_ERR_CCA", "gskkm_cca_call() / gskkm_crypt_hw()", "QYCUHDWMGR", "QYCU_GETCRYPTODEVICELIST", "qycu_getCryptoDeviceListJ", "PCIe Crypto Coprocessor (4767/4769)", "Hardware Cryptography", "Cryptographic Coprocessor CCA subsystem error.", "CPDBC14", "WRKHDWRSC TYPE(*CRP)", "WRKACTJOB SBS(QSEC)"));
        gskDetails.put(6000L, new GskDetail(6000L, "GSK_AS400_ERROR_NOT_TRUSTED", "gsk_secure_soc_init()", "QSYS/QFLAGSK", "gsk_secure_soc_init", "Native TLS Handshake", "Peer Certificate / Trust Store", "TLS Handshake Trust", "Peer certificate is not trusted. The issuing CA is missing from the *SYSTEM certificate store.", "CPDBC01", "Import Issuing CA into /QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB", "WRKLNK OBJ('/QIBM/UserData/ICSS/Cert/Server/*')"));
        gskDetails.put(6007L, new GskDetail(6007L, "GSK_AS400_ERROR_APPLICATION_NOT_REGISTERED", "gsk_environment_init()", "QSYS/QFLAGSK / QWTSECA", "gsk_environment_init", "Application ID Lookup", "Registration Facility", "Application ID not registered in IBM i Registration Facility.", "CPDBC07", "WRKREGINF (Work with Registration Information)", "DCM -> Manage Applications -> Add Application"));
        gskDetails.put(6011L, new GskDetail(6011L, "GSK_AS400_ERROR_BAD_CIPHER_SUITE", "gsk_secure_soc_init()", "QSYS/QFLAGSK", "gsk_secure_soc_init", "TLS Handshake Negotiation", "System Value QSSLCSL", "Cipher Suite Negotiation", "No overlapping cipher suite found between client and server.", "CPDBC0B", "DSPSYSVAL SYSVAL(QSSLCSL)", "DSPSYSVAL SYSVAL(QSSLPCL)"));
        gskDetails.put(6019L, new GskDetail(6019L, "GSK_AS400_ERROR_NO_DEFAULT_CERT", "gsk_environment_init()", "QSYS/QFLAGSK", "gsk_environment_init", "DCM Certificate Assignment", "Application ID Binding", "Application ID has no certificate assigned in DCM.", "CPDBC13", "DCM -> Manage Certificate Store -> Select *SYSTEM -> Update Application Assignment"));
        gskDetails.put(406L, new GskDetail(406L, "GSK_ERROR_IO", "gsk_secure_soc_read() / gsk_secure_soc_write()", "QSYS/QFLAGSK", "gsk_secure_soc_read", "Secure Socket I/O", "TCP/IP Socket", "Socket Communication Failure", "Secure socket I/O error or abrupt TCP connection reset.", "CPDBC06", "NETSTAT *CNN (Check socket state)", "WRKJOB JOB(*)"));
    }

    public GskDetail getGskDetail(long rc) {
        if (gskDetails.containsKey(rc)) {
            return gskDetails.get(rc);
        }
        String sym = gskkmerr.get(rc);
        if (sym != null) {
            String origin = sym.toLowerCase().startsWith("gskkm_") ? "gskkm_" + sym.substring(6).toLowerCase() + "()" : "gsk_secure_soc_*()";
            String sp = sym.contains("DATABASE") || sym.contains("DB") || sym.contains("PWD") ? "QYCUCSMGR" : (sym.contains("VALIDATION") || sym.contains("CERT") ? "QYCUCTMGR" : "QSYS/QFLAGSK");
            String jni = sym.contains("CERT") ? "qycu_listCertificatesJ" : "qycu_openStoreJ";
            String target = sym.contains("STH") || sym.contains("PWD") ? "DEFAULT.STH" : (sym.contains("KDB") || sym.contains("DATABASE") ? "DEFAULT.KDB" : "DCM Subsystem");
            return new GskDetail(rc, sym, origin, sp, "DCM Routine", jni, target, "Cryptographic Key Management", "GSKit / DCM return code " + rc + " (" + sym + ").", "CPDBCxx", "WRKLNK OBJ('/QIBM/UserData/ICSS/Cert/*')", "WRKJOB JOB(*)");
        }
        return null;
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

        // Pattern 1: ADMIN3 / Liberty SystemOut trace log
        if (input.contains("SystemOut") || input.contains("ProgramParameter") || input.contains("qycu_") || input.contains("CertificateAction") || (input.toLowerCase().contains("function:") && input.toLowerCase().contains("rc:"))) {
            return parseAdmin3Log(input);
        }

        // Pattern 2: Standard IBM symptom string
        Pattern symptomPat = Pattern.compile("(?:(\\d{4})\\s+)?(?:SP/)?([A-Z0-9_]+)\\s+(?:MOD/)?([A-Z0-9_]+)\\s+(?:RC)?(-?\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = symptomPat.matcher(input);
        if (matcher.find()) {
            String release = matcher.group(1) != null ? matcher.group(1) : "N/A";
            String sp = matcher.group(2).toUpperCase();
            String mod = matcher.group(3).toUpperCase();
            long rc = Long.parseLong(matcher.group(4));
            return deconstructWithProgram(input, release, sp, mod, rc);
        }

        // Pattern 3: Service Program + RC
        Pattern spRcPat = Pattern.compile("([A-Z0-9_]+)\\s+(?:RC)?(-?\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher spRcMatcher = spRcPat.matcher(input);
        if (spRcMatcher.find() && programs.containsKey(spRcMatcher.group(1).toUpperCase())) {
            String sp = spRcMatcher.group(1).toUpperCase();
            long rc = Long.parseLong(spRcMatcher.group(2));
            return deconstructWithProgram(input, "N/A", sp, sp, rc);
        }

        // Pattern 4: Pure numeric return code
        Pattern numPat = Pattern.compile("^(?:RC\\s*)?(-?\\d+)$", Pattern.CASE_INSENSITIVE);
        Matcher numMatcher = numPat.matcher(input);
        if (numMatcher.find()) {
            long rc = Long.parseLong(numMatcher.group(1));
            return findBestGlobalMatch(input, rc);
        }

        // Pattern 5: Keyword / Regex search
        return searchKeyword(input);
    }

    public String getGskkmName(long rc) {
        return gskkmerr.get(rc);
    }

    public String getQycuretName(long rc) {
        return qycuretcodes.get(rc);
    }

    public Map<String, Object> parseAdmin3Log(String logText) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("isAdmin3Log", true);
        result.put("rawLog", logText);

        String user = "N/A";
        String funcName = "N/A";
        Long rcVal = null;
        String job = "N/A";
        String actionFailure = "";
        List<Map<String, String>> params = new ArrayList<>();

        String[] lines = logText.split("\\r?\\n");
        for (String line : lines) {
            Matcher ufMatcher = Pattern.compile("(?:user:\\s*([^\\s/]+)\\s*/\\s*)?(?:function:\\s*)?([a-zA-Z0-9_]+)\\s*/\\s*rc:\\s*(-?\\d+)", Pattern.CASE_INSENSITIVE).matcher(line);
            if (ufMatcher.find()) {
                if (ufMatcher.group(1) != null) {
                    user = ufMatcher.group(1);
                }
                funcName = ufMatcher.group(2);
                rcVal = Long.parseLong(ufMatcher.group(3));
            } else {
                Matcher uMatcher = Pattern.compile("(?:^|\\s)user:\\s*([^\\s/]+)", Pattern.CASE_INSENSITIVE).matcher(line);
                if (uMatcher.find()) {
                    user = uMatcher.group(1);
                }
                Matcher fMatcher = Pattern.compile("(?:^|\\s)function:\\s*([^\\s/]+)", Pattern.CASE_INSENSITIVE).matcher(line);
                if (fMatcher.find()) {
                    funcName = fMatcher.group(1);
                }
                Matcher rMatcher = Pattern.compile("(?:^|\\s|/)rc:\\s*(-?\\d+)", Pattern.CASE_INSENSITIVE).matcher(line);
                if (rMatcher.find()) {
                    rcVal = Long.parseLong(rMatcher.group(1));
                }
            }

            Matcher jMatcher = Pattern.compile("job:\\s*([^\\s\\r\\n]+)", Pattern.CASE_INSENSITIVE).matcher(line);
            if (jMatcher.find()) {
                job = jMatcher.group(1);
            }

            Matcher aMatcher = Pattern.compile("(CertificateAction\\.[^\\s:]+:\\s*API failed)", Pattern.CASE_INSENSITIVE).matcher(line);
            if (aMatcher.find()) {
                actionFailure = aMatcher.group(1);
            }

            Matcher pMatcher = Pattern.compile("(?:(\\d+)\\s*:\\s*)?ProgramParameter(?:\\s*\\(\\s*input:\\s*([0-9A-Fa-f]*)\\s*/\\s*output length:\\s*(\\d+)\\s*/\\s*output:\\s*([0-9A-Fa-f]*)\\s*\\)|:\\s*([0-9A-Fa-f]+))", Pattern.CASE_INSENSITIVE).matcher(line);
            if (pMatcher.find()) {
                Map<String, String> pMap = new HashMap<>();
                String idx = pMatcher.group(1) != null ? pMatcher.group(1) : String.valueOf(params.size());
                String inHex = pMatcher.group(2) != null ? pMatcher.group(2) : (pMatcher.group(5) != null ? pMatcher.group(5) : "");
                String outLen = pMatcher.group(3) != null ? pMatcher.group(3) : "0";
                String outHex = pMatcher.group(4) != null ? pMatcher.group(4) : "";

                pMap.put("index", idx);
                pMap.put("inputHex", inHex);
                pMap.put("outputLength", outLen);
                pMap.put("outputHex", outHex);
                pMap.put("decodedInput", decodeEbcdicHex(inHex));
                pMap.put("decodedOutput", decodeEbcdicHex(outHex));
                params.add(pMap);
            }
        }

        result.put("user", user);
        result.put("function", funcName);
        result.put("returnCode", rcVal);
        result.put("hostJob", job);
        result.put("actionFailure", actionFailure);
        result.put("apiAttemptedAndFailed", (rcVal != null && rcVal != 0) || !actionFailure.isEmpty());
        result.put("parameters", params);

        String gskName = rcVal != null ? gskkmerr.get(rcVal) : null;
        String qycuName = rcVal != null ? qycuretcodes.get(rcVal) : null;
        result.put("gskkmName", gskName);
        result.put("qycuretName", qycuName);

        GskDetail detail = rcVal != null ? getGskDetail(rcVal) : null;
        if (detail != null) {
            result.put("originFunction", detail.originFunction);
            result.put("associatedServiceProgram", detail.serviceProgram);
            result.put("associatedEntryPoint", detail.entryPoint);
            result.put("associatedJniFunction", detail.jniFunction);
            result.put("targetObject", detail.targetObject);
            result.put("failureDomain", detail.failureDomain);
            result.put("msgId", detail.msgId);
            result.put("useCaseExplanation", detail.rootCause);
            List<String> cmds = new ArrayList<>(detail.recoveryCommands);
            cmds.add("WRKJOB JOB(" + (job.contains("/") ? job.split("/")[0] : "*") + ") - Inspect host server job");
            result.put("diagnosticCommands", cmds);
        } else {
            String explanation = "Executed " + funcName + " on IBM i host server job " + job + " under user " + user + ". Returned code " + rcVal + " (" + (gskName != null ? gskName : (qycuName != null ? qycuName : "General Code")) + ").";
            result.put("useCaseExplanation", explanation);
            List<String> recovery = new ArrayList<>();
            recovery.add("WRKLNK OBJ('/QIBM/UserData/ICSS/Cert/Server/*') - Verify certificate store file existence and stash file");
            recovery.add("CHGAUT OBJ('/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB') USER(QTMHHTTP) DTAAUT(*RWX)");
            recovery.add("WRKJOB JOB(" + (job.contains("/") ? job.split("/")[0] : "*") + ") - Inspect host server job");
            result.put("diagnosticCommands", recovery);
        }

        return result;
    }

    public String decodeEbcdicHex(String hexStr) {
        if (hexStr == null || hexStr.trim().isEmpty() || hexStr.length() % 2 != 0) return "";
        try {
            byte[] bytes = new byte[hexStr.length() / 2];
            for (int i = 0; i < hexStr.length(); i += 2) {
                bytes[i / 2] = (byte) Integer.parseInt(hexStr.substring(i, i + 2), 16);
            }
            return new String(bytes, "CP037").replace("\0", "").trim();
        } catch (Exception e) {
            return "";
        }
    }

    private Map<String, Object> deconstructWithProgram(String raw, String release, String sp, String mod, long rc) {
        ProgramInfo prog = programs.get(sp);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("input", raw);
        result.put("release", release);
        result.put("serviceProgram", sp);
        result.put("serviceProgramName", prog != null ? prog.name : "Unknown Service Program");
        result.put("module", mod);
        result.put("returnCode", rc);

        long entryVal = (rc / 1000000L) * 1000000L;
        long remainder = rc - entryVal;
        String entryName = prog != null ? prog.entryPoints.get(entryVal) : null;

        if (entryName == null && prog != null && prog.entryPoints.containsKey(rc)) {
            entryVal = rc;
            entryName = prog.entryPoints.get(rc);
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
                            if (prog != null && prog.localFailures.containsKey(localVal)) {
                                localName = prog.localFailures.get(localVal);
                            }
                            break;
                        }
                    }
                }
                if (utilName == null && prog != null && prog.localFailures.containsKey(remainder)) {
                    localVal = remainder;
                    localName = prog.localFailures.get(remainder);
                }
            }
        }

        boolean isExact = (entryName != null || entryVal == 0) && (remainder == 0 || utilName != null || localName != null);

        StringBuilder useCase = new StringBuilder();
        useCase.append("During processing in ");
        useCase.append(prog != null ? prog.name : sp);
        useCase.append(" (").append(sp).append(")");
        if (entryName != null) {
            useCase.append(", entry point ").append(entryName).append(" was executing");
        } else if (entryVal > 0) {
            useCase.append(", entry point code ").append(entryVal).append(" was invoked");
        }
        if (utilName != null) {
            useCase.append(". A failure occurred in utility function ").append(utilName);
            if (utilCategory != null) {
                useCase.append(" (").append(utilCategory).append(")");
            }
        }
        if (localName != null) {
            useCase.append(", specifically at local point of failure ").append(localName).append(" (code ").append(localVal).append(")");
        } else if (localVal > 0) {
            useCase.append(", with local failure offset ").append(localVal);
        }
        useCase.append(".");

        String operationalDomain = getOperationalDomain(sp, utilCategory);
        List<String> diagnosticCommands = getDiagnosticCommands(sp, operationalDomain);
        List<String> affectedObjects = getAffectedObjects(sp, operationalDomain);

        result.put("isExact", isExact);
        result.put("isClosest", !isExact);
        result.put("operationalDomain", operationalDomain);
        result.put("majorEntryPointCode", entryVal);
        result.put("majorEntryPointName", entryName != null ? entryName : "N/A");
        result.put("utilityFunctionCode", utilVal);
        result.put("utilityFunctionName", utilName != null ? utilName : "None (Direct Module Failure)");
        result.put("utilityFunctionCategory", utilCategory);
        result.put("localFailureCode", localVal);
        result.put("localFailureName", localName);
        result.put("useCaseExplanation", useCase.toString());
        result.put("targetObjects", affectedObjects);
        result.put("diagnosticCommands", diagnosticCommands);

        if (!isExact) {
            result.put("cspReminder", "Reminder for technicians: Search iAI assisted support searches in CSP.");
        }

        return result;
    }

    private String getOperationalDomain(String sp, String utilCategory) {
        if ("QYCUAPPMGR".equals(sp) || (utilCategory != null && utilCategory.contains("Application"))) {
            return "Application Integration & Trust";
        }
        if ("QYCUCSMGR".equals(sp) || (utilCategory != null && utilCategory.contains("Store"))) {
            return "Certificate Store & Stash Files";
        }
        if ("QYCUCTMGR".equals(sp) || (utilCategory != null && utilCategory.contains("Certificate Utility"))) {
            return "Certificate Lifecycle & CSR";
        }
        if ("QYCULCAMGR".equals(sp) || (utilCategory != null && utilCategory.contains("Local Certificate Authority"))) {
            return "Local Certificate Authority";
        }
        if ("QYCUHDWMGR".equals(sp) || (utilCategory != null && utilCategory.contains("Hardware Device"))) {
            return "Hardware Cryptographic Devices";
        }
        if ("QYCUCRLMGR".equals(sp) || "QYCUGENMGR".equals(sp) || (utilCategory != null && utilCategory.contains("Revocation"))) {
            return "Revocation & Validation (CRL)";
        }
        if ("QYCUYDOMGR".equals(sp)) {
            return "Object & Signature Manager";
        }
        return "General DCM Subsystem";
    }

    private List<String> getDiagnosticCommands(String sp, String domain) {
        List<String> cmds = new ArrayList<>();
        if (domain.contains("Application")) {
            cmds.add("WRKREGINF (Work with Registration Information for Application IDs)");
            cmds.add("WRKAUT OBJ('/QIBM/UserData/ICSS/Cert/Server')");
        } else if (domain.contains("Store")) {
            cmds.add("WRKLNK OBJ('/QIBM/UserData/ICSS/Cert/Server/*')");
            cmds.add("CHGAUT OBJ('/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB') USER(QTMHHTTP) DTAAUT(*RWX)");
        } else if (domain.contains("Local Certificate Authority")) {
            cmds.add("WRKLNK OBJ('/QIBM/UserData/ICSS/Cert/CertAuth/LOCAL_CERTIFICATE*.txt') (Verify Local CA certificate files)");
            cmds.add("WRKOBJ OBJ(QUSRSYS/*ALL) OBJTYPE(*VLDL)");
        } else if (domain.contains("Hardware")) {
            cmds.add("WRKHDWRSC TYPE(*CRP) (Work with Cryptographic Hardware Resources)");
            cmds.add("WRKACTJOB SBS(QSEC) (Check Cryptographic subsystem jobs)");
        } else if (domain.contains("Revocation")) {
            cmds.add("WRKOBJ OBJ(QUSRSYS/QYCU*) OBJTYPE(*VLDL)");
            cmds.add("PING <LDAP_OR_HTTP_CRL_SERVER>");
        } else {
            cmds.add("WRKACTJOB SBS(QHTTPSVR) (Check HTTP & Liberty server status)");
            cmds.add("DSPLOG MSGID(CPF* CPD*)");
        }
        return cmds;
    }

    private List<String> getAffectedObjects(String sp, String domain) {
        List<String> objs = new ArrayList<>();
        if (domain.contains("Application")) {
            objs.add("IBM i Registration Facility (QSYS/QUSRSYS)");
            objs.add("/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB");
        } else if (domain.contains("Store")) {
            objs.add("/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB");
            objs.add("/QIBM/UserData/ICSS/Cert/Server/DEFAULT.RDB");
            objs.add("/QIBM/UserData/ICSS/Cert/Server/DEFAULT.STH (Password Stash)");
        } else if (domain.contains("Local Certificate Authority")) {
            objs.add("/QIBM/UserData/ICSS/Cert/CertAuth/LOCAL_CERTIFICATE*.txt");
            objs.add("Local CA Policy Database (*VLDL)");
        } else if (domain.contains("Hardware")) {
            objs.add("PCIe Cryptographic Coprocessors (4767, 4769, 4770)");
            objs.add("CCA / Enterprise PKCS#11 Master Key Registers");
        } else if (domain.contains("Revocation")) {
            objs.add("Validation Lists (*VLDL) in QUSRSYS");
            objs.add("CRL Distribution Points (LDAP / HTTP)");
        } else {
            objs.add("DCM Configuration & Certificate Store Files");
        }
        return objs;
    }

    private Map<String, Object> findBestGlobalMatch(String raw, long rc) {
        // 1. Check GSKKM & GSKSSL
        if (gskkmerr.containsKey(rc)) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("isGskCode", true);
            result.put("input", raw);
            result.put("returnCode", rc);
            result.put("symbolicName", gskkmerr.get(rc));
            result.put("source", rc >= 6000L || rc == 406L || rc == 407L || rc == 408L ? "gskssl.h / QSYSINC/H(GSKSSL)" : "gskkmerr.h / c3.xpf (GSKit Error)");

            GskDetail detail = getGskDetail(rc);
            if (detail != null) {
                result.put("originFunction", detail.originFunction);
                result.put("associatedServiceProgram", detail.serviceProgram);
                result.put("associatedEntryPoint", detail.entryPoint);
                result.put("associatedJniFunction", detail.jniFunction);
                result.put("targetObject", detail.targetObject);
                result.put("failureDomain", detail.failureDomain);
                result.put("msgId", detail.msgId);
                result.put("useCaseExplanation", detail.rootCause);
                result.put("diagnosticCommands", detail.recoveryCommands);
            } else {
                result.put("useCaseExplanation", "GSKit / GSKKM return code " + rc + " maps to constant " + gskkmerr.get(rc) + ".");
            }
            return result;
        }

        // 2. Check QYCU RetCodes
        if (qycuretcodes.containsKey(rc)) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("isQycuRetCode", true);
            result.put("input", raw);
            result.put("returnCode", rc);
            result.put("symbolicName", qycuretcodes.get(rc));
            result.put("source", "qycuretcodes.H (QYCU Return Code)");
            result.put("useCaseExplanation", "QYCU API return code " + rc + " maps to constant " + qycuretcodes.get(rc) + ".");
            return result;
        }

        // 3. Check Programs deconstruct
        for (String sp : programs.keySet()) {
            Map<String, Object> dec = deconstructWithProgram(raw, "N/A", sp, sp, rc);
            if (Boolean.TRUE.equals(dec.get("isExact"))) {
                return dec;
            }
        }

        // 4. Fallback closest match
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("isClosest", true);
        result.put("input", raw);
        result.put("returnCode", rc);
        result.put("error", "Return code " + rc + " was not an exact match.");
        result.put("cspReminder", "Reminder for technicians: Search iAI assisted support searches in CSP.");
        return result;
    }

    public Map<String, Object> searchKeyword(String query) {
        Pattern regex;
        try {
            regex = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            regex = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
        }

        List<Map<String, String>> matches = new ArrayList<>();

        for (Map.Entry<Long, String> g : gskkmerr.entrySet()) {
            if (regex.matcher(g.getValue()).find() || regex.matcher(g.getKey().toString()).find()) {
                Map<String, String> m = new HashMap<>();
                m.put("type", "GSKit / GSKKM Error");
                m.put("location", "gskkmerr.h");
                m.put("name", g.getValue());
                m.put("code", g.getKey().toString());
                matches.add(m);
            }
        }

        for (Map.Entry<Long, String> q : qycuretcodes.entrySet()) {
            if (regex.matcher(q.getValue()).find() || regex.matcher(q.getKey().toString()).find()) {
                Map<String, String> m = new HashMap<>();
                m.put("type", "QYCU Return Code");
                m.put("location", "qycuretcodes.H");
                m.put("name", q.getValue());
                m.put("code", q.getKey().toString());
                matches.add(m);
            }
        }

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

