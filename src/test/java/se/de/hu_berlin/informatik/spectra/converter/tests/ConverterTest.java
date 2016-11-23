/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.converter.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
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
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "spectra.csv")));
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
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "spectra.ml")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "spectra.ml.map")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.converter.Converter#main(java.lang.String[])}.
	 */
	@Test
	public void testMainMLFormatFiltered() {
		String[] args = { 
				CmdOptions.SPECTRA_INPUT.asArg(), getStdResourcesDir() + File.separator + "Chart-7b.zip",
				CmdOptions.MODE.asArg(), "ml",
				CmdOptions.FILTER.asArg(),
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "spectra_filtered.ml" };
		Converter.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "spectra_filtered.ml")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "spectra_filtered.ml.map")));
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
