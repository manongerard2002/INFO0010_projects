import java.io.*;
import java.nio.*;

public class AnswerRecord {
	private int NAME1, NAME2, TTL;
	private int RDLength;
    private byte[] TYPE, CLASS, RData;
	private double numberRecords;

    public AnswerRecord(byte[] QTYPE, byte[] QCLASS, byte[] RData) {
		NAME1 = 0b11000000; // label : 11 than where the name is
		NAME2 = 0b00001100; // and it is in the 12 byte of our whole answer
		TYPE = QTYPE;
		CLASS = QCLASS;
		TTL = 3600; // by default
		RDLength = RData.length;
		this.RData = RData;
		numberRecords = Math.ceil((double) RDLength/255);
	}

    public byte[] responseAnswerRecord() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		byte[] data = RData;
		int DataLength = RDLength;

		for(int i = 0; i < numberRecords; i++) {
			int tmpLength = 0;

			if (DataLength > 255) {
				DataLength -= 255;
				tmpLength = 255;
			}
			else
				tmpLength = DataLength;

			data = Response.truncate(RData, tmpLength, i * 255, 0);
			ByteBuffer record = ByteBuffer.allocate(lengthAnswerRecord(tmpLength));

			record.put((byte) NAME1);
			record.put((byte) NAME2);
			record.put(TYPE);
			record.put(CLASS);
			record.put((byte) ((TTL >>> 24) & 0xFF));
			record.put((byte) ((TTL >>> 16) & 0xFF));
			record.put((byte) ((TTL >>> 8) & 0xFF));
			record.put((byte) (TTL & 0xFF));
			//RDlength
			record.put((byte) (((tmpLength + 1) >>> 8) & 0xFF));
			record.put((byte) ((tmpLength + 1) & 0xFF));

			//RData
			// <character-string> is a single length octet followed by that number of characters.
			// <character-string> is treated as binary information, and can be up to 256 characters in length (including the length octet)
			record.put((byte) (tmpLength & 0xFF));
			record.put(data);

			outputStream.write(record.array());
		}

		return outputStream.toByteArray();
	}

	public int lengthAnswerRecord(int length) {
		return 2 + 2 + 2 + 4 + 2 + (length + 1);
	}

	public int lengthAnswerRR() {
		return (int) ((2 + 2 + 2 + 4 + 2 + 1) * numberRecords + RDLength);
	}
}
