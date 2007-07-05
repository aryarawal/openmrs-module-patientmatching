package org.regenstrief.linkage.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.regenstrief.linkage.util.ColumnSortOption;
import org.regenstrief.linkage.util.ColumnSorter;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

public class OrderedCharDelimFileReader extends CharDelimFileReader {
	
	private MatchingConfig mc;
	private File sorted_file;
	
	public OrderedCharDelimFileReader(LinkDataSource lds, MatchingConfig mc){
		super(lds);
		this.mc = mc;
		char raw_file_sep = lds.getAccess().charAt(0);
		sorted_file = sortInputFile(switched_file, raw_file_sep);
		try{
			file_reader = new BufferedReader(new FileReader(sorted_file));
			next_record = line2Record(file_reader.readLine());
		}
		catch(IOException ioe){
			file_reader = null;
			next_record = null;
		}
	}
	
	private File sortInputFile(File f, char sep){
		// sort the data files based on their blocking variables
		// the blocking order determines the sort order
		// if the data file are different files, need to sort each
		// using two ColumnSorter objects with the respective seperating characters
		// method returns true or false depending on success of sorting
		//int[] column_order = data_source.getIndexesOfColumnNames(mc.getBlockingColumns());
		
		// column IDs for character delimited file should hold line array index
		// of column
		int[] column_order = data_source.getIncludeIndexesOfColumnNames(mc.getBlockingColumns());
		
		
		int[] column_types = new int[column_order.length];
		for(int i = 0; i < column_order.length; i++){
			column_types[i] = data_source.getColumnTypeByName(mc.getRowName(column_order[i]));
		}
		
		// create ColumnSortOption objects for metafile
		Vector<ColumnSortOption> options = new Vector<ColumnSortOption>();
		for(int i = 0; i < column_order.length; i++){
			// column order is zero based, column options needs to be 1 based
			options.add(new ColumnSortOption(column_order[i] + 1, ColumnSortOption.ASCENDING, column_types[i]));
		}
		
		// create FileOutputStream for the result of the sort
		File sorted;
		
		sorted = new File(f.getName() + ".sorted");
		try{
			FileOutputStream data1_fos = new FileOutputStream(sorted);
			ColumnSorter sort_data1 = new ColumnSorter(data_source.getAccess().charAt(0), options, f, data1_fos);
			sort_data1.runSort();
		}
		catch(FileNotFoundException fnfe){
			// if can't open the output stream at the stage, return signaling failure
			// as the later steps make no sense without a file from this step
			return null;
		}
		
		
		return sorted;
	}
	
	public boolean reset(){
		try {
			file_reader.close();
			file_reader = new BufferedReader(new FileReader(sorted_file));
			next_record = line2Record(file_reader.readLine());			
			return true;
		} catch (Exception e1) {
			return false;
		}
		
	}
}