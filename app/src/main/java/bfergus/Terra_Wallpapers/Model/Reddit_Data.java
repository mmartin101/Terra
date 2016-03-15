package bfergus.Terra_Wallpapers.Model;

import java.io.Serializable;

/**
 * Created by Bob on 2/25/2016.
 */
public class Reddit_Data implements Serializable {
    public Reddit_Children children [];
    public String modhash;

    public Reddit_Data(Reddit_Children children[], String modhash) {
        this.children = children;
        this.modhash = modhash;
    }
}
