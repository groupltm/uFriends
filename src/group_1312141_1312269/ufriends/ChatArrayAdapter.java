package group_1312141_1312269.ufriends;

import java.util.ArrayList;
import java.util.List;

import com.example.ufriends.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {
	 
    private TextView chatText;
    private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
    private Context context;
   
 
    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }
 
    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }
 
    public int getCount() {
        return this.chatMessageList.size();
    }
 
    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessageObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (chatMessageObj.left) {
            row = inflater.inflate(R.layout.list_chat_left_item, null);
        }else{
            row = inflater.inflate(R.layout.list_chat_right_item, null);
        }
        chatText = (TextView) row.findViewById(R.id.txtMsg);
        chatText.setText(chatMessageObj.message);
        return row;
    }
}
