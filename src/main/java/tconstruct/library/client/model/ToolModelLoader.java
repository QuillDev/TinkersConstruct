package tconstruct.library.client.model;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import tconstruct.library.TinkerRegistry;
import tconstruct.library.client.CustomTextureCreator;

public class ToolModelLoader implements ICustomModelLoader {

  public static String EXTENSION = ".tcon";

  @Override
  public boolean accepts(ResourceLocation modelLocation) {
    return modelLocation.getResourcePath().endsWith(EXTENSION); // tinkertoolmodel extension. Foo.tcon.json
  }

  @Override
  public IModel loadModel(ResourceLocation modelLocation) {
    try {
      ModelBlock modelBlock = ModelHelper.loadModelBlock(modelLocation);

      modelBlock.parent = ModelHelper.DEFAULT_PARENT;
      modelBlock.name = modelLocation.toString();

      List<MaterialModel> parts = new LinkedList<>();
      // also load the parts of the tool, defined as the layers of the tool model
      for(String s : ToolModel.getLayers()) {
        String r = modelBlock.resolveTextureName(s);
        if(!"missingno".equals(r)) {
          ModelBlock mb = ModelHelper.loadModelBlockFromTexture(r);
          parts.add(new MaterialModel(mb));
        }
      }

      List<MaterialModel> brokenParts = new LinkedList<>();
      for(String s : ToolModel.getBrokenLayers()) {
        String r = modelBlock.resolveTextureName(s);
        if(!"missingno".equals(r)) {
          ModelBlock mb = ModelHelper.loadModelBlockFromTexture(r);
          brokenParts.add(new MaterialModel(mb));
        }
      }

      String toolName = FilenameUtils.getBaseName(modelLocation.getResourcePath());
      IModel mods = ModelLoaderRegistry.getModel(ModifierModelLoader.getLocationForToolModifiers(toolName));
      ModifierModel modifiers = null;

      if(mods == null || !(mods instanceof ModifierModel)) {
        TinkerRegistry.log.trace(
            "Toolmodel {} does not have any modifiers associated with it. Be sure that the Tools internal name, the Toolmodels filename and the name used inside the Modifier Model Definition match!",
            modelLocation);
      }
      else {
        modifiers = (ModifierModel) mods;
      }

      IModel output = new ToolModel(modelBlock, parts, brokenParts, modifiers);

      // inform the texture manager about the textures it has to process
      CustomTextureCreator.registerTextures(output.getTextures());

      return output;
    } catch(IOException e) {
      TinkerRegistry.log.error("Could not load multimodel {}", modelLocation.toString());
    }
    return ModelLoaderRegistry.getMissingModel();
  }

  @Override
  public void onResourceManagerReload(IResourceManager resourceManager) {

  }

  protected ResourceLocation getModelLocation(ResourceLocation p_177580_1_) {
    return new ResourceLocation(p_177580_1_.getResourceDomain(), "models/" + p_177580_1_.getResourcePath() + ".json");
  }


}
