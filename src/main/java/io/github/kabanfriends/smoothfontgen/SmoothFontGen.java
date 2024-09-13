package io.github.kabanfriends.smoothfontgen;

import java.io.File;

public class SmoothFontGen {

    public static void main(String[] args) {
        Logger.getInstance().info("BESmoothFontGen by KabanFriends\n");

        File msdfgenDir = new File("msdfgen");
        if (!msdfgenDir.exists()) {
            msdfgenDir.mkdir();
        }

        boolean isWindows = System.getProperty("os.name").startsWith("Windows");
        File msdfgenBin = new File(isWindows ? "msdfgen/msdfgen.exe" : "msdfgen/msdfgen");
        if (!msdfgenBin.exists() || !msdfgenBin.isFile()) {
            Logger.getInstance().error("msdfgen binary was not found. Place the msdfgen binary in the msdfgen directory.");
            return;
        }

        File outDir = new File("msdfgen/out");
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        File configFile = new File("config.json");
        if (!configFile.exists() || !configFile.isFile()) {
            Logger.getInstance().error("config.json was not found.");
            return;
        }

        File fontsDir = new File("fonts");
        if (!fontsDir.exists()) {
            fontsDir.mkdir();
        }

        File smoothDir = new File("smooth");
        if (!smoothDir.exists()) {
            smoothDir.mkdir();
        }

        try {
            SmoothGenerator generator = new SmoothGenerator();
            generator.start();
        } catch (Exception e) {
            Logger.getInstance().error("Unhandled exception", e);
        }
    }
}
