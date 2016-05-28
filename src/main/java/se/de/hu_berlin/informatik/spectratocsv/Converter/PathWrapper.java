package se.de.hu_berlin.informatik.spectratocsv.Converter;

import java.nio.file.Path;

/**
 * @author Simon Heiden
 */
public class PathWrapper {

	Path zipFilePath;
	Path rankedLines;
	Path unrankedLines;
	
	public PathWrapper(Path zipFilePath, Path rankedLines, Path unrankedLines) {
		super();
		this.zipFilePath = zipFilePath;
		this.rankedLines = rankedLines;
		this.unrankedLines = unrankedLines;
	}

	public Path getZipFilePath() {
		return zipFilePath;
	}

	public Path getRankedLines() {
		return rankedLines;
	}

	public Path getUnrankedLines() {
		return unrankedLines;
	}
	
}
