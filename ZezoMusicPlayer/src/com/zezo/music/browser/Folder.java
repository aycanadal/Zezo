package com.zezo.music.browser;

import java.io.File;

public class Folder extends java.io.File {
	
	private String displayName;

	public Folder(File file) {
		super(file.getAbsolutePath());
		// TODO Auto-generated constructor stub
	}
	
	public String getDisplayName(){
		
		if(displayName != null && !displayName.isEmpty())
			return displayName;
		
		return getName();
		
	}
	
	public void setDisplayName(String displayName){
		
		this.displayName = displayName;
		
	}

}
