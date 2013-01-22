package org.openda.model_damflow;

import org.openda.blackbox.config.BBUtils;
import org.openda.interfaces.IDataObject;
import org.openda.interfaces.IExchangeItem;
import org.openda.interfaces.IPrevExchangeItem;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: Julius Sumihar
 * Date: 4-7-12
 * Time: 13:44
 * To change this template use File | Settings | File Templates.
 */
public class DupuitGFile implements IDataObject {

	private ArrayList<String> lines = new ArrayList<String>();
	private File dupuitGFile;
	private String fileHeader = "material";
	private String quantity = "Kx";
	private IExchangeItem[] exchangeItems;

	public void initialize(File workingDir, String fileName, String[] arguments) {
		this.dupuitGFile = new File(workingDir, fileName);
		readDupuitGFile();
	}

	private void writeDupuitGFile() {
		Locale locale = new Locale("EN");
		String dupuitGFormat = "%+6.4e";
		FileWriter fileWriter;
		String line;
	    try {
			File tempFile = new File(this.dupuitGFile.getParent(), "dupuitG.temp");
			fileWriter = new FileWriter(tempFile);
			BufferedWriter outputFileBufferedWriter = new BufferedWriter(fileWriter);

			// write header:
			BufferedReader dupuitGFileBufferedReader = new BufferedReader(new FileReader(this.dupuitGFile));
			line = dupuitGFileBufferedReader.readLine();
			outputFileBufferedWriter.write(line);
			outputFileBufferedWriter.newLine();
			// write nLayer:
			line = dupuitGFileBufferedReader.readLine();
			outputFileBufferedWriter.write(line);
			outputFileBufferedWriter.newLine();
			// write columns title:
			line = dupuitGFileBufferedReader.readLine();
			outputFileBufferedWriter.write(line);
			outputFileBufferedWriter.newLine();

			// write table of material properties:
			int nLayer = exchangeItems.length;
			int iColumnKx = 8;
			String[] lineFields;
			String delim = " ";
			for (int iLayer=0; iLayer<nLayer; iLayer++){
				line = dupuitGFileBufferedReader.readLine();
				lineFields = line.trim().split("[ \t]+");
				double Kx = this.exchangeItems[iLayer].getValuesAsDoubles()[0];
				lineFields[iColumnKx] = String.format(locale,dupuitGFormat,Kx);
				String updatedLine = Arrays.toString(lineFields).replace(", ", delim).replaceAll("[\\[\\]]", "");
				outputFileBufferedWriter.write(updatedLine);
				outputFileBufferedWriter.newLine();
			}
			// write the rest:
			line = dupuitGFileBufferedReader.readLine();
			while (line != null) {
				outputFileBufferedWriter.write(line);
				outputFileBufferedWriter.newLine();
				line = dupuitGFileBufferedReader.readLine();
			}
			dupuitGFileBufferedReader.close();
			outputFileBufferedWriter.close();
			BBUtils.copyFile(tempFile, this.dupuitGFile);
			tempFile.deleteOnExit();
		} catch (IOException e) {
			throw new RuntimeException("Could not write to " + this.dupuitGFile.getAbsolutePath());
		}
	}

	private void readDupuitGFile() {
		String lineFields[];
		String line;
		try {
			BufferedReader dupuitHOFileBufferedReader = new BufferedReader(new FileReader(this.dupuitGFile));
			line = dupuitHOFileBufferedReader.readLine();
			if (!line.toLowerCase().contains(fileHeader)) {
				throw new RuntimeException("DAMFlow timeseries file does not contain keyword '"+fileHeader+"':"+this.dupuitGFile.getAbsolutePath());
			} else {
				line = dupuitHOFileBufferedReader.readLine();
			}
			while (line != null) {
				lines.add(line);
				line = dupuitHOFileBufferedReader.readLine();
			}
			dupuitHOFileBufferedReader.close();
		} catch (IOException e){
			throw new RuntimeException("Could not read DAMFlow material properties file "+this.dupuitGFile.getAbsolutePath());
		}

		line = lines.get(0);
		lineFields = line.trim().split("[ \t]+");
		int nLayer = 0;
		if (lineFields[0].equalsIgnoreCase("nLayer")) {
			nLayer = Integer.parseInt(lineFields[1]);
		} else {
			throw new RuntimeException("Could not find nLayer in DAMFlow material properties file "+this.dupuitGFile.getAbsolutePath());
		}
		exchangeItems = new DupuitGFileExchangeItem[nLayer];

		int iColumnKx = 8;
		int iLine=2;
		for (int iLayer=0; iLayer<nLayer; iLayer++){
			line = lines.get(iLine);
			lineFields = line.trim().split("[ \t]+");
			double Kx = Double.parseDouble(lineFields[iColumnKx]);
			String id = "layer"+iLayer+"."+quantity;
			exchangeItems[iLayer] = new DupuitGFileExchangeItem(id,Kx);
			iLine++;
		}
	}

	@Override
	public String[] getExchangeItemIDs() {
		int nExchangeItem = exchangeItems.length;
		String[] exchangeItemIDs = new String[nExchangeItem];
		for (int i=0; i<nExchangeItem; i++){
			exchangeItemIDs[i] = exchangeItems[i].getId();
		}
		return exchangeItemIDs;
	}

	@Override
	public String[] getExchangeItemIDs(IPrevExchangeItem.Role role) {
		throw new UnsupportedOperationException("Class Name : org.openda.model_damflow.DupuitGFile - Method Name : getExchangeItemIDs");
	}

	@Override
	public IExchangeItem getDataObjectExchangeItem(String exchangeItemID) {
		int i;
		for (i=0; i<this.exchangeItems.length;i++){
			if (exchangeItemID.contentEquals(this.exchangeItems[i].getId())){
				break;
			}
		}
		return this.exchangeItems[i];
	}

	@Override
	public void finish() {
		writeDupuitGFile();
	}

	@Override
	public void initialize(File workingDir, String[] arguments) {
		String fileName = arguments[0];
		String[] remainingArguments = new String[arguments.length-1];
		System.arraycopy(arguments, 1, remainingArguments, 0, remainingArguments.length);
		initialize(workingDir, fileName, remainingArguments);
	}
}
