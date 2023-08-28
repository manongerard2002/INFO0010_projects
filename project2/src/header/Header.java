package header;

import java.nio.*;

public class Header {
	private byte[] ID;
	private Flags flags;
	private Counts counts;
    public static final int HEADERLENGTH = 12;

	public Header(byte[] ID, int RCODE, boolean TC, int OPCODE, byte QDCOUNT1, byte QDCOUNT2, byte ANCOUNT2) {
		this.ID = ID;
		flags = new Flags(RCODE, TC, OPCODE);
		counts = new Counts(QDCOUNT1, QDCOUNT2, ANCOUNT2);
	}

    public byte[] responseHeader() {
		ByteBuffer header = ByteBuffer.allocate(HEADERLENGTH);

		this.setID(header);
		this.setQueryFlags(header);
		this.setQueryCounts(header);

		return header.array();
	}

	private void setID(ByteBuffer header) {
		header.put(ID);
	}
	
	private void setQueryFlags(ByteBuffer header) {
		String[] queryflags = flags.setQueryFlags();
		String flags1 = queryflags[0];
		String flags2 = queryflags[1];

		int flags1Int = Integer.parseInt(flags1, 2);
		header.put((byte) flags1Int);
		int flags2Int = Integer.parseInt(flags2, 2);
		header.put((byte) flags2Int);
	}

	private void setQueryCounts(ByteBuffer header) {
		byte[] queryCounts = counts.setQueryCounts();
		header.put(queryCounts);
	}
}
