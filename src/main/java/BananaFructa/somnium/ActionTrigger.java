package BananaFructa.somnium;

public enum ActionTrigger {

    HORN("Player used horn."),
    BELL("Player rang a bell.");

    public String description;

    ActionTrigger(String description) {
        this.description = description;
    }

}
