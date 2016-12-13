/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.converter.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import se.de.hu_berlin.informatik.spectra.converter.Converter;
import se.de.hu_berlin.informatik.spectra.converter.Converter.CmdOptions;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class ConverterTest extends TestSettings {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		deleteTestOutputs();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		deleteTestOutputs();
	}
	
	@Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.converter.Converter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainCSV() {
		String[] args = { 
				CmdOptions.SPECTRA_INPUT.asArg(), getStdResourcesDir() + File.separator + "Chart-7b.zip",
				CmdOptions.CHANGES.asArg(), getStdResourcesDir() + File.separator + ".changes",
				CmdOptions.MODE.asArg(), "csv",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "spectra.csv" };
		Converter.main(args);
		Path out = Paths.get(getStdTestDir(), "spectra.csv");
		assertTrue(Files.exists(out));
		
		String[] args2 = { 
				CmdOptions.SPECTRA_INPUT.asArg(), getStdResourcesDir() + File.separator + "Chart-7b.zip",
				CmdOptions.MODE.asArg(), "csv",
				CmdOptions.USE_BLOCKS.asArg(),
				CmdOptions.RESTRICT_TO_FAILED.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "spectra2.csv" };
		Converter.main(args2);
		Path out2 = Paths.get(getStdTestDir(), "spectra2.csv");
		assertTrue(Files.exists(out2));
		
		assertTrue(out.toFile().length() > out2.toFile().length());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.converter.Converter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainMLFormat() {
		String[] args = { 
				CmdOptions.SPECTRA_INPUT.asArg(), getStdResourcesDir() + File.separator + "Chart-7b.zip",
				CmdOptions.MODE.asArg(), "ml",
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "spectra.ml" };
		Converter.main(args);
		Path out = Paths.get(getStdTestDir(), "spectra.ml");
		assertTrue(Files.exists(out));
		Path outMap = Paths.get(getStdTestDir(), "spectra.ml.map");
		assertTrue(Files.exists(outMap));
		
		String[] args2 = { 
				CmdOptions.SPECTRA_INPUT.asArg(), getStdResourcesDir() + File.separator + "Chart-7b.zip",
				CmdOptions.MODE.asArg(), "ml",
				CmdOptions.USE_BLOCKS.asArg(),
				CmdOptions.RESTRICT_TO_FAILED.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "spectra2.ml" };
		Converter.main(args2);
		Path out2 = Paths.get(getStdTestDir(), "spectra2.ml");
		assertTrue(Files.exists(out2));
		Path out2Map = Paths.get(getStdTestDir(), "spectra2.ml.map");
		assertTrue(Files.exists(out2Map));
		
		assertTrue(out.toFile().length() > out2.toFile().length());
		assertTrue(outMap.toFile().length() > out2Map.toFile().length());
		
		String[] args3 = { 
				CmdOptions.SPECTRA_INPUT.asArg(), getStdResourcesDir() + File.separator + "Chart-7b.zip",
				CmdOptions.MODE.asArg(), "ml",
				CmdOptions.USE_BLOCKS.asArg(),
				CmdOptions.RESTRICT_TO_FAILED.asArg(),
				CmdOptions.FILTER.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "spectra3.ml" };
		Converter.main(args3);
		Path out3 = Paths.get(getStdTestDir(), "spectra3.ml");
		assertTrue(Files.exists(out3));
		Path out3Map = Paths.get(getStdTestDir(), "spectra3.ml.map");
		assertTrue(Files.exists(out3Map));
		
		assertTrue(out2.toFile().length() > out3.toFile().length());
		assertTrue(out2Map.toFile().length() == out3Map.toFile().length());
	}
	
//	/**
//	 * Test method for {@link se.de.hu_berlin.informatik.spectra.converter.Converter#main(java.lang.String[])}.
//	 */
//	@Test
//	public void testMainMLFormatFilteredBigSpectra() {
//		String[] args = { 
//				CmdOptions.SPECTRA_INPUT.asArg(), getStdResourcesDir() + File.separator + "spectraCompressed_big.zip",
//				CmdOptions.MODE.asArg(), "ml",
//				CmdOptions.FILTER.asArg(),
//				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "spectra_filtered_big.ml" };
//		Converter.main(args);
//		assertTrue(Files.exists(Paths.get(getStdTestDir(), "spectra_filtered_big.ml")));
//		assertTrue(Files.exists(Paths.get(getStdTestDir(), "spectra_filtered_big.ml.map")));
//	}

}
