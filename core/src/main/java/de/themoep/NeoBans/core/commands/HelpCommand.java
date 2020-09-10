package de.themoep.NeoBans.core.commands;

import de.themoep.NeoBans.core.NeoBansPlugin;


/**
 * Created by Phoenix616 on 09.02.2015.
 */
public class HelpCommand extends AbstractCommand {

    public HelpCommand(NeoBansPlugin plugin, NeoSender sender, String[] args) {
        super(plugin, sender, args);
    }

    @Override
    public void execute() {
        
    }

    @Override
    public boolean validateInput() {
        return args.length >= 0;
    }

}
