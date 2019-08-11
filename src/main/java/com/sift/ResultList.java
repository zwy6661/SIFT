package com.sift;

public class ResultList {
	
	
	public static java.util.Map map=new java.util.HashMap<String,Info>();
	public static java.util.List result=new java.util.ArrayList<Info>();
	public static int index=0;
	class Info{
		
		private String name;
		private float sample;
		private String file;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public float getSample() {
			return sample;
		}
		public void setSample(float sample) {
			this.sample = sample;
		}
		public String getFile() {
			return file;
		}
		public void setFile(String file) {
			this.file = file;
		}
		
	}
}
