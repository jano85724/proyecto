package edu.proyint.objetovista;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Date;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;
import edu.dhbw.andar.ARToolkit;
import edu.dhbw.andar.AndARActivity;
import edu.dhbw.andar.exceptions.AndARException;
import edu.dhbw.andarmodelviewer.R;
import edu.proyint.objetovista.graficos.LightingRenderer;
import edu.proyint.objetovista.graficos.Marcador;
import edu.proyint.objetovista.modelos.Model;
import edu.proyint.objetovista.parser.ObjParser;
import edu.proyint.objetovista.parser.ParseException;
import edu.proyint.objetovista.parser.Util;
import edu.proyint.objetovista.util.AssetsFileUtil;
import edu.proyint.objetovista.util.BaseFileUtil;
import edu.proyint.objetovista.util.SDCardFileUtil;


public class VistaModeloAumentadoActivity extends AndARActivity implements SurfaceHolder.Callback {
	
	/**
	 * View a file in the assets folder
	 */
	public static final int TYPE_INTERNAL = 0;
	/**
	 * View a file on the sd card.
	 */
	public static final int TYPE_EXTERNAL = 1;
	
	public static final boolean DEBUG = false;
	
		

	private Model model;
	private Marcador model3d;
	private ProgressDialog waitDialog;
	private Resources res;
	
	ARToolkit artoolkit;
	
	public VistaModeloAumentadoActivity() {
		super(false);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setNonARRenderer(new LightingRenderer());
		res=getResources();
		artoolkit = getArtoolkit();		
		getSurfaceView().setOnTouchListener(new TouchEventHandler());
		getSurfaceView().getHolder().addCallback(this);
		

	}
	
	/**
	 * Inform the user about exceptions that occurred in background threads.
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		System.out.println("");
	}
	    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	super.surfaceCreated(holder);
    	//load the model
    	//this is done here, to assure the surface was already created, so that the preview can be started
    	//after loading the model
    	if(model == null) {
			waitDialog = ProgressDialog.show(this, "", 
	                getResources().getText(R.string.loading), true);
			waitDialog.show();
			new ModelLoader().execute();
		}
    }

    class TouchEventHandler implements OnTouchListener {
    	
    	private float lastX=0;
    	private float lastY=0;

		/* handles the touch events.
		 * the object will either be scaled, translated or rotated, dependen on the
		 * current user selected mode.
		 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
		 */
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(model!=null) {
				switch(event.getAction()) {
					//Action started
					default:
					case MotionEvent.ACTION_DOWN:
						lastX = event.getX();
						lastY = event.getY();
						break;
					
					//Action ended
					case MotionEvent.ACTION_CANCEL:	
					case MotionEvent.ACTION_UP:
						lastX = event.getX();
						lastY = event.getY();
						break;
				}
			}
			return true;
		}
    	
    }
    
	private class ModelLoader extends AsyncTask<Void, Void, Void> {
		
		
    	@Override
    	protected Void doInBackground(Void... params) {
    		
			Intent intent = getIntent();
			Bundle data = intent.getExtras();
			int type = data.getInt("type");
			String modelFileName = data.getString("name");
			BaseFileUtil fileUtil= null;
			File modelFile=null;
			switch(type) {
			case TYPE_EXTERNAL:
				fileUtil = new SDCardFileUtil();
				modelFile =  new File(URI.create(modelFileName));
				modelFileName = modelFile.getName();
				fileUtil.setBaseFolder(modelFile.getParentFile().getAbsolutePath());
				break;
			case TYPE_INTERNAL:
				fileUtil = new AssetsFileUtil(getResources().getAssets());
				fileUtil.setBaseFolder("models/");
				break;
			}
			
			//read the model file:						
			if(modelFileName.endsWith(".obj")) {
				ObjParser parser = new ObjParser(fileUtil);
				try {
					if(Config.DEBUG)
						Debug.startMethodTracing("AndObjViewer");
					if(type == TYPE_EXTERNAL) {
						//an external file might be trimmed
						BufferedReader modelFileReader = new BufferedReader(new FileReader(modelFile));
						String shebang = modelFileReader.readLine();				
						if(!shebang.equals("#trimmed")) {
							//trim the file:			
							File trimmedFile = new File(modelFile.getAbsolutePath()+".tmp");
							BufferedWriter trimmedFileWriter = new BufferedWriter(new FileWriter(trimmedFile));
							Util.trim(modelFileReader, trimmedFileWriter);
							if(modelFile.delete()) {
								trimmedFile.renameTo(modelFile);
							}					
						}
					}
					if(fileUtil != null) {
						BufferedReader fileReader = fileUtil.getReaderFromName(modelFileName);
						if(fileReader != null) {
							model = parser.parse("Model", fileReader);
							model3d = new Marcador(model);
						}
					}
					if(Config.DEBUG)
						Debug.stopMethodTracing();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
    		return null;
    	}
    	@Override
    	protected void onPostExecute(Void result) {
    		super.onPostExecute(result);
    		waitDialog.dismiss();
    		
    		//register model
    		try {
    			if(model3d!=null)
    				artoolkit.registerARObject(model3d);
			} catch (AndARException e) {
				e.printStackTrace();
			}
			startPreview();
    	}
    }
	
	
	
	
}
