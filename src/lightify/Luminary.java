package lightify;

import java.io.IOException;

public abstract class Luminary {
	
	Lightify connection;
	String name;
	
	public Luminary(Lightify Connection, String Name){
		connection = Connection;
		name=Name;
	}
	
	public void setOnOff(boolean on) throws IOException{
		byte[] data =connection.buildOnOff(this,on);
		connection.send(data);
		connection.recv();
	}
	
	public void setLuminance(byte luminance,short timeInMilliseconds) throws IOException{
		connection.send(connection.buildLuminance(this,luminance,timeInMilliseconds));
		connection.recv();
	}
	
	public void setRGB(byte r,byte g,byte b,short timeInMilliseconds) throws IOException{
		connection.send(connection.buildColor(this,r,g,b,timeInMilliseconds));
		connection.recv();
	}
	

	public abstract byte[] buildCommand(byte command, byte[] b);


}
