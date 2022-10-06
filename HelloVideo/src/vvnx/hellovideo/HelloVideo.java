/**
 * adb uninstall vvnx.hellovideo
 * adb install out/target/product/generic_arm64/system/app/HelloVideo/HelloVideo.apk
 * 
 * #Que pour jouer une local file
 * adb shell pm grant vvnx.hellovideo android.permission.READ_EXTERNAL_STORAGE 
 * 
 * NUC:
 * ./test-launch "( videotestsrc is-live=1 ! vaapih264enc ! rtph264pay name=pay0 pt=96 )"
 * ./test-launch "( videotestsrc is-live=1 ! video/x-raw,width=480,height=640 ! vaapih264enc ! rtph264pay name=pay0 pt=96 )"
 * RPi:
 * test-launch "( rpicamsrc bitrate=2000000 keyframe-interval=15 ! video/x-h264,framerate=15/1,width=640,height=480 ! h264parse ! rtph264pay name=pay0 pt=96 )"
 * 
 * 
 * 
 * 

 */

package vvnx.hellovideo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.util.Log;
import android.net.Uri;
//import android.widget.VideoView;
//import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.view.SurfaceView;
import android.view.SurfaceHolder;


public class HelloVideo extends Activity {
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private MediaPlayer mediaPlayer;
		
	public static String TAG = "vvnx";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    View view = getLayoutInflater().inflate(R.layout.hello_video, null);
        setContentView(view);
        
        
        
        
        //String localpath = "/storage/emulated/0/Movies/test.mp4";
        //Attention pour les streams, par défaut https obligatoire (3/4 de mes Android Devices). workaround: android:usesCleartextTraffic="true"
        //String url = "http://91.121.159.124:8000/eko-des-garrigues-128k.mp3"; 
		//String url = "http://192.168.1.14:1337/playlist.m3u8"; //HLS		
		//String url = "rtsp://192.168.1.19:8554/test";
		String url = "rtsp://192.168.49.1:8554/test"; //RTSP 192.168.49.1
		//String url = "rtsp://192.168.49.2:8554/test"; //RTSP 192.168.49.2
		
		
		mediaPlayer = new MediaPlayer();
		//mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build());
		
		surfaceView = (SurfaceView)findViewById(R.id.video_surfaceview);
		final SurfaceHolder surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(new MyCallBack()); //le surfaceHolder n'est pas prêt tout de suite, appel sans callback ->mp.setDisplay() plante
				
		
		
		//redimensionner la surfaceView en fonction des caractéristiques de la video (width + height)
		//callbacks pour avoir des infos sur le stream:
		//https://developer.android.com/reference/android/media/MediaPlayer.html#callbacks
		
		mediaPlayer.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener(){
		@Override
			public void onVideoSizeChanged(MediaPlayer mp, int videoWidth, int videoHeight) {
				
			//OnVideoSizeChangedListener: on y passe deux fois au démarrage, la première fois: 0 puis la deuxième avec width correcte, je filtre avec !=0			
			if (videoWidth != 0 && videoHeight != 0) {
				Log.e(TAG, "onVideoSizeChangedListener videoWidth="+videoWidth + " et videoHeight="+videoHeight); 
			    
				//Adaptation dims surfaceview aux dimensions de la video (portrait ou paysage)
				//https://stackoverflow.com/questions/4835060/android-mediaplayer-how-to-use-surfaceview-or-mediaplayer-to-play-video-in-cor
				//Get the width of the screen
				int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
				Log.e(TAG, "onVideoSizeChangedListener screenWidth="+screenWidth);	
				//Get the SurfaceView layout parameters
				android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
				//Set the width of the SurfaceView to the width of the screen
				lp.width = screenWidth;
				//Set the height of the SurfaceView to match the aspect ratio of the video 
				//be sure to cast these as floats otherwise the calculation will likely be 0
				lp.height = (int) (((float)videoHeight / (float)videoWidth) * (float)screenWidth);
				//Commit the layout parameters
				surfaceView.setLayoutParams(lp);	
				}
    
		    }
		});
		
		
		
		
		

		try {
		mediaPlayer.setDataSource(this, Uri.parse(url));
		mediaPlayer.prepare();		
		mediaPlayer.start();	
		
			
		} catch (Exception e) {
			Log.e(TAG, "initialize Player Exception");
			e.printStackTrace();
		}
        
        /* VideoView: archi simple mais local file (on device) only
         * 
         * final VideoView videoView = findViewById(R.id.videoView);
        if (videoView != null) {
            videoView.setVideoURI(Uri.parse(localpath));
            videoView.start();
        }*/
       
    }
    
    private class MyCallBack implements SurfaceHolder.Callback {
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
      mediaPlayer.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
    
  }
  
  

}

