import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * The Class BinaryIO.
 */
public class BinaryIO {
	
	/**
	 * Instantiates a new binary IO.
	 */
	public BinaryIO() {
	}
	
	/**
	 * Converts a string of eight 1's and 0's to one byte. 
	 * The caller MUST guarantee that the string of 1's 
	 * and 0's is 8 bits - no more, no less.
	 *
	 * @param binStr the incoming binary string for the current character
	 * @return the int generated from the binary string
	 */
	int convStrToBin(String binStr) {
		// TODO: Write this method
		int converted = 0;
		
		
		int mask = 0x80;
		int mask2 = 0x00;
		int len = binStr.length();
		for(int i = 0; i < len; i++) {
			if(binStr.charAt(i) == '1') {
				converted = (converted | mask);
				mask = mask >> 1;
			}
			else if(binStr.charAt(i) == '0') {
				converted = (converted | mask2);
				mask = mask >> 1;
			}
		}
		return converted;
		
		//check != 0
		
	}
	
	/**
	 * Convert a byte value into a string of eight 1's and 0's (MSB to LSB).
	 *
	 * @param aByte the byte to convert
	 * @return the binary string of 1's and 0's that represents aByte
	 */
	String convBinToStr(int aByte) {
		// TODO: Write this method
		String[] nib2BinStr = {"0000", "0001", "0010", "0011", "0100", 
				"0101", "0110", "0111", "1000", "1001", "1010", 
				"1011", "1100", "1101", "1110", "1111"};
		byte nlo = (byte)(aByte & 0x0F); // checks last 4 bits
		byte nhi = (byte)((aByte >>> 4) & (0x0F)); //shifts right 4 bits
		return (nib2BinStr[nhi] + nib2BinStr[nlo]);
		
	}
	
	/**
	 * WriteBinStr - this method attempts to convert a binary string 
	 *               to one or more bytes, and write them to the binary
	 *               file specified. Any remaining unwritten bits in the
	 *               binary string are returned to the caller.
	 * Algorithm:	While the binary string has *more* than 8 bits
	 *                 - convert the first 8 bits to a byte value
	 *                 - write the converted value to the file
	 *                 - remove the first 8 bits from the binary Str
	 *                 
	 *              If the binary string has 8 bits
	 *                 - convert the string to a byte value
	 *                 - write the converted value to the file
	 *                 - return "";
	 *              else 
	 *                 - return the binary string
	 *
	 * @param bos - the binary file to be created
	 * @param binStr - the binary string of 1's and 0's to be written to the file
	 * @return the string of any unwritten bits...
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	 String writeBinString(BufferedOutputStream bos, String binStr) throws IOException {
		// TODO: write this method
		 while(binStr.length() > 8) {
			int convertedStrToBin = convStrToBin(binStr.substring(0, 8));
			try {
				
				bos.write(convertedStrToBin);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			binStr = binStr.substring(8); 
		 }
		 if(binStr.length() == 8) {
			 int convertedStrToBin2 = convStrToBin(binStr);
			 try {
				
					bos.write(convertedStrToBin2);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			 binStr = "";
		 }
		 return binStr;
	 }
	
}

