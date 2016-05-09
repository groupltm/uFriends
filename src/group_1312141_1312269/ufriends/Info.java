package group_1312141_1312269.ufriends;

import java.io.Serializable;

public class Info implements Serializable {
	byte[] _image;
	String _name;

	int _age;
	boolean _sex;

	public Info() {
		_image = null;
		_name = "anonymous";
		_age = 0;
		_sex = false;
	}
	
	public void setInfo(byte[] image, String name, int age, boolean sex){
		_image = image;
		_name = name;
		_age = age;
		_sex = sex;
	}
}
