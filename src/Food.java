/* Omayr Abdelgany
 * U54298732
 * 11/16/2016
 * 
 * PA3: Food.java
 * This is the food object. It is an openGL object that draws food and drops it
 * down the tank from a random x and z point. It falls to the bottom for the 
 * fish or the shark to eat.
 * 
 */

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.gl2.GLUT;

import java.util.*;

public class Food {
	private GLUT glut;
	private Random rand;
	public int food_object;
	private boolean is_eaten;
	public float x, y, z;
	private float fall_speed;
	public float radius;
	
	
	public Food() {
		glut = new GLUT();
		rand = new Random();
		// Initialize the x and z at random positions, and the y at the top of the tank,
		// as if someone were dropping the food in there from the top.
		x = rand.nextFloat()*8 - 4;
		y = 2.0f;
		z = rand.nextFloat()*4 - 2;
		fall_speed = 0.01f;
		radius = 0.07f;
		is_eaten = false;
	}
	
	public void init(GL2 gl) {
		food_object = gl.glGenLists(1);
		gl.glNewList(food_object, GL2.GL_COMPILE);
		glut.glutSolidSphere(radius, 36, 24);
		gl.glEndList();
	}
	
	// This brings the food down to the bottom of the tank.
	// Increase the speed to make it fall faster.
	public void update(GL2 gl) {
		if (y > -1.9f) {
			y -= fall_speed;
		}
	}
	
	// Draw the food.
	public void draw(GL2 gl) {
		gl.glPushMatrix();
	    gl.glPushAttrib( GL2.GL_CURRENT_BIT );
	    gl.glTranslatef(x, y, z);
	    //gl.glColor3f( 0.625f, 0.32f, 0.176f); // brown
	    gl.glColor3f(0.85f, 0.55f, 0.20f); // orange
	    gl.glCallList( food_object );
	    gl.glPopAttrib();
	    gl.glPopMatrix();
	}
	
	// Functions to determine whether the food was eaten or not
	public boolean isEaten() {
		return is_eaten;
	}
	
	public void eaten() {
		is_eaten = true;
	}

}