
package se.de.hu_berlin.informatik.spectra.converter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.spectra.converter.modules.SpectraWrapperToCSVPipe;
import se.de.hu_berlin.informatik.spectra.converter.modules.SpectraWrapperToMLFormatPipe;
import se.de.hu_berlin.informatik.spectra.reader.PathWrapper;
import se.de.hu_berlin.informatik.spectra.reader.SpectraWrapper;
import se.de.hu_berlin.informatik.spectra.reader.modules.SpectraToSpectraWrapperModule;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.BuildBlockSpectraModule;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.FilterSpectraModule;
import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.AbstractPipe;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;
import se.de.hu_berlin.informatik.utils.tm.pipes.StringToFileWriterPipe;

/**
 * Parses a zipped spectra file and produces an output file in a desired format.
 * At the moment, only CSV output format is supported. 
 * 
 * @author Simon Heiden
 */
public class Converter {
	
	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		SPECTRA_INPUT("i", "spectraInput", true, "Path to input zip file (zipped and compressed spectra file).", true),
		FILTER("f", "filterNonExecuted", false, "Whether to filter out lines that were not executed "
				+ "(Only works for .ml output format).", false),
		USE_BLOCKS("b", "combineToBlocks", false, "Whether to combine sequences of spectra elements to larger blocks "
				+ "if they were executed by the same set of traces.", false),
		RESTRICT_TO_FAILED("r", "restrictToFailed", false, "Whether to only include nodes that were executed "
				+ "by some failing test.", false),
		CHANGES("c", "changesFile", true, "Path to file with change information (usually '.changes').", false),
		MODE("m", "mode", true, "Output format. Arguments may be: 'csv' or 'ml'. Default is 'ml'.", false),
		OUTPUT("o", "output", true, "Path to output csv data file (e.g. '~/outputDir/project/bugID/data.csv').", true);

		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;

		//adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).
					hasArg(hasArg).desc(description).build(), NO_GROUP);
		}
		
		//adds an option that is part of the group with the specified index (positive integer)
		//a negative index means that this option is part of no group
		//this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).
					hasArg(hasArg).desc(description).build(), groupId);
		}
		
		//adds the given option that will be part of the group with the given id
		CmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}
		
		//adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override public String toString() { return option.getOption().getOpt(); }
		@Override public OptionWrapper getOptionWrapper() { return option; }
	}
	
	/**
	 * @param args
	 * -s spectra-zip-file [-r ranked-lines-file] [-u unranked-lines-file] -o output-file
	 */
	public static void main(String[] args) {

		OptionParser options = OptionParser.getOptions("Converter", false, CmdOptions.class, args);

		//get the input paths and make sure they exist
		Path zipFilePath = options.isFile(CmdOptions.SPECTRA_INPUT, true);
		Path changesFile = options.hasOption(CmdOptions.CHANGES) ? options.isFile(CmdOptions.CHANGES, true) : null;
		
		//get the output path (does not need to exist)
		Path output = options.isFile(CmdOptions.OUTPUT, false);
		
		//wrap the paths of the input files
		PathWrapper paths = new PathWrapper(zipFilePath, changesFile);
		
		//we may switch this module out for another to change the output format
		//the module has to get a spectra wrapper object as input and should 
		//produce a list of Strings to write to a text-based file
		AbstractPipe<SpectraWrapper, String> converterPipe = null;
		
		//parse the given mode option. If none is given, use "ml"
		String mode = options.getOptionValue(CmdOptions.MODE, "ml").toLowerCase(Locale.getDefault());
		//add cases to switch for other modes
		switch (mode) {
		case "csv":
			converterPipe = new SpectraWrapperToCSVPipe().asPipe();
			break;
		case "ml":
			converterPipe = new SpectraWrapperToMLFormatPipe(options.hasOption(CmdOptions.FILTER), Paths.get(output.toString() + ".map")).asPipe();
			break;
		default:
			Log.warn(Converter.class, "'%s' is not a valid mode option. Using ML output format...", mode);
			converterPipe = new SpectraWrapperToMLFormatPipe(options.hasOption(CmdOptions.FILTER), Paths.get(output.toString() + ".map")).asPipe();
		}
		
		//load the spectra from the given zip file
		ISpectra<SourceCodeBlock> spectra = SpectraUtils.loadBlockSpectraFromZipFile(paths.getZipFilePath());
		
		//create a pipe linker
		PipeLinker linker = new PipeLinker();
		
		//finally, link the modules together
		
		if (options.hasOption(CmdOptions.RESTRICT_TO_FAILED)) {
			//remove nodes that were not executed by any failed test case
			linker.append(new FilterSpectraModule<SourceCodeBlock>());
		}
		
		if (options.hasOption(CmdOptions.USE_BLOCKS)) {
			//combine sequences of nodes that were executed by the 
			//same set of test cases to a larger block
			linker.append(new BuildBlockSpectraModule());
		}
		
		linker.append(
				//input: spectra, output: spectra wrapper
				new SpectraToSpectraWrapperModule(changesFile),
				//input: spectra wrapper, output: Strings (lines) to write to a file
				converterPipe,
				//input: Strings, writes to the specified output
				new StringToFileWriterPipe(output, true))
		.submitAndShutdown(spectra);//submit spectra
		
	}
	
}
