/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.reader.modules;

import se.de.hu_berlin.informatik.spectra.reader.PathWrapper;
import se.de.hu_berlin.informatik.spectra.reader.SpectraWrapper;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Reads a zipped spectra file and generates a wrapper object for easier access.
 * 
 * @author Simon Heiden
 */
public class PathWrapperToSpectraWrapperModule extends AbstractModule<PathWrapper,SpectraWrapper> {
	
	public PathWrapperToSpectraWrapperModule() {
		//if this module needs an input item
		super(true);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public SpectraWrapper processItem(PathWrapper paths) {
		
		ISpectra<SourceCodeBlock> spectra = SpectraUtils.loadBlockSpectraFromZipFile(paths.getZipFilePath());
    	
		SpectraWrapper spectraWrapper = new SpectraWrapper(spectra);

		//get all changed lines
		if (paths.getChangesFile() != null) {
			spectraWrapper.loadChanges(paths.getChangesFile());
		}
		
		return spectraWrapper;
	}

}
