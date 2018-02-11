package main;

import IOUtils.IO;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;


public class DriftCorrectionParameters {

    private static final String RANGE_SIZE_FOR_MOVIE = "RANGE_SIZE_FOR_MOVIE";
    private static final String STEP = "STEP";
    private static final String TIME_INTERVAL = "TIME_INTERVAL";
    private static final String TIME_LIMIT = "TIME_LIMIT";
    public static final String DO_ANALYSIS = "DO_ANALYSIS";
    public static final String PROJECTED = "PROJECTED";
    private static final String FLUO_ANALYSIS_PARAMETERS = "FLUO_ANALYSIS_PARAMETERS";
    private static final String MITOSIS_DETECTION_PARAMETERS = "MITOSIS_DETECTION_PARAMETERS";
    private static final String MINIMUM_DURATION = "MINIMUM_DURATION";
    private static final String DETECTION_CHANNEL = "DETECTION_CHANNEL";
    private static final String SPOT_RADIUS = "SPOT_RADIUS";
    private static final String MAXIMUM_NUMBER_OF_SPOT = "MAXIMUM_NUMBER_OF_SPOT";
    private static final String QUALITY = "QUALITY";
    private static final String SAVING_PATH = "SAVING_PATH";
    private static final String USING = "USING";
    private static final String GENERAL_ACQUISITION_PARAMETERS = "GENERAL_ACQUISITION_PARAMETERS";
    private static final String DEFAULT_CHANNEL_PARAMATERS = "DEFAULT_CHANNEL_PARAMATERS";
   private static final String DEFAULT_CONFIG_NAME = "driftCorrection_config.xml";
    private Document doc;
    private Element root;

    public DriftCorrectionParameters(InputStream defaultParametersStream) {
        final SAXBuilder sb = new SAXBuilder();
        try {
            try {
                doc = sb.build(defaultParametersStream);
            } catch (IOException e) {
                IO.printErrorToIJLog(e);
            }
        } catch (JDOMException e) {
            IO.printErrorToIJLog(e);
        }
        root = (Element) doc.getContent(0);
    }

    public DriftCorrectionParameters() {
    }

    public static int getTimePointsNb(DriftCorrectionParameters parameters) {
        double timeLimit = Double.parseDouble(parameters.getFluoParameter(DriftCorrectionParameters.TIME_LIMIT)) * 60
                * 1000;
        double fluoTimeInterval = Double.parseDouble(parameters.getFluoParameter(DriftCorrectionParameters.TIME_INTERVAL));
        return (int) (timeLimit / fluoTimeInterval);
    }

    public static int getSliceNb(DriftCorrectionParameters parameters) {
        return (int) (Double.valueOf(parameters.getFluoParameter(DriftCorrectionParameters.RANGE_SIZE_FOR_MOVIE)) /
                Double.valueOf(parameters.getFluoParameter(DriftCorrectionParameters.STEP)) + 1);
    }

    public static int getChNb(DriftCorrectionParameters parameters) {
        String channelsString = parameters.getUsingChannels();
        String[] arrayChannels = channelsString.split(",", -1);
        return arrayChannels.length;
    }

    public static String[] getChArray(DriftCorrectionParameters parameters) {
        String channelsString = parameters.getUsingChannels();
        return channelsString.split(",", -1);
    }

    /**
     * * Write the parameters into the configuration file
     *
     * @param path path to save
     */
    public void save(String path) {
        doc.setContent(root);
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        try {
            xmlOutput.output(doc, new FileWriter(path + File.separator + DEFAULT_CONFIG_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return saving folder of MAARS output
     */
    public String getSavingPath() {
        return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChildText(SAVING_PATH);
    }

    /**
     * set saving path
     *
     * @param path : corresponding value of parameter
     */
    public void setSavingPath(String path) {
        root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(SAVING_PATH).setText(path);
    }

    /**
     * @param parameter name of fluo parameter
     * @return time limit of fluorescence acquisition for one acquisition
     */
    public String getFluoParameter(final String parameter) {
        return root.getChild(FLUO_ANALYSIS_PARAMETERS).getChildText(parameter);
    }

    /**
     * @param ch: GFP, CFP, DAPI, TXRED
     * @return MAXIMUM_NUMBER_OF_SPOT of corresponding channel
     */
    public String getChMaxNbSpot(String ch) {
        return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch)
                .getChildText(MAXIMUM_NUMBER_OF_SPOT);
    }

    /**
     * @param ch: GFP, CFP, DAPI, TXRED
     * @return SPOT_RADIUS of corresponding channel
     */
    public String getChSpotRaius(String ch) {
        return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch).getChildText(SPOT_RADIUS);
    }

    /**
     * @param ch: GFP, CFP, DAPI, TXRED
     * @return SPOT_RADIUS of corresponding channel
     */
    public String getChQuality(String ch) {
        return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch).getChildText(QUALITY);
    }

    //////////// Setters

    /**
     * @return get channels used for fluo analysis
     */
    public String getUsingChannels() {
        return root.getChild(FLUO_ANALYSIS_PARAMETERS).getChildText(USING);
    }

    /**
     * @return the parameter minimum mitosis duration
     */
    public String getMinimumMitosisDuration() {
        return root.getChild(MITOSIS_DETECTION_PARAMETERS).getChildText(MINIMUM_DURATION);
    }

    /**
     * @param duration minimum mitosis duration (sec)
     */
    public void setMinimumMitosisDuration(String duration) {
        root.getChild(MITOSIS_DETECTION_PARAMETERS).getChild(MINIMUM_DURATION).setText(duration);
    }

    /**
     * @return the channel to use for mitosis detection
     */
    public String getDetectionChForMitosis() {
        return root.getChild(MITOSIS_DETECTION_PARAMETERS).getChildText(DETECTION_CHANNEL);
    }

    /**
     * @param chForMitosis the channel to detection mitosis
     */
    public void setDetectionChForMitosis(String chForMitosis) {
        root.getChild(MITOSIS_DETECTION_PARAMETERS).getChild(DETECTION_CHANNEL).setText(chForMitosis);
    }

    /**
     * @param projected whether or not project z stack
     */
    public void setProjected(String projected) {
        root.getChild(FLUO_ANALYSIS_PARAMETERS).getChild(PROJECTED).setText(projected);
    }

    /**
     * set fluo analysis parameter
     *
     * @param parameter : static final String of DriftCorrectionParameters
     * @param value     : corresponding value of parameter
     */
    public void setFluoParameter(String parameter, String value) {
        root.getChild(FLUO_ANALYSIS_PARAMETERS).getChild(parameter).setText(value);
    }

    /**
     * @param ch        GFP, CFP, DAPI, TXRED
     * @param maxNbSpot maximum number of spot for corresponding channel
     */
    public void setChMaxNbSpot(String ch, String maxNbSpot) {
        root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch).getChild(MAXIMUM_NUMBER_OF_SPOT)
                .setText(maxNbSpot);
    }

    /**
     * @param ch         GFP, CFP, DAPI, TXRED
     * @param spotRaidus spotRaidus for corresponding channel
     */
    public void setChSpotRaius(String ch, String spotRaidus) {
        root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch).getChild(SPOT_RADIUS)
                .setText(spotRaidus);
    }

    /**
     * @param ch      GFP, CFP, DAPI, TXRED
     * @param quality quality of spots for corresponding channel
     */
    public void setChQuality(String ch, String quality) {
        root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch).getChild(QUALITY).setText(quality);
    }

    /**
     * @param root the dataset of this class
     */
    private void setRoot(Element root) {
        this.root = root;
    }

}
