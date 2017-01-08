package lightify;

import java.util.ArrayList;

public class Group extends Luminary {

	int index = 0;
	ArrayList<Light> lights = new ArrayList();
	
	public Group(Lightify Connection, String Name,int index) {
		super(Connection, Name);
		this.index=index;
	}

	public void setLights(Light[] Lights){
		this.lights=new ArrayList();
		for(Light l:Lights){
			this.lights.add(l);
		}
	}
	
	@Override
	public byte[] buildCommand(byte command, byte[] b) {
		// TODO Auto-generated method stub
		return connection.buildCommand(command, index, b);
	
	}

}
