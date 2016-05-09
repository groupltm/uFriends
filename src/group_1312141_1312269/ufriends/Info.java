package group_1312141_1312269.ufriends;

import java.io.Serializable;

public class Info implements Serializable {
	String _imagePath;
	String _name;

	int _age;
	boolean _sex;

	public Info() {
		_imagePath = "";
		_name = "anonymous";
		_age = 0;
		_sex = false;
	}
	
	public void setInfo(String imagePath, String name, int age, boolean sex){
		_imagePath = imagePath;
		_name = name;
		_age = age;
		_sex = sex;
	}
}
