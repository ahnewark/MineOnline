package gg.codie.mineonline.gui;

import gg.codie.mineonline.Session;
import com.johnymuffin.LegacyTrackerServer;
import com.johnymuffin.LegacyTrackerServerRepository;
import gg.codie.mineonline.client.LegacyGameManager;
import gg.codie.mineonline.gui.rendering.Font;
import gg.codie.mineonline.gui.rendering.Renderer;
import gg.codie.mineonline.patches.SocketConstructAdvice;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class PlayerList
{
    static ArrayList<String> players;
    static int maxPlayers;
    boolean lastRequestDone = true;
    static long lastRequest = 0;
    private static InetAddress lastIp;
    private static int lastPort;

    public PlayerList() {
        requestPlayers();
    }

    public void requestPlayers() {
        if(lastRequestDone && lastRequest < System.currentTimeMillis() - 10000 && SocketConstructAdvice.serverAddress != null && LegacyGameManager.getVersion() != null && LegacyGameManager.getVersion().usePlayerList) {
            lastRequestDone = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InetAddress currentIp = SocketConstructAdvice.serverAddress;
                        int currentPort = SocketConstructAdvice.serverPort;

                        LegacyTrackerServer legacyTrackerServer = new LegacyTrackerServerRepository().getServer(SocketConstructAdvice.serverAddress.getHostAddress(), "" + SocketConstructAdvice.serverPort);

                        if (currentIp != SocketConstructAdvice.serverAddress || currentPort != SocketConstructAdvice.serverPort) {
                            lastRequestDone = true;
                            lastRequest = 0;
                            return;
                        }

                        players = new ArrayList<>(Arrays.asList(legacyTrackerServer.players));
                        if (!players.contains(Session.session.getUsername()))
                            players.add(Session.session.getUsername());

                        if (legacyTrackerServer.dontListPlayers)
                            players = null;

                        maxPlayers = legacyTrackerServer.maxUsers;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        // ignore.
                    }
                    lastRequestDone = true;
                    lastRequest = System.currentTimeMillis();
                }
            }).start();
        }
    }

    public boolean hasPlayers()
    {
        if (lastIp != SocketConstructAdvice.serverAddress || lastPort != SocketConstructAdvice.serverPort) {
            lastIp = SocketConstructAdvice.serverAddress;
            lastPort = SocketConstructAdvice.serverPort;
            players = null;
            lastRequest = 0;
        }

        if (LegacyGameManager.getVersion() == null || !LegacyGameManager.getVersion().usePlayerList)
            return false;

        requestPlayers();

        return SocketConstructAdvice.serverAddress != null && players != null;
    }

    public void drawScreen()
    {
        if (lastIp != SocketConstructAdvice.serverAddress || lastPort != SocketConstructAdvice.serverPort) {
            lastIp = SocketConstructAdvice.serverAddress;
            lastPort = SocketConstructAdvice.serverPort;
            players = null;
            lastRequest = 0;
        }

        if (!Display.isActive())
            return;

        if (!Mouse.isGrabbed())
            return;

        if (LegacyGameManager.getVersion() == null || !LegacyGameManager.getVersion().usePlayerList)
            return;

        requestPlayers();

        if((SocketConstructAdvice.serverAddress != null) && players != null)
        {
            int playerCount = players.size(); // used to be max players, looks better like this though.
            int rows = playerCount;
            int cols = 1;
            for(; rows > 20; rows = ((playerCount + cols) - 1) / cols)
            {
                cols++;
            }

            int nameWidth = 300 / cols;
            if(nameWidth > 150)
            {
                nameWidth = 150;
            }
            int x = (GUIScale.lastScaledWidth() - cols * nameWidth) / 2;
            int y = 10;
            Renderer.singleton.drawRect(x - 1, y - 1, x + nameWidth * cols, y + 9 * rows, 0x80000000);
            for(int playerIndex = 0; playerIndex < playerCount; playerIndex++)
            {
                int nameX = x + (playerIndex % cols) * nameWidth;
                int nameY = y + (playerIndex / cols) * 9;
                Renderer.singleton.drawRect(nameX, nameY, (nameX + nameWidth) - 1, nameY + 8, 0x20ffffff);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                if(playerIndex >= players.size())
                {
                    continue;
                }
                Font.minecraftFont.drawStringWithShadow(players.get(playerIndex), nameX, nameY, 0xffffff);
            }

        }
    }
}
