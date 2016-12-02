/* Omayr Abdelgany
 * U54298732
 * 11/16/2016
 * 
 * PA3: Shark.java 
 * This shark object chases after its prey, the fish.
 * It also eats food, though it does not intentionally move towards the food.
 * 
 */

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.gl2.GLUT;

import java.util.*;

public class Shark {
	private GLUT glut;
	public float x, y, z, last_x, last_y, last_z;
	private int shark_object;
	private int tail_object;
	private int body_object;
	private int fin_object;
	private int box_object;

	public float scale;

	private float tail_angle;
	private float tail_speed;
	private float tail_direction;
	private float body_speed;
	private float body_angle;

	private float translation_dir_x;
	private float translation_dir_y;
	private float translation_dir_z;
	private float translation_speed_x;
	private float translation_speed_y;
	private float translation_speed_z;
	private float fishPotentialScalar;
	private float wallPotentialScalar;
	
	public float boundingSphereRadius;
	private boolean showBoundingSphere;

	private Random rand;
	private Vivarium v;
	private Fish prey = null;

	public Shark(float _x, float _y, float _z, float _scale, float _tail_speed, Vivarium _v) {
		glut = new GLUT();
		rand = new Random();
		x = last_x = _x;
		y = last_y = _y;
		z = last_z = _z;
		shark_object = tail_object = body_object = 0;
		scale = _scale;
		tail_speed = _tail_speed;
		tail_angle = body_angle = 0;
		tail_direction = 1;
		body_speed = tail_speed / 4;
		translation_dir_x = rand.nextFloat();
		translation_dir_y = rand.nextFloat();
		translation_dir_z = rand.nextFloat();
		translation_speed_x = 0.005f;
		translation_speed_y = 0.005f;
		translation_speed_z = 0.005f;

		boundingSphereRadius = 0.35f * scale;
		showBoundingSphere = false;
		fishPotentialScalar = -0.17f;
		wallPotentialScalar = 0.05f;
		
		v = _v;
	}
	
	// Initialize the shark within the tank. 
	public void init(GL2 gl) {
		bodyCreation(gl);
		tailCreation(gl);
		finCreation(gl);
		if (showBoundingSphere){
			boundingSphere(gl);
		}
		shark_object = gl.glGenLists(1);
		gl.glNewList(shark_object, GL2.GL_COMPILE);
		const_disp_list(gl);
		gl.glEndList();
	}

	public void draw(GL2 gl) {
		gl.glPushMatrix();
		gl.glPushAttrib(gl.GL_CURRENT_BIT);
		//Color of shark
		gl.glColor3f(0.321f, 0.648f, 0.816f);
		gl.glCallList(shark_object);
		gl.glPopAttrib();
		gl.glPopMatrix();
	}

	public void update(GL2 gl) {
		calcPotential();
		calcDistances(gl);
		translate();
		moveBodyAndTail();
		gl.glNewList(shark_object, GL2.GL_COMPILE);
		const_disp_list(gl);
		gl.glEndList();
	}

	private void const_disp_list(GL2 gl) {
		gl.glPushMatrix();
		// gl.glTranslatef(x, y, z);
		gl.glScalef(scale, scale, scale);
		
		// Find a rotation matrix that points object in direction of movement
		float dx = last_x - x;
		// TODO this dy is not working... its messing up the fish for some
		// reason
		//float dy = last_y - y;
		float dy = 0.0f;
		float dz = last_z - z;

		float magnitude = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		float[] v = new float[3];
		v[0] = dx / magnitude;
		v[1] = dy / magnitude;
		v[2] = dz / magnitude;

		// up vector
		float[] up = { 0.0f, 1.0f, 0.0f };

		float[] left = { v[1] * up[2] - up[1] * v[2], v[0] * up[2] - up[0] * v[2], v[0] * up[1] - up[0] * v[1] };
		// normalize
		magnitude = (float) Math.sqrt(left[0] * left[0] + left[1] * left[1] + left[2] * left[2]);
		left[0] = (left[0] / magnitude);
		left[1] = (left[1] / magnitude);
		left[2] = (left[2] / magnitude);

		// perpendicular up
		float[] perpUp = { v[1] * left[2] - left[1] * v[2], v[0] * left[2] - left[0] * v[2], v[0] * left[1] - left[0] * v[1] };
		
		// normalize
		magnitude = (float) Math.sqrt(perpUp[0] * perpUp[0] + perpUp[1] * perpUp[1] + perpUp[2] * perpUp[2]);
		perpUp[0] = perpUp[0] / magnitude;
		perpUp[1] = perpUp[1] / magnitude;
		perpUp[2] = perpUp[2] / magnitude;

		float[] rotationMatrix = { left[0], left[1], left[2], 0.0f, perpUp[0], perpUp[1], perpUp[2], 0.0f, v[0], v[1], v[2], 0.0f, x, y, z, 1.0f };
		gl.glMultMatrixf(rotationMatrix, 0);

		// Rotate the shark's tail.
		gl.glPushMatrix();
		gl.glRotatef(tail_angle, 0, 1, 0);
		gl.glCallList(tail_object);
		gl.glPopMatrix();
		
		// Rotate the shark's body.
		gl.glPushMatrix();
		gl.glRotatef(body_angle, 0, 1, 0);
		gl.glCallList(body_object);
		gl.glPopMatrix();

		// Draw the fin on the shark's head.
		gl.glPushMatrix();
		gl.glCallList(fin_object);
		gl.glPopMatrix();
		
		// Show bounding sphere for debugging
		gl.glPushMatrix();
		gl.glCallList(box_object);
		gl.glPopMatrix();

		gl.glPopMatrix();
	}

	// Function that draws a bounding sphere for debugging purposes
	private void boundingSphere(GL2 gl) {
		box_object=gl.glGenLists(1);
		gl.glNewList(box_object, GL2.GL_COMPILE);
		gl.glPushMatrix();
		glut.glutWireSphere(0.35, 36, 24);
		gl.glPopMatrix();
		gl.glEndList();
	}
	
	// Create the parts of the shark using the next three functions.
	private void bodyCreation(GL2 gl) {
		// Creation of the body.
		body_object = gl.glGenLists(1);
		gl.glNewList(body_object, GL2.GL_COMPILE);
		gl.glPushMatrix();
		gl.glScalef(0.4f, 0.6f, 1);
		gl.glTranslatef(0, 0, -0.09f);
		glut.glutSolidSphere(0.2, 36, 24);
		gl.glPopMatrix();
		gl.glEndList();
	}
	
	private void finCreation(GL2 gl) {
		// Creation of the fin.
		fin_object = gl.glGenLists(1);
		gl.glNewList(fin_object, GL2.GL_COMPILE);
		gl.glPushMatrix();
		gl.glScalef(0.5f, 1, 1);
		gl.glTranslatef(0, 0, -0.1f);
		gl.glRotatef(-75, 1, 0, 0);
		glut.glutSolidCone(0.1, 0.27, 20, 20);
		gl.glPopMatrix();
		gl.glEndList();
	}
	
	private void tailCreation(GL2 gl) {
		// Creation of the tail.
		tail_object = gl.glGenLists(1);
		gl.glNewList(tail_object, GL2.GL_COMPILE);
		gl.glPushMatrix();
		gl.glScalef(0.5f, 1, 1);
		gl.glTranslatef(0, 0, 0.35f);
		gl.glRotatef(-180, 0, 1, 0);
		glut.glutSolidCone(0.1, 0.35, 20, 20);
		int circle_points = 100;
		// create the end cap by drawing circles.
		gl.glBegin(gl.GL_POLYGON);
		double angle;
		int i = 0;
		while (i < circle_points){
			angle = 2 * Math.PI * i / circle_points;
			gl.glVertex2f((float) Math.cos(angle) * 0.1f, (float) Math.sin(angle) * 0.1f);
			i++;
		}
		gl.glEnd();
		gl.glPopMatrix();
		gl.glEndList();
	}

	// Set the wiggle limits of the shark's movement.
	private void moveBodyAndTail() {
		tail_angle = tail_angle + (tail_speed * tail_direction);
		body_angle = body_angle + (body_speed * tail_direction * -1);

		if (tail_angle > 10 || tail_angle < -10) tail_direction = tail_direction * -1;
	}
	
	// Computes the distances between the food and the shark.
	private void calcDistances(GL2 gl) {
		Coord a = new Coord(x*scale,y*scale,z*scale);
		// food
		for (Food f : v.presentFood) {
			Coord b = new Coord(f.x,f.y,f.z);
			if (distance(a, b) < 0.3) {
				gl.glDeleteLists(f.food_object, 1);
				f.eaten();
			}
		}
	}
	
	// Uses an exponential potential function to avoid walls and go after the prey.
	private void calcPotential() {
		Coord p = new Coord(x,y,z);
		Coord q1;
		if (prey != null){
			q1 = new Coord(prey.x, prey.y, prey.z);
		}
		else q1 = new Coord(10000,100000,100000);
		Coord q2 = new Coord(3.6, y, z);
		Coord q3 = new Coord(-3.6, y, z);
		Coord q4 = new Coord(x, 1.9, z);
		Coord q5 = new Coord(x, -1.9, z);
		Coord q6 = new Coord(x, y, 1.9);
		Coord q7 = new Coord(x, y, -1.9);
		Coord[] coords = {potentialFunction(p,q1,fishPotentialScalar), potentialFunction(p,q2, wallPotentialScalar), 
				potentialFunction(p,q3,wallPotentialScalar), potentialFunction(p,q4,wallPotentialScalar),
				potentialFunction(p,q5,wallPotentialScalar), potentialFunction(p,q6,wallPotentialScalar), 
				potentialFunction(p,q7,wallPotentialScalar)};
		Coord sumResult = add(coords);
		translation_dir_x += sumResult.x;
		translation_dir_y += sumResult.y;
		translation_dir_z += sumResult.z;
		
	}
	
	// Exponential potential function
	private Coord potentialFunction(Coord p, Coord q1, float scale) {
		float x = (float) (scale*(p.x - q1.x)*Math.pow(Math.E,-1*(Math.pow((p.x-q1.x), 2) + Math.pow((p.y-q1.y), 2) + Math.pow((p.z-q1.z), 2)) ));
		float y = (float) (scale*(p.y - q1.y)*Math.pow(Math.E,-1*(Math.pow((p.x-q1.x), 2) + Math.pow((p.y-q1.y), 2) + Math.pow((p.z-q1.z), 2)) ));
		float z = (float) (scale*(p.z - q1.z)*Math.pow(Math.E,-1*(Math.pow((p.x-q1.x), 2) + Math.pow((p.y-q1.y), 2) + Math.pow((p.z-q1.z), 2)) ));
		Coord potentialResult = new Coord(x, y, z);
		return potentialResult;
	}
	
	// This function moves the shark around the tank by using a combination of the potential functions I defined above
	// and by switching the direction on the shark when it hits a wall.
	private void translate() {
		last_x = x;
		last_y = y;
		last_z = z;
		x += translation_speed_x * translation_dir_x;
		y += translation_speed_y * translation_dir_y;
		z += translation_speed_z * translation_dir_z;

		float n = rand.nextFloat();
		while (n < 0.2f) n = rand.nextFloat();
		// Got the boundaries from the Fish class:
		// Divide the boundaries by the scale factor for the shark to keep it inside the tank b/c of different dimensions
		if (x > 3.6 / scale || x < -3.6 / scale) {
			// If n is not large enough to pull it below the constraints of the tank, it will get stuck.
			// Set x at the constraint so this does not happen.
			x = 3.6f / scale;
			if (translation_dir_x > 0) translation_dir_x = -1 * n;
			else {
				x *= -1;
				translation_dir_x = n;
			}
		}
		if (y > 1.8 / scale || y < -1.8 / scale) {
			y = 1.8f / scale;
			if (translation_dir_y > 0) translation_dir_y = -1 * n;
			else {
				y *= -1;
				translation_dir_y = n;
			}
		}
		if (z > 1.8 / scale || z < -1.8 / scale) {
			z = 1.8f / scale;
			if (translation_dir_z > 0) translation_dir_z = -1 * n;
			else {
				z *= -1;
				translation_dir_z = n;
			}
		}

	}
	
	// Coordinate helper functions
		private Coord add(Coord a, Coord b) {
			a.x += b.x;
			a.y += b.y;
			a.z += b.z;
			return a;
		}
		
		private Coord add(Coord[] b) {
			Coord ret = new Coord();
			for (Coord a : b) {
				ret.x += a.x;
				ret.y += a.y;
				ret.z += a.z;
			}
			return ret;
		}
		
		private float distance(Coord a, Coord b) {
			return (float) Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2) + Math.pow(a.z - b.z, 2));
		}
		
		// To keep track of fish coordinates
		public void addPrey(Fish fish) {
			prey = fish;
		}
		
		// If fish has been eaten, stop attacking
		public void removePrey() {
			prey = null;
		}

}