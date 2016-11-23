package se.de.hu_berlin.informatik.spectra.reader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;

/**
 * Wrapper object for a line spectra.
 * 
 * @author Simon Heiden
 */
public class SpectraWrapper {

	private static Map<String, List<ChangeWrapper>> changesMap = Collections.emptyMap();
	
	private ISpectra<SourceCodeBlock> spectra;

	public SpectraWrapper(ISpectra<SourceCodeBlock> spectra) {
		super();
		this.spectra = spectra;
	}
	
	public ISpectra<SourceCodeBlock> getSpectra() {
		return spectra;
	}
	
	public List<ChangeWrapper> getModifications(SourceCodeBlock block) {
		List<ChangeWrapper> list = Collections.emptyList();
		//see if the respective file was changed
		if (changesMap.containsKey(block.getClassName())) {
			List<ChangeWrapper> changes = changesMap.get(block.getClassName());
			for (ChangeWrapper change : changes) {
				//is the ranked block part of a changed statement?
				if (block.getEndLineNumber() >= change.getStart() && block.getStartLineNumber() <= change.getEnd()) {
					if (list.isEmpty()) {
						list = new ArrayList<>(1);
					}
					list.add(change);
				}
			}
		}
		
		return list;
	}
	
	public String getModificationsAsString(SourceCodeBlock block) {
		List<ChangeWrapper> list = getModifications(block);
		
		if (list.isEmpty()) {
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
