import java.nio.*;

public class Question {
    private String hostName;
	private String typeRecord;
	public static final int QUESTIONLENGTH = 1 + 4; // +1 for the ternimal zero length octet

	public Question(String hostName, String typeRecord) {
		this.hostName = hostName;
		this.typeRecord = typeRecord;
	}

	public int getQNAMELength() {
		int byteLength = 0;
		String[] hostParts = hostName.split("\\.");
		for (int i = 0; i < hostParts.length; i++) {
			// sequence of labels: a length octet followed by that number of octets
			byteLength += 1 + hostParts[i].length();
		}
		// note: here it does not take into account the terminal zero length octet
		return byteLength;
	}

	public byte[] queryQuestion(int QNAMELength) {
		ByteBuffer question = ByteBuffer.allocate(QNAMELength + QUESTIONLENGTH);
		
		setQueryQNAME(question);
        setQueryQTYPE(question);
		setQueryQCLASS(question);

		return question.array();
	}

    private void setQueryQNAME(ByteBuffer question) {
        // QNAME: domain name: sequence of labels:
        // The name being queried is split into labels by removing the separating dots
		String[] hostParts = hostName.split("\\.");
		for (int i = 0; i < hostParts.length; i++) {
            int hostPartsLength = hostParts[i].length();
			question.put((byte) hostPartsLength); // length octet
			for (int j = 0; j < hostPartsLength; j++) {
				question.put((byte) hostParts[i].charAt(j)); // the number of octets filled one by one
			}
		}
		question.put((byte) 0x00); // terminal
	}

    private void setQueryQTYPE(ByteBuffer question) {
        // QTYPE: two octet code: type of the query:
        // A: 1: a host address
		if (typeRecord.equals("A")) {
			question.put((byte) 0);
			question.put((byte) 1);
		}
		// TXT: 16: text strings
        else if (typeRecord.equals("TXT")) {
			question.put((byte) 0);
			question.put((byte) 16);
		}
        else {
			throw new RuntimeException("Unrecognized query type in response");
		}
	}

    private void setQueryQCLASS(ByteBuffer question) {
        //QCLASS: two octet code: class of the query
        //IN: 1: the Internet
		question.put((byte) 0);
		question.put((byte) 1);
	}
}
