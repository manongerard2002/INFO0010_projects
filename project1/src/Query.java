import java.nio.*;

import header.Header;

public class Query {

	private Header header;
	private Question question;

	public Query(String hostName, String typeRecord) {
		this.header = new Header();
		this.question = new Question(hostName, typeRecord);
	}

	public byte[] getQuery() {
		int QNAMELength = question.getQNAMELength();
		int querySize = Header.HEADERLENGTH + QNAMELength + Question.QUESTIONLENGTH;

		//converting a int to byte[2]
		byte[] querySizeByte = new byte[2];
		querySizeByte[0] = (byte) ((querySize >>> 8) & 0xFF);
		querySizeByte[1] = (byte) (querySize & 0xFF);

		//first 2 bytes: length of the message, without counting these 2 bytes
		ByteBuffer queryBuffer = ByteBuffer.allocate(querySize + 2);
		queryBuffer.put(querySizeByte);
		queryBuffer.put(header.queryHeader());
		queryBuffer.put(question.queryQuestion(QNAMELength));

        return queryBuffer.array();
	}
}
