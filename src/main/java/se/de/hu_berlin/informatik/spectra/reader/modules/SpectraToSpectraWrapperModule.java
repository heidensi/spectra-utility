/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.reader.modules;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.reader.SpectraWrapper;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Reads a spectra and generates a wrapper object for easier access.
 * 
 * @author Simon Heiden
 */
public class SpectraToSpectraWrapperModule extends AbstractProcessor<ISpectra<SourceCodeBlock,?>,SpectraWrapper> {
	
	private Path changesFile;

	public SpectraToSpectraWrapperModule(Path changesFile) {
		super();
		this.changesFile = changesFile;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public SpectraWrapper processItem(ISpectra<SourceCodeBlock,?> spectra) {
    	
		SpectraWrapper spectraWrapper = new SpectraWrapper(spectra);

		//get all changed lines
		if (changesFile != null) {
			spectraWrapper.loadChanges(changesFile);
		}
		
		return spectraWrapper;
	}

}
