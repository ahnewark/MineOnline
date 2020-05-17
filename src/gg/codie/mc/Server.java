package gg.codie.mc;

import gg.codie.utils.ArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class Server {
    static String proxySet = "-DproxySet=true";
    static String proxyHost = "-Dhttp.proxyHost=127.0.0.1";
    static String proxyPortArgument = "-Dhttp.proxyPort=";

	public static void main(String[] args) throws Exception{
        Properties.loadProperties();
        int port = Proxy.launchProxy();

        if(args.length < 2) {
            System.err.println("Too few arguments. Include a jar location and main class. \n Eg minecraft-server.jar com.mojang.minecraft.server.MinecraftServer");
        }

        String[] CMD_ARRAY = Boolean.parseBoolean(Properties.properties.getProperty("useLocalProxy"))
                ? new String[] {Properties.properties.getProperty("javaCommand"), proxySet, proxyHost, proxyPortArgument + port}
                : new String[] {Properties.properties.getProperty("javaCommand")};

        CMD_ARRAY = ArrayUtils.concatenate(CMD_ARRAY, args);

        java.util.Properties props = System.getProperties();
        System.out.println("Launching Server: " + String.join(" ", CMD_ARRAY));
        ProcessBuilder processBuilder = new ProcessBuilder(CMD_ARRAY);
        Map<String, String> env = processBuilder.environment();
        for(String prop : props.stringPropertyNames()) {
            env.put(prop, props.getProperty(prop));
        }
        processBuilder.directory(new File(System.getProperty("user.dir")));
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectErrorStream(true);

        Process serverProcess = processBuilder.start();

        Thread closeLauncher = new Thread() {
            public void run() {
                serverProcess.destroy();
                Proxy.stopProxy();
            }
        };

        Runtime.getRuntime().addShutdownHook(closeLauncher);

        while(serverProcess.isAlive()) {

        }

        Proxy.stopProxy();
	}

}
