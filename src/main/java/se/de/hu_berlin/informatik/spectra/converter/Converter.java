
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
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.INode.CoverageType;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.BuildBlockSpectraModule;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.FilterSpectraModule;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.InvertTraceInvolvementSpectraModule;
import se.de.hu_berlin.informatik.stardust.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.basics.StringsToFileWriter;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.Pipe;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;

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
		USE_BLOCKS("b", "combineToBlocks", false, "Whether to combine sequences of spectra elements to larger blocks "
				+ "if they were executed by the same set of traces.", false),
		INVERT_SUCCESSFUL("invSucc", "invertSuccessful", false, "Whether to invert the involvements of nodes in successful traces. "
				+ "Will be done AFTER removing any nodes.", false),
		INVERT_FAILING("invFail", "invertFailing", false, "Whether to invert the involvements of nodes in failing traces. "
				+ "Will be done AFTER removing any nodes.", false),
		FILTER("f", "filterNonExecuted", false, "Whether to filter out lines that were not executed "
				+ "(Only works for .ml output format).", false),
		REMOVE_NODES(Option.builder("rm").longOpt("removeNodes").required(false).hasArgs()
				.desc("Whether to remove groups of nodes with certain properties from the spectra. Possible options are: " +
						Misc.enumToString(INode.CoverageType.class) + ".").build()),
		CHANGES("c", "changesFile", true, "Path to file with change information (usually '.changes').", false),
		MODE("m", "mode", true, "Output format. Arguments may be: 'csv' or 'ml'. Default is 'ml'.", false),
		OUTPUT("o", "output", true, "Path to output file (e.g. '~/outputDir/project/bugID/data.csv').", true);

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
		Pipe<SpectraWrapper, String> converterPipe = null;
		
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
		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(paths.getZipFilePath());
		
		//create a pipe linker
		PipeLinker linker = new PipeLinker();
		
		//finally, create and link the modules together
		
		if (options.hasOption(CmdOptions.REMOVE_NODES)) {
			String[] values = options.getOptionValues(CmdOptions.REMOVE_NODES);
			if (values != null) {
				for (String value : values) {
					//remove nodes that are part of certain node groups
					CoverageType type = Misc.getEnumFromToString(CoverageType.class, value.toLowerCase(Locale.getDefault()));
					if (type == null) {
						Log.abort(Converter.class, "Unknown option value: '%s'", value);
					}
					switch(type) {
					case EF_EQUALS_ZERO:
						linker.append(new FilterSpectraModule<SourceCodeBlock>(CoverageType.EF_EQUALS_ZERO));
						break;
					case EF_GT_ZERO:
						linker.append(new FilterSpectraModule<SourceCodeBlock>(CoverageType.EF_GT_ZERO));
						break;
					case EP_EQUALS_ZERO:
						linker.append(new FilterSpectraModule<SourceCodeBlock>(CoverageType.EP_EQUALS_ZERO));
						break;
					case EP_GT_ZERO:
						linker.append(new FilterSpectraModule<SourceCodeBlock>(CoverageType.EP_GT_ZERO));
						break;
					case EXECUTED:
						linker.append(new FilterSpectraModule<SourceCodeBlock>(CoverageType.EXECUTED));
						break;
					case NF_EQUALS_ZERO:
						linker.append(new FilterSpectraModule<SourceCodeBlock>(CoverageType.NF_EQUALS_ZERO));
						break;
					case NF_GT_ZERO:
						linker.append(new FilterSpectraModule<SourceCodeBlock>(CoverageType.NF_GT_ZERO));
						break;
					case NOT_EXECUTED:
						linker.append(new FilterSpectraModule<SourceCodeBlock>(CoverageType.NOT_EXECUTED));
						break;
					case NP_EQUALS_ZERO:
						linker.append(new FilterSpectraModule<SourceCodeBlock>(CoverageType.NP_EQUALS_ZERO));
						break;
					case NP_GT_ZERO:
						linker.append(new FilterSpectraModule<SourceCodeBlock>(CoverageType.NP_GT_ZERO));
						break;
					default:
						Log.abort(Converter.class, "Unknown option value: '%s'", value);
					}
				}
			}
		}
		
		if (options.hasOption(CmdOptions.USE_BLOCKS)) {
			//combine sequences of nodes that were executed by the 
			//same set of test cases to a larger block
			linker.append(new BuildBlockSpectraModule());
		}
		
		if (options.hasOption(CmdOptions.INVERT_FAILING) || options.hasOption(CmdOptions.INVERT_SUCCESSFUL)) {
			//invert involvements of nodes
			linker.append(new InvertTraceInvolvementSpectraModule<SourceCodeBlock>(
					options.hasOption(CmdOptions.INVERT_SUCCESSFUL), options.hasOption(CmdOptions.INVERT_FAILING)));
		}
		
		linker.append(
				//input: spectra, output: spectra wrapper
				new SpectraToSpectraWrapperModule(changesFile),
				//input: spectra wrapper, output: Strings (lines) to write to a file
				converterPipe,
				//input: Strings, writes to the specified output
				new StringsToFileWriter(output, true))
		.submitAndShutdown(spectra);//submit spectra
		
	}
	
}
