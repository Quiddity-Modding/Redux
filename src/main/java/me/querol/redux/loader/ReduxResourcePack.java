package me.querol.redux.loader;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.actors.threadpool.Arrays;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by winsock on 2/3/15.
 */
public class ReduxResourcePack implements IResourcePack {

    private final File textureFolder;
    private final Set<String> matchingIds = new HashSet<String>();

    public ReduxResourcePack(File reduxTextureFolder) {
        this.textureFolder = reduxTextureFolder;
    }

    @Override
    public InputStream getInputStream(ResourceLocation resource) throws IOException {
        String path = getReduxResource(resource);
        if (path == null)
            return null;
        return new FileInputStream(new File(textureFolder, path));
    }

    @Override
    public boolean resourceExists(ResourceLocation resource) {
        String path = getReduxResource(resource);
        if (path == null)
            return false;
        return new File(textureFolder, path).exists();
    }

    public void addDomain(String string) {
        matchingIds.add(string);
        IResourceManager manager = FMLClientHandler.instance().getClient().getResourceManager();
        if (manager instanceof SimpleReloadableResourceManager) {
            SimpleReloadableResourceManager reloadableResourceManager = (SimpleReloadableResourceManager)manager;
            reloadableResourceManager.reloadResourcePack(this);
        }
    }

    @Override
    public Set getResourceDomains() {
        return matchingIds;
    }

    @Override
    public IMetadataSection getPackMetadata(IMetadataSerializer p_135058_1_, String p_135058_2_) throws IOException {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        return null;
    }

    @Override
    public String getPackName() {
        return "External Redux Resources";
    }

    private String getReduxResource(ResourceLocation resourceLocation) {
        String packId = resourceLocation.getResourceDomain();
        String parsedResourcePath = packId + File.separator + resourceLocation.getResourcePath();
        return parsedResourcePath;
    }

    private void checkSandbox(String resource) {
        if (resource.contains("..") || resource.startsWith("/") || resource.charAt(1) == ':') {
            FMLCommonHandler.instance().raiseException(new SecurityException(String.format("Tried to access file(s) outside of the Redux config folder with the path of: %s", resource)),
                    "Redux: Critical security error!", true);
        }
    }
}
