package se.de.hu_berlin.informatik.spectratocsv.Converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

/**
 * Wrapper object for a line spectra.
 * 
 * @author Simon Heiden
 */
public class SpectraWrapper {

	public static enum Modification { CHANGE, DELETE, APPEND };
	
	private List<byte[]> traces;
	private String[] identifiers;
	private List<Boolean> successfulFlags;
	
	private Map<String, Modification> modifiedLines;

	public SpectraWrapper(String[] identifiers) {
		super();
		this.modifiedLines = new HashMap<>();
		this.traces = new ArrayList<>();
		this.successfulFlags = new ArrayList<>();
		this.identifiers = identifiers;
	}
	
	/**
	 * Adds a trace to the spectra.
	 * @param trace
	 * the trace to add
	 * @param successful
	 * whether the trace is successful
	 */
	public void addTrace(byte[] trace, boolean successful) {
		Assert.assertEquals(trace.length, identifiers.length);
		traces.add(trace);
		successfulFlags.add(successful);
	}
	
	/**
	 * Returns the list of traces in this spectra.
	 * @return
	 * the list of traces
	 */
	public List<byte[]> getTraces() {
		return traces;
	}
	/**
	 * Checks whether the trace with the given index is successful.
	 * @param index
	 * the index of the trace
	 * @return
	 * whether the trace is successful
	 */
	public boolean isSuccessful(int index) {
		return successfulFlags.get(index);
	}
	
	/**
	 * Returns the array of node identifiers.
	 * @return
	 * the node identifiers
	 */
	public String[] getIdentifiers() {
		return identifiers;
	}
	
	/**
	 * Returns the number of node identifiers.
	 * @return
	 * the number of node identifiers
	 */
	public int getIdentifierCount() {
		return identifiers.length;
	}
	
	/**
	 * Get a String representation of the modification that is associated to
	 * the given node identifier. If no modification is associated with the
	 * identifier, then the empty String is returned.
	 * @param identifier
	 * a node identifier
	 * @return
	 * a String representation of the modification associated to the given identifier
	 */
	public String getModification(String identifier) {
		return modifiedLines.containsKey(identifier) ? modificationToString(modifiedLines.get(identifier)) : "";
	}
	
	/**
	 * Associates the given identifier with the given modification.
	 * @param identifier
	 * the identifier to associate with the modification
	 * @param modification
	 * the modification to associate with the identifier
	 */
	public void setModification(String identifier, Modification modification) {
		modifiedLines.put(identifier, modification);
	}
	
	/**
	 * Returns a String representation of a modification.
	 * @param modification
	 * the modification
	 * @return
	 * a String representation of the given modification
	 */
	private static String modificationToString(Modification modification) {
		switch (modification) {
		case CHANGE:
			return "change";
		case DELETE:
			return "delete";
		case APPEND:
			return "append";
		}
		return "";
	}
}
