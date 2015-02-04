package me.querol.redux.loader;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by winsock on 2/3/15.
 */
public class ReduxResourcePack implements IResourcePack {

    private final File reduxConfigFolder;
    private final Set<String> matchingIds = new HashSet<String>();

    public ReduxResourcePack(File reduxConfigFolder) {
        this.reduxConfigFolder = reduxConfigFolder;
    }

    @Override
    public InputStream getInputStream(ResourceLocation resource) throws IOException {
        checkSandbox(resource.getResourcePath());
        InputStream resourceStream = null;
        if (new File(reduxConfigFolder, resource.getResourceDomain() + File.separator + resource.getResourcePath()).exists()) {
            resourceStream = new FileInputStream(new File(reduxConfigFolder, resource.getResourceDomain() + File.separator + resource.getResourcePath()));
        } else if (new File(reduxConfigFolder, resource.getResourceDomain() + ".zip").exists()) {
            ZipFile reduxPackZip = new ZipFile(new File(reduxConfigFolder, resource.getResourceDomain() + ".zip"));
            ZipEntry requestedResource = reduxPackZip.getEntry(resource.getResourcePath());
            if (requestedResource != null) {
                resourceStream = reduxPackZip.getInputStream(requestedResource);
                // Bad practice not closing the zip file. However closing it here would cause the input stream to be invalid
            }
        }
        return resourceStream;
    }

    @Override
    public boolean resourceExists(ResourceLocation resource) {
        checkSandbox(resource.getResourcePath());
        boolean resourceExists = false;
        if (new File(reduxConfigFolder, resource.getResourceDomain() + File.separator + resource.getResourcePath()).exists()) {
            resourceExists = true;
        } else if (new File(reduxConfigFolder, resource.getResourceDomain() + ".zip").exists()) {
            ZipFile reduxPackZip = null;
            try {
                reduxPackZip = new ZipFile(new File(reduxConfigFolder, resource.getResourceDomain() + ".zip"));
                resourceExists = reduxPackZip.getEntry(resource.getResourcePath()) != null;
            } catch (IOException e) {
                FMLLog.warning("Redux pack inconsistency. %s is inconsistent.", resource.getResourceDomain() + ".zip");
            } finally {
                if (reduxPackZip != null) {
                    try {
                        reduxPackZip.close();
                    } catch (IOException ignored) { }
                }
            }
        }
        return resourceExists;
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

    private void checkSandbox(String resource) {
        if (resource.contains("..") || resource.startsWith("/") || resource.charAt(1) == ':') {
            FMLCommonHandler.instance().raiseException(new SecurityException(String.format("Tried to access file(s) outside of the Redux config folder with the path of: %s", resource)),
                    "Redux: Critical security error!", true);
        }
    }
}
