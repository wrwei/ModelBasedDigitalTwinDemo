package org.eden.epsilon.modules;

import org.eclipse.epsilon.eol.EolModule;

// concrete module store for EOL
public class EolModuleStore extends BaseModuleStore<EolModule> {

    public static final EolModuleStore instance = new EolModuleStore();

    public EolModuleStore() {
        super("operations", ".eol", EolModule.class);
    }
}