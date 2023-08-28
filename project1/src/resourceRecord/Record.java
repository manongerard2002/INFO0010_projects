package resourceRecord;

public class Record {
	private int TTL;
	private int RDLength;
	private String RData;
    private String typeRecord;
	private int length;

    public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getTTL() {
		return TTL;
	}

	public void setTTL(int TTL) {
		this.TTL = TTL;
	}

	public int getRDLength() {
		return RDLength;
	}

	public void setRDLength(int RDLength) {
		this.RDLength = RDLength;
	}

	public void setRData(String RData) {
		this.RData = RData;
	}

	public String getTYPE() {
		return typeRecord;
	}

	public void setTYPE(String TYPE) {
		this.typeRecord = TYPE;
	}

	public void printRecord() {
        if (this.typeRecord.equals("A") || this.typeRecord.equals("TXT"))
			System.out.println("Answer (TYPE=" + this.typeRecord + ", TTL=" + this.TTL + ", DATA=\"" + this.RData + "\")");
	}
}
