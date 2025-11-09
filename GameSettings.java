/**
 * GameSettings - Stores game configuration including player names, timer settings, and game mode
 */
public class GameSettings {
    private String whitePlayerName;
    private String blackPlayerName;
    private boolean timerEnabled;
    private int timePerPlayerMinutes;
    private boolean isOnlineGame;
    private boolean isHost;
    
    public GameSettings() {
        // Default settings
        whitePlayerName = "White Player";
        blackPlayerName = "Black Player";
        timerEnabled = false;
        timePerPlayerMinutes = 10;
        isOnlineGame = false;
        isHost = false;
    }
    
    // Getters and Setters
    public String getWhitePlayerName() {
        return whitePlayerName;
    }
    
    public void setWhitePlayerName(String name) {
        this.whitePlayerName = name;
    }
    
    public String getBlackPlayerName() {
        return blackPlayerName;
    }
    
    public void setBlackPlayerName(String name) {
        this.blackPlayerName = name;
    }
    
    public boolean isTimerEnabled() {
        return timerEnabled;
    }
    
    public void setTimerEnabled(boolean enabled) {
        this.timerEnabled = enabled;
    }
    
    public int getTimePerPlayerMinutes() {
        return timePerPlayerMinutes;
    }
    
    public void setTimePerPlayerMinutes(int minutes) {
        this.timePerPlayerMinutes = minutes;
    }
    
    public boolean isOnlineGame() {
        return isOnlineGame;
    }
    
    public void setOnlineGame(boolean online) {
        this.isOnlineGame = online;
    }
    
    public boolean isHost() {
        return isHost;
    }
    
    public void setHost(boolean host) {
        this.isHost = host;
    }
    
    public int getTimePerPlayerSeconds() {
        return timePerPlayerMinutes * 60;
    }
}
