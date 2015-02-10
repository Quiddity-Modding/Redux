package mods.quiddity.redux.loader;

import com.google.common.collect.Sets;
import mods.quiddity.redux.Redux;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ModContainer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class handles all of the resource requests for the external packs loaded by Redux
 *
 * @author winsock on 2/4/15.
 */
public class ReduxResourcePack implements IResourcePack {

    private final ReduxPackModContainer reduxModPack;

    public ReduxResourcePack(ModContainer reduxPackModContainer) {
        if (reduxPackModContainer instanceof ReduxPackModContainer) {
            this.reduxModPack = (ReduxPackModContainer) reduxPackModContainer;
        } else {
            reduxModPack = null;
        }
        assert reduxModPack == null;
    }

    @Override
    public InputStream getInputStream(ResourceLocation resource) throws IOException {
        if (!checkSandbox(resource.getResourcePath())){
            return null;
        }
        InputStream resourceStream = null;
        if (reduxModPack.getSource().isFile() && reduxModPack.getSource().getName().endsWith(".zip")) {
            ZipFile reduxPackZip = new ZipFile(reduxModPack.getSource());
            ZipEntry requestedResource = reduxPackZip.getEntry(resource.getResourcePath());
            if (requestedResource != null) {
                resourceStream = reduxPackZip.getInputStream(requestedResource);
                // Bad practice not closing the zip file. However closing it here would cause the input stream to be invalid
            }
        } else if (reduxModPack.getSource().getParentFile().isDirectory()) {
            resourceStream = new FileInputStream(new File(reduxModPack.getSource().getParentFile(), resource.getResourcePath()));
        }
        return resourceStream;
    }

    @Override
    public boolean resourceExists(ResourceLocation resource) {
        if (!checkSandbox(resource.getResourcePath())){
            return false;
        }
        boolean resourceExists = false;
        if (reduxModPack.getSource().isFile() && reduxModPack.getSource().getName().endsWith(".zip")) {
            ZipFile reduxPackZip = null;
            try {
                reduxPackZip = new ZipFile(reduxModPack.getSource());
                resourceExists = reduxPackZip.getEntry(resource.getResourcePath()) != null;
            } catch (IOException e) {
                Redux.instance.getLogger().warn("Redux pack inconsistency. %s is inconsistent.", resource.getResourceDomain() + ".zip");
            } finally {
                if (reduxPackZip != null) {
                    try {
                        reduxPackZip.close();
                    } catch (IOException ignored) { }
                }
            }
        } else if (reduxModPack.getSource().getParentFile().isDirectory()) {
             resourceExists = new File(reduxModPack.getSource().getParentFile(), resource.getResourcePath()).exists();
        }
        return resourceExists;
    }

    @Override
    public Set getResourceDomains() {
        return Sets.newHashSet(reduxModPack.getModId());
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
        return reduxModPack.getName();
    }

    private boolean checkSandbox(String resource) {
        if (resource.contains("..") || resource.startsWith("/")) {
            Redux.instance.getLogger().warn("Critical security error! Tried to access file(s) outside of the Redux config folder with the path of: %s\nWill deny requested file.", resource);
            return false;
        }
        return true;
    }
}
