package instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Info implements Serializable {
	public String _imagePath;
	public String _name;
	public int _age;
	public boolean _sex;
	public int _status;

	public Info() {
		_imagePath = "";
		_name = "anonymous";
		_age = 0;
		_sex = false;
		_status = 2;
	}
	
	public void setInfo(String imagePath, String name, int age, boolean sex){
		_imagePath = imagePath;
		_name = name;
		_age = age;
		_sex = sex;
	} 
	
	public void setInfo(List<String> infoList){
		_imagePath = infoList.get(0);
		_name = infoList.get(1);
		_age = Integer.parseInt(infoList.get(2));
		if (infoList.get(3).equals("Male")){
			_sex = true;
		}else{
			_sex = false;
		}
	}
	
	public List<String> convertInfoToStringList(){
		List<String> infoList = new ArrayList<>();
		
		infoList.add(this._imagePath);
		infoList.add(this._name);
		infoList.add(String.valueOf(this._age));
		if (this._sex){
			infoList.add("Male");
		}else{
			infoList.add("Female");
		}
		
		return infoList;
	}
	
	protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
