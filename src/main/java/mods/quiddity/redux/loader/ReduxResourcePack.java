package mods.quiddity.redux.loader;

import com.google.common.collect.Sets;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
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
        checkSandbox(resource.getResourcePath());
        InputStream resourceStream = null;
        if (reduxModPack.getSource().isDirectory()) {
            resourceStream = new FileInputStream(new File(reduxModPack.getSource(), resource.getResourcePath()));
        } else if (reduxModPack.getSource().isFile()) {
            ZipFile reduxPackZip = new ZipFile(reduxModPack.getSource());
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
        if (reduxModPack.getSource().isDirectory()) {
            resourceExists = new File(reduxModPack.getSource(), resource.getResourcePath()).exists();
        } else if (reduxModPack.getSource().isFile()) {
            ZipFile reduxPackZip = null;
            try {
                reduxPackZip = new ZipFile(reduxModPack.getSource());
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

    private void checkSandbox(String resource) {
        if (resource.contains("..") || resource.startsWith("/") || resource.charAt(1) == ':') {
            FMLCommonHandler.instance().raiseException(new SecurityException(String.format("Tried to access file(s) outside of the Redux config folder with the path of: %s", resource)),
                    "Redux: Critical security error!", true);
        }
    }
}
