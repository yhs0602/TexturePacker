package com.kyunggi.texturepacker;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import paul.arian.fileselector.*;
import android.graphics.*;
import android.util.*;

public class MainActivity extends Activity implements View.OnClickListener
{

	private String TAG="TexturePacker";
	@Override
	public void onClick(View p1)
	{
		// TODO: Implement this method
		switch (p1.getId())
		{
			case R.id.btAdd:
				Intent intent = new Intent(getBaseContext(), FileSelectionActivity.class);
                startActivityForResult(intent, 0);
				break;
			case R.id.btCopy:
				setClipboard(this, etJava.getText().toString());
				Toast.makeText(this, "Copied!", 2).show();
				break;
			case R.id.btCreate:
				int totDim=	npTotDim.getValue();
				int dim= npDim.getValue();
				totDim = roundUpToPowerOfTwo(totDim);
				dim = roundUpToPowerOfTwo(dim);
				CreateAlias(totDim, dim);
				break;
		}
		return ;
	}

	class CoordInfo
	{
		String name="";
		RectF rect=new RectF();
	}
	private void CreateAlias(int totDim, int  dim)
	{
		// TODO: Implement this method
		Bitmap alias=Bitmap.createBitmap(totDim, totDim, Bitmap.Config.ARGB_8888);
		Canvas canvas=new Canvas(alias);
		int numfiles=files.length;
		//int totdimlen=dim*numfiles;
		int col=totDim / dim;
		int row=numfiles / col + 1;
		int left=numfiles % col;
		CoordInfo[] coords=new CoordInfo[numfiles];
		if (row > col)
		{
			Toast.makeText(this, "Dim is too small", 3).show();
			alias.recycle();
			return;
		}
		Rect dst=new Rect();
		for (int i=0;i < row - 1;++i)
		{
			for (int j=0;j < col;++j)
			{
				File file=files[i * col + j];
				coords[i*col+j]=new CoordInfo();
				coords[i*col+j].name=file.getName().replaceAll(".png","").replaceAll(".jpg","");
				
				Bitmap thebit=BitmapFactory.decodeFile(file.getAbsolutePath());
				if (thebit == null)
				{
					Log.e(TAG,"thebit null");
					continue;
				}
				dst.set(j * dim, i * dim, (j + 1) * dim, (i + 1) * dim);
				coords[i*col+j].rect=new RectF((float)dst.left/totDim,(float)dst.top/totDim,(float)dst.right/totDim,(float)dst.bottom/totDim);
				canvas.drawBitmap(thebit, (Rect)null, dst, (Paint)null);
				thebit.recycle();
			}
		}
		for (int j=0;j < left;++j)
		{
			File file=files[(row-1) * col + j];
			coords[(row-1)*col+j]=new CoordInfo();
			coords[(row-1)*col+j].name=file.getName().replaceAll(".png","").replaceAll(".jpg","");
			
			Bitmap thebit=BitmapFactory.decodeFile(file.getAbsolutePath());
			if (thebit == null)
			{
				continue;
			}
			dst.set(j * dim, (row-1) * dim, (j + 1) * dim, row * dim);
			coords[(row-1)*col+j].rect=new RectF((float)dst.left/totDim,(float)dst.top/totDim,(float)dst.right/totDim,(float)dst.bottom/totDim);
			canvas.drawBitmap(thebit, (Rect)null, dst, (Paint)null);
		}
		try
		{
			FileOutputStream fos = new FileOutputStream("/sdcard/hello.png");
			alias.compress(Bitmap.CompressFormat.PNG, 100, fos);
		}
		catch (FileNotFoundException e)
		{
			Toast.makeText(this,Log.getStackTraceString(e),2).show();
		}
		String ls=System.lineSeparator();
		StringBuilder sb=new StringBuilder("public RectF getCoord(int id){"+ls+
											"	switch(id){"+ls);
		for(CoordInfo ci:coords)
		{
			sb.append("		case R.drawable.").append(ci.name).append(":").append(ls)
			.append("		return new RectF(").append((float)ci.rect.left).append("f,")
			.append(ci.rect.top).append("f,").append(ci.rect.right).append("f,").append(ci.rect.bottom).append("f);").append(ls);
		}
		sb.append("default: return new RectF();}").append(ls).append("}");
		etJava.setText(sb.toString());
		return ;
	}

	private int roundUpToPowerOfTwo(int totDim)
	{
		// TODO: Implement this method
		int i=1;
		while(totDim>i)i<<=1;
		return i;
	}

	Button btCreate;
	Button btAdd;
	Button btCopy;
	NumberPicker npTotDim;
	NumberPicker npDim;
	EditText etPath;
	TextView tvList;
	TextView tvInfo;
	EditText etJava;
	File[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		btCreate = (Button) findViewById(R.id.btCreate);
		btAdd = (	Button) findViewById(R.id.btAdd);
		btCopy = (	Button) findViewById(R.id.btCopy);
		npTotDim = (	NumberPicker) findViewById(R.id.npTotDim);
		npDim = (	NumberPicker) findViewById(R.id.npDim);
		etPath = (	EditText) findViewById(R.id.etPath);
		tvList = (	TextView) findViewById(R.id.tvList);
		tvInfo = (	TextView) findViewById(R.id.tvInfo);
		etJava = (	EditText) findViewById(R.id.etJava);
		npTotDim.setMaxValue(4096);
		npTotDim.setMinValue(32);
		npTotDim.setValue(4096);
		npDim.setMaxValue(2048);
		npTotDim.setMinValue(4);
		npTotDim.setValue(128);
		btCreate.setOnClickListener(this);
		btAdd.setOnClickListener(this);
		btCopy.setOnClickListener(this);
		return;
    }
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
        if (requestCode == 0 && resultCode == RESULT_OK)
		{
            ArrayList<File> Files = (ArrayList<File>) data.getSerializableExtra(FILES_TO_UPLOAD); //file array list
            String [] files_paths = new String[Files.size()]; //string array
            int i = 0;
			files = new File[Files.size()];
			StringBuilder sb=new StringBuilder();
            for (File file : Files)
			{
                //String fileName = file.getName();
                String uri = file.getAbsolutePath();
                files_paths[i] = uri.toString(); //storing the selected file's paths to string array files_paths
				files[i] = new File(files_paths[i]);
				sb.append("File " + i + ": " + files_paths[i] + System.lineSeparator());
                i++;
            }
			etPath.setText(sb.toString());
			tvInfo.setText(sb.toString() + "Total " + i + " files");
        }
		else
		{
        }

    }
	//https://stackoverflow.com/a/28780585/8614565
	private void setClipboard(Context context, String text)
	{
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
		{
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
		}
		else
		{
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
			clipboard.setPrimaryClip(clip);
		}
	}
	private static final String FILES_TO_UPLOAD = "upload";
}
