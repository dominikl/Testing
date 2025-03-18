import java.util.concurrent.Callable;

import org.perf4j.StopWatch;

import omero.ServerError;
import omero.api.RawPixelsStorePrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ImageData;

/**
 * Runnable which measures the time it takes to load a tile from an image.
 * Does not include opening/closing PixelsStore.
 */
public class LoadTileTest extends Test {

    public LoadTileTest(Gateway gw, SecurityContext ctx, ImageData img, int width, int height) throws ServerError, DSOutOfServiceException {
        super(gw, ctx, img, width, height);
    }

    @Override
    public Long call() throws Exception { 
        try {
            StopWatch sw = new StopWatch("LoadTileTest");
            RawPixelsStorePrx ps = gateway.getPixelsStore(ctx);
            ps.setPixelsId(img.getDefaultPixels().getId(), true);
            sw.start();
            byte[] pixels = ps.getTile(0, 0, 0, 0, 0, width, height);
            sw.stop();
            ps.close();
            return sw.getElapsedTime();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
