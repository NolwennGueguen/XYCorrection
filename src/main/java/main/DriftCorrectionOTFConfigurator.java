package main;

import gui.DriftCorrectionDialog;
import main.DriftCorrectionParameters;
import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;

public class DriftCorrectionOTFConfigurator implements ProcessorConfigurator {
    Studio studio;
    DriftCorrectionParameters parameters;
    DriftCorrectionDialog dialog;

    public DriftCorrectionOTFConfigurator(Studio studio1, DriftCorrectionParameters parameters1){
        studio = studio1;
        parameters = parameters1;
    }

    @Override
    public void showGUI() {
        dialog = new DriftCorrectionDialog(parameters);
    }

    @Override
    public void cleanup() {
        if (dialog != null && dialog.isVisible()) {
            dialog.setVisible(true);
        }
        dialog = null;
        parameters = null;
    }

    @Override
    public PropertyMap getSettings() {
        return null;
    }
}
