import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import myfileio.MyFileIO;

/**
 * The Class EncodeDecode. 
 */
public class EncodeDecode {
	
	/** The encodeMap maps each ascii value to its huffman code */
	private String[] encodeMap;
	
	/** Instance of the huffman compression utilites for building the tree and encode man */
	private HuffmanCompressionUtilities huffUtil;
	
	/** Instance of GenWeights used to generate the frequency weights if no weights file is specified */
	private GenWeights gw;
	
	/** Instance of HuffCompAlerts for relaying information to the GUI or console */
	private HuffCompAlerts hca;
	
	/**  Provides facilities to robustly handle external file IO. */
	private MyFileIO fio;
	
	/** The bin util. */
	private BinaryIO binUtil;
	
	/**  The array for storing the frequency weights. */
	private int[] weights;	

	/**
	 * Instantiates a new EncodeDecode instance
	 *
	 * @param gw - instance of GenWeights
	 * @param hca - instance of HuffCompAlerts
	 */
	public EncodeDecode (GenWeights gw, HuffCompAlerts hca) {
		fio = new MyFileIO();
		this.gw = gw;
		this.hca = hca;
		huffUtil = new HuffmanCompressionUtilities();
		binUtil = new BinaryIO();
	}
	
	/**
	 * Encode. This function will do the following actions:
	 *         1) Error check the inputs
	 * 	       - Perform error checking on the file to encode, using MyFileIO fio.
	 *         - Generate the array of frequency weights - either read from a file in the output/ directory
	 *           or regenerate from the file to encode in the data/ directory
	 *         - Error check the output file...
	 *         Any errors will abort the conversion...
	 *         
	 *         2) set the weights in huffUtils
	 *         3) build the Huffman tree using huffUtils;
	 *         4) create the Huffman codes by traversing the trees.
	 *         5) call executeEncode to perform the conversion.
	 *
	 * @param fName 	the name of the input file to be encoded
	 * @param bfName 	the name of the binary (compressed) file to be created
	 * @param freqWts 	the name of the file to read for the frequency weights. If blank, or other error,
	 *                  generate the frequency weights from fName.
	 * @param optimize 	if true, ONLY add leaf nodes with non-zero weights to the priority queue
	 */
	void encode(String fName,String bfName, String freqWts, boolean optimize) {
		// TODO: write this method and any required helper methods
		//freqWts = "weights/warAndPeace.csv";
		//optimize = false;
		File file = fio.getFileHandle(fName);
		File binFile = fio.getFileHandle(bfName);
		File weightFile = fio.getFileHandle(freqWts);
		
		
		
		
		int errorInp = inputError(file);
		int binError = binError(binFile);
		int errorWt = weightError(weightFile);
		
		if(errorWt == MyFileIO.FILE_OK) {
			huffUtil.setWeights(huffUtil.readFreqWeights(weightFile));
			
		}
		else {
			
			huffUtil.setWeights(gw.readInputFileAndReturnWeights(fName)); 
			
		}
		
		if(binError == -1) {
			return;
		}
		
		
		huffUtil.buildHuffmanTree(optimize);
		huffUtil.createHuffmanCodes(huffUtil.getTreeRoot(), "", 0);
		executeEncode(file, binFile);
	}
	
	/**
	 * Input error.
	 *
	 * @param file the file
	 * @return the int
	 */
	public int inputError(File file) {
		int status1 = fio.getFileStatus(file,true);
		
		if(status1 == MyFileIO.EMPTY_NAME) {
			hca.issueAlert(HuffAlerts.INPUT, "WARNING","filename is empty");
			return MyFileIO.EMPTY_NAME;
		}
		else if(status1 == MyFileIO.FILE_DOES_NOT_EXIST || status1 == MyFileIO.READ_ZERO_LENGTH) {
			hca.issueAlert(HuffAlerts.INPUT, "WARNING","file does not exist or is empty");
			return MyFileIO.READ_ZERO_LENGTH;
		}
		else if(status1 == MyFileIO.NO_READ_ACCESS) {
			hca.issueAlert(HuffAlerts.INPUT, "WARNING", "not readable");
			return MyFileIO.NO_READ_ACCESS;
		}
		
		return MyFileIO.FILE_OK;
	}
	
	/**
	 * Bin error.
	 *
	 * @param binFile the bin file
	 * @return the int
	 */
	public int binError(File binFile) {
		int status1 = fio.getFileStatus(binFile,false);
		
		if(status1 == MyFileIO.EMPTY_NAME) {
			hca.issueAlert(HuffAlerts.INPUT, "WARNING","filename is empty");
			return MyFileIO.EMPTY_NAME;
		}
		else if(status1 == MyFileIO.FILE_DOES_NOT_EXIST || status1 == MyFileIO.READ_ZERO_LENGTH) {
			hca.issueAlert(HuffAlerts.INPUT, "WARNING","file does not exist or is empty");
			return MyFileIO.READ_ZERO_LENGTH;
		}
		else if(status1 == MyFileIO.NO_WRITE_ACCESS) {
			hca.issueAlert(HuffAlerts.INPUT, "WARNING", "not writeable");
			return MyFileIO.NO_WRITE_ACCESS;
		}
		else if(status1 == MyFileIO.WRITE_EXISTS) {
			if(!hca.issueAlert(HuffAlerts.CONFIRM, "CONFIRM", "overwrite the file? or cancel")) {
				return -1;
			}
			else {
				return MyFileIO.FILE_OK;
			}
		}
		
		return MyFileIO.FILE_OK;
	}
	
	/**
	 * Checks for errors.
	 *
	 * @param file the file
	 * @param bfName the bf name
	 * @param freqWts the freq wts
	 * @return true, if successful
	 */
	public int weightError(File wtFile) {
		int status1 = fio.getFileStatus(wtFile,true);
		
		if(status1 == MyFileIO.FILE_OK) {
			return  MyFileIO.FILE_OK;
		}
		else {
			if(status1 == MyFileIO.EMPTY_NAME) {
				hca.issueAlert(HuffAlerts.INPUT, "WARNING","filename is empty");
				return MyFileIO.EMPTY_NAME;
			}
			else if(status1 == MyFileIO.FILE_DOES_NOT_EXIST || status1 == MyFileIO.READ_ZERO_LENGTH) {
				hca.issueAlert(HuffAlerts.INPUT, "WARNING","file does not exist or is empty");
				return MyFileIO.READ_ZERO_LENGTH;
			}
			return MyFileIO.EMPTY_NAME;
		}
	}
	
	/**
	 * Execute encode. This function will write compressed binary file as part of part 3
	 * 
	 * This functions should:
	 * 1) get the encodeMap from HuffUtils 
	 * 2) initialize binStr to ""
	 * 3) open a BufferedReader for the text file and a BufferedOutputStream for the binary file
	 * 4) for each character in the textfile:
	 * 	  - append the huffman code to binStr;
	 *    - if binStr length >= 8, write the binStr to the binary file using binUtils.writeBinString();
	 *      binStr should be set to any returned string value.
	 * 5) when the input file is exhausted, write the EOF character, padding with 0's if needed 
	 * 6) close the the input and output files...
	 *
	 * @param inFile the File object that represents the file to be compressed
	 * @param binFile the File object that represents the compressed output file
	 */
	private void executeEncode(File inFile, File binFile) {
		// TODO: write this method and any required helper methods
		String[] encodeMap = huffUtil.getEncodeMap();
		String binStr = "";
		BufferedReader br = fio.openBufferedReader(inFile);
		BufferedOutputStream bos = fio.openBufferedOutputStream(binFile);
		long length = inFile.length();
		
		try {
			for(int i = 0; i < length; i++) { //while br.read() != -1
				
				int x = br.read();
				
				String code = encodeMap[x];
				binStr += code;
				if(binStr.length() >= 8) {
					binStr = binUtil.writeBinString(bos, binStr);
				}
			}
			binStr += encodeMap[0];
			while((binStr.length() % 8) != 0) {
				binStr = binStr + "0";
			}
			binStr = binUtil.writeBinString(bos, binStr);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		fio.closeFile(br);
		fio.closeStream(bos);
	}
	
	// DO NOT CODE THIS METHOD UNTIL EXPLICITLY INSTRUCTED TO DO SO!!!
	/**
	 * Decode. This function will only be addressed in part 5. It will 
	 *         1) Error check the inputs
	 * 	       - Perform error checking on the file to decode
	 *         - Generate the array of frequency weights - this MUST be provided as a file
	 *         - Error check the output file...
	 *         Any errors will abort the conversion...
	 *         
	 *         2) set the weights in huffUtils
	 *         3) build the Huffman tree using huffUtils;
	 *         4) create the Huffman codes by traversing the trees.
	 *         5) executeDecode
	 *
	 * @param bfName 	the name of the binary file to read
	 * @param ofName 	the name of the text file to write...
	 * @param freqWts the freq wts
	 * @param optimize - exclude 0-weight nodes from the tree
	 */
	void decode(String bfName, String ofName, String freqWts,boolean optimize) {
		//bfName = "encode/some_file_maclinux.bin";
		//ofName = "decode/cat.txt";
		File txtFile = fio.getFileHandle(ofName);
		File binFile = fio.getFileHandle(bfName);
		//freqWts = "weights/The Cat in the Hat.csv";
		File weightFile = fio.getFileHandle(freqWts);
		
		
		
		
		int errorTxt = binError(txtFile);
		int binError = inputError(binFile);
		int errorWt = inputError(weightFile);
		
		if(errorTxt != MyFileIO.FILE_OK) {
			if(errorTxt != MyFileIO.WRITE_EXISTS) {
				return;
			}
		}
		
		if(errorWt == MyFileIO.FILE_OK) {
			huffUtil.setWeights(huffUtil.readFreqWeights(weightFile));
			
		}
		else {
			return;
		}
		
		
		if(binError == -1) {
			return;
		}
		
		
		huffUtil.buildHuffmanTree(optimize);
		huffUtil.createHuffmanCodes(huffUtil.getTreeRoot(), "", 0);
		
		try {
			executeDecode(binFile, txtFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}
	
	// DO NOT CODE THIS METHOD UNTIL EXPLICITLY INSTRUCTED TO DO SO!!!
	/**
	 * Execute decode.  - This is part of PART 5...
	 * This function performs the decode of the binary(compressed) file.
	 * It will read each byte from the binary file and convert it to a string of 1's and 0's
	 * This will be appended to any leftover bits from prior conversions.
	 * Starting from the head of the string, decode occurs by traversing the Huffman Tree from the root
	 * until a Leaf node is reached. If a leaf node is reached, the character is written to the output
	 * file, and the corresponding # of bits is removed from the string. If the end of the bit string is reached
	 * without reaching a leaf node, the next byte is processed, and so on until the encoded EOF
	 * character is encountered. 
	 * After completely decoding the file, close the input file and
	 * flushed and close the output file.
	 *
	 * @param binFile the file object for the binary input file
	 * @param outFile the file object for the binary output file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void executeDecode(File binFile, File outFile) throws IOException {
		String binStr = "";
		BufferedInputStream bis = fio.openBufferedInputStream(binFile);
		
		BufferedWriter bw = fio.openBufferedWriter(outFile);
		encodeMap = huffUtil.getEncodeMap();
		int decodedChar = 0;
		int x;
		while((x = bis.read()) != -1) {
			
			binStr += binUtil.convBinToStr(x);
			//System.out.println("binStr: " + binStr);
			decodedChar = 0;
			while(decodedChar != -1) {
				decodedChar = huffUtil.decodeString(binStr);
				//System.out.println("decodedChar: " + decodedChar);
				if(decodedChar == 0) {
					
					bis.close();
					bw.flush();
					bw.close();
					return;
				}
				else if(decodedChar != -1) {
					bw.write((decodedChar)); 
					binStr = binStr.substring(encodeMap[decodedChar].length());
				}
				else {
					decodedChar = -1;
				}
			}
		}
	}

}
