/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.reader.modules;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.spectra.reader.SpectraWrapper;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Reads a spectra and generates a wrapper object for easier access.
 * 
 * @author Simon Heiden
 */
public class SpectraToSpectraWrapperModule extends AbstractModule<ISpectra<SourceCodeBlock>,SpectraWrapper> {
	
	private Path changesFile;

	public SpectraToSpectraWrapperModule(Path changesFile) {
		//if this module needs an input item
		super(true);
		this.changesFile = changesFile;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public SpectraWrapper processItem(ISpectra<SourceCodeBlock> spectra) {
    	
		SpectraWrapper spectraWrapper = new SpectraWrapper(spectra);

		//get all changed lines
		if (changesFile != null) {
			spectraWrapper.loadChanges(changesFile);
		}
		
		return spectraWrapper;
	}

}
