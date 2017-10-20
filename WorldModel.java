import processing.core.PImage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

final class WorldModel
{
   private int numRows;
   private int numCols;
   private Background background[][];
   private Entity occupancy[][];
   private Set<Entity> entities;
   private static Point position;

   private static final int MINER_NUM_PROPERTIES = 7;
   private static final int MINER_ID = 1;
   private static final int MINER_COL = 2;
   private static final int MINER_ROW = 3;
   private static final int MINER_LIMIT = 4;
   private static final int MINER_ACTION_PERIOD = 5;
   private static final int MINER_ANIMATION_PERIOD = 6;
   private static final int OBSTACLE_NUM_PROPERTIES = 4;
   private static final int OBSTACLE_ID = 1;
   private static final int OBSTACLE_COL = 2;
   private static final int OBSTACLE_ROW = 3;
   private static final int ORE_NUM_PROPERTIES = 5;
   private static final int ORE_ID = 1;
   private static final int ORE_COL = 2;
   private static final int ORE_ROW = 3;
   private static final int ORE_ACTION_PERIOD = 4;
   private static final int SMITH_NUM_PROPERTIES = 4;
   private static final int SMITH_ID = 1;
   private static final int SMITH_COL = 2;
   private static final int SMITH_ROW = 3;
   private static final int VEIN_NUM_PROPERTIES = 5;
   private static final int VEIN_ID = 1;
   private static final int VEIN_COL = 2;
   private static final int VEIN_ROW = 3;
   private static final int VEIN_ACTION_PERIOD = 4;

   private static final int BGND_NUM_PROPERTIES = 4;
   private static final int BGND_ID = 1;
   private static final int BGND_COL = 2;
   private static final int BGND_ROW = 3;

   public Set<Entity> getEntities()
   {
      return entities;
   }

   public int getNumRows(){
      return numRows;
   }

   public int getNumCols(){
      return numCols;
   }

   public Point getPosition() {
      return position;
   }

    public WorldModel(int numRows, int numCols, Background defaultBackground)
   {
      this.numRows = numRows;
      this.numCols = numCols;
      this.background = new Background[numRows][numCols];
      this.occupancy = new Entity[numRows][numCols];
      this.entities = new HashSet<>();

      for (int row = 0; row < numRows; row++)
      {
         Arrays.fill(this.background[row], defaultBackground);
      }
   }

    public Optional<Entity> getOccupant(Point pos)
    {
       if (isOccupied(pos))
       {
          return Optional.of(getOccupancyCell(pos));
       }
       else
       {
          return Optional.empty();
       }
    }

    public void tryAddEntity(Entity entity)
    {
       if (isOccupied(entity.position))
       {
          // arguably the wrong type of exception, but we are not
          // defining our own exceptions yet
          throw new IllegalArgumentException("position occupied");
       }

       addEntity(entity);
    }

    public boolean parseVein(String[] properties,
                            ImageStore imageStore)
   {
      if (properties.length == VEIN_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[VEIN_COL]),
            Integer.parseInt(properties[VEIN_ROW]));
         Entity entity = pt.createVein(properties[VEIN_ID],
                 Integer.parseInt(properties[VEIN_ACTION_PERIOD]),
            Functions.getImageList(imageStore, Entity.VEIN_KEY));
         this.tryAddEntity(entity);
      }

      return properties.length == VEIN_NUM_PROPERTIES;
   }

   public void setBackgroundCell(Point pos,
                                 Background background)
   {
      this.background[pos.getY()][pos.getX()] = background;
   }

   public boolean parseSmith(String[] properties,
                             ImageStore imageStore)
   {
      if (properties.length == SMITH_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[SMITH_COL]),
            Integer.parseInt(properties[SMITH_ROW]));
         Entity entity = Entity.createBlacksmith(pt, properties[SMITH_ID],
                 Functions.getImageList(imageStore, Entity.SMITH_KEY));
         this.tryAddEntity(entity);
      }

      return properties.length == SMITH_NUM_PROPERTIES;
   }

   public boolean parseOre(String[] properties,
                           ImageStore imageStore)
   {
      if (properties.length == ORE_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[ORE_COL]),
            Integer.parseInt(properties[ORE_ROW]));
         Entity entity = pt.createOre(properties[ORE_ID],
                 Integer.parseInt(properties[ORE_ACTION_PERIOD]),
            Functions.getImageList(imageStore, Entity.ORE_KEY));
         this.tryAddEntity(entity);
      }

      return properties.length == ORE_NUM_PROPERTIES;
   }

   public boolean parseObstacle(String[] properties,
                                ImageStore imageStore)
   {
      if (properties.length == OBSTACLE_NUM_PROPERTIES)
      {
         Point pt = new Point(
            Integer.parseInt(properties[OBSTACLE_COL]),
            Integer.parseInt(properties[OBSTACLE_ROW]));
         Entity entity = pt.createObstacle(properties[OBSTACLE_ID],
                 Functions.getImageList(imageStore, Entity.OBSTACLE_KEY));
         this.tryAddEntity(entity);
      }

      return properties.length == OBSTACLE_NUM_PROPERTIES;
   }

   public boolean parseMiner(String[] properties,
                             ImageStore imageStore)
   {
      if (properties.length == MINER_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[MINER_COL]),
            Integer.parseInt(properties[MINER_ROW]));
         Entity entity = pt.createMinerNotFull(properties[MINER_ID],
            Integer.parseInt(properties[MINER_LIMIT]),
                 Integer.parseInt(properties[MINER_ACTION_PERIOD]),
            Integer.parseInt(properties[MINER_ANIMATION_PERIOD]),
            Functions.getImageList(imageStore, Entity.MINER_KEY));
         this.tryAddEntity(entity);
      }

      return properties.length == MINER_NUM_PROPERTIES;
   }

   public boolean parseBackground(String[] properties,
                                  ImageStore imageStore)
   {
      if (properties.length == BGND_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[BGND_COL]),
            Integer.parseInt(properties[BGND_ROW]));
         String id = properties[BGND_ID];
         setBackground(pt,
            new Background(id, Functions.getImageList(imageStore, id)));
      }

      return properties.length == BGND_NUM_PROPERTIES;
   }

   public Background getBackgroundCell(Point pos)
   {
      return this.background[pos.getY()][pos.getX()];
   }

   public void setOccupancyCell(Point pos,
                                Entity entity)
   {
      this.occupancy[pos.getY()][pos.getX()] = entity;
   }

   public Entity getOccupancyCell(Point pos)
   {
      return this.occupancy[pos.getY()][pos.getX()];
   }

   public void setBackground(Point pos,
                             Background background)
   {
      if (withinBounds(pos))
      {
         this.setBackgroundCell(pos, background);
      }
   }

   public Optional<PImage> getBackgroundImage(Point pos)
   {
      if (withinBounds(pos))
      {
         return Optional.of(Entity.getCurrentImage(this.getBackgroundCell(pos)));
      }
      else
      {
         return Optional.empty();
      }
   }

   public void removeEntityAt(Point pos)
   {
      if (withinBounds(pos)
         && this.getOccupancyCell(pos) != null)
      {
         Entity entity = this.getOccupancyCell(pos);

         /* this moves the entity just outside of the grid for
            debugging purposes */
         entity.position = new Point(-1, -1);
         this.entities.remove(entity);
         this.setOccupancyCell(pos, null);
      }
   }

   public void removeEntity(Entity entity)
   {
      this.removeEntityAt(entity.position);
   }

   public void moveEntity(Entity entity, Point pos)
   {
      Point oldPos = entity.position;
      if (withinBounds(pos) && !pos.equals(oldPos))
      {
         this.setOccupancyCell(oldPos, null);
         this.removeEntityAt(pos);
         this.setOccupancyCell(pos, entity);
         entity.position = pos;
      }
   }

   public boolean withinBounds(Point pos)
   {
      return pos.getY() >= 0 && pos.getY() < this.numRows &&
         pos.getX() >= 0 && pos.getX() < this.numCols;
   }

   public boolean isOccupied(Point pos)
   {
      return this.withinBounds(pos) &&
         this.getOccupancyCell(pos) != null;
   }

   /*
         Assumes that there is no entity currently occupying the
         intended destination cell.
      */
   public void addEntity(Entity entity)
   {
      if (this.withinBounds(entity.position))
      {
         this.setOccupancyCell(entity.position, entity);
         this.entities.add(entity);
      }
   }
}
