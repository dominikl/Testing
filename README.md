# Performance testing OMERO tile loading

Set parameteres via environment variables:

```
USERNAME (default: "public")
PASSWORD (default: "public")
HOST (default: "localhost")
PORT (default: "4064")
THREADS (default: "5")
SCREEN (default: null)  <- required
WIDTH (default: "512")
HEIGHT (default: "512")
TEST (default: "RoundtripTest")
IMAGE_COUNT (default: "100")
```

For example, to run with custom settings:

```
WIDTH=1280 HEIGHT=1024 TEST=LoadTileTest SCREEN=2001 java -jar Testing.jar
```

