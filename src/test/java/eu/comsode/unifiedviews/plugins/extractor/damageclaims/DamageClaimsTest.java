package eu.comsode.unifiedviews.plugins.extractor.damageclaims;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.Test;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;

public class DamageClaimsTest {

	@Test
	public void execute() throws Exception {
		// Prepare config.
		DamageClaimsConfig_V1 config = new DamageClaimsConfig_V1();
		DamageClaims dpu = new DamageClaims();
        String outputFileName = config.getFileName().substring(0, config.getFileName().lastIndexOf('.')) + ".csv";
		// Prepare DPU.
        dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
		// Prepare test environment.
		TestEnvironment environment = new TestEnvironment();
		// Prepare data unit.
        WritableFilesDataUnit filesOutput = environment.createFilesOutput("fileOutput");
		try {
			// Run.
			environment.run(dpu);
            File csvFile = new File(outputFileName);
            assertTrue(csvFile.exists());

            BufferedReader br = new BufferedReader(new FileReader(outputFileName));
            assertTrue(br.readLine() != null);
            br.close();
		} finally {
			// Release resources.
			environment.release();
		}
	}
}
