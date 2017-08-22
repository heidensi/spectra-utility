package se.de.hu_berlin.informatik.spectra.reader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper.ModificationType;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;

/**
 * Wrapper object for a line spectra.
 * 
 * @author Simon Heiden
 */
public class SpectraWrapper {

	private Map<String, List<ChangeWrapper>> changesMap = Collections.emptyMap();
	
	private ISpectra<SourceCodeBlock> spectra;

	public SpectraWrapper(ISpectra<SourceCodeBlock> spectra) {
		super();
		this.spectra = spectra;
	}
	
	public ISpectra<SourceCodeBlock> getSpectra() {
		return spectra;
	}
	
	/**
	 * Returns the list of changes relevant to the given {@link SourceCodeBlock}.
	 * @param ignoreRefactorings
	 * whether to ignore changes that are refactorings
	 * @param block
	 * the block to check
	 * @param changesMap
	 * the map of all existing changes
	 * @return
	 * list of changes relevant to the given block; {@code null} if no changes match
	 */
	public List<ChangeWrapper> getModifications(boolean ignoreRefactorings, 
			SourceCodeBlock block) {
		List<ChangeWrapper> list = null;
		//see if the respective file was changed
		List<ChangeWrapper> changes = changesMap.get(block.getFilePath());
		if (changes != null) {
			for (ChangeWrapper change : changes) {
				
				if (change.getModificationType() == ModificationType.NO_CHANGE) {
					continue;
				}
				
				if (ignoreRefactorings) {
					//no semantic change like changes to a comment or something like that? then proceed...
					if (change.getModificationType() == ModificationType.PROB_NO_CHANGE) {
						continue;
					}
				}
				
				//is the ranked block part of a changed statement?
				for (int deltaLine : change.getIncludedDeltas()) {
					if (block.getStartLineNumber() <= deltaLine && deltaLine <= block.getEndLineNumber()) {
						if (list == null) {
							list = new ArrayList<>(1);
						}
						list.add(change);
						break;
					}
				}
			}
		}
		return list;
	}
	
	public String getModificationsAsString(SourceCodeBlock block) {
		List<ChangeWrapper> list = getModifications(false, block);
		
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
