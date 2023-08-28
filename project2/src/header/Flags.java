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

    public Flags(int RCODE, boolean TC, int OPCODE) {
        this();
        this.RCODE = Integer.toBinaryString(RCODE);
        this.TC = TC ? "1" : "0";
        this.OPCODE = "";
        this.OPCODE += Integer.toBinaryString((OPCODE >>> 24) & 0xFF);
		this.OPCODE += Integer.toBinaryString((OPCODE >>> 16) & 0xFF);
		this.OPCODE += Integer.toBinaryString((OPCODE >>> 8) & 0xFF);
		this.OPCODE += Integer.toBinaryString((OPCODE & 0xFF));
    }

    public void setFlags() {
        flags[0] = QR + OPCODE + AA + TC + RD;
		flags[1] = RA + Z + RCODE;
    }

    public String[] setQueryFlags() {
        // 1) QR: 0 = query & 1 = response
        this.setQR();
        // 2) OPCODE: already set
        // 3) AA: valid in responses...
        this.setAA();
        // 4) TC: already set
        // 5) RD: set in a query to pursue the query recursively & copied into the response
        // 6) RA: is set or cleared in a response...
        // 7) Z: 3bits 0 in all queries and responses.
        // 8) RCODE: already set

        this.setFlags();

        return flags;
    }

    private void setQR() {
        QR = "1";
    }

    private void setAA() {
        AA = "1";
    }
}
