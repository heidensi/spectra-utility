/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.converter.modules;

import se.de.hu_berlin.informatik.spectra.reader.SpectraWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.AbstractPipe;

/**
 * Module that takes a spectra wrapper object and produces a sequence of Strings
 * in a machine learning format format that can be written to a file.
 * 
 * @author Simon Heiden
 */
public class SpectraWrapperToMLFormatPipe extends AbstractPipe<SpectraWrapper,String> {

	public SpectraWrapperToMLFormatPipe() {
		super(true);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public String processItem(SpectraWrapper spectra) {
		toCSV(spectra);
		
		return null;
	}

	/**
     * Turns a spectra wrapper object into machine learning format representation like this:
     * <pre>
     * node1 \t [0|1|2|3].t1
     * node1 \t [0|1|2|3].t1
     * node1 \t [0|1|2|3].t1
     * ...
     * nodeX \t [0|1|2|3].tY
     * </pre>
     * First is the node identifier, then a TAB space, then a number from the set {0,1,2,3}, where
     * <pre>
     * - 0 means a passing non-executed testcase,
     * - 1 means a passing executed testcase,
     * - 2 means a failing non-executed testcase,
     * - 3 means a failing executed testcase.
     * </pre>
     * @param spectra
     * the spectra wrapper object
     * @return 
     * the combined CSV string to write to a file
     */
    private void toCSV(SpectraWrapper spectra) {
        final StringBuffer line = new StringBuffer();
        
        Log.out(this, "node identifiers: %d,\ttest cases: %d", spectra.getIdentifierCount(), spectra.getTraces().size());
        //iterate over the node identifiers
        for (int i = 0; i < spectra.getIdentifierCount(); ++i) {
        	//iterate over the traces for each identifier
        	for (int j = 0; j < spectra.getTraces().size(); ++j) {
        		line.append(spectra.getIdentifiers()[i] + "\t");
        		int spectraValue = 0;
        		boolean executed = spectra.getTraces().get(j)[i] == 1;
        		if (executed) {
        			if (spectra.isSuccessful(j)) {
        				spectraValue = 1;
        			} else {
        				spectraValue = 3;
        			}
        		} else {
        			if (spectra.isSuccessful(j)) {
        				spectraValue = 0;
        			} else {
        				spectraValue = 2;
        			}
        		}
        		line.append(String.valueOf(spectraValue) + "." + String.valueOf(j));
//            	line.append(spectra.getModification(spectra.getIdentifiers()[i]));
        		
        		//send the string to the output of this pipe
            	submitProcessedItem(line.toString());
    			line.setLength(0);
        	}
        }
    }
}
