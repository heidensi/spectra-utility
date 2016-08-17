/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.converter;

import se.de.hu_berlin.informatik.spectra.reader.SpectraWrapper;
import se.de.hu_berlin.informatik.spectra.reader.SpectraWrapper.Modification;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor;

/**
 * Takes a {@link String} that is a node identifier in a spectra object together with a
 * modification identifier ('a', 'c' or 'd'). The format of the processed lines has to be
 * <pre>
 * "identifier (a|c|d)"
 * </pre>
 * 
 * @author Simon Heiden
 */
public class IdentifierWithModificationLineProcessor implements IStringProcessor<Boolean> {

	private SpectraWrapper spectra;
	
	public IdentifierWithModificationLineProcessor(SpectraWrapper spectra) {
		super();
		this.spectra = spectra;
	}
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.stringprocessor.IStringProcessor#process(java.lang.String)
	 */
	public boolean process(String line) {
		String[] parts = line.split(" ");
		if (parts.length != 2) {
			Log.abort(this, "Line '%s' is in a wrong format.", line);
		}
		Modification modification = null;
		switch (parts[1]) {
		case "a":
			modification = Modification.APPEND;
			break;
		case "c":
			modification = Modification.CHANGE;
			break;
		case "d":
			modification = Modification.DELETE;
			break;
		default:
			Log.abort(this, "Unknown modification '%s'.", parts[1]);
		}
		spectra.setModification(parts[0], modification);
		return true;
	}

	@Override
	public Boolean getResult() {
		return null;
	}



}
