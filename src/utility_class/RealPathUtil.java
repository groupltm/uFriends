package utility_class;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class RealPathUtil {
	
	public static final int WidthAvatar = 300;
	public static final int HeightAvatar = 300;
	
    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API20(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

         // Split at colon, use second item in the array
         String id = wholeID.split(":")[1];

         String[] column = { MediaStore.Images.Media.DATA };     

         // where id is equal to             
         String sel = MediaStore.Images.Media._ID + "=?";

         Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
                                   column, sel, new String[]{ id }, null);
         
         int columnIndex = cursor.getColumnIndex(column[0]);

         if (cursor.moveToFirst()) {
             filePath = cursor.getString(columnIndex);
         }   
         cursor.close();
         return filePath;
    }
    
    
    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to19(Context context, Uri contentUri) {
          String[] proj = { MediaStore.Images.Media.DATA };
          String result = null;
           
          CursorLoader cursorLoader = new CursorLoader(
                  context, 
            contentUri, proj, null, null, null);        
          Cursor cursor = cursorLoader.loadInBackground();
          
          if(cursor != null){
           int column_index = 
             cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
           cursor.moveToFirst();
           result = cursor.getString(column_index);
          }
          return result;  
    }
    
    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
               String[] proj = { MediaStore.Images.Media.DATA };
               Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
               int column_index
          = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
               cursor.moveToFirst();
               return cursor.getString(column_index);
    }
    
    public static Bitmap createBitmapWithPath(String imagePath, int width, int height){
        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(imagePath, options);

        int imvWidth = width;
        int imvHeight = height;

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, imvWidth, imvHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap oldbm = BitmapFactory.decodeFile(imagePath, options);

        Bitmap bm = createCorrectBitmap(imagePath, oldbm);

        return bm;
    }
    
    public static Bitmap createBitmapWithBitmap(Bitmap bm, int width, int height){
    	Bitmap newBm = Bitmap.createScaledBitmap(bm, width, height, false);
    	return newBm;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    
    public static Bitmap createCorrectBitmap(String filePath, Bitmap oldbm) {

        Bitmap newbm;

        int rotate = 0;
        try {
            File imageFile = new File(filePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        newbm = Bitmap.createBitmap(oldbm, 0, 0, oldbm.getWidth(), oldbm.getHeight(), matrix, true);

        return newbm;
    }
}
