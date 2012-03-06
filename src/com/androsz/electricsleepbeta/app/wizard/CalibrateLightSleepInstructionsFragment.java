/* @(#)CalibrateLightSleepInstructionsFragment.java
 *
 *========================================================================
 * Copyright 2011 by Zeo Inc. All Rights Reserved
 *========================================================================
 *
 * Date: $Date$
 * Author: Jon Willis
 * Author: Brandon Edens <brandon.edens@myzeo.com>
 * Version: $Revision$
 */

package com.androsz.electricsleepbeta.app.wizard;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.LayoutFragment;

/**
 * Fragment that displays instructions on how to calibrate for light sleep.
 *
 * @author Jon Willis
 * @author Brandon Edens
 * @version $Revision$
 */
public class CalibrateLightSleepInstructionsFragment extends LayoutFragment {


    @Override
    public int getLayoutResourceId() {
        return R.layout.wizard_calibration_lightsleep_instructions;
    }

}

