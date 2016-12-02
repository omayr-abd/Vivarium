/* Omayr Abdelgany
 * U54298732
 * 11/16/2016
 * 
 * PA3: Vivarium.java 
 * This is the Vivarium class, where all components of the program are initialized,
 * updated, added, and drawn.
 *  
 */

import javax.media.opengl.*;
import com.jogamp.opengl.util.*;
import java.util.*;

public class Vivarium
{
  private Tank tank;
  public Fish preyFish;
  public Shark predatorShark;
  public ArrayList<Food> presentFood;
  private boolean addedFood;
  private boolean addedFish;

  //Create the vivarium with the tank, a fish, a shark, and a list of all the food.
  public Vivarium()
  {
	tank = new Tank( 8.0f, 4.0f, 4.0f );
	preyFish = new Fish(0, 0, 0, 0.8f, 0.7f, this);
	predatorShark = new Shark(-2, 0, -1, 1.8f, 0.7f, this);
	presentFood = new ArrayList<Food>();
	addedFood = false;
	addedFish = false;

  }
  
  // Initialize all of our objects in the vivarium.
  public void init( GL2 gl )
  {
	  tank.init( gl );
	  preyFish.init( gl );
	  predatorShark.init( gl );
	  for (Food food : presentFood) {
		  food.init( gl );
	  }
	    
	  preyFish.addPredator(predatorShark);
	  predatorShark.addPrey(preyFish);
  }

  // Update each object in the vivarium.
  public void update( GL2 gl )
  {
	  tank.update( gl );
	  // Here we reinitialize the fish if it has been reset.
	  if (addedFish) {
		  preyFish.init(gl);
		  addedFish = false;
	  } else {
		  preyFish.update( gl );
	  }
	  
	  //Update the shark.
	  predatorShark.update( gl );
	    
	  // Here we initialize food that the user added by pressing the F/f key.
	  if (addedFood) {
		  for (Food food : presentFood) {
			  food.init( gl );
	  	}
		  addedFood = false;
	  }
	  //Update all the food.
	  for (Food food : presentFood) {
		  food.update( gl );
	  }
	    
	  //Remove all of the food that has been eaten.
	  for (ListIterator<Food> iter = this.presentFood.listIterator(); iter.hasNext();) {
		  Food f = iter.next();
	      if (f.isEaten()) {
	    	  iter.remove();
	      }
	  }
  }

  // Reset the fish when the user presses the N/n key.
  public void newFish() 
  {
	  preyFish = new Fish(0, 0, 0, 0.8f, 0.5f, this);
	  addedFish = true;
	  predatorShark.addPrey(preyFish);
	  preyFish.addPredator(predatorShark);
  }
  
  // Add new food to tank when the user presses the F/f key.
  public void addFood() 
  {
	  Food f = new Food();
	  presentFood.add(f);
	  addedFood=true;
  }
  
  // Draw the tank, fish, shark and food.
  public void draw( GL2 gl )
  {
	  tank.draw( gl );
	  preyFish.draw( gl );
	  predatorShark.draw( gl );
	  for (Food food : presentFood) {
		  food.draw( gl );
	  }

}
  
}
  
