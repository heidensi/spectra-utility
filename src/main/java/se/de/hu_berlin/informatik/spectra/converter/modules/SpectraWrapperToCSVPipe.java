/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.converter.modules;

import se.de.hu_berlin.informatik.spectra.reader.SpectraWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.AbstractPipe;

/**
 * Module that takes a spectra wrapper object and produces a sequence of Strings
 * in CSV format that can be written to a file.
 * 
 * @author Simon Heiden
 */
public class SpectraWrapperToCSVPipe extends AbstractPipe<SpectraWrapper,String> {

	/**
     * Used CSV delimiter. May be changed as desired.
     */
    public static final char CSV_DELIMITER = ';';

	public SpectraWrapperToCSVPipe() {
		super(true);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public String processItem(SpectraWrapper spectra) {
		System.out.println(spectra);
		toCSV(spectra);
		
		return null;
	}

	/**
     * Turns a spectra wrapper object into CSV representation like this:
     * <pre>
     * 	-ids- ( t1 , t2 , t3 , ... , tN ) -modification- <- test case identifiers! Not stored, currently
     * 	node1 |  1 |  1 |  0 | ... |  1 |                <- involvement with first node identifier
     * 	node2 |  1 |  0 |  0 | ... |  0 |     append     <- involvement with second node identifier
     * 	node3 |  0 |  0 |  1 | ... |  1 |     change     <- ...
     * 	node4 |  1 |  1 |  0 | ... |  0 |
     * 	                  ...
     * 	nodeM |  0 |  1 |  1 | ... |  0 |     delete
     * 	      |succ|fail|succ| ... |succ|                <- successfulness flags
     * </pre>
     * Node identifiers could be added at the left side or the right side, if needed.
     * 
     * @param spectra
     * the spectra wrapper object
     * @return 
     * the combined CSV string to write to a file
     */
    private void toCSV(SpectraWrapper spectra) {
        final StringBuffer line = new StringBuffer();
        
        Log.out(this, "node identifiers: %d,\ttest cases: %d", spectra.getIdentifierCount(), spectra.getTraces().size());
        //iterate over the identifiers
        for (int i = 0; i < spectra.getIdentifierCount(); ++i) {
        	line.append(spectra.getIdentifiers()[i] + CSV_DELIMITER);
        	//iterate over the traces for each identifier
        	for (int j = 0; j < spectra.getTraces().size(); ++j) {
        		line.append(spectra.getTraces().get(j)[i] + String.valueOf(CSV_DELIMITER));
        	}
        	line.append(spectra.getModification(spectra.getIdentifiers()[i]));
        	//send the string to the output of this pipe
        	submitProcessedItem(line.toString());
			line.setLength(0);
        }
        
        line.append("<test result>" + CSV_DELIMITER);
        // at the end, collect the data from the successfulness flags
        for (int j = 0; j < spectra.getTraces().size(); ++j) {
    		line.append((spectra.isSuccessful(j) ? "succ" : "fail") + String.valueOf(CSV_DELIMITER));
    	}
        //send the string to the output of this pipe
    	submitProcessedItem(line.toString());
		line.setLength(0);
    }
}
