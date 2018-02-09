package gui;

import ij.IJ;
import main.DriftCorrectionParameters;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class DriftCorrectionDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private JCheckBox doAnaChx_ = new JCheckBox("Do analysis?");
    private DriftCorrectionParameters parameters_;
    private JFormattedTextField mitosisDurationTf_;
    private JCheckBox projected_;
    private ArrayList<ArrayList<Component>> allChComponents_;

    public DriftCorrectionDialog(DriftCorrectionParameters parameters) {
        super();
        doAnaChx_.setSelected(true);
        doAnaChx_.setEnabled(false);
        createDialog(parameters);
    }

    DriftCorrectionDialog(DriftCorrectionParameters parameters, JFrame mainFrame) {
        super(mainFrame);
        createDialog(parameters);
    }

    private void createDialog(DriftCorrectionParameters parameters) {
        setUpDialog(new Dimension(300, 200));
        int timePointsNb = DriftCorrectionParameters.getTimePointsNb(parameters);
        int slicePerFrame = DriftCorrectionParameters.getSliceNb(parameters);
        int nbChannel = DriftCorrectionParameters.getChNb(parameters);
        add(generateSummaryPanel(timePointsNb, slicePerFrame, nbChannel), BorderLayout.NORTH);
        add(generateAnaParamPanel(parameters), BorderLayout.CENTER);
        add(generateButtonPanel(parameters), BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    private JPanel generateButtonPanel(DriftCorrectionParameters parameters) {
        JPanel butPanel = new JPanel(new GridLayout(0, 3));
        JButton overwriteBut = new JButton("Overwrite");
        butPanel.add(overwriteBut);
        overwriteBut.addActionListener(action -> {
            int overWrite = JOptionPane.showConfirmDialog(this,"This will overwrite loaded configuration, still proceed?");
            if (JOptionPane.YES_OPTION == overWrite) {
                updateParameters(parameters);
                parameters.save(parameters.getSavingPath());

            }
        });
        JButton saveBut = new JButton("Save");
        butPanel.add(saveBut);
        saveBut.addActionListener(o -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            chooser.setDialogTitle("Directory to save config file");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                parameters.save(String.valueOf(chooser.getSelectedFile()));
            } else {
                IJ.log("No folder Selected");
            }

        });
        JButton okBut = new JButton("Ok");
        getRootPane().setDefaultButton(okBut);
        butPanel.add(okBut);
        okBut.addActionListener(actionEvent -> {
            updateParameters(parameters);
            parameters_ = parameters;
            setVisible(false);
        });
        return butPanel;
    }

    private void updateParameters(DriftCorrectionParameters parameters) {
        parameters.setMinimumMitosisDuration(mitosisDurationTf_.getText());
        parameters.setProjected(String.valueOf(projected_.isSelected()));
        parameters.setFluoParameter(DriftCorrectionParameters.DO_ANALYSIS, String.valueOf(doAnaChx_.isSelected()));
        String[] chNames = DriftCorrectionParameters.getChArray(parameters);
        for (int i = 0; i < chNames.length; i++) {
            String ch = chNames[i];
            ArrayList array = allChComponents_.get(i);
            parameters.setChMaxNbSpot(ch, ((JFormattedTextField) array.get(1)).getText());
            parameters.setChSpotRaius(ch, ((JFormattedTextField) array.get(2)).getText());
            parameters.setChQuality(ch, ((JFormattedTextField) array.get(3)).getText());
            if (((JRadioButton) array.get(5)).isSelected()) {
                parameters.setDetectionChForMitosis(ch);
            }
        }
    }

    private JPanel generateAnaParamPanel(DriftCorrectionParameters parameters) {
        JPanel anaParamPanel = new JPanel(new BorderLayout(2, 0));
        anaParamPanel.setBackground(GuiUtils.bgColor);
        anaParamPanel.setBorder(GuiUtils.addSecondaryTitle("Analysis parameters"));
        anaParamPanel.add(generateChbxPanel(parameters), BorderLayout.NORTH);
        anaParamPanel.add(generateFluoParamPanel(parameters), BorderLayout.CENTER);
        return anaParamPanel;
    }

    private JPanel generateFluoParamPanel(DriftCorrectionParameters parameters) {
        int chNb = DriftCorrectionParameters.getChNb(parameters);
        ArrayList<JPanel> arrayPanels = generateParamPanels(chNb);
        allChComponents_ = generateAllChComponents(parameters);

        JPanel p = new JPanel(new GridLayout(0, 6));
        p.setBackground(GuiUtils.bgColor);
        ButtonGroup group = new ButtonGroup();
        for (ArrayList<Component> chComponents : allChComponents_) {
            for (int i = 0; i < chComponents.size(); i++) {
                arrayPanels.get(i).add(chComponents.get(i));
                if (i == chComponents.size() - 1) {
                    group.add((JRadioButton) chComponents.get(i));
                }
            }
        }
        for (JPanel jp : arrayPanels) {
            p.add(jp);
        }
        return p;
    }

    private ArrayList<ArrayList<Component>> generateAllChComponents(DriftCorrectionParameters parameters) {
        ArrayList<ArrayList<Component>> allChComponents = new ArrayList<>();
        for (String ch : DriftCorrectionParameters.getChArray(parameters)) {
            allChComponents.add(generateComposOfCh(ch, parameters));
        }
        return allChComponents;
    }

    private ArrayList<Component> generateComposOfCh(String ch, DriftCorrectionParameters parameters) {
        ArrayList<Component> components = new ArrayList<>();
        JLabel chLabel = new JLabel(ch);
        JFormattedTextField maxDotTf = new JFormattedTextField(Integer.class);
        maxDotTf.setText(parameters.getChMaxNbSpot(ch));
        JFormattedTextField radiusTf = new JFormattedTextField(Double.class);
        radiusTf.setText(parameters.getChSpotRaius(ch));
        JFormattedTextField qualityTf = new JFormattedTextField(Double.class);
        qualityTf.setText(parameters.getChQuality(ch));
        JButton previewBut = new JButton("Preview");
//        previewBut.addActionListener(actionEvent -> {
//            double radius = Double.valueOf(radiusTf.getText());
//            double quality = Double.valueOf(qualityTf.getText());
//            MaarsTrackmate.executeTrackmate(IJ.getImage(), radius, quality);
//        });
        JRadioButton radioBut = new JRadioButton();
        radioBut.setSelected(parameters.getDetectionChForMitosis().equals(ch));
        components.add(chLabel);
        components.add(maxDotTf);
        components.add(radiusTf);
        components.add(qualityTf);
        components.add(previewBut);
        components.add(radioBut);

        return components;
    }

    private ArrayList<JPanel> generateParamPanels(int chNb) {
        ArrayList<JPanel> array = new ArrayList<>();

        JPanel chNamesP = new JPanel(new GridLayout(chNb, 0));
        chNamesP.setBackground(GuiUtils.bgColor);
        chNamesP.setBorder(GuiUtils.addSecondaryTitle("Channel Names"));
        JPanel maxDotP = new JPanel(new GridLayout(chNb, 0));
        maxDotP.setBackground(GuiUtils.bgColor);
        maxDotP.setBorder(GuiUtils.addSecondaryTitle("Max # of dot"));
        JPanel dotRadiusP = new JPanel(new GridLayout(chNb, 0));
        dotRadiusP.setBackground(GuiUtils.bgColor);
        dotRadiusP.setBorder(GuiUtils.addSecondaryTitle("Dot Radius"));
        JPanel qualityP = new JPanel(new GridLayout(chNb, 0));
        qualityP.setBackground(GuiUtils.bgColor);
        qualityP.setBorder(GuiUtils.addSecondaryTitle("Quality"));
        JPanel previewP = new JPanel(new GridLayout(chNb, 0));
        previewP.setBackground(GuiUtils.bgColor);
        previewP.setBorder(GuiUtils.addSecondaryTitle("Preview detection"));
        JPanel spindleP = new JPanel(new GridLayout(chNb, 0));
        spindleP.setBackground(GuiUtils.bgColor);
        spindleP.setBorder(GuiUtils.addSecondaryTitle("Spindle ?"));

        array.add(chNamesP);
        array.add(maxDotP);
        array.add(dotRadiusP);
        array.add(qualityP);
        array.add(previewP);
        array.add(spindleP);
        return array;
    }

    private JPanel generateChbxPanel(DriftCorrectionParameters parameters) {
        JPanel chbxPanel = new JPanel(new GridLayout(1, 0));
        chbxPanel.setBackground(GuiUtils.bgColor);
        chbxPanel.add(doAnaChx_);
        doAnaChx_.setSelected(Boolean.valueOf(parameters.getFluoParameter(DriftCorrectionParameters.DO_ANALYSIS)));
        projected_ = new JCheckBox("Project cropped images?",
                Boolean.parseBoolean(parameters.getFluoParameter(DriftCorrectionParameters.PROJECTED)));
        chbxPanel.add(projected_);
        JPanel mitosisDurationPanel = new JPanel(new GridLayout(1, 1));
        mitosisDurationPanel.setBackground(GuiUtils.bgColor);
        mitosisDurationPanel.setBorder(GuiUtils.addSecondaryTitle("Minimum mitosis duration (s)"));
        mitosisDurationTf_ = new JFormattedTextField(Integer.class);
        mitosisDurationTf_.setText(parameters.getMinimumMitosisDuration());
        mitosisDurationPanel.add(mitosisDurationTf_);
        chbxPanel.add(mitosisDurationPanel);
        return chbxPanel;
    }

    private JPanel generateSummaryPanel(int timePointsNb, int slicePerFrame, int nbChannel) {
        JPanel summaryPanel = new JPanel();
        summaryPanel.setBackground(GuiUtils.bgColor);
        summaryPanel.setBorder(GuiUtils.addSecondaryTitle("Acquisition summary"));
        String lineSep = "<br>";
        JLabel summaryLabel = new JLabel();
        summaryLabel.setVerticalTextPosition(JLabel.CENTER);
        summaryLabel.setText(
                "<html><body>Nb of time points: " + String.valueOf(timePointsNb) + lineSep +
                        "Nb of slices: " + String.valueOf(slicePerFrame) + lineSep +
                        "Nb of channels: " + String.valueOf(nbChannel) + lineSep);
        summaryPanel.add(summaryLabel);
        return summaryPanel;
    }

    public DriftCorrectionParameters getParameters() {
        return parameters_;
    }

    private void setUpDialog(Dimension dim) {
        setModalityType(ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("MAARS - Fluorescence Analysis Parameters");
        setLayout(new BorderLayout());
        setMinimumSize(dim);
        setSize(750, 500);
        setBackground(GuiUtils.bgColor);
    }
    
    
    
}
