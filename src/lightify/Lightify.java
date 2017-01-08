package lightify;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;
import java.nio.*;

public class Lightify {
	
	String ipAddr;
	int port;
	
	HashMap<byte[],Light> lights = new HashMap<byte[],Light>();
	HashMap<String,Group> groups = new HashMap<>();

	Socket connection;
	OutputStream outputStream;
	InputStream inputStream;
	
	int sequenceNo=1;
	
	public Lightify(String IPAddr,int Port) throws UnknownHostException, IOException{
		this.ipAddr=IPAddr;
		this.port=Port;
		connection = new Socket(ipAddr,Port);
		outputStream = connection.getOutputStream();
		inputStream = connection.getInputStream();

	}
	
	private int getSequenceNumber(){
		int tmp = sequenceNo;
		sequenceNo+=1;
		return tmp;
	}
	
	public byte[] buildGlobalCommand(byte command,byte[] data){
        short length = (short) (6 + data.length);
        ByteBuffer buf = ByteBuffer.allocate(length+2).order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort(length);
        buf.put((byte)0x02);
        buf.put(command);
        buf.put((byte)0);
        buf.put((byte)0);
        buf.put((byte)0x7);
        buf.put((byte)getSequenceNumber());
        buf.put(data);
        
    	return buf.array();
	}
	
	public byte[] buildBasicCommand(byte flag, byte command, byte[] lightOrGroup,byte[] data){
		short length = (short) (14 + data.length);
        ByteBuffer buf = ByteBuffer.allocate(length+2).order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort(length);
        buf.put((byte)flag);
        buf.put(command);
        buf.put((byte)0);
        buf.put((byte)0);
        buf.put((byte)0x7);
        buf.put((byte)getSequenceNumber());
        buf.put(lightOrGroup);
        buf.put(data);
    	return buf.array();
	}
	
	public byte[] buildCommand(byte command,int groupIndex, byte[] data){
        ByteBuffer buf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        buf.put((byte)groupIndex);
        buf.put((byte)0);
        buf.put((byte)0);
        buf.put((byte)0);
        buf.put((byte)0);
        buf.put((byte)0);
        buf.put((byte)0);
        buf.put((byte)0);
    	return buildBasicCommand((byte)0x02, command, buf.array(), data);
	}
	
	public byte[] buildLightCommand(byte command,byte[] lightAddr,byte[] data){
    	return buildBasicCommand((byte)0x00, command, 
    				ByteBuffer.allocate(8)
    					.order(ByteOrder.LITTLE_ENDIAN)
    					.put(lightAddr)
    					.array(),
    			data);
	}
	
	public void send(byte[] data) throws IOException{
		outputStream.write(data);
		outputStream.flush();
	}
	public byte[] recv() throws IOException{
		int lengthsize=2;
		byte[] data = readFromInputStreamBytes(lengthsize);
		int length = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getShort();
		
		int expected = length+lengthsize-data.length;
		return readFromInputStreamBytes(expected);
	}

	private byte[] readFromInputStreamBytes(int len) throws IOException{
		byte[] result = new byte[len];
		int alreadyRead = 0;
		while(alreadyRead != len)
			alreadyRead+=inputStream.read(result, alreadyRead, len-alreadyRead);
		return result;
	}
	
	public byte[] buildOnOff(Luminary luminary, boolean on) {
		// TODO Auto-generated method stub
		return luminary.buildCommand(Commands.COMMAND_ONOFF,on?new byte[]{0x1}: new byte[]{0x0});
	}

	public byte[] buildLuminance(Luminary luminary, byte luminance, short timeInMilliseconds) {
		// TODO Auto-generated method stub
		ByteBuffer buf = ByteBuffer.allocate(3).order(ByteOrder.LITTLE_ENDIAN);
        buf.put((byte)luminance);
        buf.putShort(timeInMilliseconds);

		
		return luminary.buildCommand(Commands.COMMAND_LUMINANCE,buf.array());
	}

	public byte[] buildColor(Luminary luminary, byte r, byte g, byte b, short timeInMilliseconds) {
		ByteBuffer buf = ByteBuffer.allocate(6).order(ByteOrder.LITTLE_ENDIAN);
        buf.put(r);
        buf.put(g);
        buf.put(b);
        buf.put((byte)0xff);
        buf.putShort(timeInMilliseconds);
		return luminary.buildCommand(Commands.COMMAND_COLOUR, buf.array());
	}
	
	public byte[] buildAllLightStatus(byte flag){
		return buildGlobalCommand(Commands.COMMAND_ALL_LIGHT_STATUS, new byte[]{flag});
	}
	
	public byte[] buildGroupList(){
		return buildGlobalCommand(Commands.COMMAND_GROUP_LIST, new byte[]{});
	}
	
	public void update_all_light_status() throws IOException{
		byte[] data = buildAllLightStatus((byte)1);
		send(data);
		data = recv();
		int num = ByteBuffer.wrap(data, 6, 2).getShort(); //anzahl lampen
		
		int status_len=50;
		
		for(int i=0;i<num;i++){
			int pos = 9+i*status_len;
			ByteBuffer bb = ByteBuffer.wrap(data,pos,status_len).order(ByteOrder.LITTLE_ENDIAN);
			bb.getShort();
			byte[] addr = new byte[8];
			for(int j=0;j<8;j++){
				addr[j]=bb.get();
			}
			byte[] statbuf = new byte[16];
			for(int j=0;j<16;j++){
				statbuf[j]=bb.get();
			}
			//stat.replace("\0", "");
			
			
			
			byte[] namebuf = new byte[16];
			for(int j=0;j<16;j++){
				namebuf[j]=bb.get();
			}
			String name = new String(namebuf,"cp437");
			name = name.trim();
			//name.replace("\0", "");
			
			if(!lights.containsKey(addr)){
				lights.put(addr, new Light(this,name,addr));
			}
			
			Light currentLight = lights.get(addr);
			ByteBuffer statbb = ByteBuffer.wrap(statbuf).order(ByteOrder.LITTLE_ENDIAN);
			statbb.position(8);
			byte onoff = statbb.get();
			byte luminance = statbb.get();
			short temp = statbb.getShort();
			byte r=statbb.get();
			byte g=statbb.get();
			byte b=statbb.get();
			currentLight.updateStatus(onoff,luminance,temp,r,g,b);
		}
		
	}
	public void groupInfo(Group g) throws IOException{
		ArrayList<Light> lights = new ArrayList<>();
		byte[] data = buildGroupInfo(g);
		send(data);
		data = recv();
		ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		bb.position(6);
		int idx = bb.getShort();
		byte[] namebuf = new byte[16];
		for(int j=0;j<16;j++){
			namebuf[j]=bb.get();
		}
		String name=new String(namebuf,"cp437");
		int num=bb.get();
		
		
		for(int i=0;i<num;i++){
			int pos = 6+19+i*8;
			bb.position(pos);
			byte[] addr = new byte[8];
			for(int j=0;j<8;j++){
				addr[j]=bb.get();
			}
			lights.add(this.lights.get(addr));
		}
		g.lights=lights;
		
		
	}
	
	
	
	public HashMap<String, Group> groupList() throws IOException{
		HashMap<String,Group> groups = new HashMap<>();
		byte[] data = buildGroupList();
		send(data);
		data = recv();
		int num = ByteBuffer.wrap(data, 6, 2).getShort(); //anzahl lampen
		
		for(int i=0;i<num;i++){
			int pos = 9+i*18;
			ByteBuffer bb = ByteBuffer.wrap(data,pos,18).order(ByteOrder.LITTLE_ENDIAN);
			int idx = bb.getShort();
			
			byte[] namebuf = new byte[16];
			for(int j=0;j<16;j++){
				namebuf[j]=bb.get();
			}
			String name = new String(namebuf,"cp437");
			
			name.replace("\0", "");
			name.replace("\"", "");
			name = name.trim();
			
			groups.put(name,new Group(this,name,idx));
		}
		return groups;
	}
	
	public void updateGroupList() throws IOException{
		HashMap<String,Group> groups = groupList();
		for(Entry<String, Group> g:groups.entrySet()){
			groupInfo(g.getValue());
		}
		this.groups=groups;
	}

	private byte[] buildGroupInfo(Group g) {
		// TODO Auto-generated method stub
		return buildCommand(Commands.COMMAND_GROUP_INFO, g.index, new byte[]{});
	}

	public byte[] buildTemp(Luminary luminary, short temp, short timeInMilliseconds) {
		// TODO Auto-generated method stub
		return luminary.buildCommand(Commands.COMMAND_TEMP, ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putShort(temp).putShort(timeInMilliseconds).array());
	}

}
