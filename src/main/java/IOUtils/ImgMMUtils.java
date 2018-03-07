//package IOUtils;
//
//import org.micromanager.internal.MMStudio;
//
//public class ImgMMUtils {
//    public static Boolean IsOkToProceed() {
//        boolean isOk = true;
//        MMStudio mm = MMStudio.getInstance();
//        if (mm.live().getIsLiveModeOn()){
//            mm.logs().showError("MAARS segmentation is not designed for live streaming, please launch MDA acquisition.");
//            mm.live().setLiveMode(false);
//            isOk = false;
//        }else if (!mm.acquisitions().getAcquisitionSettings().save){
//            mm.logs().showError("You need to save your images to perform segmentation.");
//            mm.acquisitions().setPause(true);
//            mm.acquisitions().haltAcquisition();
//            mm.getAcquisitionEngine2010().stop();
//            isOk = false;
//        }
//        return isOk;
//    }
//}
