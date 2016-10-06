package catwalks;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class Conf {
	
	public static boolean shouldHaveLaddeyGrabbey = Const.RAND.nextDouble() <= ( 5 / 100.0 ); // 5% chance
	
	public static float ladderSpeed  = 2;
	public static int catwalkSpeed = 1;
	public static boolean showScaffoldInsideFaces = false;
	
	public static boolean[] CUSTOM_ENABLED = new boolean[8];
	
	public static boolean logPackets = false;
	
	public static File file;
	public static Configuration config;
	
	public static final String CATEGORY_GENERAL = "general";
	public static final String CATEGORY_DEV = "developer options";
	
	@SubscribeEvent
    public void onConfigChangedEvent(OnConfigChangedEvent event)
    {
        if (Const.MODID.equals(event.getModID()))
        {
            loadConfigs(config);
        }
    }

    public static void loadConfigsFromFile(File configFile)
    {
        file = configFile;
        config = new Configuration(configFile);
        config.load();

        loadConfigs(config);
    }

    public static void loadConfigs(Configuration conf)
    {
    	Property prop;
    	
    	prop = conf.get(CATEGORY_GENERAL, "Catwalk Speed Potion Level", 1).setRequiresMcRestart(false);
    	prop.setComment("The speed boost on catwalks will apply a speed boost equivalent to Speed N");
    	catwalkSpeed = prop.getInt();
    	
    	prop = conf.get(CATEGORY_GENERAL, "Ladder Speed Multiplier", 1.5).setRequiresMcRestart(false);
    	prop.setComment("Caged ladders will be N times as fast as normal ladders");
    	ladderSpeed = (float)prop.getDouble();
    	
    	prop = conf.get(CATEGORY_GENERAL, "Show Scaffold Inside Faces", false).setRequiresMcRestart(false);
    	prop.setComment("Whether the faces of scaffolds should show if they are next to another scaffold");
    	showScaffoldInsideFaces = (boolean)prop.getBoolean();
    	
    	prop = conf.get(CATEGORY_DEV, "Log packet handling", false).setRequiresMcRestart(false);
    	prop.setComment("Useful to see if more packets are being sent than needed");
    	logPackets = (boolean)prop.getBoolean();
    	
        if (conf.hasChanged() == true)
        {
            conf.save();
            CatwalksMod.proxy.reloadConfigs();
        }
    }

	
}
