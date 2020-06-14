package javafxmusicplayer;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import javafx.scene.control.Label;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
/**
 *
 * @author kiran
 */
public class FXMLDocumentController implements Initializable {
    
     private String filePath; 
     private MediaPlayer mediaPlayer;
     private final boolean repeat = false;
     private boolean atEndOfMedia = false;
     private boolean stopRequested = false;
     private Duration duration;
     private boolean isMute = true;
    @FXML
    private Label playTime;
     
    @FXML
    private Label label;
   
     @FXML
    private Slider slider;
            
    @FXML
    private Slider seekSlider;
            
    @FXML
    private MediaView mediaView;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
         
        try{
            
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Music Files Only", "*.mp3");
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showOpenDialog(null);
        filePath = file.toURI().toString();
        
        
        String decoded = URLDecoder.decode(filePath, "UTF-8");
        File f = new File(decoded);
//        System.out.println("dec = "+decoded);
        if (filePath != null) {
            try{
                mediaPlayer.stop();
            }
            catch(Exception ex){
            }
         label.setText(f.getName());

            Media media = new Media(filePath);
            mediaPlayer= new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            
            slider.setValue(mediaPlayer.getVolume()*100);
            
            slider.valueProperty().addListener(new InvalidationListener(){
                @Override
                public void invalidated(javafx.beans.Observable observable) {
                    mediaPlayer.setVolume(slider.getValue()/100);
                }
            });
            
             seekSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
               if (seekSlider.isValueChanging()) {
               // multiply duration by percentage calculated by slider position
                  mediaPlayer.seek(duration.multiply(seekSlider.getValue() / 100.0));
               }
            }
        });     

            mediaPlayer.play();
        }
         mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() 
        {
            public void invalidated(Observable ov) {
                updateValues();
            }
        });
         mediaPlayer.setOnReady(new Runnable() {
            public void run() {
                duration = mediaPlayer.getMedia().getDuration();
                updateValues();
            }
        });
         mediaPlayer.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            public void run() {
                if (!repeat) {
                    stopRequested = true;
                    atEndOfMedia = true;
                }
            }
       });
      }  
      catch(Exception ex){
         Alert alert = new Alert(AlertType.ERROR, "Select a music file to play .", ButtonType.CLOSE);
			alert.showAndWait();
      }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    @FXML
    public void exitApp(){
        System.exit(0);
    }
    
    @FXML
    private void pauseMusic(ActionEvent event){
            mediaPlayer.pause(); 
    }
    @FXML
    private void playMusic(ActionEvent event){
        try{
                    mediaPlayer.play();
                    mediaPlayer.setRate(1);
          
            MediaPlayer.Status status = mediaPlayer.getStatus();

             if (status == MediaPlayer.Status.UNKNOWN  || status == MediaPlayer.Status.HALTED)
                {
                   // don't do anything in these states
                   return;
                }

                  if ( status == MediaPlayer.Status.PAUSED
                     || status == MediaPlayer.Status.READY
                     || status == MediaPlayer.Status.STOPPED)
                  {
                     // rewind the music if we're at the end
                     if (atEndOfMedia) {
                        mediaPlayer.seek(mediaPlayer.getStartTime());
                        atEndOfMedia = false;
                     }
                     mediaPlayer.play();
                     } else {
                       mediaPlayer.pause();
                     }
        }
        catch(Exception ex){
//         Alert alert = new Alert(AlertType.ERROR, "Error :" + ex.getMessage(), ButtonType.CLOSE);
//			alert.showAndWait();
      }
    }
    @FXML
    private void stopMusic(ActionEvent event){
            mediaPlayer.stop();
    }
    @FXML
    private void muteMusic(ActionEvent event){
            if(isMute){
                mediaPlayer.setMute(true);
                isMute = false ;
            }
            else{
                 mediaPlayer.setMute(false);
                isMute = true; 
            }
    }
    @FXML
    private void fasterMusic(ActionEvent event){
        mediaPlayer.setRate(2);
    }
    @FXML
    private void slowerMusic(ActionEvent event){
        mediaPlayer.setRate(0.50);
    }
//    @FXML
//    Button minimizeBtn;
//    public void minimize(){
//        Stage s = JavaFXMusicPlayer.getPrimaryStage();
//        s.close();
//        minimizeBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent me) {
//                s.toBack();
//            }
//        });
//    }
    @FXML  void minimize(){
        System.out.println("minnnnnn");
             Stage stage = JavaFXMusicPlayer.getPrimaryStage();
            stage.setIconified(true);
    }
  
    public void updateValues() {
        try{
            if (playTime != null && seekSlider != null && slider != null) {
               Platform.runLater(new Runnable() {
                  public void run() {
                      try {
                          Duration currentTime = mediaPlayer.getCurrentTime();
                          playTime.setText(formatTime(currentTime, duration));
                          seekSlider.setDisable(duration.isUnknown());
                          if (!seekSlider.isDisabled()
                                  && duration.greaterThan(Duration.ZERO)
                                  && !seekSlider.isValueChanging()) {
                              seekSlider.setValue(currentTime.divide(duration).toMillis()
                                      * 100.0);
                          }
                          if (!slider.isValueChanging()) {
                              slider.setValue((int)Math.round(mediaPlayer.getVolume()
                                      * 100));
                          } } catch (Exception ex) {
                          Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                      }
                  }
               });
            }
        }
        catch(Exception ex){
         Alert alert = new Alert(AlertType.ERROR, "Error :" + ex.getMessage(), ButtonType.CLOSE);
			alert.showAndWait();
      }
      }
     public static String formatTime(Duration elapsed, Duration duration)throws Exception {
         
//         System.out.println("elap="+elapsed);           
//         System.out.println("durt="+duration);           
         int intElapsed = (int)Math.floor(elapsed.toSeconds());
         int elapsedHours = intElapsed / (60 * 60);

         if (elapsedHours > 0) {
                    intElapsed -= elapsedHours * 60 * 60;
         }
         
         int elapsedMinutes = intElapsed / 60;
         int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 
                                        - elapsedMinutes * 60;


         if (duration.greaterThan(Duration.ZERO)) {
         
             int intDuration = (int)Math.floor(duration.toSeconds());
             int durationHours = intDuration / (60 * 60);
                   if (durationHours > 0) {
                      intDuration -= durationHours * 60 * 60;
                   }
                   int durationMinutes = intDuration / 60;
                   int durationSeconds = intDuration - durationHours * 60 * 60 - 
                       durationMinutes * 60;
                   if (durationHours > 0) {
                      return String.format("%d:%02d:%02d/%d:%02d:%02d", 
                         elapsedHours, elapsedMinutes, elapsedSeconds,
                         durationHours, durationMinutes, durationSeconds);
                   } else {
                       return String.format("%02d:%02d/%02d:%02d",
                         elapsedMinutes, elapsedSeconds,durationMinutes, 
                             durationSeconds);
                   }
            } 
            else{
                       if (elapsedHours > 0) {
                          return String.format("%d:%02d:%02d", elapsedHours, 
                                 elapsedMinutes, elapsedSeconds);
                         } else {
                             return String.format("%02d:%02d",elapsedMinutes, 
                                 elapsedSeconds);
                         }
            }
    }
    
}
