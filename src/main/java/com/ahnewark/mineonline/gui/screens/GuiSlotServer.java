package com.ahnewark.mineonline.gui.screens;

import com.ahnewark.mineonline.MinecraftVersion;
import com.ahnewark.mineonline.MinecraftVersionRepository;
import com.ahnewark.mineonline.api.SavedMinecraftServer;
import com.ahnewark.mineonline.server.ThreadPollServers;
import com.johnymuffin.LegacyTrackerServer;
import com.ahnewark.mineonline.gui.rendering.Font;
import com.ahnewark.mineonline.gui.rendering.Loader;
import com.ahnewark.mineonline.gui.rendering.Renderer;
import com.ahnewark.mineonline.gui.textures.EGUITexture;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

public class GuiSlotServer extends GuiSlot
{

    public GuiSlotServer(GuiMultiplayer guimultiplayer)
    {
        super(guimultiplayer.getWidth(), guimultiplayer.getHeight(), 32, guimultiplayer.getHeight() - 55, 36, 304);
        guiMultiplayer = guimultiplayer;
    }

    protected int getSize()
    {
        if (MinecraftVersionRepository.getSingleton().isLoadingInstalledVersions())
            return 0;

        return guiMultiplayer != null ? guiMultiplayer.getSavedServers().size() + guiMultiplayer.getListedServers().size() + 1 : 1;
    }

    protected void elementClicked(int slotIndex, boolean doubleClicked)
    {
        if (slotIndex != guiMultiplayer.getSavedServers().size())
            guiMultiplayer.select(slotIndex);
        boolean flag1 = guiMultiplayer.getSelectedIndex() >= 0 && guiMultiplayer.getSelectedIndex() < getSize();
        guiMultiplayer.getConnectButton().enabled = flag1;
        if(doubleClicked && flag1)
        {
            guiMultiplayer.joinServer(slotIndex);
        }
    }

    protected boolean isSelected(int slotIndex)
    {
        return guiMultiplayer != null ? slotIndex == guiMultiplayer.getSelectedIndex() : false;
    }

    protected int getContentHeight()
    {
        return getSize() * 36;
    }

    protected void drawBackground()
    {
        guiMultiplayer.drawDefaultBackground();
    }

    @Override
    public void drawScreen(int mousex, int mousey) {
        super.drawScreen(mousex, mousey);

        if (MinecraftVersionRepository.getSingleton().isLoadingInstalledVersions())
            Font.minecraftFont.drawCenteredStringWithShadow("Loading versions...", guiMultiplayer.getWidth() / 2, guiMultiplayer.getHeight() / 2, 0x808080);
    }

    protected void drawSlot(int slotIndex, int xPos, int yPos, int zPos)
    {
        resize(guiMultiplayer.getWidth(), guiMultiplayer.getHeight(), 32, (guiMultiplayer.getHeight() - 55));

        if (slotIndex == guiMultiplayer.getSavedServers().size()) {
            Font.minecraftFont.drawCenteredString("Searching for games listed online...", guiMultiplayer.getWidth() / 2, yPos + 12, 0xffffff);
            return;
        } else if (slotIndex < guiMultiplayer.getSavedServers().size()) {
            SavedMinecraftServer server = guiMultiplayer.getSavedServers().get(slotIndex);

            MinecraftVersion version = MinecraftVersionRepository.getSingleton().getVersionByMD5(server.clientMD5);
            String versionName = "Unknown Version";
            if (version != null) {
                versionName = version.name;
            }

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, Loader.singleton.getGuiTexture(EGUITexture.UNKNOWN_PACK));

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Renderer.singleton.startDrawingQuads();
            Renderer.singleton.setColorRGBA(255, 255, 255, 255);
            Renderer.singleton.addVertexWithUV(xPos, yPos + zPos, 0.0D, 0.0D, 1.0D);
            Renderer.singleton.addVertexWithUV(xPos + 32, yPos + zPos, 0.0D, 1.0D, 1.0D);
            Renderer.singleton.addVertexWithUV(xPos + 32, yPos, 0.0D, 1.0D, 0.0D);
            Renderer.singleton.addVertexWithUV(xPos, yPos, 0.0D, 0.0D, 0.0D);
            Renderer.singleton.draw();

            Font.minecraftFont.drawString(server.name, xPos + 32 + 2, yPos + 1, 0xffffff);
            Font.minecraftFont.drawString(versionName, xPos + 32 + 2, yPos + 12, 0x808080);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, Loader.singleton.getGuiTexture(EGUITexture.MINEONLINE_GUI_ICONS));
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

            int connectionIconTypeIndex;
            int connectionIconIndex;
            String tooltipText;
            Long latency;
            if (ThreadPollServers.serverLatencies.containsKey(server.address) && (latency = ThreadPollServers.serverLatencies.get(server.address)) != -2L) {
                connectionIconTypeIndex = 0;
                if (latency < 0L) {
                    connectionIconIndex = 5;
                } else if (latency < 150L) {
                    connectionIconIndex = 0;
                } else if (latency < 300L) {
                    connectionIconIndex = 1;
                } else if (latency < 600L) {
                    connectionIconIndex = 2;
                } else if (latency < 1000L) {
                    connectionIconIndex = 3;
                } else {
                    connectionIconIndex = 4;
                }
                if (latency < 0L) {
                    tooltipText = "(no connection)";
                } else {
                    tooltipText = (new StringBuilder()).append(latency).append("ms").toString();
                }
            } else {
                connectionIconTypeIndex = 1;
                connectionIconIndex = (int) (System.currentTimeMillis() / 100L + (long) (slotIndex * 2) & 7L);
                if (connectionIconIndex > 4) {
                    connectionIconIndex = 8 - connectionIconIndex;
                }
                tooltipText = "Polling..";
            }
            Renderer.singleton.drawSprite(xPos + slotWidth - 14, yPos, 0 + connectionIconTypeIndex * 10, 176 + connectionIconIndex * 8, 10, 8);
            byte byte0 = 4;
            if (mouseX >= (xPos + slotWidth - 14) - byte0 && mouseY >= yPos - byte0 && mouseX <= xPos + (slotWidth - 14) + 10 + byte0 && mouseY <= yPos + 8 + byte0) {
                guiMultiplayer.setTooltip(tooltipText);
            }
        } else {
            LegacyTrackerServer server = guiMultiplayer.getListedServers().get(slotIndex - guiMultiplayer.getSavedServers().size() - 1);

            String versionName = "Unknown Version";
            List<MinecraftVersion> versions = MinecraftVersionRepository.getSingleton().getVersionsByBaseVersion(server.baseVersion);
            if (versions.size() > 0)
                versionName = versions.get(0).name;

            if (server.serverIcon != null) {
                BufferedImage image;
                byte[] imageByte;
                try {
                    Base64.Decoder decoder = Base64.getDecoder();
                    imageByte = decoder.decode(server.serverIcon);
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
                    image = ImageIO.read(bis);
                    bis.close();

                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", os);
                    InputStream is = new ByteArrayInputStream(os.toByteArray());

                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, Loader.singleton.loadTexture("/servers/" + server.ip + ":" + server.port + "/server-icon.png", is));
                } catch (Exception e) {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, Loader.singleton.getGuiTexture(EGUITexture.UNKNOWN_PACK));
                    e.printStackTrace();
                }
            } else {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, Loader.singleton.getGuiTexture(EGUITexture.UNKNOWN_PACK));
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Renderer.singleton.startDrawingQuads();
            Renderer.singleton.setColorRGBA(255, 255, 255, 255);
            Renderer.singleton.addVertexWithUV(xPos, yPos + zPos, 0.0D, 0.0D, 1.0D);
            Renderer.singleton.addVertexWithUV(xPos + 32, yPos + zPos, 0.0D, 1.0D, 1.0D);
            Renderer.singleton.addVertexWithUV(xPos + 32, yPos, 0.0D, 1.0D, 0.0D);
            Renderer.singleton.addVertexWithUV(xPos, yPos, 0.0D, 0.0D, 0.0D);
            Renderer.singleton.draw();

            Font.minecraftFont.drawString(server.name, xPos + 32 + 2, yPos + 1, 0xffffff);
            Font.minecraftFont.drawString(versionName, xPos + 32 + 2, yPos + 12, 0x808080);
            Font.minecraftFont.drawString(server.users + "§8/§7" + server.maxUsers, (xPos + slotWidth - 4) - Font.minecraftFont.width(server.users + "/" + server.maxUsers), yPos + 12, 0xAAAAAA);

            if (server.motd != null)
                Font.minecraftFont.drawString(server.motd, xPos + 32 + 2, yPos + 12 + 11, 0x808080);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, Loader.singleton.getGuiTexture(EGUITexture.MINEONLINE_GUI_ICONS));
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

            if (server.featured)
                Renderer.singleton.drawSprite(xPos + slotWidth - 29, yPos, 20, 184, 10, 8);
            else if (server.onlineMode)
                Renderer.singleton.drawSprite(xPos + slotWidth - 29, yPos, 20, 176, 10, 8);

            if (server.whitelisted)
                if (server.featured || server.onlineMode)
                    Renderer.singleton.drawSprite(xPos + slotWidth - 44, yPos, 20, 192, 10, 8);
                else
                    Renderer.singleton.drawSprite(xPos + slotWidth - 29, yPos, 20, 192, 10, 8);


            int connectionIconTypeIndex;
            int connectionIconIndex;
            String tooltipText;
            Long latency;
            if(ThreadPollServers.serverLatencies.containsKey(server.ip + ":" + server.port) && (latency = ThreadPollServers.serverLatencies.get(server.ip + ":" + server.port)) != -2L)
            {
                connectionIconTypeIndex = 0;
                if(latency < 0L)
                {
                    connectionIconIndex = 5;
                } else
                if(latency < 150L)
                {
                    connectionIconIndex = 0;
                } else
                if(latency< 300L)
                {
                    connectionIconIndex = 1;
                } else
                if(latency < 600L)
                {
                    connectionIconIndex = 2;
                } else
                if(latency < 1000L)
                {
                    connectionIconIndex = 3;
                } else
                {
                    connectionIconIndex = 4;
                }
                if(latency < 0L)
                {
                    tooltipText = "(no connection)";
                } else
                {
                    tooltipText = (new StringBuilder()).append(latency).append("ms").toString();
                }
            } else
            {
                connectionIconTypeIndex = 1;
                connectionIconIndex = (int)(System.currentTimeMillis() / 100L + (long)(slotIndex * 2) & 7L);
                if(connectionIconIndex > 4)
                {
                    connectionIconIndex = 8 - connectionIconIndex;
                }
                tooltipText = "Polling..";
            }
            Renderer.singleton.drawSprite(xPos + slotWidth - 14, yPos, 0 + connectionIconTypeIndex * 10, 176 + connectionIconIndex * 8, 10, 8);
            byte byte0 = 4;
            if (mouseX >= (xPos + slotWidth - 14) - byte0 && mouseY >= yPos - byte0 && mouseX <= xPos + (slotWidth - 14) + 10 + byte0 && mouseY <= yPos + 8 + byte0)
            {
                guiMultiplayer.setTooltip(tooltipText);
            }

            if (mouseX >= (xPos + slotWidth - 29) - byte0 && mouseY >= yPos - byte0 && mouseX <= xPos + (slotWidth - 29) + 10 + byte0 && mouseY <= yPos + 8 + byte0)
            {
                if (server.featured)
                    guiMultiplayer.setTooltip("Featured");
                else if (server.onlineMode)
                    guiMultiplayer.setTooltip("Online Mode");
                else if (server.whitelisted)
                    guiMultiplayer.setTooltip("Whitelisted");
            }

            if (mouseX >= (xPos + slotWidth - 44) - byte0 && mouseY >= yPos - byte0 && mouseX <= xPos + (slotWidth - 44) + 10 + byte0 && mouseY <= yPos + 8 + byte0)
            {
                if (server.whitelisted && (server.onlineMode || server.featured))
                    guiMultiplayer.setTooltip("Whitelisted");
            }
        }
    }

    final GuiMultiplayer guiMultiplayer;
}
