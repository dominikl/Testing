import java.util.concurrent.Callable;

import omero.ServerError;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ImageData;

/**
 * Runnable which measures the time it takes to get a tile from an image
 */
public abstract class Test implements Callable<Long> {

    Gateway gateway;
    SecurityContext ctx;
    ImageData img;
    int width;
    int height;

    public Test(Gateway gw, SecurityContext ctx, ImageData img, int width, int height) throws ServerError, DSOutOfServiceException {
        this.gateway = gw;
        this.ctx = ctx;
        this.img = img;
        this.width = width;
        this.height = height;
    }

    public abstract Long call() throws Exception;
}
