import processing.core.PImage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

final class Point
{
   private final int x;
   private final int y;

   private static final String QUAKE_ID = "quake";
   private static final int QUAKE_ACTION_PERIOD = 1100;
   private static final int QUAKE_ANIMATION_PERIOD = 100;
   private static final int ORE_REACH = 1;

   public int getX(){
       return x;
   }

    public int getY(){
        return y;
    }

    public Point(int x, int y)
   {
      this.x = x;
      this.y = y;
   }

    public Optional<Entity> findNearest(WorldModel world,
                                        EntityKind kind)
    {
       List<Entity> ofType = new LinkedList<>();
       for (Entity entity : world.getEntities())
       {
          if (entity.getKind() == kind)
          {
             ofType.add(entity);
          }
       }

       return nearestEntity(ofType);
    }

    public Optional<Entity> nearestEntity(List<Entity> entities)
    {
       if (entities.isEmpty())
       {
          return Optional.empty();
       }
       else
       {
          Entity nearest = entities.get(0);
          int nearestDistance = nearest.position.distanceSquared(this);

          for (Entity other : entities)
          {
             int otherDistance = other.position.distanceSquared(this);

             if (otherDistance < nearestDistance)
             {
                nearest = other;
                nearestDistance = otherDistance;
             }
          }

          return Optional.of(nearest);
       }
    }

    public Entity createMinerFull(String id, int resourceLimit,
                                  int actionPeriod, int animationPeriod,
                                  List<PImage> images)
    {
       return new Entity(EntityKind.MINER_FULL, id, this, images,
          resourceLimit, resourceLimit, actionPeriod, animationPeriod);
    }

    public Entity createMinerNotFull(String id, int resourceLimit,
                                     int actionPeriod, int animationPeriod,
                                     List<PImage> images)
    {
       return new Entity(EntityKind.MINER_NOT_FULL, id, this, images,
          resourceLimit, 0, actionPeriod, animationPeriod);
    }

    public Entity createObstacle(String id,
                                 List<PImage> images)
    {
       return new Entity(EntityKind.OBSTACLE, id, this, images,
          0, 0, 0, 0);
    }

    public Entity createOre(String id, int actionPeriod,
                            List<PImage> images)
    {
       return new Entity(EntityKind.ORE, id, this, images, 0, 0,
          actionPeriod, 0);
    }

    public Entity createOreBlob(String id,
                                int actionPeriod, int animationPeriod, List<PImage> images)
    {
       return new Entity(EntityKind.ORE_BLOB, id, this, images,
             0, 0, actionPeriod, animationPeriod);
    }

    public Entity createVein(String id, int actionPeriod,
                             List<PImage> images)
    {
       return new Entity(EntityKind.VEIN, id, this, images, 0, 0,
          actionPeriod, 0);
    }

    public Optional<Point> findOpenAround(WorldModel world)
    {
       for (int dy = -ORE_REACH; dy <= ORE_REACH; dy++)
       {
          for (int dx = -ORE_REACH; dx <= ORE_REACH; dx++)
          {
             Point newPt = new Point(this.x + dx, this.y + dy);
             if (world.withinBounds(newPt) &&
                !world.isOccupied(newPt))
             {
                return Optional.of(newPt);
             }
          }
       }

       return Optional.empty();
    }

    public Entity createQuake(List<PImage> images)
   {
      return new Entity(EntityKind.QUAKE, QUAKE_ID, this, images,
         0, 0, QUAKE_ACTION_PERIOD, QUAKE_ANIMATION_PERIOD);
   }

   public boolean adjacent(Point p2)
   {
      return (this.x == p2.x && Math.abs(this.y - p2.y) == 1) ||
         (this.y == p2.y && Math.abs(this.x - p2.x) == 1);
   }

   public int distanceSquared(Point p2)
   {
      int deltaX = this.x - p2.x;
      int deltaY = this.y - p2.y;

      return deltaX * deltaX + deltaY * deltaY;
   }

   public String toString()
   {
      return "(" + x + "," + y + ")";
   }

   public boolean equals(Object other)
   {
      return other instanceof Point &&
         ((Point)other).x == this.x &&
         ((Point)other).y == this.y;
   }

   public int hashCode()
   {
      int result = 17;
      result = result * 31 + x;
      result = result * 31 + y;
      return result;
   }
}
