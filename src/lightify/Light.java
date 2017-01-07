package lightify;

import java.io.IOException;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Light extends Luminary {

	byte[] addr;
	byte r,g,b;
	byte luminance;
	boolean on;
	int temp;
	
	public Light(Lightify Connection, String Name, byte[] Addr) {
		super(Connection, Name);
		addr=Addr;
	}

	public byte[] getAddr(){
		return this.addr;
	}
	public boolean getOn(){
		return on;
	}
	public byte getRed(){
		return r;
	}
	public byte getGreen(){
		return g;
	}
	public byte getBlue(){
		return b;
	}
	public int getLuminance(){
		return luminance;
	}
	public int getTemperature(){
		return temp;
	}
	
	public void setOnOff(boolean On) throws IOException{
		this.on=On;
		super.setOnOff(On);
		if(luminance == 0 && On){
			luminance=1;
		}
	}
	
	public void setLuminance(byte luminance,short time) throws IOException{
		this.luminance=luminance;
		super.setLuminance(luminance, time);
		if(luminance > 0 && !on ){
			on=true;
		}else{
			on=false;
		}
	}
	
	public void setTemperature(byte temperature){
		throw new NotImplementedException();
	}
	
	public void setRGB(byte r,byte g,byte b,short time) throws IOException{
		this.r=r;
		this.g=g;
		this.b=b;
		super.setRGB(r, g, b, time);
	}
	
	
	@Override
	public byte[] buildCommand(byte command, byte[] b) {
		// TODO Auto-generated method stub
		return super.connection.buildLightCommand(command, addr, b);
	}

	public void updateStatus(byte onoff, byte luminance, short temp, byte r, byte g, byte b) {
		this.on=onoff>0;
		this.luminance=luminance;
		this.temp=temp;
		this.r=r;
		this.g=g;
		this.b=b;
		
	}

}
