import java.io.*;
import java.nio.*;
import java.net.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import header.Header;

public class Response {
    private Request request;
    private int lengthRequest, RCODE;
    private String tunneledData, IPaddress;
    private byte[] answer, responseBuffer;
    private final static int MAXBYTES = 60000;
    private boolean wrongDomain;
    private boolean TC = false;
    private byte ANCOUNT2 = 0x00;

    Response(Request request, String IPaddress, int lengthRequest) throws IOException {
        this.request = request;
        this.IPaddress = IPaddress;
        this.lengthRequest = lengthRequest;
        wrongDomain = request.wrongDomain();
        RCODE = request.getRCODE();
        tunneledData = request.getTunneledData();
        getFromURL();
    }

    private void getFromURL() throws IOException {
        if (RCODE == 0) {
            try {
                // http request
                URL url = new URL(tunneledData);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.addRequestProperty("User-Agent", "DNSTunnel/1.0");
                BufferedReader buffer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String input;
                StringBuffer response = new StringBuffer();
                while((input = buffer.readLine()) != null) {
                    response.append(input);
                }
                buffer.close();

                // than encode it in base64
                String responseString = response.toString();
                answer = Base64.getDecoder().decode(Base64.getEncoder().encodeToString(responseString.getBytes(StandardCharsets.ISO_8859_1)));

                // verify response <= 60000 bytes
                if (answer.length > MAXBYTES) {
                    TC = true;
                    answer = truncate(answer, MAXBYTES - 1);
                }
                int tmp = (int) (Math.ceil((double) answer.length/255));
                ANCOUNT2 =  (byte) tmp;
            }
            catch(Exception e) {
                RCODE = 3;
            }
        }
        createResponse();

        System.out.println("Question (CL=" + this.IPaddress + ", NAME=" + request.getQNAMEparsed() + ", TYPE=" + request.getQTYPEString() + ") => " + RCODE);
    }

    public static byte[] truncate(byte[] array, int newLength, int start, int dest) {
        if (array.length < newLength) {
            return array;
        } else {
            byte[] truncated = new byte[newLength];
            System.arraycopy(array, start, truncated, dest, newLength);

            return truncated;
        }
    }

    public static byte[] truncate(byte[] array, int newLength) {
        return truncate(array, newLength, 0, 0);
    }

    public boolean wrongDomain() {
        return wrongDomain;
    }

    public void createResponse() throws IOException {
        int responseSize = lengthRequest;

        // get arguments needed for the response
        byte[] ID = request.getID();
        int OPCODE = request.getOPCODE();
        byte QDCOUNT1 = request.getQDCOUNT1();
        byte QDCOUNT2 = request.getQDCOUNT2();
        byte[] QTYPE = request.getQTYPE();
        byte[] QCLASS = request.getQCLASS();

        // create the header
        Header header = new Header(ID, RCODE, TC, OPCODE, QDCOUNT1, QDCOUNT2, ANCOUNT2);
        byte[] headerByte = header.responseHeader();

        // get the question
        byte[] question = request.getQuestion();
        
        // create the answer if needed
        // and adjust the responseSize according to it
        AnswerRecord answerRR = null;
        if (answer != null) {
            answerRR = new AnswerRecord(QTYPE, QCLASS, answer);
            responseSize += answerRR.lengthAnswerRR();
        }

        // converting the int responseSize to byte[2]
		byte[] responseSizeByte = new byte[2];
		responseSizeByte[0] = (byte) ((responseSize >>> 8) & 0xFF);
		responseSizeByte[1] = (byte) (responseSize & 0xFF);

        // first 2 bytes: length of the message, without counting these 2 bytes
        ByteBuffer responseBuffer = ByteBuffer.allocate(2 + responseSize);
        responseBuffer.put(responseSizeByte);
        responseBuffer.put(headerByte);
        responseBuffer.put(question);
        if (answerRR != null) {
            responseBuffer.put(answerRR.responseAnswerRecord());
        }

        this.responseBuffer = responseBuffer.array();
    }

    public byte[] getResponse() {
        return responseBuffer;
    }
}
