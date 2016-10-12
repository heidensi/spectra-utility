/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.reader.modules;

import se.de.hu_berlin.informatik.spectra.converter.IdentifierWithModificationLineProcessor;
import se.de.hu_berlin.informatik.spectra.reader.PathWrapper;
import se.de.hu_berlin.informatik.spectra.reader.SpectraWrapper;
import se.de.hu_berlin.informatik.utils.compression.CompressedByteArrayToByteArrayModule;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ReadZipFileModule;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.fileoperations.FileLineProcessorModule;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Reads a zipped spectra file and generates a wrapper object for easier access.
 * 
 * @author Simon Heiden
 */
public class PathWrapperToSpectraWrapperModule extends AbstractModule<PathWrapper,SpectraWrapper> {

	/**
	 * Should not be changed! Used to split the spectra node identifiers.
	 */
	private static final String IDENTIFIER_DELIMITER = "\t";
	
	public PathWrapperToSpectraWrapperModule() {
		//if this module needs an input item
		super(true);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public SpectraWrapper processItem(PathWrapper paths) {
		//read the input zip file and return a wrapper for simpler access
		ZipFileWrapper zip = new ReadZipFileModule().submit(paths.getZipFilePath()).getResult();

		//parse the file containing the identifiers
		String[] identifiers = new String(zip.get(0)).split(IDENTIFIER_DELIMITER);

		//parse the file containing the involvement table
		byte[] involvementTable = zip.get(1);

		//decode the compressed involvement table (each involvement flag was compressed into one single bit beforehand)
		involvementTable = new CompressedByteArrayToByteArrayModule().submit(involvementTable).getResult();
		
		SpectraWrapper spectra = new SpectraWrapper(identifiers);
		
		//parse all the traces from the involvement table
		addTraces(spectra, involvementTable, identifiers.length);
		
		//get all ranked modified lines
		if (paths.getRankedLines() != null) {
			new FileLineProcessorModule<Boolean>(new IdentifierWithModificationLineProcessor(spectra))
			.submit(paths.getRankedLines());
		}
		//also get all unranked modified lines (not really necessary, but maybe for later...)
		if (paths.getUnrankedLines() != null) {
			new FileLineProcessorModule<Boolean>(new IdentifierWithModificationLineProcessor(spectra))
			.submit(paths.getUnrankedLines());
		}
		
		return spectra;
	}

	/**
     * Adds traces from a spectra byte array to the provided spectra wrapper for easier access.
     * @param spectra
     * the spectra wrapper object to add the traces to
     * @param dataArray 
     * an array containing the data elements
     * @param traceLength
     * the length of each trace
     */
    private void addTraces(SpectraWrapper spectra, final byte[] dataArray, int traceLength) {
    	int i = -1;
    	// example: sequenceLength == 6 (1 successfulness flag + 5 identifier involvement flags)
        // we have to iterate over all traces to get involvement flags connected to the same identifier
        // | succFlag | 0 | 0 | 1 | 0 | 1 | succFlag | 1 | 0 | 0 | 1 | 1 | ...
        while ((i+1) < dataArray.length) {
        	boolean successful = (dataArray[++i] == 1);
        	byte[] trace = new byte[traceLength];
        	for (int j = 0; j < traceLength; ++j) {
        		trace[j] = dataArray[++i];
        	}
        	spectra.addTrace(trace, successful);
        }
    }
}
