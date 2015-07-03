package eu.comsode.unifiedviews.plugins.transformer.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Rectangle;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.Utils;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import eu.comsode.unifiedviews.plugins.transformer.corrector.Corrector;
import eu.comsode.unifiedviews.plugins.transformer.damageclaims.DamageClaimsConfig_V1;

public class Tabula {
	private List<List<String>> data = new ArrayList<>();
	private File inputFile;
	private File outputFile;
	private Corrector corrector;

    private DamageClaimsConfig_V1 config;

	public void runTabula(String fileName) {

		corrector = new Corrector();
		inputFile = new File(fileName);
        config = new DamageClaimsConfig_V1();
        outputFile = new File(config.getFileName().substring(0, config.getFileName().lastIndexOf('.')) + ".csv");
		String[] args = corrector.getArgs();
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine line = parser.parse(buildOptions(), args);
			extractTables(line);
		} catch (ParseException exp) {
			System.err.println("Error: " + exp.getMessage());
		}
		// printData();
		makeCSV();
	}

	private void extractTables(CommandLine line) throws ParseException {
		if (!inputFile.exists()) {
			throw new ParseException("File does not exist");
		}

		/* Vymedzenie len urcitej casti strany dokumentu */
		Rectangle area = null;
		if (line.hasOption('a')) {
			List<Float> f = parseFloatList(line.getOptionValue('a'));
			if (f.size() != 4) {
				throw new ParseException(
						"area parameters must be top,left,bottom,right");
			}
			area = new Rectangle(f.get(0), f.get(1), f.get(3) - f.get(1),
					f.get(2) - f.get(0));
		}

		/* Rozstrihanie na stlpce */
		List<Float> verticalRulingPositions = null;
		if (line.hasOption('c')) {
			verticalRulingPositions = parseFloatList(line.getOptionValue('c'));
		}

		/* Definicia rozsahu stran, zmena defaultu na all */
		String pagesOption = line.hasOption('p') ? line.getOptionValue('p')
				: "all";
		ExtractionMethod method = whichExtractionMethod(line);
		List<Integer> pages = Utils.parsePagesOption(pagesOption);

		/* Odheslovanie zaheslovaneho PDF suboru */
		try {
			ObjectExtractor oe = line.hasOption('s') ? new ObjectExtractor(
					PDDocument.load(inputFile), line.getOptionValue('s'))
					: new ObjectExtractor(PDDocument.load(inputFile));
			BasicExtractionAlgorithm basicExtractor = new BasicExtractionAlgorithm();
			SpreadsheetExtractionAlgorithm spreadsheetExtractor = new SpreadsheetExtractionAlgorithm();

			PageIterator pageIterator = pages == null ? oe.extract() : oe
					.extract(pages);
			Page page;
			List<Table> tables = new ArrayList<Table>();

			while (pageIterator.hasNext()) {
				page = pageIterator.next();

				if (area != null) {
					page = page.getArea(area);
				}

				if (method == ExtractionMethod.DECIDE) {
					method = spreadsheetExtractor.isTabular(page) ? ExtractionMethod.SPREADSHEET
							: ExtractionMethod.BASIC;
				}

				switch (method) {
				case BASIC:
					if (line.hasOption('g')) {

					}
					tables.addAll(verticalRulingPositions == null ? basicExtractor
							.extract(page) : basicExtractor.extract(page,
							verticalRulingPositions));

					break;
				case SPREADSHEET:
					// TODO add useLineReturns
					tables.addAll(spreadsheetExtractor.extract(page));
				default:
					break;
				}
				for (Table t : tables) {
					createData(tables);
				}
				tables.clear();
			}

		} catch (IOException e) {
			throw new ParseException(e.getMessage());
		}
	}

	@SuppressWarnings("static-access")
	private Options buildOptions() {
		Options o = new Options();

		o.addOption("v", "version", false, "Print version and exit.");
		o.addOption("h", "help", false, "Print this help text.");
		o.addOption("g", "guess", false,
				"Guess the portion of the page to analyze per page.");
		o.addOption("d", "debug", false,
				"Print detected table areas instead of processing");
		o.addOption(
				"r",
				"spreadsheet",
				false,
				"Force PDF to be extracted using spreadsheet-style extraction (if there are ruling lines separating each cell, as in a PDF of an Excel spreadsheet)");
		o.addOption(
				"n",
				"no-spreadsheet",
				false,
				"Force PDF not to be extracted using spreadsheet-style extraction (if there are ruling lines separating each cell, as in a PDF of an Excel spreadsheet)");
		o.addOption("i", "silent", false, "Suppress all stderr output.");
		o.addOption("u", "use-line-returns", false,
				"Use embedded line returns in cells. (Only in spreadsheet mode.)");
		o.addOption("d", "debug", false,
				"Print detected table areas instead of processing.");

		o.addOption(OptionBuilder
				.withLongOpt("columns")
				.withDescription(
						"X coordinates of column boundaries. Example --columns 10.1,20.2,30.3")
				.hasArg().withArgName("COLUMNS").create("c"));
		o.addOption(OptionBuilder
				.withLongOpt("area")
				.withDescription(
						"Portion of the page to analyze (top,left,bottom,right). Example: --area 269.875,12.75,790.5,561. Default is entire page")
				.hasArg().withArgName("AREA").create("a"));
		o.addOption(OptionBuilder
				.withLongOpt("pages")
				.withDescription(
						"Comma separated list of ranges, or all. Examples: --pages 1-3,5-7, --pages 3 or --pages all. Default is --pages 1")
				.hasArg().withArgName("PAGES").create("p"));

		return o;
	}

	private List<Float> parseFloatList(String option) throws ParseException {
		String[] f = option.split(",");
		List<Float> rv = new ArrayList<Float>();
		try {
			for (int i = 0; i < f.length; i++) {
				rv.add(Float.parseFloat(f[i]));
			}
			return rv;
		} catch (NumberFormatException e) {
			throw new ParseException("Wrong number syntax");
		}
	}

	private enum ExtractionMethod {
		BASIC, SPREADSHEET, DECIDE
	}

	ExtractionMethod whichExtractionMethod(CommandLine line) {
		ExtractionMethod rv = ExtractionMethod.DECIDE;
		if (line.hasOption('r')) {
			rv = ExtractionMethod.SPREADSHEET;
		} else if (line.hasOption('n') || line.hasOption('c')
				|| line.hasOption('a') || line.hasOption('g')) {
			rv = ExtractionMethod.BASIC;
		}
		return rv;
	}

	private void createData(Iterable<Table> tables) throws IOException {
		Iterator<Table> iter = tables.iterator();
		while (iter.hasNext()) {
			for (List<RectangularTextContainer> row : iter.next().getRows()) {
				List<String> rowData = new ArrayList<String>(row.size());
				for (RectangularTextContainer<?> b : row) {
					rowData.add(b.getText().replace('\n', ' ')
							.replace('\r', ' '));
				}
				List<String> correctRow = corrector.correct(rowData);
				if (correctRow != null)
					data.add(correctRow);
			}
		}
	}

	private void printData() {
		for (List<String> row : data) {
			for (String cell : row) {
				System.out.println(cell);
			}
			System.out.println();
		}
	}

	private void makeCSV() {
		try {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile), "UTF-8"));
			for (List<String> row : data) {
				String line = "";
				for (String cell : row) {
					line += ("\"" + cell + "\",");
				}
				writer.print(line.substring(0, line.length() - 1) + "\n");
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}