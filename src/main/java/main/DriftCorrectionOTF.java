package main;

import IOUtils.ImgMMUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import org.micromanager.acquisition.ChannelSpec;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.data.Image;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorContext;
import org.micromanager.internal.MMStudio;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DriftCorrectionOTF extends Processor {
    private DriftCorrectionParameters parameters;
    private MMStudio mmStudio = MMStudio.getInstance();
    private Calibration cal = new Calibration();
    private HashMap<String, ArrayList<Image>> chZstack = new HashMap<>();
    private ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    DriftCorrectionOTF(DriftCorrectionParameters parameters1) {
        parameters = parameters1;
        SequenceSettings seqSetting = mmStudio.getAcquisitionManager().getAcquisitionSettings();
        for (ChannelSpec chspc : seqSetting.channels) {
            if (chspc.useChannel) {
                chZstack.put(chspc.config, new ArrayList<>());
            }
        }
        cal.pixelDepth = Math.abs(seqSetting.slices.get(0) - seqSetting.slices.get(1));
        cal.pixelWidth = mmStudio.getCore().getPixelSizeUm();
        cal.pixelHeight = mmStudio.getCore().getPixelSizeUm();
        cal.frameInterval = seqSetting.intervalMs;
    }

    @Override
    public void processImage(Image image, ProcessorContext processorContext) {
//        boolean isOk = ImgMMUtils.IsOkToProceed();
//        String currentCh = null;
//        String currentPos = mmStudio.getPositionList().getPosition(image.getCoords().getStagePosition()).getLabel();
//        try {
//            currentCh = mmStudio.getCMMCore().getCurrentConfig("Channel");
//        } catch (Exception e) {
//            IJ.error("Your group name of channels is unknown.");
//        }
//        ArrayList<Image> currentChImgs = chZstacks.get(currentCh);
//        currentChImgs.add(image);
//        processorContext.outputImage(image);
//
//        if (currentChImgs.size() == mmStudio.acquisitions().getAcquisitionSettings().slices.size() && isOk){
//            chZstacks.get(currentCh).clear();
//            ImagePlus imp = ImgMMUtils.convertWithMetadata(currentChImgs, mmStudio.getCachedPixelSizeUm());
//            try {
//                es_.submit(new FluoAnalyzer(zProjectedFluoImg, cal_,
//                        posSoc_.get(currentPos), currentCh, Integer.parseInt(parameters.getChMaxNbSpot(currentCh)),
//                        Double.parseDouble(parameters.getChSpotRaius(currentCh)),
//                        Double.parseDouble(parameters.getChQuality(currentCh)), image.getCoords().getTime(),
//                        null, parameters.useDynamic())).get();
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
//            es_.shutdown();
//        }
    }
}
