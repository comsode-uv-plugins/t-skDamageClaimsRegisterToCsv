package eu.comsode.unifiedviews.plugins.transformer.damageclaims;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

public class DamageClaimsVaadinDialog extends AbstractDialog<DamageClaimsConfig_V1> {

    /**
     * 
     */
    private static final long serialVersionUID = -1539890500190668003L;

    public DamageClaimsVaadinDialog() {
        super(DamageClaims.class);
    }

    @Override
    protected void buildDialogLayout() {
    }

    @Override
    protected void setConfiguration(DamageClaimsConfig_V1 c) throws DPUConfigException {
    }

    @Override
    protected DamageClaimsConfig_V1 getConfiguration() throws DPUConfigException {

        final DamageClaimsConfig_V1 cnf = new DamageClaimsConfig_V1();
        return cnf;
    }

}