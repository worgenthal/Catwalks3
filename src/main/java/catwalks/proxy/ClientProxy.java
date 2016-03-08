package catwalks.proxy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import catwalks.CatwalksMod;
import catwalks.block.BlockCatwalkBase.Face;
import catwalks.register.BlockRegister;
import catwalks.register.ItemRegister;
import catwalks.render.catwalk.CatwalkSmartModel;
import catwalks.render.catwalk.CatwalkStairSmartModel;
import catwalks.shade.ccl.vec.Matrix4;
import catwalks.shade.ccl.vec.Vector3;
import catwalks.texture.TextureGenerator;
import catwalks.util.ExtendedFlatHighlightMOP;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Tuple;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {
	
	public EntityPlayer getPlayerLooking(Vec3 start, Vec3 end) {
		return Minecraft.getMinecraft().thePlayer;
	}
	
	public static List<Tuple<Vector3, Double>> hits = new ArrayList<>();
	
	public void preInit() {
		BlockRegister.initRender();
		ItemRegister.initRender();
		MinecraftForge.EVENT_BUS.register(TextureGenerator.instance);
//		LangPlus.addMod(CatwalksMod.MODID);
//		( (IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager() ).registerReloadListener(TextureGenerator.instance);
	}
	
	Map<ModelResourceLocation, IBakedModel> models = new HashMap<>();
	
	private void model(String loc, IBakedModel model) {
		models.put(new ModelResourceLocation(CatwalksMod.MODID + ":" + loc), model);
	}
	
	@SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent event) {
		models.clear();
		
		model("catwalk", new CatwalkSmartModel());
		model("catwalkStair", new CatwalkStairSmartModel());
		
        for (Entry<ModelResourceLocation, IBakedModel> model : models.entrySet()) {
			
        	Object object =  event.modelRegistry.getObject(model.getKey());
            if (object != null) {
                event.modelRegistry.putObject(model.getKey(), model.getValue());
            }
        	
		}
    }
	
	@SubscribeEvent
	public void highlight(DrawBlockHighlightEvent event) {
		BlockPos pos = event.target.getBlockPos();
		
		if(event.target instanceof ExtendedFlatHighlightMOP) {
			event.setCanceled(true);
			
			ExtendedFlatHighlightMOP mop = (ExtendedFlatHighlightMOP) event.target;
			
			GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
            GL11.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.pushMatrix();
            
            double d0 = event.player.lastTickPosX + (event.player.posX - event.player.lastTickPosX) * (double)event.partialTicks;
            double d1 = event.player.lastTickPosY + (event.player.posY - event.player.lastTickPosY) * (double)event.partialTicks;
            double d2 = event.player.lastTickPosZ + (event.player.posZ - event.player.lastTickPosZ) * (double)event.partialTicks;
            
            GlStateManager.translate(pos.getX()-d0, pos.getY()-d1, pos.getZ()-d2);
            
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            
            worldrenderer.begin(1, DefaultVertexFormats.POSITION);
            
            Face q = mop.quad.copy();
            Vector3[] points = q.points();
            
            Matrix4 matrix = new Matrix4();
            matrix.translate(Vector3.center.copy().multiply(-1));
            
            if(!Minecraft.getMinecraft().theWorld.isSideSolid(event.target.getBlockPos().offset(event.target.sideHit), event.target.sideHit.getOpposite()))
            	matrix.scale(new Vector3(1.002, 1.002, 1.002));
            matrix.translate(Vector3.center);
            q.apply(matrix);
            
            Vector3 prev = null;
            
            for (int i = 0; i < points.length; i++) {
            	if(i == 0)
            		prev = points[points.length-1];
				Vector3 point = points[i];
				
				worldrenderer.pos(prev.x,  prev.y,  prev.z ).endVertex();
				worldrenderer.pos(point.x, point.y, point.z).endVertex();
				
				for (int j = 0; j < points.length/2; j++) {
					if(j != i && j != i+1 && j != i-1) {
						
						Vector3 opp = points[j];
						
						worldrenderer.pos(opp.x,   opp.y,   opp.z  ).endVertex();
						worldrenderer.pos(point.x, point.y, point.z).endVertex();
						
					}
				}
				prev = point;
			}
            
            tessellator.draw();
            

            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
		}
	}
	
	{ /* dev only */ }
	
	@SubscribeEvent
	public void debugText(RenderGameOverlayEvent.Text event) {
		if(!CatwalksMod.developmentEnvironment)
			return;
		
		if(!Minecraft.getMinecraft().gameSettings.showDebugInfo)
			return;
		
		Minecraft mc = Minecraft.getMinecraft();
		
		if(Minecraft.getMinecraft().thePlayer.isSneaking()) {
			if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.getBlockPos() != null)
            {
                BlockPos blockpos = mc.objectMouseOver.getBlockPos();
                IBlockState iblockstate = mc.theWorld.getBlockState(blockpos);
                
                IBlockState maybeExtended = iblockstate.getBlock().getExtendedState(iblockstate, mc.theWorld, blockpos);
                
                if(maybeExtended instanceof IExtendedBlockState) {
                	IExtendedBlockState extended = (IExtendedBlockState)maybeExtended;
                	
                	for (IUnlistedProperty<?> entry : extended.getUnlistedNames())
                    {
                		Object value = extended.getValue(entry);
                        String s = value.toString();

                        if (value == Boolean.TRUE)
                        {
                            s = EnumChatFormatting.GREEN + "" + EnumChatFormatting.ITALIC + s;
                        }
                        else if (value == Boolean.FALSE)
                        {
                            s = EnumChatFormatting.RED + "" +  EnumChatFormatting.ITALIC + s;
                        }

                        event.right.add(EnumChatFormatting.ITALIC + entry.getName() + ": " + s);
                    }
                }
                
                event.left.add("Looking at side: " + mc.objectMouseOver.sideHit.getName() );
            }
		}
	}
	

	@SubscribeEvent
	public void worldRender(RenderWorldLastEvent event)
	{
		if(!CatwalksMod.developmentEnvironment)
			return;
		
		Minecraft mc = Minecraft.getMinecraft();

		GlStateManager.pushAttrib();
		GlStateManager.disableLighting();
		
		GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
        GL11.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        
		GlStateManager.pushMatrix();
		
		EntityPlayer rootPlayer = Minecraft.getMinecraft().thePlayer;
		double x = rootPlayer.lastTickPosX + (rootPlayer.posX - rootPlayer.lastTickPosX) * event.partialTicks;
        double y = rootPlayer.lastTickPosY + (rootPlayer.posY - rootPlayer.lastTickPosY) * event.partialTicks;
        double z = rootPlayer.lastTickPosZ + (rootPlayer.posZ - rootPlayer.lastTickPosZ) * event.partialTicks;
        GlStateManager.translate(-x, -y, -z);
		
		mc.mcProfiler.startSection("blockBBs");
		
		if(!mc.getRenderManager().isDebugBoundingBox() || mc.theWorld == null || mc.thePlayer == null || mc.thePlayer.getEntityBoundingBox() == null)
		{} else {
			
			double d = 0.0020000000949949026D;
	        AxisAlignedBB searchBB = mc.thePlayer.getEntityBoundingBox().expand(2,2,2);
	        
			List<AxisAlignedBB> aabbs = mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, searchBB);
			
			for (AxisAlignedBB bb : aabbs) {
				RenderGlobal.drawOutlinedBoundingBox(bb.expand(d, d, d), 127, 255, 127, 255);
			}
		}
		
		mc.mcProfiler.endStartSection("hitDist");
		
        GlStateManager.enableTexture2D();
		
		Tessellator t = Tessellator.getInstance();
		WorldRenderer w = t.getWorldRenderer();
		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
//		w.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		hits.sort(new Comparator<Tuple<Vector3, Double>>() {
			@Override
			public int compare(Tuple<Vector3, Double> o1, Tuple<Vector3, Double> o2) {
				if(o1.getSecond() < o2.getSecond())
					return 1;
				if(o1.getSecond() == o2.getSecond())
					return 0;
				if(o1.getSecond() > o2.getSecond())
					return -1;
				return 0;
			}
		});
		int i = 0;
		for (Tuple<Vector3, Double> tuple : hits) {
			Vector3 v = tuple.getFirst();
			GlStateManager.pushMatrix();
//			GlStateManager.translate((float) (x - renderManager.viewerPosX), (float) (y - renderManager.viewerPosY), (float) (z - renderManager.viewerPosZ));
			GlStateManager.translate(v.x, v.y, v.z);
			GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(0.02, 0.02, 0.02);
			GlStateManager.rotate(180, 0, 0, 1);
			GlStateManager.translate(5, i*9, 0);
			mc.fontRendererObj.drawString(String.format("%.6f", tuple.getSecond() ), 0, 0, 0xFFFFFF);
			
			GlStateManager.popMatrix();
			i++;
		}
		
//		t.draw();
		
		mc.mcProfiler.endSection();
		
		GlStateManager.depthMask(true);
//        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
		
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}
	
}
