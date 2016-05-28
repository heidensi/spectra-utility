
package se.de.hu_berlin.informatik.spectratocsv.Converter;

import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.spectratocsv.Converter.modules.SpectraWrapperToCSVModule;
import se.de.hu_berlin.informatik.spectratocsv.Converter.modules.PathWrapperToSpectraWrapperModule;
import se.de.hu_berlin.informatik.utils.fileoperations.StringListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;

/**
 * Collects all JUnit tests from a list of classes provided by an input file.
 * 
 * @author Simon Heiden
 */
public class Converter {
	
	private static final String SPECTRA_INPUT_OPT = "s";
	private static final String RANKED_INPUT_OPT = "r";
	private static final String UNRANKED_INPUT_OPT = "u";
	private static final String OUTPUT_OPT = "o";
	
	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "Converter -s spectra-zip-file [-r ranked-lines-file] [-u unranked-lines-file] -o output-file"; 
		final String tool_usage = "Converter";
		final OptionParser options = new OptionParser(tool_usage, args);

		options.add(SPECTRA_INPUT_OPT, "spectraZip", true, "Path to input zip file (zipped and compressed spectra file).", true);
		options.add(RANKED_INPUT_OPT, "rankedLines", true, "Path to file with ranked modified lines (usually '.ranked_mod_lines').");
		options.add(UNRANKED_INPUT_OPT, "unrankedLines", true, "Path to file with unranked modified lines (usually '.unranked_mod_lines').");
		
		options.add(OUTPUT_OPT, "output", true, "Path to output csv data file (e.g. '~/outputDir/project/bugID/data.csv').", true);
        
        options.parseCommandLine();
        
        return options;
	}
	
	/**
	 * @param args
	 * -s spectra-zip-file [-r ranked-lines-file] [-u unranked-lines-file] -o output-file
	 */
	public static void main(String[] args) {

		OptionParser options = getOptions(args);

		//get the input paths and make sure they exist
		Path zipFilePath = options.isFile(SPECTRA_INPUT_OPT, true);
		Path rankedLines = options.hasOption(RANKED_INPUT_OPT) ? options.isFile(RANKED_INPUT_OPT, true) : null;
		Path unrankedLines = options.hasOption(UNRANKED_INPUT_OPT) ? options.isFile(UNRANKED_INPUT_OPT, true) : null;
		
		//get the output path (does not need to exist)
		Path output = options.isFile(OUTPUT_OPT, false);
		
		//wrap the paths of the input files
		PathWrapper paths = new PathWrapper(zipFilePath, rankedLines, unrankedLines);
		
		//--> switch this module out for another to change the output format
		// the module has to get a spectra wrapper object as input and should produce a
		// list of Strings to write to a text-based file
		AModule<SpectraWrapper, List<String>> module = new SpectraWrapperToCSVModule();
		
		//link the following modules together
		new ModuleLinker().link(
				//input: path wrapper, produces spectra
				new PathWrapperToSpectraWrapperModule(),
				//input: spectra, produces list of Strings
				module,
				//input: list of Strings, writes to the specified output
				new StringListToFileWriterModule<List<String>>(output, true))
		.submit(paths);//submission of input paths
		
	}
	
}
