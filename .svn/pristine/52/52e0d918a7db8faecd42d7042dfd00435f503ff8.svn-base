package com.tuixin11sms.tx.utils;


import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

public class ContentTextWatcher implements TextWatcher {

	int length;
	Context context;
	EditText editText;
	public ContentTextWatcher(int length,Context context,EditText view){
		super();
		this.length = length;
		this.context = context;
		this.editText = view;
	}
	@Override
	public void afterTextChanged(Editable s) {
		//

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if(s.length() > length){
			Toast.makeText(context, "字数不得多于"+ length +"字", Toast.LENGTH_SHORT).show();
			editText.setText(editText.getText().subSequence(0, length));
			editText.setSelection(editText.getText().length());
		}
			

	}

}
