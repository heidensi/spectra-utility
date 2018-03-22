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
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;

/**
 * Module that takes a spectra wrapper object and produces a sequence of Strings
 * in a machine learning format format that can be written to a file.
 * 
 * @author Simon Heiden
 */
public class SpectraWrapperToMLFormatPipe extends AbstractProcessor<SpectraWrapper,String> {

	private final boolean filterNonExecuted;
	
	private final Map<SourceCodeBlock, Integer> map;
	private final Path output;

	public SpectraWrapperToMLFormatPipe(final boolean filterNonExecuted, final Path output) {
		super();
		this.filterNonExecuted = filterNonExecuted;
		this.output = output;
		this.map = new HashMap<>();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public String processItem(SpectraWrapper spectra, ProcessorSocket<SpectraWrapper, String> socket) {
		toML(spectra, socket);
		
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
     * @param spectraWrapper
     * the spectra wrapper object
     * @param socket
     * the socket
     * @return 
     * the combined ML format string to write to a file
     */
    private void toML(SpectraWrapper spectraWrapper, ProcessorSocket<SpectraWrapper, String> socket) {
        final StringBuffer line = new StringBuffer();
  
        ISpectra<SourceCodeBlock, ?> spectra = spectraWrapper.getSpectra();
        
        Log.out(this, "node identifiers: %d,\ttest cases: %d", spectra.getNodes().size(), spectra.getTraces().size());
//        socket.setTracker(new NewProgressBarTracker(spectra.getNodes().size()/50 + 1, spectra.getNodes().size()));
        socket.enableTracking(10);
        //iterate over the node identifiers
        for (INode<SourceCodeBlock> node : spectra.getNodes()) {
        	socket.track();
        	//iterate over the traces for each identifier
        	int j = 0;
        	for (ITrace<SourceCodeBlock> trace : spectra.getTraces()) {
        		int spectraValue = 0;
        		boolean executed = trace.isInvolved(node);
        		if (executed) {
        			if (trace.isSuccessful()) {
        				spectraValue = 1;
        			} else {
        				spectraValue = 3;
        			}
        		} else {
        			if (trace.isSuccessful()) {
        				spectraValue = 0;
        			} else {
        				spectraValue = 2;
        			}
        		}
        		if (!filterNonExecuted || executed) {
        			//gets or computes an integer value for the given identifier
        			line.append(map.computeIfAbsent(node.getIdentifier(), k -> map.size()) 
        					+ "\t" + String.valueOf(spectraValue) + "." + String.valueOf(j));
//        			line.append(spectraWrapper.getModificationsAsString(node.getIdentifier()));
        			//send the string to the output of this pipe
        			socket.produce(line.toString());
        			line.setLength(0);
        		}
        		++j;
        	}
        }
    }

	@Override
	public String getResultFromCollectedItems() {
		//store the actual identifier names in a separate file for reference
		List<String> lines = new ArrayList<>(map.size());
		Map<SourceCodeBlock,Integer> identifierNames = Misc.sortByValue(map);
		for (Entry<SourceCodeBlock, Integer> identifier : identifierNames.entrySet()) {
			lines.add(identifier.getValue() + ":" + identifier.getKey().toCompressedString());
		}
		
		new ListToFileWriter<List<String>>(output, true)
		.submit(lines);
		
		return null;
	}   
    
}
