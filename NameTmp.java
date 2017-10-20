import processing.core.PImage;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class NameTmp implements Entity {
    private EntityKind kind;
    private String id;
    public Point position;

    private List<PImage> images;
    private int imageIndex;
    private int resourceLimit;
    private int resourceCount;
    private int actionPeriod;
    private int animationPeriod;

    private static final Random rand = new Random();
    private static final String BLOB_ID_SUFFIX = " -- blob";
    private static final int BLOB_PERIOD_SCALE = 4;
    private static final int BLOB_ANIMATION_MIN = 50;
    private static final int BLOB_ANIMATION_MAX = 150;
    private static final String ORE_ID_PREFIX = "ore -- ";
    private static final int ORE_CORRUPT_MIN = 20000;
    private static final int ORE_CORRUPT_MAX = 30000;
    private static final int QUAKE_ANIMATION_REPEAT_COUNT = 10;

    public static final String BLOB_KEY = "blob";
    public static final String QUAKE_KEY = "quake";
    public static final String MINER_KEY = "miner";
    public static final String OBSTACLE_KEY = "obstacle";
    public static final String ORE_KEY = "ore";
    public static final String SMITH_KEY = "blacksmith";
    public static final String VEIN_KEY = "vein";



    public NameTmp(EntityKind kind, String id, Point position,
                   List<PImage> images, int resourceLimit, int resourceCount,
                   int actionPeriod, int animationPeriod)
    {
        this.kind = kind;
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.resourceLimit = resourceLimit;
        this.resourceCount = resourceCount;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }

    public EntityKind getKind() {
        return kind;
    }


    public static PImage getCurrentImage(Object entity)
    {
        if (entity instanceof Background)
        {
            return ((Background)entity).getImages()
                    .get(((Background)entity).getImageIndex());
        }
        else if (entity instanceof Entity)
        {
            return ((Entity)entity).images.get(((Entity)entity).imageIndex);
        }
        else
        {
            throw new UnsupportedOperationException(
                    String.format("getCurrentImage not supported for %s",
                            entity));
        }
    }



    public Action createActivityAction(WorldModel world,
                                       ImageStore imageStore)
    {
        return new Action(ActionKind.ACTIVITY, this, world, imageStore, 0);
    }

    public Action createAnimationAction(int repeatCount)
    {
        return new Action(ActionKind.ANIMATION, this, null, null, repeatCount);
    }

    public int getAnimationPeriod()
    {
        switch (this.kind)
        {
            case MINER_FULL:
            case MINER_NOT_FULL:
            case ORE_BLOB:
            case QUAKE:
                return this.animationPeriod;
            default:
                throw new UnsupportedOperationException(
                        String.format("getAnimationPeriod not supported for %s",
                                this.kind));
        }
    }

    public Point nextPositionOreBlob(WorldModel world,
                                     Point destPos)
    {
        int horiz = Integer.signum(destPos.getX() - this.position.getX());
        Point newPos = new Point(this.position.getX() + horiz,
                this.position.getY());

        Optional<Entity> occupant = world.getOccupant(newPos);

        if (horiz == 0 ||
                (occupant.isPresent() && !(occupant.get().kind == EntityKind.ORE)))
        {
            int vert = Integer.signum(destPos.getY() - this.position.getY());
            newPos = new Point(this.position.getX(), this.position.getY() + vert);
            occupant = world.getOccupant(newPos);

            if (vert == 0 ||
                    (occupant.isPresent() && !(occupant.get().kind == EntityKind.ORE)))
            {
                newPos = this.position;
            }
        }

        return newPos;
    }

    public Point nextPositionMiner(WorldModel world,
                                   Point destPos)
    {
        int horiz = Integer.signum(destPos.getX() - this.position.getX());
        Point newPos = new Point(this.position.getX() + horiz,
                this.position.getY());

        if (horiz == 0 || world.isOccupied(newPos))
        {
            int vert = Integer.signum(destPos.getY() - this.position.getY());
            newPos = new Point(this.position.getX(),
                    this.position.getY() + vert);

            if (vert == 0 || world.isOccupied(newPos))
            {
                newPos = this.position;
            }
        }

        return newPos;
    }

    public void nextImage()
    {
        this.imageIndex = (this.imageIndex + 1) % this.images.size();
    }

    public boolean moveToOreBlob(WorldModel world,
                                 Entity target, EventScheduler scheduler)
    {
        if (this.position.adjacent(target.position))
        {
            world.removeEntity(target);
            scheduler.unscheduleAllEvents(target);
            return true;
        }
        else
        {
            Point nextPos = this.nextPositionOreBlob(world, target.position);

            if (!this.position.equals(nextPos))
            {
                Optional<Entity> occupant = world.getOccupant(nextPos);
                if (occupant.isPresent())
                {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                world.moveEntity(this, nextPos);
            }
            return false;
        }
    }

    public boolean moveToFull(WorldModel world,
                              Entity target, EventScheduler scheduler)
    {
        if (this.position.adjacent(target.position))
        {
            return true;
        }
        else
        {
            Point nextPos = this.nextPositionMiner(world, target.position);

            if (!this.position.equals(nextPos))
            {
                Optional<Entity> occupant = world.getOccupant(nextPos);
                if (occupant.isPresent())
                {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                world.moveEntity(this, nextPos);
            }
            return false;
        }
    }

    public boolean moveToNotFull(WorldModel world,
                                 Entity target, EventScheduler scheduler)
    {
        if (this.position.adjacent(target.position))
        {
            this.resourceCount += 1;
            world.removeEntity(target);
            scheduler.unscheduleAllEvents(target);

            return true;
        }
        else
        {
            Point nextPos = this.nextPositionMiner(world, target.position);

            if (!this.position.equals(nextPos))
            {
                Optional<Entity> occupant = world.getOccupant(nextPos);
                if (occupant.isPresent())
                {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                world.moveEntity(this, nextPos);
            }
            return false;
        }
    }

    public void transformFull(WorldModel world,
                              EventScheduler scheduler, ImageStore imageStore)
    {
        Entity miner = this.position.createMinerNotFull(this.id, this.resourceLimit,
                this.actionPeriod, this.animationPeriod,
                this.images);

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        world.addEntity(miner);
        miner.scheduleActions(scheduler, world, imageStore);
    }

    public boolean transformNotFull(WorldModel world,
                                    EventScheduler scheduler, ImageStore imageStore)
    {
        if (this.resourceCount >= this.resourceLimit)
        {
            Entity miner = this.position.createMinerFull(this.id, this.resourceLimit,
                    this.actionPeriod, this.animationPeriod,
                    this.images);

            world.removeEntity(this);
            scheduler.unscheduleAllEvents(this);

            world.addEntity(miner);
            miner.scheduleActions(scheduler, world, imageStore);

            return true;
        }

        return false;
    }

    public void scheduleActions(EventScheduler scheduler,
                                WorldModel world, ImageStore imageStore)
    {
        switch (this.kind)
        {
            case MINER_FULL:
                scheduler.scheduleEvent(this,
                        this.createActivityAction(world, imageStore),
                        this.actionPeriod);
                scheduler.scheduleEvent(this, this.createAnimationAction(0),
                        this.getAnimationPeriod());
                break;

            case MINER_NOT_FULL:
                scheduler.scheduleEvent(this,
                        this.createActivityAction(world, imageStore),
                        this.actionPeriod);
                scheduler.scheduleEvent(this,
                        this.createAnimationAction(0), this.getAnimationPeriod());
                break;

            case ORE:
                scheduler.scheduleEvent(this,
                        this.createActivityAction(world, imageStore),
                        this.actionPeriod);
                break;

            case ORE_BLOB:
                scheduler.scheduleEvent(this,
                        this.createActivityAction(world, imageStore),
                        this.actionPeriod);
                scheduler.scheduleEvent(this,
                        this.createAnimationAction(0), this.getAnimationPeriod());
                break;

            case QUAKE:
                scheduler.scheduleEvent(this,
                        this.createActivityAction(world, imageStore),
                        this.actionPeriod);
                scheduler.scheduleEvent(this,
                        this.createAnimationAction(QUAKE_ANIMATION_REPEAT_COUNT),
                        this.getAnimationPeriod());
                break;

            case VEIN:
                scheduler.scheduleEvent(this,
                        this.createActivityAction(world, imageStore),
                        this.actionPeriod);
                break;

            default:
        }
    }

    public void executeVeinActivity(WorldModel world,
                                    ImageStore imageStore, EventScheduler scheduler)
    {
        Optional<Point> openPt = this.position.findOpenAround(world);

        if (openPt.isPresent())
        {
            Entity ore = openPt.get().createOre(ORE_ID_PREFIX + this.id,
                    ORE_CORRUPT_MIN +
                            rand.nextInt(ORE_CORRUPT_MAX - ORE_CORRUPT_MIN),
                    Functions.getImageList(imageStore, ORE_KEY));
            world.addEntity(ore);
            ore.scheduleActions(scheduler, world, imageStore);
        }

        scheduler.scheduleEvent(this,
                this.createActivityAction(world, imageStore),
                this.actionPeriod);
    }

    public void executeQuakeActivity(WorldModel world,
                                     ImageStore imageStore, EventScheduler scheduler)
    {
        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);
    }

    public void executeOreBlobActivity(WorldModel world,
                                       ImageStore imageStore, EventScheduler scheduler)
    {
        Optional<Entity> blobTarget = this.position.findNearest(world,
                EntityKind.VEIN);
        long nextPeriod = this.actionPeriod;

        if (blobTarget.isPresent())
        {
            Point tgtPos = blobTarget.get().position;

            if (this.moveToOreBlob(world, blobTarget.get(), scheduler))
            {
                Entity quake = tgtPos.createQuake(
                        Functions.getImageList(imageStore, QUAKE_KEY));

                world.addEntity(quake);
                nextPeriod += this.actionPeriod;
                quake.scheduleActions(scheduler, world, imageStore);
            }
        }

        scheduler.scheduleEvent(this,
                this.createActivityAction(world, imageStore),
                nextPeriod);
    }

    public void executeOreActivity(WorldModel world,
                                   ImageStore imageStore, EventScheduler scheduler)
    {
        Point pos = this.position;  // store current position before removing

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        Entity blob = pos.createOreBlob(this.id + BLOB_ID_SUFFIX,
                this.actionPeriod / BLOB_PERIOD_SCALE,
                BLOB_ANIMATION_MIN +
                        rand.nextInt(BLOB_ANIMATION_MAX - BLOB_ANIMATION_MIN),
                Functions.getImageList(imageStore, BLOB_KEY));

        world.addEntity(blob);
        blob.scheduleActions(scheduler, world, imageStore);
    }

    public void executeMinerNotFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
    {
        Optional<Entity> notFullTarget = this.position.findNearest(world,
                EntityKind.ORE);

        if (!notFullTarget.isPresent() ||
                !this.moveToNotFull(world, notFullTarget.get(), scheduler) ||
                !this.transformNotFull(world, scheduler, imageStore))
        {
            scheduler.scheduleEvent(this,
                    this.createActivityAction(world, imageStore),
                    this.actionPeriod);
        }
    }

    public void executeMinerFullActivity(WorldModel world,
                                         ImageStore imageStore, EventScheduler scheduler)
    {
        Optional<Entity> fullTarget = this.position.findNearest(world,
                EntityKind.BLACKSMITH);

        if (fullTarget.isPresent() &&
                this.moveToFull(world, fullTarget.get(), scheduler))
        {
            this.transformFull(world, scheduler, imageStore);
        }
        else
        {
            scheduler.scheduleEvent(this,
                    this.createActivityAction(world, imageStore),
                    this.actionPeriod);
        }
    }
}
