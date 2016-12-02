/* Omayr Abdelgany
 * U54298732
 * 11/16/2016
 * 
 * PA3: Fish.java 
 * This fish object avoids its predator, the shark. 
 * It also eats food, intentionally moving towards the food as it falls through the tank
 * and also as it lies on the tank floor.
 * 
 */

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.gl2.GLUT;

import java.util.*;

public class Fish {
	private GLUT glut;
	public float x, y, z, last_x, last_y, last_z;
	private int fish_object;
	private int tail_object;
	private int body_object;
	private int box_object;

	private float scale;
	private boolean is_dead;

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
	
	private float sharkPotentialScalar;
	private float wallPotentialScalar;
	private float foodPotentialScalar;
	
	public float boundingSphereRadius;
	private boolean showBoundingSphere;
	
	private Shark predator = null;
	private Coord color;
	private Random rand;
	private Vivarium v;

	public Fish(float _x, float _y, float _z, float _scale, float _tail_speed, Vivarium _v) {
		glut = new GLUT();
		rand = new Random();
		x = last_x = _x;
		y = last_y = _y;
		z = last_z = _z;
		fish_object = tail_object = body_object = 0;
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
		
		sharkPotentialScalar = 0.25f;
		wallPotentialScalar = 0.1f;
		foodPotentialScalar = -0.2f;
		
		boundingSphereRadius = 0.35f;
		showBoundingSphere = false;
		
		v = _v;
		
		// Color of the fish (yellow).
		color = new Coord(1.0f, 1.0f, 0.0f);
		is_dead = false;
	}

	// Initialize the fish within the tank.
	public void init(GL2 gl) {
		bodyCreation(gl);
		tailCreation(gl);
		if (showBoundingSphere) {
			boundingSphere(gl);
		}
		fish_object = gl.glGenLists(1);
		gl.glNewList(fish_object, GL2.GL_COMPILE);
		const_disp_list(gl);
		gl.glEndList();
	}

	public void draw(GL2 gl) {
		gl.glPushMatrix();
		gl.glPushAttrib(gl.GL_CURRENT_BIT);
		gl.glColor3f( (float)color.x, (float)color.y, (float)color.z); // Orange
		gl.glCallList(fish_object);
		gl.glPopAttrib();
		gl.glPopMatrix();
	}

	public void update(GL2 gl) {
		calcDistances(gl);
		calcPotential();
		translate();
		if (!is_dead){
			moveTailAndBody();
		}
		gl.glNewList(fish_object, GL2.GL_COMPILE);
		const_disp_list(gl);
		gl.glEndList();
	}

	private void const_disp_list(GL2 gl) {
		gl.glPushMatrix();

		// gl.glTranslatef(x, y, z);
		
		// Find a rotation matrix that points object in direction of movement
		float dx = last_x - x;
		// TODO this dy is not working... its messing up the fish for some
		// reason
		//float dy = last_y - y;
		float dy = 0.0f;
		float dz = last_z - z;

		float mag = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		float[] v = new float[3];
		v[0] = dx / mag;
		v[1] = dy / mag;
		v[2] = dz / mag;

		// up vector
		float[] up = { 0.0f, 1.0f, 0.0f };

		float[] left = { v[1] * up[2] - up[1] * v[2], v[0] * up[2] - up[0] * v[2], v[0] * up[1] - up[0] * v[1] };
		// normalize
		mag = (float) Math.sqrt(left[0] * left[0] + left[1] * left[1] + left[2] * left[2]);
		left[0] = left[0] / mag;
		left[1] = left[1] / mag;
		left[2] = left[2] / mag;

		// perpendicular up
		float[] perpUp = { left[1] * v[2] - v[1] * left[2], left[0] * v[2] - v[0] * left[2], left[0] * v[1] - v[0] * left[1] };
		// normalize
		mag = (float) Math.sqrt(perpUp[0] * perpUp[0] + perpUp[1] * perpUp[1] + perpUp[2] * perpUp[2]);
		perpUp[0] = perpUp[0] / mag;
		perpUp[1] = perpUp[1] / mag;
		perpUp[2] = perpUp[2] / mag;

		float[] rotationMatrix = { left[0], left[1], left[2], 0.0f, perpUp[0], perpUp[1], perpUp[2], 0.0f, v[0], v[1], v[2], 0.0f, x, y, z, 1.0f };
		gl.glMultMatrixf(rotationMatrix, 0);
		
		if (is_dead) gl.glRotatef(-90, 0, 0, 1);
		
		// Rotate the fish's tail.
		gl.glPushMatrix();
		gl.glRotatef(tail_angle, 0, 1, 0);
		gl.glCallList(tail_object);
		gl.glPopMatrix();
		
		// Rotate the fish's body.
		gl.glPushMatrix();
		gl.glRotatef(body_angle, 0, 1, 0);
		gl.glCallList(body_object);
		gl.glPopMatrix();
		
		// If showing bounding sphere, add to fish_object display list
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
	
	// Create the parts of the fish using the next two functions.
	private void bodyCreation(GL2 gl) {
		// body
		body_object = gl.glGenLists(1);
		gl.glNewList(body_object, GL2.GL_COMPILE);
		gl.glPushMatrix();
		gl.glScalef(0.4f, 0.6f, 1);
		gl.glTranslatef(0, 0, -0.09f);
		glut.glutSolidSphere(0.2, 36, 24);
		gl.glPopMatrix();
		gl.glEndList();
	}

	private void tailCreation(GL2 gl) {
		// tail
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
		while (i < circle_points) {
			angle = 2 * Math.PI * i / circle_points;
			gl.glVertex2f((float) Math.cos(angle) * 0.1f, (float) Math.sin(angle) * 0.1f);
			i++;
		}
		gl.glEnd();
		gl.glPopMatrix();
		gl.glEndList();
	}
	
	// Set the wiggle limits of the fish's movement.
	private void moveTailAndBody() {
		tail_angle = tail_angle + (tail_speed * tail_direction);
		body_angle = body_angle + (body_speed * tail_direction * -1);

		if (tail_angle > 10 || tail_angle < -10) tail_direction = tail_direction * -1;

	}
	
	// Uses an exponential potential function to avoid walls, sharks, and go after food.
	private void calcPotential() {
		Coord p = new Coord(x,y,z);
		Coord q1 = new Coord(predator.x, predator.y, predator.z);
		Coord q2 = new Coord(3.6, y, z);
		Coord q3 = new Coord(-3.6, y, z);
		Coord q4 = new Coord(x, 1.9, z);
		Coord q5 = new Coord(x, -1.9, z);
		Coord q6 = new Coord(x, y, 1.9);
		Coord q7 = new Coord(x, y, -1.9);
		Coord[] coords = {potentialFunction(p,q1,sharkPotentialScalar), potentialFunction(p,q2, wallPotentialScalar), 
				potentialFunction(p,q3,wallPotentialScalar), potentialFunction(p,q4,wallPotentialScalar),
				potentialFunction(p,q5,wallPotentialScalar), potentialFunction(p,q6,wallPotentialScalar), 
				potentialFunction(p,q7,wallPotentialScalar)};
		Coord sumResult = add(coords);
		for (Food f : v.presentFood) {
			Coord qi = new Coord(f.x, f.y, f.z);
			qi = potentialFunction(p, qi, foodPotentialScalar);
			Coord[] m = {sumResult, qi};
			sumResult = add(m);
		}
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
	
	// Computes the distances between food and sharks. If the fish collide's with a shark, it dies and floats to the top of the tank.
	private void calcDistances(GL2 gl){
		Coord a = new Coord(x,y,z);
		// food
		for (Food f : v.presentFood) {
			Coord b = new Coord(f.x,f.y,f.z);
			if (distance(a, b) < 0.3) {
				gl.glDeleteLists(f.food_object, 1);
				f.eaten();
			}
		}
		
		Coord shark = new Coord(predator.x*predator.scale, predator.y*predator.scale, predator.z*predator.scale);
		if (distance(a, shark) < predator.boundingSphereRadius) {
			color = new Coord(1,0,0);
			predator.removePrey();
			is_dead = true;
		}
	}
	
	// This function moves the fish around the tank by using a combination of the potential functions I defined above
	// and by switching the direction on the fish when it hits a wall.
	private void translate() {
		if (is_dead) {
			if (y < 1.9) y = y + translation_speed_y;
		} else {
			last_x = x;
			last_y = y;
			last_z = z;
			x += translation_speed_x * translation_dir_x;
			y += translation_speed_y * translation_dir_y;
			z += translation_speed_z * translation_dir_z;
			
			float n = rand.nextFloat();
			while (n < 0.2f) n = rand.nextFloat();
			
			if (x > 3.6 || x < -3.6) {
				// If n is not large enough to pull it below the constraints of the tank, it will get stuck.
				// Set x at the constraint so this does not happen.
				x = 3.6f;
				if (translation_dir_x > 0) translation_dir_x = -1 * n;
				else {
					x *= -1;
					translation_dir_x = n;
				}
			}
			if (y > 1.9 || y < -1.9) {
				y = 1.9f;
				if (translation_dir_y > 0) translation_dir_y = -1 * n;
				else {
					y *= -1;
					translation_dir_y = n;
				}
			}
			if (z > 1.9 || z < -1.9) {
				z = 1.9f;
				if (translation_dir_z > 0) translation_dir_z = -1 * n;
				else {
					z*= -1;
					translation_dir_z = n;
				}
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
	
	// To access fish location
	public void addPredator(Shark s) {
		predator = s;
	}

}

