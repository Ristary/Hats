package hats.common.entity;

import hats.client.core.HatInfoClient;
import hats.common.Hats;
import hats.common.core.HatInfo;
import hats.common.core.SessionState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityHat extends Entity 
{

	public EntityLivingBase parent;
	public EntityLivingBase renderingParent; 
	public boolean render;
	public String hatName;
	
	public HatInfoClient info;
	
	public int reColour;
	
	public int prevR;
	public int prevG;
	public int prevB;
	
	private int colourR;
	private int colourG;
	private int colourB;
	
	public long lastUpdate;
	
	public EntityHat(World par1World) 
	{
		super(par1World);
		setSize(0.1F, 0.1F);
		
		hatName = "";
		
		reColour = 0;
		
		prevR = 0;
		prevG = 0;
		prevB = 0;
		
		colourR = 0;
		colourG = 0;
		colourB = 0;
		lastUpdate = par1World.getWorldTime();
		ignoreFrustumCheck = true;
		renderDistanceWeight = 10D;
	}
	
	public EntityHat(World par1World, EntityLivingBase ent, HatInfo hatInfo) 
	{
		super(par1World);
		setSize(0.1F, 0.1F);
		renderingParent = parent = ent;
		hatName = hatInfo.hatName;
		
		setLocationAndAngles(parent.posX, parent.boundingBox.minY, parent.posZ, parent.rotationYaw, parent.rotationPitch);
		
		reColour = 0;
		
		prevR = 0;
		prevG = 0;
		prevB = 0;

		colourR = hatInfo.colourR;
		colourG = hatInfo.colourG;
		colourB = hatInfo.colourB;
		lastUpdate = par1World.getWorldTime();
		ignoreFrustumCheck = true;
		renderDistanceWeight = 10D;
	}
	
	@Override
	public void onUpdate()
	{
		renderingParent = parent;
		render = true;
		ticksExisted++;
		
		if(reColour > 0)
		{
			reColour--;
		}
		
		if(parent == null || !parent.isEntityAlive() || parent.isChild())
		{
			setDead();
			return;
		}
		
		if(Hats.hasMorphMod && parent instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)parent;
			
			EntityLivingBase morphEnt = morph.api.Api.getMorphEntity(player.username, true);
			if(morphEnt != null)
			{
				renderingParent = morphEnt;
			}
		}
		
		lastUpdate = worldObj.getWorldTime();

		validateHatInfo();
//		if(!(parent instanceof EntityPlayer))
//		{
//			worldObj.spawnParticle("smoke", posX, posY, posZ, 1.0D, 0.0D, 0.0D);
//			worldObj.spawnParticle("smoke", posX, posY, posZ, -1.0D, 0.0D, 0.0D);
//			worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0D, 1.0D, 0.0D);
//			worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0D, -1.0D, 0.0D);
//			worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0D, 0.0D, 1.0D);
//			worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0D, 0.0D, -1.0D);
//		}
	}
	
	@Override
    public int getBrightnessForRender(float par1)
    {
		if(Hats.renderHats == 13131)
		{
			return 15728880;
		}
        return super.getBrightnessForRender(par1);
    }
	
	public void validateHatInfo()
	{
		boolean regen = true;
		if(info != null && info.hatName.equalsIgnoreCase(hatName) && info.colourR == getR() && info.colourG == getG() && info.colourB == getB() && info.prevColourR == prevR && info.prevColourG == prevG && info.prevColourB == prevB)
		{
			regen = false;
		}
		if(regen)
		{
			info = new HatInfoClient(hatName, getR(), getG(), getB());
		}
		info.recolour = reColour;
		info.doNotRender = !render;
		info.prevColourR = prevR;
		info.prevColourG = prevG;
		info.prevColourB = prevB;
	}
	
	public void setR(int i)
	{
		prevR = colourR;
		colourR = i;
	}
	
	public void setG(int i)
	{
		prevG = colourG;
		colourG = i;
	}
	
	public void setB(int i)
	{
		prevB = colourB;
		colourB = i;
	}
	
	public int getR()
	{
		return colourR;
	}
	
	public int getG()
	{
		return colourG;
	}
	
	public int getB()
	{
		return colourB;
	}
	
	@Override
	public void setDead()
	{
		super.setDead();
		if(SessionState.serverHasMod && SessionState.serverHatMode == 4 && parent != null)
		{
			Hats.proxy.tickHandlerClient.requestedMobHats.remove((Object)parent.entityId);
		}
	}
	
	@Override
	public void entityInit() 
	{
	}
	
	@Override
    public boolean writeToNBTOptional(NBTTagCompound par1NBTTagCompound)//disable saving of the entity
    {
    	return false;
    }

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) 
	{
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) 
	{
	}

}
