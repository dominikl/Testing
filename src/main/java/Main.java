import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.LoadFacility;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.WellData;
import omero.gateway.model.WellSampleData;
import omero.log.SimpleLogger;

public class Main {
    public static void main(String[] args) {
        try {
            String username = getEnv("USERNAME", "public");
            String password = getEnv("PASSWORD", "public");
            String host = getEnv("HOST", "localhost");
            String port = getEnv("PORT", "4064");
            String threads = getEnv("THREADS", "5");
            String screenID = getEnv("SCREEN", null); // idr0035 = 2001 , idr0035_2 = 3551
            String width = getEnv("WIDTH", "512");
            String height = getEnv("HEIGHT", "512");
            String test = getEnv("TEST", "RoundtripTest");
            String imageCount = getEnv("IMAGE_COUNT", "100");
            if (screenID == null) {
                System.err.println("Screen ID is required (e.g. use 2001 for idr0035)");
                return;
            }
            System.out.println("Parameters:");
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
            System.out.println("Host: " + host);
            System.out.println("Port: " + port);
            System.out.println("Password: " + password);
            System.out.println("Host: " + host);
            System.out.println("Port: " + port);
            System.out.println("Threads: " + threads);
            System.out.println("Screen: " + screenID);
            System.out.println("Width: " + width);
            System.out.println("Height: " + height);
            System.out.println("Test: " + test);
            System.out.println("Image Count: " + imageCount);

            Gateway gateway = new Gateway(new SimpleLogger());
            LoginCredentials credentials = new LoginCredentials(username, password, host, Integer.parseInt(port));
            ExperimenterData user = gateway.connect(credentials);
            SecurityContext ctx = new SecurityContext(user.getGroupId());
            LoadFacility load = gateway.getFacility(LoadFacility.class);
            ScreenData screen = load.getScreen(ctx, Long.parseLong(screenID));
            Set<ImageData> images = getRandomImages(load, ctx, screen, Integer.parseInt(imageCount));
            System.out.println("Screen: "+screen.getName());
            runTests(test, gateway, ctx, images, Integer.parseInt(threads), Integer.parseInt(width), Integer.parseInt(height));
            gateway.disconnect();

        } catch (Exception e) {
            System.err.println("Error connecting to OMERO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runTests(String test, Gateway gateway, SecurityContext ctx, Set<ImageData> images, int threads, int width, int height) throws Exception {
        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Long>> futures = new ArrayList<>();
        for (ImageData img : images) {
            Class<Test> testClass = (Class<Test>)Class.forName(test);
            Test tester = testClass.getConstructor(Gateway.class, SecurityContext.class, ImageData.class, int.class, int.class).newInstance(gateway, ctx, img, width, height);
            Future<Long> future = executor.submit(tester);
            futures.add(future);
        }

        int isDone = 0;
        while (isDone < futures.size()) {
            isDone = 0;
            for (Future<Long> future : futures) {
                if (future.isDone()) {
                    isDone++;
                }
            }
            Thread.sleep(1000);
        }

        Long minTime = Long.MAX_VALUE;
        Long maxTime = 0L;
        int successCount = 0;
        Long totalTime = 0L;
        
        for (int i=0; i<futures.size(); i++) {
            try {
                Long result = futures.get(i).get(30, TimeUnit.SECONDS);
                if (result != null && result > 0) {
                    minTime = Math.min(minTime, result);
                    maxTime = Math.max(maxTime, result);
                    totalTime += result;
                    successCount++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            System.err.println("Some tasks did not complete within timeout");
            executor.shutdownNow();
        }

        double avgTime = totalTime / successCount;

        System.out.printf("Fastest Time:        %d ms%n", minTime);
        System.out.printf("Slowest Time:        %d ms%n", maxTime);
        System.out.printf("Average Time:        %.1f ms%n", avgTime);
        System.out.printf("Total Time:          %d ms%n", totalTime);
        
        double duration = (System.currentTimeMillis() - start) / 1000.0;
        System.out.printf("Test Duration:       %.1f s%n", duration);
    }

    private static Set<ImageData> getRandomImages(LoadFacility load, SecurityContext ctx, ScreenData screen, int count) {
        Set<ImageData> images = new HashSet<>();
        Set<Long> imageIds = new HashSet<>();
        List<PlateData> plates = new ArrayList<>(screen.getPlates());
        try {
            while (images.size() < count) {
                PlateData plate = plates.get(new Random().nextInt(plates.size()));
                plate = load.getPlate(ctx, plate.getId());
                List<WellData> wells = new ArrayList<>(plate.getWells());
                WellData well = wells.get(new Random().nextInt(wells.size()));
                well = load.getWell(ctx, well.getId());
                List<WellSampleData> wss = well.getWellSamples();
                if (wss.isEmpty()) {
                    continue;
                }
                ImageData img = load.getImage(ctx, wss.get(new Random().nextInt(wss.size())).getImage().getId());
                if (!imageIds.contains(img.getId())) {
                    imageIds.add(img.getId());
                    images.add(img); 
                }
            }   
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    static String getEnv(String name, String defaultValue) {
        String ret = System.getenv(name);
        if (ret == null || ret.trim().isEmpty()) {
            ret = defaultValue;
        }
        return ret;
    }
}
