package hats.common.core;

import hats.common.Hats;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class HatHandler 
{

	public static boolean readHatFromFile(File file)
	{
		if(file.getName().endsWith(".tcn"))
		{
//			file.
			boolean hasTexture = false;
			try
			{
				ZipFile zipFile = new ZipFile(file);
				Enumeration entries = zipFile.entries();
				
				while(entries.hasMoreElements())
				{
					ZipEntry entry = (ZipEntry)entries.nextElement();
					if(!entry.isDirectory())
					{
						if(entry.getName().endsWith(".png"))
						{
							hasTexture = true;
						}
					}
				}
				
				zipFile.close();

			}
			catch(EOFException e1)
			{
				Hats.console("Failed to load: " + file.getName() + " is corrupted!", true);
			}
			catch(IOException e1)
			{
				Hats.console("Failed to load: " + file.getName() + " cannot be read!", true);
			} 
			catch (Exception e1) 
			{
				Hats.console("Failed to load: " + file.getName() + " threw a generic exception!", true);
				e1.printStackTrace();
			}
			
			if(hasTexture)
			{
				Hats.proxy.loadHatFile(file);
				return true;
			}
			else
			{
				Hats.console("Failed to load: " + file.getName() + " has no texture!", true);
			}
		}
		return false;
	}
	
	public static void requestHat(String name)
	{
		
	}
	
	public static void receiveHatData(DataInputStream stream, boolean isServer)
	{
		try
		{
			String hatName = stream.readUTF();
			byte packets = stream.readByte();
			byte packetNumber = stream.readByte();
			int bytesSize = stream.readInt();
			
			byte[] byteValues = new byte[bytesSize];
			
			stream.read(byteValues);
			
			ArrayList<byte[]> byteArray = hatParts.get(hatName);
			if(byteArray == null)
			{
				byteArray = new ArrayList<byte[]>();
				
				for(int i = 0; i < packets; i++)
				{
					byteArray.add(new byte[0]);
				}
			}
			
			byteArray.set(packetNumber, byteValues);
			
			boolean hasAllInfo = true;
			
			for(int i = 0; i < byteArray.size(); i++)
			{
				byte[] byteList = byteArray.get(i);
				if(byteList.length == 0)
				{
					hasAllInfo = false;
				}
			}
			
			if(hasAllInfo)
			{
				File file = new File(hatsFolder, hatName + ".tcn");
				
				FileOutputStream fis = new FileOutputStream(file);
				
				for(int i = 0; i < byteArray.size(); i++)
				{
					byte[] byteList = byteArray.get(i);
					fis.write(byteList);
				}
				
				fis.close();
				
				
				if(isServer)
				{
					ArrayList<String> queuedLists = queuedHats.get(hatName);
					if(queuedLists != null)
					{
						for(String name : queuedLists)
						{
							EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(name);
							if(player != null)
							{
								sendHat(hatName, player);
							}
						}
					}
				}
				else
				{
					
				}
			}
		}
		catch(IOException e)
		{
		}
	}
	
	public static void sendHat(String hatName, EntityPlayer player)
	{
		File file = null;
		
		for(Entry<File, String> e : hatNames.entrySet())
		{
			if(e.getValue().equalsIgnoreCase(hatName))
			{
				file = e.getKey();
			}
		}

		if(file != null)
		{
			try
			{
				
				int fileSize = (int)file.length();
				
				if(fileSize > 100000)
				{
					Hats.console("Unable to send " + file.getName() + ". It is above the size limit!", true);
				}
				
				FileInputStream fis = new FileInputStream(file);
				int packetCount = 0;
				
				while(fileSize > 0)
				{
			        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			        DataOutputStream stream = new DataOutputStream(bytes);
	
			        stream.writeByte(2); //id
			        stream.writeUTF(hatName); //name
			        stream.writeByte((int)Math.ceil((float)fileSize / 32000F)); //number of overall packets to send
			        stream.writeByte(packetCount); // packet number being sent
			        stream.writeInt(fileSize > 32000 ? 32000 : fileSize); //byte size
			        
			        byte[] fileBytes = new byte[fileSize > 32000 ? 32000 : fileSize];
			        fis.read(fileBytes);
			        stream.write(fileBytes); //hat info

			        packetCount++;
			        fileSize -= 32000;
			        
			        if(player != null)
			        {
			        	PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Hats", bytes.toByteArray()), (Player)player);
			        }
			        else
			        {
			        	PacketDispatcher.sendPacketToServer(new Packet250CustomPayload("Hats", bytes.toByteArray()));
			        }
				}
				
				fis.close();
			}
			catch(Exception e)
			{
				
			}
			
			//byte size should be 32000
		}
	}
	
	public static boolean hasHat(String name)
	{
		for(Entry<File, String> e : hatNames.entrySet())
		{
			if(e.getValue().equalsIgnoreCase(name))
			{
				return true;
			}
		}
		return false;
	}
	
	public static File hatsFolder;
	
	public static HashMap<String, ArrayList<String>> queuedHats = new HashMap<String, ArrayList<String>>();
	
	public static HashMap<String, ArrayList<byte[]>> hatParts = new HashMap<String, ArrayList<byte[]>>();
	
	public static HashMap<File, String> hatNames = new HashMap<File, String>();

}