/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.converter.modules;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.spectra.reader.SpectraWrapper;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.AbstractPipe;
import se.de.hu_berlin.informatik.utils.tracking.ProgressBarTracker;

/**
 * Module that takes a spectra wrapper object and produces a sequence of Strings
 * in a machine learning format format that can be written to a file.
 * 
 * @author Simon Heiden
 */
public class SpectraWrapperToMLFormatPipe extends AbstractPipe<SpectraWrapper,String> {

	private final boolean filterNonExecuted;
	
	private final Map<String, Integer> map;
	private final Path output;

	public SpectraWrapperToMLFormatPipe(final boolean filterNonExecuted, final Path output) {
		super(true);
		this.filterNonExecuted = filterNonExecuted;
		this.output = output;
		this.map = new HashMap<>();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public String processItem(SpectraWrapper spectra) {
		toML(spectra);
		
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
     * the combined ML format string to write to a file
     */
    private void toML(SpectraWrapper spectra) {
        final StringBuffer line = new StringBuffer();
  
        Log.out(this, "node identifiers: %d,\ttest cases: %d", spectra.getIdentifierCount(), spectra.getTraces().size());
        setTracker(new ProgressBarTracker(spectra.getIdentifierCount()/50, 50));
        //iterate over the node identifiers
        for (int i = 0; i < spectra.getIdentifierCount(); ++i) {
        	track();
        	//iterate over the traces for each identifier
        	for (int j = 0; j < spectra.getTraces().size(); ++j) {
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
        		if (!filterNonExecuted || executed) {
        			//gets or computes an integer value for the given identifier
        			line.append(map.computeIfAbsent(spectra.getIdentifiers()[i], k -> map.size()) 
        					+ "\t" + String.valueOf(spectraValue) + "." + String.valueOf(j));
//        			line.append(spectra.getModification(spectra.getIdentifiers()[i]));
        			//send the string to the output of this pipe
                	submitProcessedItem(line.toString());
        			line.setLength(0);
        		}
        	}
        }
    }

	@Override
	public String getResultFromCollectedItems() {
		//store the actual identifier names in a separate file for reference
		List<String> lines = new ArrayList<>(map.size());
		Map<String,Integer> identifierNames = Misc.sortByValue(map);
		for (Entry<String, Integer> identifier : identifierNames.entrySet()) {
			lines.add(identifier.getValue() + ":" + identifier.getKey());
		}
		
		new ListToFileWriterModule<List<String>>(output, true)
		.submit(lines);
		
		return null;
	}
    
    
}
