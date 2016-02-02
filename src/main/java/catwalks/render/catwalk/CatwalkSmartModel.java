package catwalks.render.catwalk;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import catwalks.CatwalksMod;
import catwalks.block.BlockCatwalk;
import catwalks.render.BakedModelBase;
import catwalks.render.ModelUtils;
import catwalks.render.SmartModelBase;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class CatwalkSmartModel extends SmartModelBase {

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return null;
	}

	@Override
	public IBakedModel newBakedModel(IExtendedBlockState state) {
		return new Model(
        		state.getValue(BlockCatwalk.BOTTOM),
        		state.getValue(BlockCatwalk.NORTH),
        		state.getValue(BlockCatwalk.SOUTH),
        		state.getValue(BlockCatwalk.EAST),
        		state.getValue(BlockCatwalk.WEST)
        	);
	}

	@Override
	public IBakedModel newBakedModel() {
		return new Model();
	}

	public static class Model extends BakedModelBase {
		
		private TextureAtlasSprite side, bottom;

        private boolean north;
        private boolean south;
        private boolean west;
        private boolean east;
        private boolean down;
		
		public Model() {
			side = ModelUtils.getSprite( new ResourceLocation(CatwalksMod.MODID + ":gen/catwalk_side_tl") );
			bottom = ModelUtils.getSprite( new ResourceLocation(CatwalksMod.MODID + ":gen/catwalk_side") );
		}
		
		public Model(boolean down, boolean north, boolean south, boolean west, boolean east) {
			this();
			this.north = north;
            this.south = south;
            this.west = west;
            this.east = east;
            this.down = down;
            genFaces();
		}
		
		List<BakedQuad> quads = new ArrayList<>();
		
		public void genFaces() {
	        if(north) {
	        	ModelUtils.putFace(quads, EnumFacing.NORTH, side);
	        }
	        if(south) {
	        	ModelUtils.putFace(quads, EnumFacing.SOUTH, side);
	        }
	        if(east) {
	        	ModelUtils.putFace(quads, EnumFacing.EAST, side);
	        }
	        if(west) {
	        	ModelUtils.putFace(quads, EnumFacing.WEST, side);
	        }
	        
	        if(down) {
	        	ModelUtils.putFace(quads, EnumFacing.DOWN, bottom);
	        }
		}
		
		@Override
		public List<BakedQuad> getFaceQuads(EnumFacing side) {
			List<BakedQuad> faceQuads = new ArrayList<>();
			for (BakedQuad bakedQuad : quads) {
				if(bakedQuad.getFace() == side) {
					faceQuads.add(bakedQuad);
				}
			}
			return faceQuads;
		}

		@Override
		public List<BakedQuad> getGeneralQuads() {
			return ImmutableList.of();
	    }

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return null;
		}
		
	}
	
}