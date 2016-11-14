
package se.de.hu_berlin.informatik.spectra.converter;

import java.nio.file.Path;
import java.util.Locale;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.spectra.converter.modules.SpectraWrapperToCSVPipe;
import se.de.hu_berlin.informatik.spectra.converter.modules.SpectraWrapperToMLFormatPipe;
import se.de.hu_berlin.informatik.spectra.reader.PathWrapper;
import se.de.hu_berlin.informatik.spectra.reader.SpectraWrapper;
import se.de.hu_berlin.informatik.spectra.reader.modules.PathWrapperToSpectraWrapperModule;
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
		SPECTRA_INPUT("s", "spectraZip", true, "Path to input zip file (zipped and compressed spectra file).", true),
		RANKED_INPUT("r", "rankedLines", true, "Path to file with ranked modified lines (usually '.ranked_mod_lines').", false),
		UNRANKED_INPUT("u", "unrankedLines", true, "Path to file with unranked modified lines (usually '.unranked_mod_lines').", false),
		MODE("m", "mode", true, "Output format. Arguments may be: 'csv'. Default is 'csv'.", false),
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

		OptionParser options = OptionParser.getOptions("Converter", true, CmdOptions.class, args);

		//get the input paths and make sure they exist
		Path zipFilePath = options.isFile(CmdOptions.SPECTRA_INPUT, true);
		Path rankedLines = options.hasOption(CmdOptions.RANKED_INPUT) ? options.isFile(CmdOptions.RANKED_INPUT, true) : null;
		Path unrankedLines = options.hasOption(CmdOptions.UNRANKED_INPUT) ? options.isFile(CmdOptions.UNRANKED_INPUT, true) : null;
		
		//get the output path (does not need to exist)
		Path output = options.isFile(CmdOptions.OUTPUT, false);
		
		//wrap the paths of the input files
		PathWrapper paths = new PathWrapper(zipFilePath, rankedLines, unrankedLines);
		
		//we may switch this module out for another to change the output format
		//the module has to get a spectra wrapper object as input and should 
		//produce a list of Strings to write to a text-based file
		AbstractPipe<SpectraWrapper, String> converterPipe = null;
		
		//parse the given mode option. If none is given, use "ml"
		String mode = options.getOptionValue(CmdOptions.MODE, "ml").toLowerCase(Locale.getDefault());
		//add cases to switch for other modes
		switch (mode) {
		case "csv":
			converterPipe = new SpectraWrapperToCSVPipe();
			break;
		case "ml":
			converterPipe = new SpectraWrapperToMLFormatPipe();
			break;
		default:
			Log.warn(Converter.class, "'%s' is not a valid mode option. Using ML output format...", mode);
			converterPipe = new SpectraWrapperToMLFormatPipe();
		}
		
		//link the following modules together
		new PipeLinker().append(
				//input: path wrapper, produces spectra
				new PathWrapperToSpectraWrapperModule(),
				//input: spectra, produces Strings
				converterPipe,
				//input: Strings, writes to the specified output
				new StringToFileWriterPipe(output, true))
		.submitAndShutdown(paths);//submission of input paths
		
	}
	
}
