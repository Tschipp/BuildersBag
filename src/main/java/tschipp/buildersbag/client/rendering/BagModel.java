package tschipp.buildersbag.client.rendering;

import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;

public class BagModel implements IBakedModel
{

	private IBakedModel old;
	
	
	public BagModel(IBakedModel old)
	{
		this.old = old;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData)
	{
		return old.getQuads(state, side, rand, extraData);
	}

	@Override
	public boolean usesBlockLight()
	{
		return old.usesBlockLight();
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return old.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return old.isGui3d();
	}

	@Override
	public boolean isCustomRenderer()
	{
		return true;
	}

	public IBakedModel getInternal()
	{
		return old;
	}

	public void setInternal(IBakedModel internal)
	{
		this.old = internal;
	}

	@SuppressWarnings("deprecation")
	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return old.getParticleIcon();
	}
	
	@Override
	public TextureAtlasSprite getParticleTexture(IModelData data)
	{
		return old.getParticleTexture(data);
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.EMPTY;
	}
	
	@Override
	public IBakedModel handlePerspective(TransformType cameraTransformType, MatrixStack mat)
	{
		return old.handlePerspective(cameraTransformType, mat);
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand)
	{
		return old.getQuads(state, side, rand);
	}

	

}
