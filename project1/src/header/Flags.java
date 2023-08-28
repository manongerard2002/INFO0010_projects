package header;
public class Flags {
    String QR;
    String OPCODE;
    String AA;
    String TC;
    String RD;
    String RA;
    String Z;
    String RCODE;
    String[] flags;

    public Flags() {
        QR = "0";
        OPCODE = "0000";
        AA = "0";
        TC = "0";
        RD = "0";
        RA = "0";
        Z = "000";
        RCODE = "0000";
        flags = new String[2];
    }

    public void setFlags() {
        flags[0] = QR + OPCODE + AA + TC + RD;
		flags[1] = RA + Z + RCODE;
    }

    public String[] setQueryFlags() {
        // 1) QR: 0 = query
        // 2) OPCODE: 4bit: kind of query: 0 = standard & copied into the response
        // 3) AA: valid in responses...
        // 4) TC: message was truncated
        // 5) RD: set in a query to pursue the query recursively & copied into the response
        this.setRD();
        // 6) RA: is set or cleared in a response...
        // 7) Z: 3bits 0 in all queries and responses.
        // 8) RCODE: 4 bit field is set in responses: 0: No error condition

        this.setFlags();

        return flags;
    }

    private void setRD() {
        RD = "1";
    }
}
