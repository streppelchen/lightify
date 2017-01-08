package lightify;

import java.io.IOException;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.Map.Entry;

public class Main {
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException{
		
		Lightify lify = new Lightify("192.168.2.31", 4000);
		lify.update_all_light_status();
		lify.updateGroupList();
		
		for(Entry<String,Group> g:lify.groups.entrySet()){
			if(g.getValue().name.equals("Wohnzimmer")){
				g.getValue().setOnOff(true);
				g.getValue().setRGB((byte)0xff, (byte)0, (byte)0,(short) 0);
				
				Thread.sleep(10000);
				g.getValue().setOnOff(false);
			}
			
			
		}
		
		
		/*for(Entry<byte[], Light> l:lify.lights.entrySet()){
			l.getValue().setOnOff(true);
			l.getValue().setRGB((byte)0xff,(byte) 0xD3,(byte) 0x9B, (short)0x0);
			l.getValue().setLuminance((byte)(0x0), (short)0);
			
		}*/
	}
}
