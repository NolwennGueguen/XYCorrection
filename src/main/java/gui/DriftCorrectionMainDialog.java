//package gui;
//
//import ij.IJ;
//import main.DriftCorrection;
//import org.micromanager.internal.MMStudio;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//public class DriftCorrectionMainDialog extends JFrame implements ActionListener {
//
//    //Declaration of all GUI Objects
//    public static JButton okButton;
//    private final MMStudio mm_;
//    private JFormattedTextField pathToImage1Tf_;
//    private JFormattedTextField pathToImage2Tf_;
//    private String pathToImage1;
//    private String pathToImage2;
//
//    public DriftCorrectionMainDialog(MMStudio mmStudio) {
//        mm_ = mmStudio;
//
//        IJ.log("create main dialog ...");
//        setDefaultLookAndFeelDecorated(true);
//        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//
//        // set minimal dimension of mainDialog
//        int maxDialogWidth = 150;
//        int maxDialogHeight = 150;
//        Dimension minimumSize = new Dimension(maxDialogWidth, maxDialogHeight);
//        setMinimumSize(minimumSize);
//
//        JPanel pathToImage1Panel = new JPanel(new GridLayout(1,0));
//        pathToImage1Panel.setBorder(BorderFactory.createTitledBorder("Path to reference image"));
//
//        pathToImage1Tf_ = new JFormattedTextField(String.class);
//        pathToImage1Panel.add(pathToImage1Tf_);
//        pathToImage1 = pathToImage1Tf_.getText();
//
//        JPanel pathToImage2Panel = new JPanel(new GridLayout(1,0));
//        pathToImage1Panel.setBorder(BorderFactory.createTitledBorder("Path to reference image"));
//
//        pathToImage2Tf_ = new JFormattedTextField(String.class);
//        pathToImage2Panel.add(pathToImage2Tf_);
//        pathToImage2 = pathToImage2Tf_.getText();
//
//
//        //Button to run analysis
//        JPanel okPanel = new JPanel(new GridLayout(1, 0));
//        okButton = new JButton("Get the drift matrix!");
//        okButton.addActionListener(this);
//        okPanel.add(okButton);
//
//        //Add all components to a JFrame
//        JPanel mainPanel = new JPanel();
//        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//        mainPanel.add(pathToImage1Panel);
//        mainPanel.add(pathToImage2Panel);
//        mainPanel.add(okPanel);
//        add(mainPanel);
//        pack();
//        setVisible(true);
//    }
//
//    @Override
//    public void actionPerformed(ActionEvent actionEvent) {
////        if (actionEvent.getSource() == okButton) {
////            if (pathToImage1 != null && pathToImage2 != null) {
////                DriftCorrection.driftCorrection(pathToImage1, pathToImage2);
////            }
////        }
////        else {
////            IJ.log("One or both path are missing, please complete them before to continue.");
////        }
//    }
//
//}
