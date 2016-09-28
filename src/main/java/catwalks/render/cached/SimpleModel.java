package catwalks.render.cached;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleModel {
	
	public List<List<BakedQuad>> getQuads(List<Object> key) {
		List<List<BakedQuad>> quads = new ArrayList<>();
		List<BakedQuad> rawQuads = generateQuads(key);
		
		for (int i = 0; i < EnumFacing.VALUES.length+1; i++) {
			EnumFacing side = null;
			if(i < EnumFacing.VALUES.length)
				side = EnumFacing.VALUES[i];
			List<BakedQuad> sideQuads = new ArrayList<>();
			for (BakedQuad quad : rawQuads) {
				if(quad.getFace() == side)
					sideQuads.add(quad);
			}
			quads.add(ImmutableList.copyOf(sideQuads));
		}
		return ImmutableList.copyOf(quads);
	}
	
	public abstract List<Object> getKey(IBlockState state);
	
	protected abstract List<BakedQuad> generateQuads(List<Object> list);

}
