package org.openda.model_damflow;

import org.openda.blackbox.config.BBUtils;
import org.openda.interfaces.IDataObject;
import org.openda.interfaces.IExchangeItem;
import org.openda.interfaces.IPrevExchangeItem;

import java.io.*;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: Julius Sumihar
 * Date: 2-7-12
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */
public class DupuitPFile implements IDataObject {
	// TODO: For the moment, the wrapper simply works with the sTime and eTime parameters. In the future,
	// there may be a need to work with other parameters, e.g. for consistency check of information
	// on other files.
	// TODO: file format of the output is not yet identical with the original file. Check if this is of
	// importance, with John van Esch.
	private File dupuitPFile;
	private double eTime;
	private double sTime;
	private String[] exchangeItemIDs = new String[]{"sTime","eTime"};
	private IExchangeItem[] exchangeItems;

	public void initialize(File workingDir, String fileName, String[] arguments) {
		this.dupuitPFile = new File(workingDir, fileName);
		readDupuitPFile();
		exchangeItems = new DupuitPFileExchangeItem[2];
		exchangeItems[0] = new DupuitPFileExchangeItem(exchangeItemIDs[0],this);
		exchangeItems[1] = new DupuitPFileExchangeItem(exchangeItemIDs[1],this);
	}

	@Override
	public String[] getExchangeItemIDs() {
		return new String[] {exchangeItems[0].getId(),exchangeItems[1].getId()};
	}

	@Override
	public String[] getExchangeItemIDs(IPrevExchangeItem.Role role) {
		throw new UnsupportedOperationException("Class Name : org.openda.model_damflow.DupuitPFile - Method Name : getExchangeItemIDs");
	}

	@Override
	public IExchangeItem getDataObjectExchangeItem(String exchangeItemID) {
		if (exchangeItemID.equalsIgnoreCase(this.exchangeItems[0].getId())){
			return this.exchangeItems[0];
		} else if (exchangeItemID.equalsIgnoreCase(this.exchangeItems[1].getId())) {
			return this.exchangeItems[1];
		} else {
			throw new RuntimeException(this.getClass()+": no exchange item with ID "+exchangeItemID+". Available exchange item: "+this.exchangeItems[0].getId()+", "+this.exchangeItems[1].getId()+".");
		}
	}

	public void finish(){
		Locale locale = new Locale("EN");
		String dupuitPFormat = "%+6.4e";
		FileWriter fileWriter;

		String line;
		try {
			File tempFile = new File(this.dupuitPFile.getParent(), "dupuitP.temp");
			fileWriter = new FileWriter(tempFile);
			BufferedWriter outputFileBufferedWriter = new BufferedWriter(fileWriter);

			BufferedReader dupuitPFileBufferedReader = new BufferedReader(new FileReader(this.dupuitPFile));
			line = dupuitPFileBufferedReader.readLine();
			while (line != null) {
				if (line.contains("sTime")){
					outputFileBufferedWriter.write("sTime\t"+String.format(locale,dupuitPFormat,this.sTime));
				} else if (line.contains("eTime")){
					outputFileBufferedWriter.write("eTime\t"+String.format(locale,dupuitPFormat,this.eTime));
				} else {
					outputFileBufferedWriter.write(line);
				}
				outputFileBufferedWriter.newLine();
				line = dupuitPFileBufferedReader.readLine();
			}
			dupuitPFileBufferedReader.close();
			outputFileBufferedWriter.close();
			BBUtils.copyFile(tempFile, this.dupuitPFile);
			tempFile.deleteOnExit();
		} catch (IOException e){
			throw new RuntimeException("Could not read/write DAMFlow input file "+this.dupuitPFile.getAbsolutePath());
		}
	}

	private void readDupuitPFile() {
		String lineFields[];
		String line;
		int timeParamFound = 0;
		try {
			BufferedReader dupuitPFileBufferedReader = new BufferedReader(new FileReader(this.dupuitPFile));
			line = dupuitPFileBufferedReader.readLine();
			while (line != null) {
				if (line.contains("sTime")){
					lineFields = line.trim().split("[ \t]+");
					this.sTime = Double.parseDouble(lineFields[1]);
					timeParamFound++;
				} else if (line.contains("eTime")){
					lineFields = line.trim().split("[ \t]+");
					this.eTime = Double.parseDouble(lineFields[1]);
					timeParamFound++;
				}
				line = dupuitPFileBufferedReader.readLine();
			}
			dupuitPFileBufferedReader.close();
		} catch (IOException e){
			throw new RuntimeException("Could not read DAMFlow input file "+this.dupuitPFile.getAbsolutePath());
		}
		if (timeParamFound!=2){
			throw new RuntimeException("Could not find sTime and/or eTime in DAMFlow input file "+this.dupuitPFile.getAbsolutePath());
		}

	}

	protected double getSTime() {
		return this.sTime;
	}

	protected double getETime() {
		return this.eTime;
	}

	protected void setSTime(double values) {
		this.sTime = values;
	}

	protected void setETime(double values) {
		this.eTime = values;
	}

	@Override
	public void initialize(File workingDir, String[] arguments) {
		String fileName = arguments[0];
		String[] remainingArguments = new String[arguments.length-1];
		System.arraycopy(arguments, 1, remainingArguments, 0, remainingArguments.length);
		initialize(workingDir, fileName, remainingArguments);
	}
}
