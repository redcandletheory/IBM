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
    public void testGsksslLookup() {
        Map<String, Object> dec0 = service.searchGskCode(0);
        assertTrue((Boolean) dec0.get("success"));
        assertEquals("GSK_OK", dec0.get("constant"));

        Map<String, Object> dec406 = service.searchGskCode(406);
        assertTrue((Boolean) dec406.get("success"));
        assertEquals("GSK_ERROR_IO", dec406.get("constant"));

        Map<String, Object> dec6000 = service.searchGskCode(6000);
        assertTrue((Boolean) dec6000.get("success"));
        assertEquals("GSK_AS400_ERROR_NOT_TRUSTED", dec6000.get("constant"));

        Map<String, Object> dec6019 = service.searchGskCode(6019);
        assertTrue((Boolean) dec6019.get("success"));
        assertEquals("GSK_AS400_ERROR_NO_DEFAULT_CERT", dec6019.get("constant"));
    }

    @Test
    public void testEbcdicHexDecoding() {
        String hexKdb = "61D8C9C2D461E4E2C5D9C4C1E3C161C9C3E2E261C3C5D9E361E2C5D9E5C5D961C4C5C6C1E4D3E34BD2C4C200";
        String decoded = DcmSearchService.decodeEbcdicHex(hexKdb);
        assertEquals("/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB", decoded);

        String hexAllKeys = "C1D3D36DD2C5E8E200";
        assertEquals("ALL_KEYS", DcmSearchService.decodeEbcdicHex(hexAllKeys));

        String hexHasPrivKey = "C8C1E2D7D9C9E5C1E3C5D2C5E800";
        assertEquals("HASPRIVATEKEY", DcmSearchService.decodeEbcdicHex(hexHasPrivKey));
    }

    @Test
    public void testAdmin3LogParsing() {
        String sampleLog = "user: QSECOFR\n" +
                "current thread: Thread[WebContainer : 3,5,main]\n" +
                "class: com.ibm.as400.access.AS400\n" +
                "function: qycu_listCertificatesJ\n" +
                "0: ProgramParameter(input: 61D8C9C2D461E4E2C5D9C4C1E3C161C9C3E2E261C3C5D9E361E2C5D9E5C5D961C4C5C6C1E4D3E34BD2C4C200 / output length: 0 / output: )\n" +
                "1: ProgramParameter(input: C1D3D36DD2C5E8E200 / output length: 0 / output: )\n" +
                "rc: 6000\n" +
                "Job: 123456/QTMHHTTP/ADMIN3";

        Map<String, Object> result = service.parseAdmin3Log(sampleLog);
        assertNotNull(result);
        assertEquals("QSECOFR", result.get("user"));
        assertEquals("qycu_listCertificatesJ", result.get("funcName"));
        assertEquals(6000L, result.get("rc"));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> params = (List<Map<String, String>>) result.get("parameters");
        assertNotNull(params);
        assertTrue(params.size() >= 2);
        assertEquals("/QIBM/UserData/ICSS/Cert/Server/DEFAULT.KDB", params.get(0).get("decodedInput"));
        assertEquals("ALL_KEYS", params.get(1).get("decodedInput"));
        assertEquals("GSK_AS400_ERROR_NOT_TRUSTED", result.get("constant"));
    }
}
