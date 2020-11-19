package gg.codie.mineonline.gui.rendering;

import gg.codie.mineonline.LauncherFiles;
import gg.codie.mineonline.Settings;
import gg.codie.mineonline.client.LegacyGameManager;
import gg.codie.mineonline.gui.rendering.textures.EGUITexture;
import gg.codie.mineonline.patches.HashMapPutAdvice;
import gg.codie.mineonline.patches.minecraft.HDTextureFXHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.opengl.*;
import org.newdawn.slick.opengl.renderer.Renderer;
import org.newdawn.slick.opengl.renderer.SGL;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Loader {

    private List<Integer> vaos = new ArrayList<Integer>();
    private List<Integer> vbos = new ArrayList<Integer>();
    private HashMap<String, Integer> textures = new HashMap<>();

    public final int MISSING_TEXTURE_ID;

    public static Loader singleton;

    public Loader() {
        MISSING_TEXTURE_ID = loadTexture("missingno.", LauncherFiles.MISSING_TEXTURE);
        singleton = this;
    }

    public int loadToVAO(float[] positions, float[] textureCoords) {
        int vaoID = createVAO();
        storeDataInAttributeList(0, 2, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        unbindVAO();
        return vaoID;
    }

    public void unloadTexture(String name) {
        if (textures.containsKey(name)) {
            GL11.glDeleteTextures(textures.get(name));
            textures.remove(name);
        }
    }

    public int loadTexture(String name, URL url) {
        if (textures.containsKey(name))
            return textures.get(name);

        Texture texture;
        try {
            texture = TextureLoader.getTexture("PNG", url.openStream());
        } catch (Exception e) {
            return MISSING_TEXTURE_ID;
        }

        int textureID = texture.getTextureID();

        textures.put(name, textureID);

        return textureID;
    }

    public int loadTexture(String name, String path) {
        if (textures.containsKey(name))
            return textures.get(name);

        Texture texture;
        try {
            if(path.startsWith("http")) {
                texture = TextureLoader.getTexture("PNG", new URL(path).openStream());
            } else {
                texture = TextureLoader.getTexture("PNG", new FileInputStream(path));
            }
        } catch (Exception e) {
            return MISSING_TEXTURE_ID;
        }

        int textureID = texture.getTextureID();

        textures.put(name, textureID);

        return textureID;
    }

    public int loadTexture(String name, InputStream stream) {
        if (textures.containsKey(name))
            return textures.get(name);

        Texture texture;
        try {
            texture = TextureLoader.getTexture("PNG", stream);
        } catch (Exception e) {
            return MISSING_TEXTURE_ID;
        }

        int textureID = texture.getTextureID();

//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
//        GL11.glTexParameteri(3553 /*GL_TEXTURE_2D*/, 10241 /*GL_TEXTURE_MIN_FILTER*/, 9728 /*GL_NEAREST*/);
//        GL11.glTexParameteri(3553 /*GL_TEXTURE_2D*/, 10240 /*GL_TEXTURE_MAG_FILTER*/, 9728 /*GL_NEAREST*/);
//        GL11.glTexParameteri(3553 /*GL_TEXTURE_2D*/, 10242 /*GL_TEXTURE_WRAP_S*/, 10496 /*GL_CLAMP*/);
//        GL11.glTexParameteri(3553 /*GL_TEXTURE_2D*/, 10243 /*GL_TEXTURE_WRAP_T*/, 10496 /*GL_CLAMP*/);

        textures.put(name, textureID);

        return textureID;
    }

    public void overwriteTexture(int textureID, InputStream in, String resourceName) throws IOException {
        SGL GL = Renderer.get();
        LoadableImageData imageData = ImageDataFactory.getImageDataFor(resourceName);
        ByteBuffer textureBuffer = imageData.loadImage(new BufferedInputStream(in), false, null);
//        int textureID = createTextureID();
        TextureImpl texture = new TextureImpl(resourceName, GL11.GL_TEXTURE_2D, textureID);
        GL.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        int width = imageData.getWidth();
        int height = imageData.getHeight();
        boolean hasAlpha = imageData.getDepth() == 32;
        texture.setTextureWidth(imageData.getTexWidth());
        texture.setTextureHeight(imageData.getTexHeight());
        int texWidth = texture.getTextureWidth();
        int texHeight = texture.getTextureHeight();
        IntBuffer temp = BufferUtils.createIntBuffer(16);
        GL.glGetInteger(3379, temp);
        int max = temp.get(0);
        if (texWidth <= max && texHeight <= max) {
            int srcPixelFormat = hasAlpha ? 6408 : 6407;
            int componentCount = hasAlpha ? 4 : 3;
            texture.setWidth(width);
            texture.setHeight(height);
            texture.setAlpha(hasAlpha);
            //if (TextureLoader.holdTextureData) {
                texture.setTextureData(srcPixelFormat, componentCount, 9729, 9729, textureBuffer);
            //}

            GL.glTexParameteri(GL11.GL_TEXTURE_2D, 10241, GL11.GL_NEAREST);
            GL.glTexParameteri(GL11.GL_TEXTURE_2D, 10240, GL11.GL_NEAREST);
            GL.glTexImage2D(GL11.GL_TEXTURE_2D, 0, 6408, get2Fold(width), get2Fold(height), 0, srcPixelFormat, 5121, textureBuffer);
        } else {
            throw new IOException("Attempt to allocate a texture to big for the current hardware");
        }
    }

    public static void reloadMinecraftTextures() {
        String[] textureNames = new String[] {
                "/terrain.png",
                "/particles.png",
                "/clouds.png",
                "/rain.png",
                "/rock.png",
                "/water.png",
                "/dirt.png",
                "/grass.png",
                "/char.png",
                "/2char.png",

                "/gui/gui.png",
                "/gui/background.png",
                "/gui/container.png",
                "/gui/crafting.png",
                "/gui/logo.png",
                "/gui/furnace.png",
                "/gui/inventory.png",
                "/gui/items.png",
                "/gui/unknown_pack.png",
                "/mineonline/gui/icons.png",

                "/armor/chain_1.png",
                "/armor/chain_2.png",
                "/armor/cloth_1.png",
                "/armor/cloth_2.png",
                "/armor/diamond_1.png",
                "/armor/diamond_2.png",
                "/armor/gold_1.png",
                "/armor/gold_2.png",
                "/armor/iron_1.png",
                "/armor/iron_2.png",

                "/art/kz.png",

                "/environment/clouds.png",
                "/environment/rain.png",
                "/environment/snow.png",

//                "/font/default.png",

                "/item/arrows.png",
                "/item/boat.png",
                "/item/cart.png",
                "/item/door.png",
                "/item/sign.png",

                "/misc/dial.png",
                //"/misc/foliagecolor.png", Needs to be patched separately.
                //"/misc/grasscolor.png", Needs to be patched separately.
                "/misc/pumpkinblur.png",
                "/misc/shadow.png",
                "/misc/vignette.png",
                "/misc/water.png",

                "/mob/char.png",
                "/mob/chicken.png",
                "/mob/cow.png",
                "/mob/creeper.png",
                "/mob/ghast.png",
                "/mob/ghast_fire.png",
                "/mob/pig.png",
                "/mob/pigman.png",
                "/mob/pigzombie.png",
                "/mob/saddle.png",
                "/mob/sheep.png",
                "/mob/sheep_fur.png",
                "/mob/skeleton.png",
                "/mob/slime.png",
                "/mob/spider.png",
                "/mob/spider_eyes.png",
                "/mob/zombie.png",

                "/terrain/moon.png",
                "/terrain/sun.png",

//                "/default.png"
        };

//        for(EGUITexture texture : EGUITexture.values()) {
//            if(texture.useTexturePack) {
//                Loader.singleton.unloadTexture(texture);
//            }
//        }

        if (LegacyGameManager.isInGame() && !LegacyGameManager.getVersion().useTexturepackPatch)
            return;

        String texturePack = Settings.singleton.getTexturePack();

        ZipFile texturesZip = null;

        for (String textureName : textureNames) {
            try {
                if (texturesZip == null)
                    texturesZip = new ZipFile(LauncherFiles.MINECRAFT_TEXTURE_PACKS_PATH + texturePack);

                ZipEntry texture = texturesZip.getEntry(textureName.substring(1));

                if (texture != null) {
                    Loader.singleton.overwriteTexture(HashMapPutAdvice.textures.get(textureName), texturesZip.getInputStream(texture), textureName);

                    if(textureName.equals("/terrain.png")) {
                        try {
                            BufferedImage terrain = ImageIO.read(texturesZip.getInputStream(texture));
                            HDTextureFXHelper.scale = (float)terrain.getHeight() / 256;
                        } catch (Exception ex) {
                            HDTextureFXHelper.scale = 1;
                        }
                        HDTextureFXHelper.reloadTextures();
                    }

                    continue;
                } else {
                    if(textureName.equals("/terrain.png")) {
                        HDTextureFXHelper.scale = 1;
                        HDTextureFXHelper.reloadTextures();
                    }
                }
            } catch (Exception ex) {

            }
            try {

                if (LegacyGameManager.getAppletWrapper().getMinecraftAppletClass() != null)
                    Loader.singleton.overwriteTexture(HashMapPutAdvice.textures.get(textureName), LegacyGameManager.getAppletWrapper().getMinecraftAppletClass().getResourceAsStream(textureName), textureName);
                if(textureName.equals("/terrain.png")) {
                    HDTextureFXHelper.scale = 1;
                    HDTextureFXHelper.reloadTextures();
                }
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    public static int get2Fold(int fold) {
        int ret;
        for(ret = 2; ret < fold; ret *= 2) {
        }

        return ret;
    }

    public int getGuiTexture(EGUITexture eguiTexture) {
        if (!textures.containsKey(eguiTexture.textureName)) {
            Settings.singleton.loadSettings();
            if (eguiTexture.useTexturePack ) {
                try {
                    ZipFile texturesZip = new ZipFile(LauncherFiles.MINECRAFT_TEXTURE_PACKS_PATH + Settings.singleton.getTexturePack());
                    ZipEntry texture = texturesZip.getEntry(eguiTexture.textureName.substring(1));
                    if (texture != null) {
                        return loadTexture(eguiTexture.textureName, texturesZip.getInputStream(texture));
                    }
                } catch (Exception ex) {

                }
            }

            return loadTexture(eguiTexture.textureName, Loader.class.getResource(eguiTexture.textureName));
        } else
            return textures.get(eguiTexture.textureName);
    }

    public void unloadTexture(EGUITexture eguiTexture) {
        if (textures.containsKey(eguiTexture.textureName)) {
            GL11.glDeleteTextures(textures.get(eguiTexture.textureName));
            textures.remove(eguiTexture.textureName);
        }
    }

    private int createVAO() {
        int vaoID = GL30.glGenVertexArrays();
        vaos.add(vaoID);
        GL30.glBindVertexArray(vaoID);
        return vaoID;
    }

    private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer buffer = storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private void unbindVAO() {
        GL30.glBindVertexArray(0);
    }

    private void bindIndicesBuffer(int[] indices) {
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        IntBuffer buffer = storeDataIntoIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    private IntBuffer storeDataIntoIntBuffer(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    private FloatBuffer storeDataInFloatBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    public void cleanUp() {
        for (int vao : vaos) {
            GL30.glDeleteVertexArrays(vao);
        }

        for (int vbo : vbos) {
            GL15.glDeleteBuffers(vbo);
        }

        for (int texture : textures.values()) {
            GL11.glDeleteTextures(texture);
            textures.clear();
        }
    }

}
