package edu.proyint.objetovista.graficos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;
import edu.proyint.objetovista.VistaModeloAumentadoActivity;
import edu.proyint.objetovista.modelos.Vector3D;
import edu.proyint.objetovista.util.MemUtil;

/**
 * cares about 3d rendering of the scene
 */
public class Renderer implements GLSurfaceView.Renderer {
	
	private final Vector<Marcador> models;
	private final Vector3D cameraPosition = new Vector3D(0, 3, 50);
	
	// FPS stuff
	long frame=0,time,timebase=0;
	//end FPS stuff
	
	public Renderer(Vector<Marcador> models) {
		this.models = models;
	}
	
	public void addModel(Marcador model) {
		if(!models.contains(model)) {
			models.add(model);
		}
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		if(VistaModeloAumentadoActivity.DEBUG) {
			frame++;
			time=System.currentTimeMillis();
			if (time - timebase > 1000) {
				Log.d("fps: ", String.valueOf(frame*1000.0f/(time-timebase)));
			 	timebase = time;		
				frame = 0;
			}
		}
		//String extensions = gl.glGetString(GL10.GL_EXTENSIONS);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		GLU.gluLookAt(gl, cameraPosition.x, cameraPosition.y, cameraPosition.z,
				0, 0, 0, 0, 1, 0);
		for (Iterator<Marcador> iterator = models.iterator(); iterator.hasNext();) {
			Marcador model = iterator.next();
			model.draw(gl);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0,0,width,height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();  
        GLU.gluPerspective(gl, 45.0f, ((float)width)/height, 0.11f, 100f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();        
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(1,1,1,1);
		
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
		//gl.glEnable(GL10.GL_CULL_FACE);
		
		//lighting stuff
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glDisable(GL10.GL_COLOR_MATERIAL);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_LIGHTING);
		float[] ambientlight = {.6f, .6f, .6f, 1f};
		float[] diffuselight = {1f, 1f, 1f, 1f};
		float[] specularlight = {1f, 1f, 1f, 1f};
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, MemUtil.makeFloatBuffer(ambientlight));
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, MemUtil.makeFloatBuffer(diffuselight));
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, MemUtil.makeFloatBuffer(specularlight));
		gl.glEnable(GL10.GL_LIGHT0);
		
		//initialize the models
		for (Iterator<Marcador> iterator = models.iterator(); iterator.hasNext();) {
			Marcador model = iterator.next();
			model.init(gl);
		}
		
	}

}
