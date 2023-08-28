package header;
public class Counts {
    byte QDCOUNT1;
    byte QDCOUNT2;
    byte ANCOUNT1;
    byte ANCOUNT2;
    byte NSCOUNT1;
    byte NSCOUNT2;
    byte ARCOUNT1;
    byte ARCOUNT2;
    byte[] counts;

    public Counts() {
        QDCOUNT1 = 0x00;
        QDCOUNT2 = 0x00;
        ANCOUNT1 = 0x00;
        ANCOUNT2 = 0x00;
        NSCOUNT1 = 0x00;
        NSCOUNT2 = 0x00;
        ARCOUNT1 = 0x00;
        ARCOUNT2 = 0x00;
        counts = new byte[8];
    }

    private void setCounts() {
        counts[0] = QDCOUNT1;
        counts[1] = QDCOUNT2;
        counts[2] = ANCOUNT1;
        counts[3] = ANCOUNT2;
        counts[4] = NSCOUNT1;
        counts[5] = NSCOUNT2;
        counts[6] = ARCOUNT1;
        counts[7] = ARCOUNT2;
    }

    public byte[] setQueryCounts() {
        // 1) QDCOUNT: unsigned 16 bit int: #entries in the question section
        this.setQDCOUNT2();

	    // 2) ANCOUNT, 3) NSCOUNT, 4) ARCOUNT: unsigned 16 bit int: set in response
        // all = to 0
        this.setCounts();

        return counts;
    }

    private void setQDCOUNT2() {
        QDCOUNT2 = 0x01;
    }
}
