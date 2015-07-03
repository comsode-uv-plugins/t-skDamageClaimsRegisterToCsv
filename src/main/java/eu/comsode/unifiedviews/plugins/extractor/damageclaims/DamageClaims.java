package eu.comsode.unifiedviews.plugins.extractor.damageclaims;

import eu.comsode.unifiedviews.plugins.extractor.service.Tabula;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;

@DPU.AsExtractor
public class DamageClaims extends AbstractDpu<DamageClaimsConfig_V1> {

	@DataUnit.AsOutput(name = "fileOutput")
	public WritableFilesDataUnit fileOutput;

	public DamageClaims() {
		super(DamageClaimsVaadinDialog.class, ConfigHistory
				.noHistory(DamageClaimsConfig_V1.class));
	}

	@Override
	protected void innerExecute() throws DPUException {
        new Tabula().runTabula(config.getFileName());
	}
}