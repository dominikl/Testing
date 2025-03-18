import java.util.concurrent.Callable;

import org.perf4j.StopWatch;

import omero.ServerError;
import omero.api.RawPixelsStorePrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ImageData;

/**
 * Runnable which measures the time it takes to get a tile from an image.
 * Includes opening/closing PixelsStore.
 */
public class RoundtripTest extends Test {

    public RoundtripTest(Gateway gw, SecurityContext ctx, ImageData img, int width, int height) throws ServerError, DSOutOfServiceException {
        super(gw, ctx, img, width, height);
    }

    @Override
    public Long call() throws Exception { 
        try {
            StopWatch sw = new StopWatch("RoundtripTest");
            sw.start();
            RawPixelsStorePrx ps = gateway.getPixelsStore(ctx);
            ps.setPixelsId(img.getDefaultPixels().getId(), true);
            byte[] pixels = ps.getTile(0, 0, 0, 0, 0, width, height);
            ps.close();
            sw.stop();
            return sw.getElapsedTime();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
