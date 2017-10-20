import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import processing.core.PImage;
import processing.core.PApplet;

final class Functions
{
   private static final int COLOR_MASK = 0xffffff;
   private static final int KEYED_IMAGE_MIN = 5;
   private static final int KEYED_RED_IDX = 2;
   private static final int KEYED_GREEN_IDX = 3;
   private static final int KEYED_BLUE_IDX = 4;

   private static final int PROPERTY_KEY = 0;
   private static final String BGND_KEY = "background";


   public static List<PImage> getImageList(ImageStore imageStore, String key)
   {
      return imageStore.getImages().getOrDefault(key, imageStore.getDefaultImages());
   }

   public static void loadImages(Scanner in, ImageStore imageStore,
      PApplet screen)
   {
      int lineNumber = 0;
      while (in.hasNextLine())
      {
         try
         {
            processImageLine(imageStore.getImages(), in.nextLine(), screen);
         }
         catch (NumberFormatException e)
         {
            System.out.println(String.format("Image format error on line %d",
               lineNumber));
         }
         lineNumber++;
      }
   }

   public static void processImageLine(Map<String, List<PImage>> images,
      String line, PApplet screen)
   {
      String[] attrs = line.split("\\s");
      if (attrs.length >= 2)
      {
         String key = attrs[0];
         PImage img = screen.loadImage(attrs[1]);
         if (img != null && img.width != -1)
         {
            List<PImage> imgs = getImages(images, key);
            imgs.add(img);

            if (attrs.length >= KEYED_IMAGE_MIN)
            {
               int r = Integer.parseInt(attrs[KEYED_RED_IDX]);
               int g = Integer.parseInt(attrs[KEYED_GREEN_IDX]);
               int b = Integer.parseInt(attrs[KEYED_BLUE_IDX]);
               setAlpha(img, screen.color(r, g, b), 0);
            }
         }
      }
   }

   public static List<PImage> getImages(Map<String, List<PImage>> images,
      String key)
   {
      List<PImage> imgs = images.get(key);
      if (imgs == null)
      {
         imgs = new LinkedList<>();
         images.put(key, imgs);
      }
      return imgs;
   }

   /*
     Called with color for which alpha should be set and alpha value.
     setAlpha(img, color(255, 255, 255), 0));
   */
   public static void setAlpha(PImage img, int maskColor, int alpha)
   {
      int alphaValue = alpha << 24;
      int nonAlpha = maskColor & COLOR_MASK;
      img.format = PApplet.ARGB;
      img.loadPixels();
      for (int i = 0; i < img.pixels.length; i++)
      {
         if ((img.pixels[i] & COLOR_MASK) == nonAlpha)
         {
            img.pixels[i] = alphaValue | nonAlpha;
         }
      }
      img.updatePixels();
   }

   public static void load(Scanner in, WorldModel world, ImageStore imageStore)
   {
      int lineNumber = 0;
      while (in.hasNextLine())
      {
         try
         {
            if (!processLine(in.nextLine(), world, imageStore))
            {
               System.err.println(String.format("invalid entry on line %d",
                  lineNumber));
            }
         }
         catch (NumberFormatException e)
         {
            System.err.println(String.format("invalid entry on line %d",
               lineNumber));
         }
         catch (IllegalArgumentException e)
         {
            System.err.println(String.format("issue on line %d: %s",
               lineNumber, e.getMessage()));
         }
         lineNumber++;
      }
   }

   public static boolean processLine(String line, WorldModel world,
      ImageStore imageStore)
   {
      String[] properties = line.split("\\s");
      if (properties.length > 0)
      {
         switch (properties[PROPERTY_KEY])
         {
         case BGND_KEY:
            return world.parseBackground(properties, imageStore);
         case Entity.MINER_KEY:
            return world.parseMiner(properties, imageStore);
         case Entity.OBSTACLE_KEY:
            return world.parseObstacle(properties, imageStore);
         case Entity.ORE_KEY:
            return world.parseOre(properties, imageStore);
         case Entity.SMITH_KEY:
            return world.parseSmith(properties, imageStore);
         case Entity.VEIN_KEY:
            return world.parseVein(properties, imageStore);
         }
      }

      return false;
   }

   public static int clamp(int value, int low, int high)
   {
      return Math.min(high, Math.max(value, low));
   }

}