import gui.DriftCorrectionMainDialog;
import ij.process.ImageProcessor;
import main.DriftCorrectionOTFConfigurator;
import main.DriftCorrectionOTFFactory;
import main.DriftCorrectionParameters;
import mmcorej.CMMCore;
import mmcorej.StrVector;
import org.micromanager.AutofocusPlugin;
import org.micromanager.MenuPlugin;
import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.data.ProcessorFactory;
import org.micromanager.data.ProcessorPlugin;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.AutofocusBase;
import org.micromanager.internal.utils.MMException;
import org.micromanager.internal.utils.PropertyItem;
import org.micromanager.internal.utils.ReportingUtils;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

import java.util.Vector;

@Plugin(type = ProcessorPlugin.class)
public class DriftCorrectionOTFPlugin implements ProcessorPlugin, SciJavaPlugin {

    private static final String VERSION_INFO = "1.0.0";
    private static final String NAME = "Drift Correction";
    private static final String DESCRIPTION = "Micro-Manager plugin for correct X and Y drift";
    private static final String COPYRIGHT_NOTICE = "Not defined yet";
    private DriftCorrectionParameters parameters;
    private Studio studio;
    private CMMCore cmmCore;

    @Override
    public ProcessorConfigurator createConfigurator(PropertyMap propertyMap) {
        PropertyMap.PropertyMapBuilder driftBuilder = studio.data().getPropertyMapBuilder();
        studio.profile().insertProperties(DriftCorrectionOTFPlugin.class, driftBuilder.build());
        return new DriftCorrectionOTFConfigurator(studio, parameters);
    }

    @Override
    public ProcessorFactory createFactory(PropertyMap propertyMap) {
        return new DriftCorrectionOTFFactory(studio, parameters);
    }

    @Override
    public void setContext(Studio studio1) {
        this.studio = studio1;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getHelpText() {
        return DESCRIPTION;
    }

    @Override
    public String getVersion() {
        return VERSION_INFO;
    }

    @Override
    public String getCopyright() {
        return COPYRIGHT_NOTICE;
    }
}