package com.ibmi.iws.service;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;
import com.ibm.as400.access.QSYSObjectPathName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to execute native IBM i programs (*PGM) via IBM Toolbox for Java (JT400).
 * When running directly on IBM i (WebSphere Liberty 24.0.0.12+), default constructor connects locally using system authority.
 */
public class ProgramCallerService {

    public Map<String, Object> callProgram(String library, String programName, String inputParam, int outputLength) throws Exception {
        Map<String, Object> result = new HashMap<>();

        // Connect locally when executing on IBM i (localhost)
        AS400 as400 = new AS400();

        try {
            String pgmPath = QSYSObjectPathName.toPath(library.toUpperCase(), programName.toUpperCase(), "PGM");

            // Convert string input to EBCDIC byte parameter (CCSID 37 / default)
            AS400Text inputConverter = new AS400Text(Math.max(inputParam.length(), 1));
            byte[] inBytes = inputConverter.toBytes(inputParam);

            // Output parameter buffer
            AS400Text outputConverter = new AS400Text(outputLength);
            byte[] outBytes = new byte[outputConverter.getByteLength()];

            ProgramParameter[] parameterList = new ProgramParameter[] {
                new ProgramParameter(inBytes),
                new ProgramParameter(outBytes)
            };

            ProgramCall program = new ProgramCall(as400, pgmPath, parameterList);

            boolean success = program.run();

            result.put("success", success);
            result.put("program", pgmPath);

            if (success) {
                byte[] outData = parameterList[1].getOutputData();
                String outText = (outData != null) ? (String) outputConverter.toObject(outData) : "";
                result.put("output", outText.trim());
            } else {
                List<String> messages = new ArrayList<>();
                for (AS400Message msg : program.getMessageList()) {
                    messages.add(msg.getID() + ": " + msg.getText());
                }
                result.put("errors", messages);
            }
        } finally {
            as400.disconnectAllServices();
        }

        return result;
    }
}
