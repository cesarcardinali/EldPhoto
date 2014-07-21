package org.eldorado.eldphoto;

public class ArqAdapter {

	String Name, Path;
	
	public ArqAdapter(String Name, String Path){
		this.Name = Name;
		this.Path = Path;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getPath() {
		return Path;
	}

	public void setPath(String path) {
		Path = path;
	}
	
}
