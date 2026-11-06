package com.ibmi.ias.service;

import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DcmSearchServiceTest {

    private DcmSearchService service;

    @Before
    public void setUp() {
        service = new DcmSearchService();
    }

    @Test
    public void testGskkmLookup() {
        String name6 = service.getGskkmName(6);
        assertEquals("GSKKM_ERR_DATABASE_OPEN", name6);

        String name16 = service.getGskkmName(16);
        assertEquals("GSKKM_ERR_DATABASE_INVALID_PASSWORD", name16);

        DcmSearchService.GskDetail detail6 = service.getGskDetail(6);
        assertNotNull(detail6);
        assertEquals("GSKKM_ERR_DATABASE_OPEN", detail6.constant);
        assertEquals("gskkm_open_database() / gskkm_open_key_database()", detail6.originFunction);
        assertEquals("QYCUCSMGR / QYCUCTMGR", detail6.serviceProgram);
        assertEquals("/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB", detail6.targetObject);

        DcmSearchService.GskDetail detail6000 = service.getGskDetail(6000);
        assertNotNull(detail6000);
        assertEquals("GSK_AS400_ERROR_NOT_TRUSTED", detail6000.constant);
        assertEquals("gsk_secure_soc_init()", detail6000.originFunction);
        assertEquals("QSYS/QFLAGSK", detail6000.serviceProgram);

        Map<String, Object> dec = service.decode("6");
        assertNotNull(dec);
        assertTrue((Boolean) dec.get("success"));
        assertEquals("GSKKM_ERR_DATABASE_OPEN", dec.get("symbolicName"));
        assertEquals("gskkm_open_database() / gskkm_open_key_database()", dec.get("originFunction"));
    }

    @Test
    public void testEbcdicHexDecoding() {
        String hexKdb = "61D8C9C2D461E4E2C5D9C4C1E3C161C9C3E2E261C3C5D9E361E2C5D9E5C5D961C4C5C6C1E4D3E34BD2C4C200";
        String decoded = service.decodeEbcdicHex(hexKdb);
        assertEquals("/QIBM/USERDATA/ICSS/CERT/SERVER/DEFAULT.KDB", decoded);

        String hexAllKeys = "C1D3D36DD2C5E8E200";
        assertEquals("ALL_KEYS", service.decodeEbcdicHex(hexAllKeys));

        String hexHasPrivKey = "C8C1E2D7D9C9E5C1E3C5D2C5E800";
        assertEquals("HASPRIVATEKEY", service.decodeEbcdicHex(hexHasPrivKey));
    }

    @Test
    public void testAdmin3LogParsing() {
        String sampleLog = "user: QSECOFR\n" +
                "current thread: Thread[WebContainer : 3,5,main]\n" +
                "class: com.ibm.as400.access.AS400\n" +
                "function: qycu_listCertificatesJ\n" +
                "0: ProgramParameter(input: 61D8C9C2D461E4E2C5D9C4C1E3C161C9C3E2E261C3C5D9E361E2C5D9E5C5D961C4C5C6C1E4D3E34BD2C4C200 / output length: 0 / output: )\n" +
                "1: ProgramParameter(input: C1D3D36DD2C5E8E200 / output length: 0 / output: )\n" +
                "rc: 6\n" +
                "Job: 123456/QTMHHTTP/ADMIN3";

        Map<String, Object> result = service.parseAdmin3Log(sampleLog);
        assertNotNull(result);
        assertEquals("QSECOFR", result.get("user"));
        assertEquals("qycu_listCertificatesJ", result.get("function"));
        assertEquals(6L, result.get("returnCode"));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> params = (List<Map<String, String>>) result.get("parameters");
        assertNotNull(params);
        assertTrue(params.size() >= 2);
        assertEquals("/QIBM/USERDATA/ICSS/CERT/SERVER/DEFAULT.KDB", params.get(0).get("decodedInput"));
        assertEquals("ALL_KEYS", params.get(1).get("decodedInput"));

        assertEquals("GSKKM_ERR_DATABASE_OPEN", result.get("gskkmName"));
        assertEquals("gskkm_open_database() / gskkm_open_key_database()", result.get("originFunction"));
        assertEquals("QYCUCSMGR / QYCUCTMGR", result.get("associatedServiceProgram"));
        assertEquals("/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB", result.get("targetObject"));
    }

    @Test
    public void testSymptomStringDecoding() {
        String symptom = "5733SC100 SP/QYCUAPPMGR MOD/QYCUAPPMGR RC4000000";
        Map<String, Object> result = service.decode(symptom);
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("QYCUAPPMGR", result.get("serviceProgram"));
    }

    @Test
    public void testSearchKeyword() {
        Map<String, Object> result = service.searchKeyword("GSKKM_ERR_DATABASE_OPEN");
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> matches = (List<Map<String, String>>) result.get("matches");
        assertFalse(matches.isEmpty());
        assertEquals("GSKit / GSKKM Error", matches.get(0).get("type"));
    }
}
