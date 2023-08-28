import java.nio.*;
import java.nio.charset.StandardCharsets;

public class Request {
    private byte[] responseBuffer;
    private int length;
    private String ownedDomain;
    private byte[] ID, QTYPE, QCLASS;
    private boolean QR, AA, TC, RD, RA;
    private int RCODE = 0, QDCount, ANCount, NSCount, ARCount, OPCODE, Z;
    private byte QDCOUNT1, QDCOUNT2;
    public static final int HEADERLENGTH = 12;
    private String QNAMEparsed;
    private String qtypeRecord = "unknown";
    private String tunneledData;
    private boolean wrongDomain = false;
    private byte[] question;

    public Request(byte[] responseBuffer, int length, String ownedDomain) {
        this.responseBuffer = responseBuffer;
        this.length = length;
        this.ownedDomain = ownedDomain;
        this.parseHeader();
        this.parseQuestion();
    }

    private void parseHeader() {
        byte[] ID = new byte[2];
        for(int i = 0; i < 2; i++) {
            ID[i] = responseBuffer[i];
        }
        this.ID = ID;

        this.QR = getBit(7, responseBuffer[2]) == 1;
        this.verifyQR();
        this.OPCODE = (responseBuffer[2] & 0x78) >> 3;
        this.verifyOPCODE();
        this.AA = getBit(2, responseBuffer[2]) == 1;
        this.TC = getBit(1, responseBuffer[2]) == 1;
        this.RD = getBit(0, responseBuffer[2]) == 1;
        this.RA = getBit(7, responseBuffer[3]) == 1;
        this.Z = (responseBuffer[3] & 0x70) >> 4;

        byte[] QDCount = {responseBuffer[4], responseBuffer[5]};
        QDCOUNT1 = QDCount[0];
        QDCOUNT2 = QDCount[1];
        ByteBuffer buf1 = ByteBuffer.wrap(QDCount);
        this.QDCount = buf1.getShort();
        this.verifyQDCount();

        byte[] ANCount = {responseBuffer[6], responseBuffer[7]};
        ByteBuffer buf2 = ByteBuffer.wrap(ANCount);
        this.ANCount = buf2.getShort();
        this.verifyCount(this.ANCount);

        byte[] NSCount = {responseBuffer[8], responseBuffer[9]};
        ByteBuffer buf3 = ByteBuffer.wrap(NSCount);
        this.NSCount = buf3.getShort();
        this.verifyCount(this.NSCount);

        byte[] ARCount = {responseBuffer[10], responseBuffer[11]};
        ByteBuffer buf4 = ByteBuffer.wrap(ARCount);
        this.ARCount = buf4.getShort();
        this.verifyCount(this.ARCount);
    }

    private void verifyCount(int count) {
        if(count != 0) {
            RCODE = 1;
        }
    }
    
    private void verifyQDCount() {
        if(this.QDCount != 1) {
            RCODE = 1;
        }
    }

    private void verifyOPCODE() {
        if(this.OPCODE != 0) {
            RCODE = 1;
        }
    }

    private void verifyQR() {
        if (this.QR) {
            RCODE = 1;
        }
    }

    private void parseQuestion() {
        // question after the header
        int index = HEADERLENGTH;
        int start = index;

        // QNAME:
        String QNAME = "";
        boolean first = true;
        // while it isn't the terminated byte
        while (this.responseBuffer[index] != 0) {
            length = this.responseBuffer[index];
            for(int i = 1; i < length + 1; i++) {
                byte[] tmp = new byte[1];
                tmp[0] = responseBuffer[index + i];
                QNAME += new String(tmp, StandardCharsets.ISO_8859_1);
            }

            index += length + 1;
            if (responseBuffer[index] != 0) {
                if (first) {
                    first = false;
                    // base32 only of a part
                    // no need of adding padding, works without !
                    tunneledData = new String(Base32.decode(QNAME), StandardCharsets.ISO_8859_1);
                }
                QNAME += ".";
            }
        }
        this.QNAMEparsed = QNAME;
        this.verifyQNAME(this.QNAMEparsed);

        // QTYPE:
        byte[] QTYPE = new byte[2];
        QTYPE[0] = this.responseBuffer[index + 1];
        QTYPE[1] = this.responseBuffer[index + 2];

        if (QTYPE[0] == 0) {
            if (QTYPE[1] == 1)
            {
                qtypeRecord = "A";
            }
            else if (QTYPE[1] == 16)
            {
                qtypeRecord = "TXT";
            }
        }
        this.QTYPE = QTYPE;
        this.verifyQTYPE(qtypeRecord);

        // QCLASS:
        byte[] QCLASS = new byte[2];
        QCLASS[0] = this.responseBuffer[index + 3];
        QCLASS[1] = this.responseBuffer[index + 4];
        this.QCLASS = QCLASS;
        this.verifyQCLASS(this.QCLASS);

        // Store the question as a byte table to be able to send it back
        int end = index + 4;
        question = new byte[end - start + 1];
        for (int i = start; i <= end; i++) {
            question[i - start] = this.responseBuffer[i];
        }
    }

    private void verifyQNAME(String QNAME) {
        // QNAME must contain the ownedDomain
        if (!QNAME.contains(ownedDomain)) {
            wrongDomain = true;
            RCODE = 5;
        }
    }

    private void verifyQTYPE(String qtypeRecord) {
        // QR is supposed to be 0 ("TXT") in request
        if (! qtypeRecord.equals("TXT")) {
            RCODE = 1;
        }
    }

    private void verifyQCLASS(byte[] QCLASS) {
        // CLASS is supposed to be IN = 1
        if (QCLASS[0] != 0 && QCLASS[1] != 1) {
            RCODE = 1;
        }
    }

    private int getBit(int position, byte b) {
    	return (b >> position) & 1;
    }

    public int getRCODE() {
        return RCODE;
    }

    public int getOPCODE() {
        return OPCODE;
    }

    public byte getQDCOUNT1() {
        return QDCOUNT1;
    }
    
    public byte getQDCOUNT2() {
        return QDCOUNT2;
    }
    
    public byte[] getID() {
        return ID;
    }

    public byte[] getQuestion() {
        return question;
    }
    
    public String getTunneledData() {
        return tunneledData;
    }

    public String getQNAMEparsed() {
        return QNAMEparsed;
    }

    public String getQTYPEString() {
        return qtypeRecord;
    }

    public byte[] getQTYPE() {
        return QTYPE;
    }

    public byte[] getQCLASS() {
        return QCLASS;
    }

    public boolean wrongDomain() {
        return wrongDomain;
    }
}
