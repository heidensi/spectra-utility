
package se.de.hu_berlin.informatik.spectra.converter;

import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.spectra.converter.modules.SpectraWrapperToCSVModule;
import se.de.hu_berlin.informatik.spectra.reader.PathWrapper;
import se.de.hu_berlin.informatik.spectra.reader.SpectraWrapper;
import se.de.hu_berlin.informatik.spectra.reader.modules.PathWrapperToSpectraWrapperModule;
import se.de.hu_berlin.informatik.utils.fileoperations.StringListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;

/**
 * Parses a zipped spectra file and produces an output file in a desired format.
 * At the moment, only CSV output format is supported. 
 * 
 * @author Simon Heiden
 */
public class Converter {
	
	//option constants
	private static final String SPECTRA_INPUT_OPT = "s";
	private static final String RANKED_INPUT_OPT = "r";
	private static final String UNRANKED_INPUT_OPT = "u";
	private static final String MODE_OPT = "m";
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
		
		options.add(MODE_OPT, "mode", true, "Output format. Arguments may be: 'csv'. Default is 'csv'.");
		
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
		
		//we may switch this module out for another to change the output format
		//the module has to get a spectra wrapper object as input and should 
		//produce a list of Strings to write to a text-based file
		AModule<SpectraWrapper, List<String>> converterModule = null;
		
		//parse the given mode option. If none is given, use "csv"
		String mode = options.getOptionValue(MODE_OPT, "csv").toLowerCase();
		//add cases to switch for other modes
		switch (mode) {
		case "csv":
			converterModule = new SpectraWrapperToCSVModule();
			break;
		default:
			Misc.err((Object)null, "'%s' is not a valid mode option. Using CSV output format...", mode);
			converterModule = new SpectraWrapperToCSVModule();
		}
		
		//link the following modules together
		new ModuleLinker().link(
				//input: path wrapper, produces spectra
				new PathWrapperToSpectraWrapperModule(),
				//input: spectra, produces list of Strings
				converterModule,
				//input: list of Strings, writes to the specified output
				new StringListToFileWriterModule<List<String>>(output, true))
		.submit(paths);//submission of input paths
		
	}
	
}
