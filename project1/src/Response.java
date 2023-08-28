
import java.net.*;
import java.nio.*;

import header.Header;
import resourceRecord.RDATA;
import resourceRecord.Record;

public class Response {
	private byte[] responseBuffer;
    private boolean QR;
    private int RCODE, ANCOUNT;
    private Record[] answerRecords;
    private String typeRecord;

	public Response(byte[] responseBuffer, int requestLength, String typeRecord) throws UnknownHostException {
		this.responseBuffer = responseBuffer;
		this.typeRecord = typeRecord;

        this.verifyHeader();
        this.verifyQuestion();
        
        // after the header and question (of size : requestLength): answer 
        // ANCOUNT: an unsigned 16 bit int: #resource records in the answer
        answerRecords = new Record[ANCOUNT];
        int length = requestLength;
        for (int i = 0; i < ANCOUNT; i++) {
        	answerRecords[i] = this.parseAnswer(length);
        	length += answerRecords[i].getLength();
        }
    }

    private void verifyHeader() {
        // identifier : 2 octets

        // Flags :
        byte flags1 = responseBuffer[2];
        byte flags2 = responseBuffer[3];
        // 1) QR: 1 = response
        this.QR = getBit(flags1, 7) == 1;
        this.verifyQR();

        // 8) RCODE: 4 bit field is set in responses
        this.RCODE = flags2 & 0x0F;
        this.verifyRCODE();

        // counts:
        // 2) ANCOUNT: an unsigned 16 bit int: #resource records in the answer
        byte[] ANCount = {responseBuffer[6], responseBuffer[7]};
        ByteBuffer wrapped = ByteBuffer.wrap(ANCount);
        this.ANCOUNT = wrapped.getShort();
    }

    private int getBit(byte b, int position) {
    	return (b >> position) & 1;
    }

    private void verifyRCODE() {
	    switch(this.RCODE) {
            case 0:
                //No error condition
                break;
            case 1:
                throw new RuntimeException("Format error - The name server was unable to interpret the query");
            case 2:
                throw new RuntimeException("Server failure - The name server was unable to process this query due to a problem with the name server");
            case 3:
                throw new RuntimeException("Name Error - authoritative name server : The domain name referenced in the query does not exist");
            case 4:
                throw new RuntimeException("Not implemented - The name server does not support the requested kind of query");
            case 5:
                throw new RuntimeException("Refused - The name server refuses to perform the specified operation for policy reasons");
            default:
                break;
        }
    }

    private void verifyQR() {
        //QR is supposed to be 1 in response
        if (!this.QR) {
            throw new RuntimeException("The message from the server is not a response");
        }
    }

    private void verifyQuestion() {
        // question after the header
        int index = Header.HEADERLENGTH;

        // QNAME:
        // while it isn't the terminated byte
        while (this.responseBuffer[index] != 0) {
            index++;
        }

        // QTYPE:
        byte[] QTYPE = new byte[2];
        QTYPE[0] = this.responseBuffer[index + 1];
        QTYPE[1] = this.responseBuffer[index + 2];
        String qtypeRecord;

        if (QTYPE[0] == 0) {
            if (QTYPE[1] == 1)
            {
                qtypeRecord = "A";
            }
            else if (QTYPE[1] == 16)
            {
                qtypeRecord = "TXT";
            }
            else {
            	throw new RuntimeException("Unrecognized query type in response");
            }
        }
        else {
        	throw new RuntimeException("Unrecognized query type in response");
        }
        this.verifyQTYPE(qtypeRecord);

        // QCLASS:
        byte[] QCLASS = new byte[2];
        QCLASS[0] = this.responseBuffer[index + 1];
        QCLASS[1] = this.responseBuffer[index + 2];
        this.verifyQCLASS(QCLASS);
    }

    private void verifyQTYPE(String qtypeRecord) {
        if (! qtypeRecord.equals(this.typeRecord)) {
            throw new RuntimeException("The response query type is different than the request query type");
        }
    }

    private void verifyQCLASS(byte[] QCLASS) {
        // CLASS : IN = 1
        if (QCLASS[0] != 0 && QCLASS[1] != 1) {
            throw new RuntimeException("Unrecognized class type in response, should be 1");
        }
    }

    private Record parseAnswer(int index) throws UnknownHostException {
    	Record result = new Record();
    	
        int countByte = index;

        //NAME
        RDATA IPaddressResult = getIPaddress(countByte);
        countByte += IPaddressResult.getLength();

        result.setTYPE(getAndVerifyTYPE(countByte));
        countByte += 2;

        VerifyCLASS(countByte);
        countByte +=2;

        result.setTTL(getTTL(countByte));
        countByte += 4;

        result.setRDLength(getRDLength(countByte));
        countByte += 2;

        switch (result.getTYPE()) {
            case "A":
                result.setRData(parseATypeRDATA(result.getRDLength(), countByte));
                break;
            case "TXT":
                result.setRData(parseTXTTypeRDATA(result.getRDLength(), countByte));
                break;
            default:
                result.setRData("notSupported");
                break;
        }
        result.setLength(countByte + result.getRDLength() - index);
        return result;
    }

    private String parseATypeRDATA(int RDLength, int countByte) throws UnknownHostException {
        String rdata = "";
        byte[] byteAddress = new byte[RDLength];
        for (int i = 0; i < RDLength; i++) {
            byteAddress[i] = responseBuffer[countByte + i];
        }

        InetAddress inetaddress = InetAddress.getByAddress(byteAddress);
        //take away the "/"
        rdata = inetaddress.toString().substring(1);
        return rdata;
    }

    private String parseTXTTypeRDATA(int RDLength, int countByte) throws UnknownHostException {
        // single length octet followed by that number of characters
        // need to do & 0xff to have an unsigned value
        int length = responseBuffer[countByte] & 0xff;
        String rdata = "";
        
        //one or more strings -> at least 1 time we go in "do"
        /*if ((RDLength - 1) != length) {
            throw new RuntimeException("Error in the length of the TXT response");
        }

        
        for (int i = 0; i < length; i++) {
            rdata += (char) responseBuffer[countByte + 1 + i];
        }*/
        do {
            for (int i = 0; i < length; i++) {
                rdata += (char) responseBuffer[countByte + 1 + i];
            }
            RDLength -= 1 + length;
        } while (RDLength != 0);

        return rdata;
    }
    
    private RDATA getIPaddress(int index) {
    	RDATA result = new RDATA();
    	int wordLength = responseBuffer[index];
    	String IPaddress = "";
    	int length = 0;
        boolean start = true;

        // end with the 0x00 label
        while(wordLength != 0) {
			if (!start) {
				IPaddress += ".";
			}
	    	if ((wordLength & 0xC0) == (int) 0xC0) {
                // Message compression
                // 2 octets: 11 + offset
                // 0xC0 = 11000000
	    		byte[] offset = new byte[2];
                // 0x3F : 00111111
                // to keep the offset : without the first "11"
                offset[0] = (byte) (responseBuffer[index] & 0x3F);
                offset[1] = responseBuffer[index + 1];
                ByteBuffer wrapped = ByteBuffer.wrap(offset);
	            IPaddress += getIPaddress(wrapped.getShort()).getIPaddress();
                index += 2;
                wordLength = 0;
	            length += 2;
	    	}
            else {
                // for labels
    	        for (int i = 0; i < wordLength; i++) {
    	        	IPaddress += (char) responseBuffer[index + 1 + i]; // takes one octet at a time
		        }
	    		index += 1 + wordLength; // +1 for the label
                wordLength = responseBuffer[index];
	    		length += 1 + wordLength;
	    	}
            start = false;
    	}
    	result.setIPaddress(IPaddress);
    	result.setLength(length);

    	return result;
    }

    private String getAndVerifyTYPE(int countByte) {
        byte[] TYPE = new byte[2];
        TYPE[0] = responseBuffer[countByte];
        TYPE[1] = responseBuffer[countByte + 1];
        if (TYPE[0] == 0) {
            if (TYPE[1] == 1) {
                return "A";
            }
            else if (TYPE[1] == 16) {
                return  "TXT";
            }
        }
        return "notSupported";
    }

    private void VerifyCLASS(int countByte) {
        byte[] CLASS = new byte[2];
        CLASS[0] = responseBuffer[countByte];
        CLASS[1] = responseBuffer[countByte + 1];
        this.verifyQCLASS(CLASS);
    }

    private int getTTL(int countByte) {
        byte[] TTL = new byte[4];
        TTL[0] = responseBuffer[countByte];
        TTL[1] = responseBuffer[countByte + 1];
        TTL[2] = responseBuffer[countByte + 2];
        TTL[3] = responseBuffer[countByte + 3];
        ByteBuffer wrapped = ByteBuffer.wrap(TTL);
        return wrapped.getInt();
    }

    private short getRDLength(int countByte) {
        byte[] RDLength = new byte[2];
        RDLength[0] = responseBuffer[countByte];
        RDLength[1] = responseBuffer[countByte + 1];
        ByteBuffer wrapped = ByteBuffer.wrap(RDLength);
        return wrapped.getShort();
    }

    public void printResponse() {
        for (int i = 0; i < answerRecords.length; i++) {
            answerRecords[i].printRecord();
        }
    }
}
