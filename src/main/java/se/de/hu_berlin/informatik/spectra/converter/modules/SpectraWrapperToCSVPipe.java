/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.converter.modules;

import se.de.hu_berlin.informatik.spectra.reader.SpectraWrapper;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;
import se.de.hu_berlin.informatik.utils.tracking.NewProgressBarTracker;

/**
 * Module that takes a spectra wrapper object and produces a sequence of Strings
 * in CSV format that can be written to a file.
 * 
 * @author Simon Heiden
 */
public class SpectraWrapperToCSVPipe extends AbstractProcessor<SpectraWrapper,String> {

	/**
     * Used CSV delimiter. May be changed as desired.
     */
    public static final char CSV_DELIMITER = ';';

	public SpectraWrapperToCSVPipe() {
		super();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public String processItem(SpectraWrapper spectra, ProcessorSocket<SpectraWrapper, String> socket) {
		toCSV(spectra, socket);
		
		return null;
	}

	/**
     * Turns a spectra wrapper object into CSV representation like this:
     * <pre>
     * 	-ids- ( t1 , t2 , t3 , ... , tN ) -modification- <- test case identifiers
     * 	node1 |  1 |  1 |  0 | ... |  1 |                <- involvement with first node identifier
     * 	node2 |  1 |  0 |  0 | ... |  0 |     append     <- involvement with second node identifier
     * 	node3 |  0 |  0 |  1 | ... |  1 |     change     <- ...
     * 	node4 |  1 |  1 |  0 | ... |  0 |
     * 	                  ...
     * 	nodeM |  0 |  1 |  1 | ... |  0 |     delete
     * 	      |succ|fail|succ| ... |succ|                <- successfulness flags
     * </pre>
     * Node identifiers could be added at the left side or the right side, if needed.
	 * @param socket 
     * the socket
     * @param spectraWrapper
     * the spectra wrapper object
     * @return 
     * the combined CSV string to write to a file
     */
    private void toCSV(SpectraWrapper spectraWrapper, ProcessorSocket<SpectraWrapper, String> socket) {
        final StringBuffer line = new StringBuffer();
        
        ISpectra<SourceCodeBlock> spectra = spectraWrapper.getSpectra();
        
        Log.out(this, "node identifiers: %d,\ttest cases: %d", spectra.getNodes().size(), spectra.getTraces().size());
        socket.setTracker(new NewProgressBarTracker(spectra.getNodes().size()/50 + 1, spectra.getNodes().size()));
        //iterate over the traces to get the test case identifiers
        for (ITrace<SourceCodeBlock> trace : spectra.getTraces()) {
    		line.append(CSV_DELIMITER + trace.getIdentifier().replace(CSV_DELIMITER, '_'));
    	}
        //send the string to the output of this pipe
        socket.produce(line.toString());
		line.setLength(0);
		
        //iterate over the identifiers
        for (INode<SourceCodeBlock> node : spectra.getNodes()) {
        	socket.track();
        	line.append(node.getIdentifier().toString().replace(CSV_DELIMITER, '_') + CSV_DELIMITER);
        	//iterate over the traces for each identifier
        	for (ITrace<SourceCodeBlock> trace : spectra.getTraces()) {
        		line.append((trace.isInvolved(node) ? 1 : 0) + String.valueOf(CSV_DELIMITER));
        	}
        	line.append(spectraWrapper.getModificationsAsString(node.getIdentifier()));
        	//send the string to the output of this pipe
        	socket.produce(line.toString());
			line.setLength(0);
        }
        
        line.append("<test result>" + CSV_DELIMITER);
        // at the end, collect the data from the successfulness flags
        for (ITrace<SourceCodeBlock> trace : spectra.getTraces()) {
    		line.append((trace.isSuccessful() ? "succ" : "fail") + String.valueOf(CSV_DELIMITER));
    	}
        //send the string to the output of this pipe
        socket.produce(line.toString());
		line.setLength(0);
    }

}
