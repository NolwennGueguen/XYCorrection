import gui.DriftCorrectionMainDialog;
import ij.process.ImageProcessor;
import mmcorej.CMMCore;
import mmcorej.StrVector;
import org.micromanager.AutofocusPlugin;
import org.micromanager.Studio;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.*;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

import java.util.Vector;

@Plugin(type = AutofocusPlugin.class)
public class DriftCorrectionPlugin extends AutofocusBase implements AutofocusPlugin, SciJavaPlugin {

    private static final String VERSION_INFO = "1.0.0";
    private static final String NAME = "Drift Correction";
    private static final String DESCRIPTION = "Micro-Manager plugin for correct X and Y drift";
    private static final String COPYRIGHT_NOTICE = "Not defined yet";
    private MMStudio mmStudio;
    private CMMCore cmmCore;
    private String deviceName;

    public DriftCorrectionPlugin() {
    }


    @Override
    public double fullFocus() throws MMException {
        if (this.cmmCore == null) {
            return 0.0D;
        }
        else {
            try {
                this.cmmCore.setAutoFocusDevice(this.deviceName);
                this.cmmCore.fullFocus();
            } catch (Exception e) {
                throw new MMException(e.getMessage());
            }
            try {
                return this.cmmCore.getLastFocusScore();
            } catch (Exception e2) {
                ReportingUtils.showError(e2);
                return 0.0D;
            }
        }
    }

    @Override
    public String getVerboseStatus() {
        return "No message at this time!";
    }

    @Override
    public double incrementalFocus() throws MMException {
        if (this.cmmCore == null) {
            return 0.0D;
        } else {
            try {
                this.cmmCore.setAutoFocusDevice(this.deviceName);
                this.cmmCore.incrementalFocus();
            } catch (Exception e3) {
                throw new MMException(e3.getMessage());
            }

            try {
                return this.cmmCore.getLastFocusScore();
            } catch (Exception e2) {
                ReportingUtils.logError(e2);
                return 0.0D;
            }
        }
    }

    @Override
    public String[] getPropertyNames() {
        Vector propNames = new Vector();

        try {
            this.cmmCore.setAutoFocusDevice(this.deviceName);
            StrVector propNamesVect = this.cmmCore.getDevicePropertyNames(this.deviceName);

            for (int i = 0; (long)i < propNamesVect.size(); i++) {
                if (!this.cmmCore.isPropertyReadOnly(this.deviceName, propNamesVect.get(i)) && this.cmmCore.isPropertyPreInit(this.deviceName, propNamesVect.get(i))) {
                    propNames.add(propNamesVect.get(i));
                }
            }
        } catch (Exception e4) {
            ReportingUtils.logError(e4);
        }
        return (String[])propNames.toArray(new String[propNames.size()]);
    }

    @Override
    public PropertyItem[] getProperties() {
        Vector props = new Vector();

        try {
            this.cmmCore.setAutoFocusDevice(this.deviceName);
            StrVector propsVect = this.cmmCore.getDevicePropertyNames(this.deviceName);
            for(int i = 0; (long)i < propsVect.size(); ++i) {
                PropertyItem p = new PropertyItem();
                p.device = this.deviceName;
                p.name = propsVect.get(i);
                p.value = this.cmmCore.getProperty(this.deviceName, p.name);
                p.readOnly = this.cmmCore.isPropertyReadOnly(this.deviceName, p.name);
                if (this.cmmCore.hasPropertyLimits(this.deviceName, p.name)) {
                    p.lowerLimit = this.cmmCore.getPropertyLowerLimit(this.deviceName, p.name);
                    p.upperLimit = this.cmmCore.getPropertyUpperLimit(this.deviceName, p.name);
                }

                StrVector vals = this.cmmCore.getAllowedPropertyValues(this.deviceName, p.name);
                p.allowed = new String[(int)vals.size()];

                for(int j = 0; (long)j < vals.size(); ++j) {
                    p.allowed[j] = vals.get(j);
                }

                props.add(p);
            }
        } catch (Exception e5) {
            ReportingUtils.logError(e5);
        }

        return (PropertyItem[])props.toArray(new PropertyItem[0]);
    }

    @Override
    public String getPropertyValue(String name) throws MMException {
        try {
            return this.cmmCore.getProperty(this.deviceName, name);
        } catch (Exception e6) {
            throw new MMException(e6.getMessage());
        }
    }

    @Override
    public PropertyItem getProperty(String name) throws MMException {
        try {
            if (this.cmmCore.hasProperty(this.deviceName, name)) {
                throw new MMException("Unknown property: " + name);
            } else {
                PropertyItem p = new PropertyItem();
                p.device = this.deviceName;
                p.name = name;
                p.value = this.cmmCore.getProperty(this.deviceName, p.name);
                p.readOnly = this.cmmCore.isPropertyReadOnly(this.deviceName, p.name);
                if (this.cmmCore.hasPropertyLimits(this.deviceName, p.name)) {
                    p.lowerLimit = this.cmmCore.getPropertyLowerLimit(this.deviceName, p.name);
                    p.upperLimit = this.cmmCore.getPropertyUpperLimit(this.deviceName, p.name);
                }
                StrVector vals = this.cmmCore.getAllowedPropertyValues(this.deviceName, p.name);
                p.allowed = new String[(int)vals.size()];

                for(int j = 0; (long)j < vals.size(); ++j) {
                    p.allowed[j] = vals.get(j);
                }
                return p;
            }
        } catch (Exception e7) {
            throw new MMException(e7.getMessage());
        }
    }

    @Override
    public void setPropertyValue(String name, String value) throws MMException {
        try {
            this.cmmCore.setProperty(this.deviceName, name, value);
        } catch (Exception e8) {
            throw new MMException(e8.getMessage());
        }
    }

    @Override
    public double getCurrentFocusScore() {
        try {
            this.cmmCore.setAutoFocusDevice(this.deviceName);
        } catch (Exception e2) {
            ReportingUtils.logError(e2);
            return 0.0D;
        }
        return this.cmmCore.getCurrentFocusScore();
    }

    @Override
    public void applySettings() {
    }

    @Override
    public void saveSettings() {
    }

    @Override
    public int getNumberOfImages() {
        return this.cmmCore.getRemainingImageCount();
    }

    @Override
    public void setProperty(PropertyItem propertyItem) throws MMException {
        try {
            this.cmmCore.setProperty(this.deviceName, propertyItem.name, propertyItem.value);
        } catch (Exception e9) {
            throw new MMException(e9.getMessage());
        }
    }

    @Override
    public void enableContinuousFocus(boolean enable) throws MMException {
        try {
            this.cmmCore.setAutoFocusDevice(this.deviceName);
            this.cmmCore.enableContinuousFocus(enable);
        } catch (Exception e10) {
            throw new MMException(e10.getMessage());
        }
    }

    @Override
    public boolean isContinuousFocusEnabled() throws MMException {
        try {
            this.cmmCore.setAutoFocusDevice(this.deviceName);
            return this.cmmCore.isContinuousFocusEnabled();
        } catch (Exception e2) {
            throw new MMException(e2.getMessage());
        }
    }

    @Override
    public boolean isContinuousFocusLocked() throws MMException {
        try {
            this.cmmCore.setAutoFocusDevice(this.deviceName);
            return this.cmmCore.isContinuousFocusLocked();
        } catch (Exception e2) {
            throw new MMException(e2.getMessage());
        }
    }

    @Override
    public double computeScore(ImageProcessor imageProcessor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setContext(Studio mmstudio) {
        this.cmmCore = mmstudio.getCMMCore();
        this.deviceName = this.cmmCore.getAutoFocusDevice();
        this.mmStudio = (MMStudio) mmstudio;
        this.mmStudio.events().registerForEvents(this);
        new DriftCorrectionMainDialog(mmStudio);
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
