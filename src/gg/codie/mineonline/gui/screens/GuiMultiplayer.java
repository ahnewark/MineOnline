package gg.codie.mineonline.gui.screens;

import gg.codie.minecraft.api.LauncherAPI;
import gg.codie.mineonline.*;
import gg.codie.mineonline.api.MineOnlineAPI;
import gg.codie.mineonline.api.MineOnlineServer;
import gg.codie.mineonline.api.MineOnlineServerRepository;
import gg.codie.mineonline.client.LegacyGameManager;
import gg.codie.mineonline.server.ThreadPollServers;
import gg.codie.mineonline.gui.MenuManager;
import gg.codie.mineonline.gui.components.GuiButton;
import gg.codie.mineonline.gui.rendering.DisplayManager;
import gg.codie.mineonline.gui.rendering.FontRenderer;
import gg.codie.mineonline.utils.JREUtils;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GuiMultiplayer extends AbstractGuiScreen
{
    public GuiMultiplayer(AbstractGuiScreen guiscreen)
    {
        selectedIndex = -1;
        tooltip = null;
        parentScreen = guiscreen;

        serverRepository.onGotServers(gotServersListener);
        serverRepository.loadServers();

        controlList.clear();
        guiSlotServer = new GuiSlotServer(this);
    }

    static MineOnlineServerRepository.GotServersListener gotServersListener = new MineOnlineServerRepository.GotServersListener() {
        @Override
        public void GotServers(LinkedList<MineOnlineServer> servers) {
            for(MineOnlineServer server : servers) {
                if(!ThreadPollServers.serverLatencies.containsKey(server.connectAddress + ":" + server.port))
                    ThreadPollServers.pollServer(server);
            }
        }
    };

    public void initGui()
    {
        controlList.clear();

        controlList.add(connectButton = new GuiButton(1, getWidth() / 2 - 154, getHeight() - 48, 100, 20, "Join Server", new GuiButton.GuiButtonListener() {
            @Override
            public void OnButtonPress() {
                joinServer(serverRepository.getServers().get(selectedIndex));
            }
        }));
        AbstractGuiScreen thisScreen = this;
        controlList.add(new GuiButton(4, getWidth() / 2 - 50, getHeight() - 48, 100, 20, "Direct Connect", new GuiButton.GuiButtonListener() {
            @Override
            public void OnButtonPress() {
                if (LegacyGameManager.isInGame())
                    LegacyGameManager.setGUIScreen(new GuiDirectConnect(thisScreen));
                else
                    MenuManager.setMenuScreen(new GuiDirectConnect(thisScreen));
            }
        }));
        controlList.add(new GuiButton(3, getWidth() / 2 + 4 + 50, getHeight() - 48, 100, 20, "Cancel", new GuiButton.GuiButtonListener() {
            @Override
            public void OnButtonPress() {
                if (LegacyGameManager.isInGame())
                    LegacyGameManager.setGUIScreen(parentScreen);
                else
                    MenuManager.setMenuScreen(parentScreen);            }
        }));
        connectButton.enabled = selectedIndex >= 0 && selectedIndex < guiSlotServer.getSize();
    }

    public void onGuiClosed()
    {
        serverRepository.offGotServers(gotServersListener);
    }

    protected void keyTyped(char c, int i)
    {
        if(c == '\r')
        {
            actionPerformed((GuiButton)controlList.get(2));
        }
    }

    protected void mouseClicked(int i, int j, int k)
    {
        super.mouseClicked(i, j, k);
    }

    public void drawScreen(int i, int j)
    {
        initGui();

        tooltip = null;
        drawDefaultBackground();
        guiSlotServer.drawScreen(i, j);
        drawCenteredString("Play Multiplayer", getWidth() / 2, 20, 0xffffff);
        super.drawScreen(i, j);
        if(tooltip != null)
        {
            renderTooltip(tooltip, i, j);
        }
    }

    public void joinServer(int i)
    {
        joinServer(serverRepository.getServers().get(i));
    }

    private void joinServer(MineOnlineServer server)
    {
        MinecraftVersion serverVersion = MinecraftVersionRepository.getSingleton().getVersionByMD5(server.md5);

        Set<String> minecraftJars = MinecraftVersionRepository.getSingleton().getInstalledJars().keySet();

        String clientPath = null;

        if (serverVersion != null) {
            clientloop:
            for (String compatibleClientBaseVersion : serverVersion.clientVersions) {
                for (String path : minecraftJars) {
                    MinecraftVersion clientVersion = MinecraftVersionRepository.getSingleton().getInstalledJars().get(path);

                    if (clientVersion != null && clientVersion.baseVersion.equals(compatibleClientBaseVersion)) {
                        clientPath = path;
                        break clientloop;
                    }
                }

                try {
                    File clientJar = new File(LauncherFiles.MINECRAFT_VERSIONS_PATH + compatibleClientBaseVersion + File.separator + "client.jar");
                    try {
                        MineOnlineAPI.downloadVersion(compatibleClientBaseVersion);
                    } catch (Exception ex) {
                        // ignore
                    }
                    if (!clientJar.exists())
                        LauncherAPI.downloadVersion(compatibleClientBaseVersion);

                    MinecraftVersionRepository.getSingleton().addInstalledVersion(clientJar.getPath());
                    clientPath = clientJar.getPath();
                    break clientloop;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        try {
            String mppas = MineOnlineAPI.getMpPass(Session.session.getAccessToken(), Session.session.getUsername(), Session.session.getUuid(), server.ip, server.port + "");
            MinecraftVersion.launchMinecraft(clientPath, server.ip, server.port + "", mppas);

            if (LegacyGameManager.isInGame())
                LegacyGameManager.closeGame();
            else {
                Display.destroy();
                DisplayManager.getFrame().dispose();
                System.exit(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // ignore for now
        }
    }

    protected void renderTooltip(String s, int i, int j)
    {
        if(s == null)
        {
            return;
        } else
        {
            int k = i + 12;
            int l = j - 12;
            int i1 = FontRenderer.minecraftFontRenderer.getStringWidth(s);
            drawGradientRect(k - 3, l - 3, k + i1 + 3, l + 8 + 3, 0xc0000000, 0xc0000000);
            FontRenderer.minecraftFontRenderer.drawStringWithShadow(s, k, l, -1);
            return;
        }
    }

    public List<MineOnlineServer> getServers()
    {
        return serverRepository.getServers() != null ? serverRepository.getServers() : new LinkedList<>();
    }

    public int select(int i)
    {
        return selectedIndex = i;
    }

    public int getSelectedIndex()
    {
        return selectedIndex;
    }

    public GuiButton getConnectButton()
    {
        return connectButton;
    }

    public String setTooltip(String s)
    {
        return tooltip = s;
    }

    private AbstractGuiScreen parentScreen;
    private GuiSlotServer guiSlotServer;
    private int selectedIndex;
    private GuiButton connectButton;
    private String tooltip;
    private MineOnlineServerRepository serverRepository = new MineOnlineServerRepository();
}
