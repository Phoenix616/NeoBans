package de.themoep.NeoBans.core.commands;

import java.util.List;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public interface NeoCommand {
    /**
     * Tun the command
     */
    public void execute();

    /**
     * Validate user input
     * @return true if input is valied
     */
    public boolean validateInput();

    /**
     * Get the available tab complete options
     * @param cursor The inputed string to get the suggestions for
     * @return A list of suggestions to complete with
     */
    public List<String> getTabSuggestions(String cursor);

}
