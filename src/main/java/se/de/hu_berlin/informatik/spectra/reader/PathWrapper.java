package se.de.hu_berlin.informatik.spectra.reader;

import java.nio.file.Path;

/**
 * @author Simon Heiden
 */
public class PathWrapper {

	Path zipFilePath;
	Path changesFile;
	
	public PathWrapper(Path zipFilePath, Path changesFile) {
		super();
		this.zipFilePath = zipFilePath;
		this.changesFile = changesFile;
	}

	public Path getZipFilePath() {
		return zipFilePath;
	}

	public Path getChangesFile() {
		return changesFile;
	}
	
}
