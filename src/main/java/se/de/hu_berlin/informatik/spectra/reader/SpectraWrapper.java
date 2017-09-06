package se.de.hu_berlin.informatik.spectra.reader;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.changechecker.ChangeCheckerUtils;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;

/**
 * Wrapper object for a line spectra.
 * 
 * @author Simon Heiden
 */
public class SpectraWrapper {

	private Map<String, List<ChangeWrapper>> changesMap = Collections.emptyMap();
	
	private ISpectra<SourceCodeBlock, ?> spectra;

	public SpectraWrapper(ISpectra<SourceCodeBlock, ?> spectra2) {
		super();
		this.spectra = spectra2;
	}
	
	public ISpectra<SourceCodeBlock, ?> getSpectra() {
		return spectra;
	}
	
	public String getModificationsAsString(SourceCodeBlock block) {
		List<ChangeWrapper> list = ChangeCheckerUtils.getModifications(block.getFilePath(), 
				block.getStartLineNumber(), block.getEndLineNumber(), true, changesMap);
		
		if (list == null) {
			return "";
		} else {
			StringBuilder builder = new StringBuilder();
			boolean first = true;
			for (ChangeWrapper change : list) {
				if (first) {
					first = false;
				} else {
					builder.append(", ");
				}
				builder.append(change.getModificationType());
			}
			return builder.toString();
		}
	}
	
	public void loadChanges(Path changeFile) {
		changesMap = ChangeWrapper.readChangesFromFile(changeFile);
	}
}
