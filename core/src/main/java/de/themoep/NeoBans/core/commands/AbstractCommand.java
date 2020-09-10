package de.themoep.NeoBans.core.commands;

import com.google.common.collect.ImmutableMap;

import de.themoep.NeoBans.core.NeoBansPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Phoenix616 on 09.02.2015.
 */
public abstract class AbstractCommand implements NeoCommand {
    /**
     * NeoBans plugin
     */
    protected NeoBansPlugin plugin;
    
    /**
     * Command sender
     */
    protected NeoSender sender;

    /**
     * Arguments to this command
     */
    protected String[] args;

    /**
     * Tab completions to this command
     */
    protected Map<String, ArrayList<String>> completions;

    /**
     * Constructor
     * @param sender
     * @param args
     * @param completions
     */
    public AbstractCommand(NeoBansPlugin plugin, NeoSender sender, String[] args, Map<String, ArrayList<String>> completions) {
        this.plugin = plugin;
        this.sender = sender;
        this.args = args;
        this.completions = new HashMap<>(completions);
        this.completions.put("", new ArrayList<String>());
    }
    
    /**
     * Constructor
     * @param sender
     * @param args
     */
    public AbstractCommand(NeoBansPlugin plugin, NeoSender sender, String[] args) {
        this(plugin, sender, args, ImmutableMap.of("%no-argument%", new ArrayList<String>()));
    }

    @Override
    public List<String> getTabSuggestions(String cursor) {
        boolean space = cursor.endsWith(" ");
        String arg = "";
        if ((space && args.length == 0) || (!space && args.length == 1))
            arg = "%no-argument%";
        else if (space && completions.containsKey(args[args.length - 1]))
            arg = args[args.length - 1];
        else if (!space && args.length > 1 && completions.containsKey(args[args.length - 2]))
            arg = args[args.length - 2];
            
        List<String> completionList = completions.get(arg);
        if(completionList.size() == 0)
            return completionList;
        
        List<String> completionListOnline = new ArrayList<String>();
        for(String s : completionList)
            if(s.equals("%online%"))
                completionListOnline.addAll(plugin.getOnlinePlayers());
            else
                completionListOnline.add(s);

        if(!space && args.length > 0) {
            List<String> completionListInput = new ArrayList<String>();
            for(String s : completionListOnline)
                if(s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    completionListInput.add(s);
            return completionListInput;
        }
        return completionListOnline;
    }
}
