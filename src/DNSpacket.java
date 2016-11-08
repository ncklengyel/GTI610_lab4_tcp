import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class DNSpacket {

	// HEADER
	private int id;
	private int qr;
	private String opcode;
	private int aa;
	private int tc;
	private int rd;
	private int ra;
	private int z;
	private int rCode;
	private int qDCount;
	private int aNAcount;
	private int nSCount;
	private int aRCount;

	// Requete
	private String qName;
	private String qType;
	private String qClasse;
	
	//Reponse
	private String type;
	private String classe;
	private int rDLength;
	private String rData;
	
	public static final int REQUETE = 0;
	public static final int REPONSE = 1;
	
	public DNSpacket(ByteArrayInputStream inputStream) {

		// ID
		byte[] bb = new byte[0xFF];
		inputStream.read(bb, 0, 2);
		ByteBuffer wrapped = ByteBuffer.wrap(bb);
		this.id = wrapped.getChar();

		// QR
		bb = new byte[0xFF];
		inputStream.read();
		wrapped = ByteBuffer.wrap(bb);
		int value = wrapped.getChar();
		String thirdByte = charArrayToString(Integer.toString(value).toCharArray());
		this.qr = Integer.parseInt(thirdByte.substring(0,1));
		
		//OPCODE
		this.opcode = thirdByte.substring(1,5);
		
		if (this.qr==REQUETE) {
			
			//QNAME
			this.qName = getQNAME(inputStream);
			
			
			//QTYPE
			bb = new byte[0xFF];
			inputStream.read(bb, 0, 2);
			wrapped = ByteBuffer.wrap(bb);
			value = wrapped.getChar();
			this.qType = buildQtype(value);
			
			//QCLASS
			bb = new byte[0xFF];
			inputStream.read(bb, 0, 2);
			wrapped = ByteBuffer.wrap(bb);
			value = wrapped.getChar();
			this.qClasse = buildQClasse(value);
			
			
		}else if (this.qr==REPONSE) {
			
			//NAME mais je ne le prend pas
			bb = new byte[0xFF];
			inputStream.read(bb,0,2);
			
			//TYPE
			bb = new byte[0xFF];
			inputStream.read(bb,0,2);
			wrapped = ByteBuffer.wrap(bb);
			value = wrapped.getChar();
			this.type = buildQtype(value); //mm chose que q type alors je prend la mm méthode
			
			
			//CLASSE
			bb = new byte[0xFF];
			inputStream.read(bb,0,2);
			wrapped = ByteBuffer.wrap(bb);
			value = wrapped.getChar();
			this.classe = buildQClasse(value); //mm chose que QCLASSE alors je prend la mm méthode
			
			//TTL(32 bits= 4octets) mais je ne le prend pas 
			inputStream.read(bb,0,4);
			
			//RDLENGTH
			bb = new byte[0xFF];
			inputStream.read(bb,0,2);
			wrapped = ByteBuffer.wrap(bb);
			value = wrapped.getChar();
			this.rDLength = value; //Unsigned 16-bit value that defines the length in bytes (octets) of the RDATA record.
			
			//RDATA (sur 4 octet aka adresse ip)
			
			int section1 = inputStream.read();
			int section2 = inputStream.read();
			int section3 = inputStream.read();
			int section4 = inputStream.read();
			this.rData = buildRdata(section1,section2,section3,section4);
			
			
		}

	}

	public int getId() {
		return id;
	}

	public int getQr() {
		return qr;
	}

	public String getOpcode() {
		return opcode;
	}

	public int getAa() {
		return aa;
	}

	public int getTc() {
		return tc;
	}

	public int getRd() {
		return rd;
	}

	public int getRa() {
		return ra;
	}

	public int getZ() {
		return z;
	}

	public int getrCode() {
		return rCode;
	}
	
	

	public String getrData() {
		return rData;
	}

	public int getqDCount() {
		return qDCount;
	}

	public int getaNAcount() {
		return aNAcount;
	}

	public int getnSCount() {
		return nSCount;
	}

	public int getaRCount() {
		return aRCount;
	}

	public String getqName() {
		return qName;
	}

	public String getqType() {
		return qType;
	}

	public String getqClasse() {
		return qClasse;
	}
	
	
	public String getType() {
		return type;
	}

	public String getClasse() {
		return classe;
	}

	public int getrDLength() {
		return rDLength;
	}

	public String getRdata() {
		return rData;
	}

	public static int getRequete() {
		return REQUETE;
	}

	public static int getReponse() {
		return REPONSE;
	}

	public void printInfo(){
		
		System.out.println("ID: "+this.id);
		System.out.println("QR: "+this.qr);
		System.out.println("OPCODE: "+this.opcode);
		
		if (this.qr==REQUETE) {
			System.out.println("QNAME: "+this.getqName());
			System.out.println("QTYPE: "+this.getqType());
			System.out.println("QCLASSE: "+this.getqClasse());
		}
		
		
	}

	private String charArrayToString(char[] charArray) {

		char[] newArray = new char[8];
		
		if (charArray.length != 8) {

			int positionDeplus = 8 - charArray.length;
			
			for (int i = 0; i < positionDeplus; i++) {
				newArray[i] = '0';
			}

			for (int j = 0; j < charArray.length; j++) {
				newArray[j + positionDeplus] = charArray[j];
			}

		} else {
			newArray = charArray;
		}
		
		String s = new String (newArray);
		
		return s;

	}
	
	private String getQNAME(ByteArrayInputStream tabInputStream) {
		byte[] bb = new byte[0xFF];
		tabInputStream.read(bb, 0, 9);
		int qnameEnd = tabInputStream.read();
		
		int offset = 14;
		ArrayList<String> list = new ArrayList<>();
		
		while (qnameEnd != 0) {
			bb = new byte[0xFF];
			tabInputStream.read(bb, offset, qnameEnd);
			list.add(new String(bb).trim());
			offset += qnameEnd + 1;
			qnameEnd = tabInputStream.read();
		}

		return buildDomaineName(list);

	}
	
	//http://www.zytrax.com/books/dns/ch15/ 
	//pour les valeurs
	private String buildQtype(int val){
		
		String s = "not good";
		
		if (val==1) {
			s = "A";
		}
		
		return s;
		
	}
	//http://www.zytrax.com/books/dns/ch15/ 
	//pour les valeurs
	private String buildQClasse(int val){
		
		String s = "not good";
		
		if (val==1) {
			s = "IN";
		}
		
		return s;
		
	}
	
	private String buildRdata(int section1,int section2,int section3,int section4){
		
		StringBuilder sb = new StringBuilder();
		sb.append(Integer.toString(section1));
		sb.append(".");
		sb.append(Integer.toString(section2));
		sb.append(".");
		sb.append(Integer.toString(section3));
		sb.append(".");
		sb.append(Integer.toString(section4));
		
		return sb.toString();
	
		
		
	}
	
	
	private String buildDomaineName(ArrayList<String> list) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {

			if (i < list.size() - 1) {
				sb.append(list.get(i));
				sb.append(".");
			} else {
				sb.append(list.get(i));
			}

		}

		return sb.toString();

	}
}


