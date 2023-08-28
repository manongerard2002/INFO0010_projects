package header;
import java.nio.*;
import java.util.*;

public class Header {
	private byte[] ID;
	private Flags flags;
	private Counts counts;
    public static final int HEADERLENGTH = 12;

	public Header() {
		ID = new byte[2];
		flags = new Flags();
		counts = new Counts();
	}

    public byte[] queryHeader() {
		ByteBuffer header = ByteBuffer.allocate(HEADERLENGTH);

		this.setRandomID(header);
		this.setQueryFlags(header);
		this.setQueryCounts(header);

		return header.array();
	}

	private void setRandomID(ByteBuffer header) {
		new Random().nextBytes(ID);
		header.put(ID);
	}
	
	private void setQueryFlags(ByteBuffer header) {
		String[] queryflags = flags.setQueryFlags();
		String flags1 = queryflags[0];
		String flags2 = queryflags[1];
		header.put(Byte.parseByte(flags1));
		header.put(Byte.parseByte(flags2));
	}

	private void setQueryCounts(ByteBuffer header) {
		byte[] queryCounts = counts.setQueryCounts();
		header.put(queryCounts);
	}
}
