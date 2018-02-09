package main;

import org.micromanager.Studio;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorFactory;

public class DriftCorrectionOTFFactory implements ProcessorFactory {
    private Studio studio;
    private DriftCorrectionParameters parameters;

    public DriftCorrectionOTFFactory(Studio studio1, DriftCorrectionParameters parameters1) {
        studio = studio1;
        parameters = parameters1;
    }

    @Override
    public Processor createProcessor() {
        parameters.setSavingPath(studio.acquisitions().getAcquisitionSettings().root);
        return new DriftCorrectionOTF(parameters);
    }
}
